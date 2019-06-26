/**
 * The MIT License
 * Copyright (c) 2014 Halil-Cem Guersoy and all contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.artifactpromotion;

import hudson.model.TaskListener;
import hudson.util.Secret;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.deployment.DeployResult;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.jenkinsci.plugins.artifactpromotion.exception.PromotionException;

import java.io.Serializable;
import java.util.Map;

public class NexusOSSPromoterClosure implements Serializable, IPromotorClosure {

    private static final long serialVersionUID = 1L;

    private String localRepositoryURL;
    private Map<PromotionBuildTokens, String> expandedTokens;
    private String releaseUser;
    private Secret releasePassword;
    private String stagingUser;
    private Secret stagingPassword;
    private boolean skipDeletion;
    private TaskListener listener;
    private boolean debug;


    /**
     * @param localRepositoryURL
     * @param listener
     * @param expandedTokens
     * @param releaseUser
     * @param releasePassword
     * @param stagingUser
     * @param stagingPassword
     * @param skipDeletion - if true, skip the deletion of the artifact out of the source repo
     */
    public NexusOSSPromoterClosure(
            TaskListener listener,
            String localRepositoryURL,
            Map<PromotionBuildTokens, String> expandedTokens,
            String releaseUser, Secret releasePassword,
            String stagingUser, Secret stagingPassword,
            boolean skipDeletion, boolean debug) {
        super();

        this.expandedTokens = expandedTokens;
        this.listener = listener;
        this.releaseUser = releaseUser;
        this.releasePassword = releasePassword;
        this.stagingUser = stagingUser;
        this.stagingPassword = stagingPassword;
        this.localRepositoryURL = localRepositoryURL;
        this.skipDeletion = skipDeletion;
        this.debug = debug;
    }

    /* (non-Javadoc)
     * @see org.jenkinsci.plugins.artifactpromotion.IPromotorClosure#promote()
     */
    public void promote() throws PromotionException {

        this.listener.getLogger().println("Started with promotion");

        AetherInteraction aether = new AetherInteraction(this.listener);
        RepositorySystem system = aether.getNewRepositorySystem();
        RepositorySystemSession session = aether.getRepositorySystemSession(
                system, localRepositoryURL);

        RemoteRepository stagingRepository =
                aether.getRepository(stagingUser,
                          stagingPassword,
                          "stagingrepo",
                          this.expandedTokens
                                    .get(PromotionBuildTokens.STAGING_REPOSITORY));

        ArtifactWrapper artifact = getArtifact(aether, system, session,
                stagingRepository);
        if (artifact == null) {
            throw new PromotionException(
                    "Could not fetch artifacts for promotion");
        }

        // upload the artifact and its pom to the release repos
        DeployResult result = deployPromotionArtifact(aether, system, session,
                artifact);
        if (result == null) {
            throw new PromotionException(
                    "Could not deploy artifacts to release repository");
        }

        if (skipDeletion == false) {
            deleteArtifact(stagingRepository, artifact);
        } else {
            this.listener
                    .getLogger()
                    .println(
                            "Skipping deletion of artifact from source repo as requested by user");
        }
    }

    private ArtifactWrapper getArtifact(AetherInteraction aether,
            RepositorySystem system, RepositorySystemSession session,
            RemoteRepository stagingRepo) {

        this.listener.getLogger().println("Get Artifact and corresponding POM");
        Artifact artifact = null;
        Artifact pom = null;
        try {
            artifact = aether.getArtifact(session, system, stagingRepo,
                    this.expandedTokens.get(PromotionBuildTokens.GROUP_ID),
                    this.expandedTokens.get(PromotionBuildTokens.ARTIFACT_ID),
                    this.expandedTokens.get(PromotionBuildTokens.CLASSIFIER),
                    this.expandedTokens.get(PromotionBuildTokens.EXTENSION),
                    this.expandedTokens.get(PromotionBuildTokens.VERSION));
            pom = aether.getArtifact(session, system, stagingRepo,
                    this.expandedTokens.get(PromotionBuildTokens.GROUP_ID),
                    this.expandedTokens.get(PromotionBuildTokens.ARTIFACT_ID),
                    null, // POM doesn't have a classifier
                    ArtifactPromotionBuilder.POMTYPE,
                    this.expandedTokens.get(PromotionBuildTokens.VERSION));
        } catch (ArtifactResolutionException e) {
            this.listener.getLogger().println(
                    "Could not resolve artifact: " + e.getMessage());
            return null;
        }

        return new ArtifactWrapper(artifact, pom);
    }

    private DeployResult deployPromotionArtifact(AetherInteraction aether, RepositorySystem system, RepositorySystemSession session,
            ArtifactWrapper artifact) {

        RemoteRepository releaseRepository =
                    aether.getRepository(releaseUser,
                                              releasePassword,
                                              "releaserepo",
                                              this.expandedTokens
                                                        .get(PromotionBuildTokens.RELEASE_REPOSITORY));
        try {
            return aether.deployArtifact(session, system, releaseRepository,
                    artifact.getArtifact(), artifact.getPom());
        } catch (DeploymentException e) {
            this.listener.getLogger().println(
                    "Could not deploy artifact to " + releaseRepository
                            + " using User " + releaseUser + ":"
                            + e.getMessage());
            return null;
        }
    }

    private void deleteArtifact(RemoteRepository aetherStagingRepo,
            ArtifactWrapper artifact) {
        IDeleteArtifact deleter = new DeleteArtifactNexusOSS(this.listener, this.stagingUser,
                this.stagingPassword, this.debug);
        deleter.deleteArtifact(aetherStagingRepo, artifact.getArtifact());
    }

}
