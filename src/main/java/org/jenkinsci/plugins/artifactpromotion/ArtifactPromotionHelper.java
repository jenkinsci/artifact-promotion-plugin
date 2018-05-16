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

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.artifactpromotion.exception.PromotionException;
import org.jenkinsci.plugins.artifactpromotion.jobdsl.ArtifactPromotionJobDslExtension;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * In this class we encapsulate the process of moving an artifact from one
 * repository into another one.
 *
 * @author Julian Sauer (julian_sauer@mx.net)
 */
public class ArtifactPromotionHelper implements Serializable {

    protected final String groupId;
    protected final String artifactId;
    protected final String classifier;
    protected final String version;
    protected String extension;

    /**
     * The location of the local repository system relative to the workspace. In
     * this repository the downloaded artifact will be saved.
     */
    protected final String localRepoLocation = "target" + File.separator
            + "local-repo";

    /**
     * Name of the promoter class.
     */
    protected final String promoterClass;

    // Fields for UI
    /**
     * The repository there the artifact is. In a normal case a staging
     * repository.
     */
    protected final String stagingRepository;

    /**
     * The User for the staging Repository
     */
    protected final String stagingUser;

    /**
     * The staging secret. We should still save the passwords using the
     * credentials plugin but its so bad documented :-(
     */
    protected final Secret stagingPW;

    /**
     * The user for the release repo.
     */
    protected final String releaseUser;

    /**
     * The release repo secret
     */
    protected final Secret releasePW;

    /**
     * The repository into the artifact has to be moved.
     */
    protected final String releaseRepository;

    /**
     * Flag to write more info in the job console.
     */
    protected final boolean debug;

    /**
     * If true don't delete the artifact from the source repository.
     */
    protected boolean skipDeletion;

    /**
     * The default constructor. The parameters are injected by jenkins builder
     * and are the same as the (private) fields.
     *
     * @param groupId           The groupId of the artifact
     * @param artifactId        The artifactId of the artifact.
     * @param classifier        The classifier of the artifact.
     * @param version           The version of the artifact.
     * @param extension         The file extension of the artifact.
     * @param stagingRepository The URL of the staging repository.
     * @param stagingUser       User to be used on staging repo.
     * @param stagingPW         Password to be used on staging repo.
     * @param releaseUser       User to be used on release repo.
     * @param releasePW         Password to be used on release repo.
     * @param releaseRepository The URL of the staging repository
     * @param promoterClass     The vendor specific class which is used for the promotion, e.g. for NexusOSS
     * @param debug             Flag for debug output. Currently not used.
     */
    public ArtifactPromotionHelper(String groupId, String artifactId, String classifier,
                                   String version, String extension, String stagingRepository,
                                   String stagingUser, String stagingPW, String releaseUser,
                                   String releasePW, String releaseRepository, String promoterClass,
                                   boolean debug, boolean skipDeletion) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.classifier = classifier;
        this.version = version;
        this.extension = extension == null ? "jar" : extension;
        this.stagingRepository = stagingRepository;
        this.stagingUser = stagingUser;
        this.stagingPW = Secret.fromString(stagingPW);
        this.releaseUser = releaseUser;
        this.releasePW = Secret.fromString(releasePW);
        this.releaseRepository = releaseRepository;
        this.debug = debug;
        this.promoterClass = promoterClass;
        this.skipDeletion = skipDeletion;
    }

    public void perform(PrintStream logger, Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws PromotionException {
        AbstractPromotor artifactPromotor = null;

        // Initialize the promoter class
        // moved to here as the constructor of the builder is bypassed by stapler
        try {

            if (debug) {
                logger.println("Used promoter class: " + promoterClass);
            }

            artifactPromotor = (AbstractPromotor) Jenkins.get()
                    .getExtensionList(this.promoterClass).iterator().next();

        } catch (ClassNotFoundException e) {
            logger.println("ClassNotFoundException - unable to pick correct promotor class: " + e);
            throw new RuntimeException(e);
        }

        if (artifactPromotor == null) {
            logger.println("artifactPromotor is null - ABORTING!");
            throw new RuntimeException("artifactPromotor is null!");
        }
        artifactPromotor.setListener(listener);

        Map<PromotionBuildTokens, String> expandedTokens = expandTokens(build, workspace,
                listener);
        if (expandedTokens == null) {
            logger.println("Could not expand tokens - ABORTING!");
            throw new RuntimeException("Could not expand tokens");
        }
        artifactPromotor.setExpandedTokens(expandedTokens);
        artifactPromotor.setReleasePassword(releasePW);
        artifactPromotor.setReleaseUser(releaseUser);
        artifactPromotor.setStagingPassword(stagingPW);
        artifactPromotor.setStagingUser(stagingUser);
        artifactPromotor.setSkipDeletion(skipDeletion);

        String localRepoPath = workspace.getRemote() + File.separator
                + this.localRepoLocation;
        artifactPromotor.setLocalRepositoryURL(localRepoPath);

        if (debug) {
            logger.println("Local repository path: [" + localRepoPath + "]");
        }

        try {
            artifactPromotor.callPromotor(launcher.getChannel());
        } catch (PromotionException promEx) {
            logger.println(promEx.getMessage());
            throw promEx;
        }
    }

    /**
     * Expands needed build tokens
     *
     * @param build
     * @param listener
     * @return Map<PromotionBuildTokens, String> of expanded tokens
     */
    private Map<PromotionBuildTokens, String> expandTokens(
            Run<?, ?> build, FilePath workspace, TaskListener listener) {
        PrintStream logger = listener.getLogger();
        Map<PromotionBuildTokens, String> tokens = new HashMap<PromotionBuildTokens, String>();
        try {
            tokens.put(PromotionBuildTokens.GROUP_ID,
                    TokenMacro.expandAll(build, workspace, listener, groupId));
            tokens.put(PromotionBuildTokens.ARTIFACT_ID,
                    TokenMacro.expandAll(build, workspace, listener, artifactId));
            tokens.put(PromotionBuildTokens.CLASSIFIER,
                    TokenMacro.expandAll(build, workspace, listener, classifier));
            tokens.put(PromotionBuildTokens.VERSION,
                    TokenMacro.expandAll(build, workspace, listener, version));
            tokens.put(PromotionBuildTokens.EXTENSION, "".equals(extension) ? "jar" :
                    TokenMacro.expandAll(build, workspace, listener, extension));
            tokens.put(PromotionBuildTokens.STAGING_REPOSITORY,
                    TokenMacro.expandAll(build, workspace, listener, stagingRepository));
            tokens.put(PromotionBuildTokens.RELEASE_REPOSITORY,
                    TokenMacro.expandAll(build, workspace, listener, releaseRepository));
        } catch (MacroEvaluationException mee) {
            logger.println("Could not evaluate a makro" + mee);
            return null;

        } catch (IOException ioe) {
            logger.println("Got an IOException during evaluation of a makro token"
                    + ioe);
            return null;
        } catch (InterruptedException ie) {
            logger.println("Got an InterruptedException during avaluating a makro token"
                    + ie);
            return null;
        }
        return tokens;
    }

}
