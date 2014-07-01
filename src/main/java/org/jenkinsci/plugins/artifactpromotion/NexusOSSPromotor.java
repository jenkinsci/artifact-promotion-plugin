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

import hudson.Extension;
import hudson.model.Descriptor;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.deployment.DeployResult;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.jenkinsci.plugins.artifactpromotion.exception.PromotionException;

/**
 * 
 * @author Timo "timii" Paananen
 * 
 */
@Extension
public class NexusOSSPromotor extends AbstractPromotor {

	
	public void promote() throws PromotionException {
		AetherInteraction aether = getAether();
		RepositorySystem system = aether.getNewRepositorySystem();
		RepositorySystemSession session = aether.getRepositorySystemSession(
				system, getLocalRepositoryURL());

		RemoteRepository stagingRepository = getStagingRepository(getExpandedTokens()
						.get(PromotionBuildTokens.STAGING_REPOSITORY));
		
		ArtifactWrapper artifact = getArtifact(aether, system, session,
				stagingRepository);
		if (artifact == null) {
			throw new PromotionException(
					"Could not fetch artifacts for promotion");
		}

		// upload the artifact and its pom to the release repos
		DeployResult result = deployPromotionArtifact(system, session,
				artifact);
		if (result == null) {
			throw new PromotionException(
					"Could not deploy artifacts to release repository");
		}

		deleteArtifact(stagingRepository, artifact);
	}

	private ArtifactWrapper getArtifact(AetherInteraction aether,
			RepositorySystem system, RepositorySystemSession session,
			RemoteRepository stagingRepo) {

		getListener().getLogger().println("Get Artifact and corresponding POM");
		Artifact artifact = null;
		Artifact pom = null;
		try {
			artifact = aether.getArtifact(session, system, stagingRepo,
					getExpandedTokens().get(PromotionBuildTokens.GROUP_ID),
					getExpandedTokens().get(PromotionBuildTokens.ARTIFACT_ID),
					getExpandedTokens().get(PromotionBuildTokens.EXTENSION),
					getExpandedTokens().get(PromotionBuildTokens.VERSION));
			pom = aether.getArtifact(session, system, stagingRepo,
					getExpandedTokens().get(PromotionBuildTokens.GROUP_ID),
					getExpandedTokens().get(PromotionBuildTokens.ARTIFACT_ID),
					ArtifactPromotionBuilder.POMTYPE,
					getExpandedTokens().get(PromotionBuildTokens.VERSION));
		} catch (ArtifactResolutionException e) {
			getListener().getLogger().println(
					"Could not resolve artifact: " + e.getMessage());
			return null;
		}

		return new ArtifactWrapper(artifact, pom);
	}

	private DeployResult deployPromotionArtifact(RepositorySystem system, RepositorySystemSession session,
			ArtifactWrapper artifact) {

		RemoteRepository releaseRepository = getReleaseRepository(getExpandedTokens()
						.get(PromotionBuildTokens.RELEASE_REPOSITORY));

		try {
			return getAether().deployArtifact(session, system, releaseRepository,
					artifact.getArtifact(), artifact.getPom());
		} catch (DeploymentException e) {
			getListener().getLogger().println(
					"Could not deploy artifact to " + releaseRepository
							+ " using User " + getReleaseUser() + ":"
							+ e.getMessage());
			return null;
		}
	}

	private void deleteArtifact(RemoteRepository aetherStagingRepo,
			ArtifactWrapper artifact) {
		IDeleteArtifact deleter = new DeleteArtifactNexusOSS(getStagingUser(),
				getStagingPassword(), getListener().getLogger(), false);
		deleter.deleteArtifact(aetherStagingRepo, artifact.getArtifact());
	}

	@SuppressWarnings("unchecked")
	public Descriptor<Promotor> getDescriptor() {
		return new AbstractPromotorDescription() {

			@Override
			public String getDisplayName() {
				return "Nexus OSS";
			}
		};
	}
}
