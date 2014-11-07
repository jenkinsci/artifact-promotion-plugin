package org.jenkinsci.plugins.artifactpromotion.promotor.nexus;

import hudson.model.BuildListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.deployment.DeployResult;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.jenkinsci.plugins.artifactpromotion.PromotionBuildTokens;
import org.jenkinsci.plugins.artifactpromotion.Repository;
import org.jenkinsci.plugins.artifactpromotion.exception.PromotionException;

public class NexusOSSPromoterClosure extends AbstractNexusOSSPromoterClosure {

	private static final long serialVersionUID = 1L;

	/**
	 * @param localRepositoryURL
	 * @param listener
	 * @param expandedTokens
	 * @param stagingRepository
	 * @param releaseRepository
	 */
	public NexusOSSPromoterClosure(BuildListener listener, String localRepositoryURL,
			Map<PromotionBuildTokens, String> expandedTokens, Repository stagingRepository,
			Repository releaseRepository, boolean generatePom) {
		super(listener, localRepositoryURL, expandedTokens, stagingRepository, releaseRepository, generatePom);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jenkinsci.plugins.artifactpromotion.IPromotorClosure#promote()
	 */
	public void promote() throws PromotionException {
		this.listener.getLogger().println("Started with promotion");

		List<Artifact> artifacts = getArtifact();
		if (artifacts == null || artifacts.isEmpty()) {
			throw new PromotionException("Could not fetch artifacts for promotion");
		}

		// upload the artifact and its pom to the release repos
		DeployResult result = deployPromotionArtifact(artifacts.toArray(new Artifact[artifacts.size()]));
		if (result == null) {
			throw new PromotionException("Could not deploy artifacts to release repository");
		}

		deleteArtifact(artifacts.toArray(new Artifact[artifacts.size()]));
	}

	private List<Artifact> getArtifact() {
		this.listener.getLogger().println("Get Artifact and corresponding POM");
		List<Artifact> artifacts = new ArrayList<Artifact>();
		try {
			artifacts.add(aether.getArtifact(this.expandedTokens.get(PromotionBuildTokens.GROUP_ID),
					this.expandedTokens.get(PromotionBuildTokens.ARTIFACT_ID), null,
					this.expandedTokens.get(PromotionBuildTokens.EXTENSION),
					this.expandedTokens.get(PromotionBuildTokens.VERSION), stagingRepository));
			if (!generatePom) {
				artifacts.add(aether.getArtifact(this.expandedTokens.get(PromotionBuildTokens.GROUP_ID),
						this.expandedTokens.get(PromotionBuildTokens.ARTIFACT_ID), null,
						this.expandedTokens.get("pom"), this.expandedTokens.get(PromotionBuildTokens.VERSION),
						stagingRepository));
			}

		} catch (ArtifactResolutionException e) {
			this.listener.getLogger().println("Could not resolve artifact: " + e.getMessage());
			return null;
		}

		try {
			artifacts.add(aether.getArtifact(this.expandedTokens.get(PromotionBuildTokens.GROUP_ID),
					this.expandedTokens.get(PromotionBuildTokens.ARTIFACT_ID), "javadoc", "jar",
					this.expandedTokens.get(PromotionBuildTokens.VERSION), stagingRepository));
		} catch (ArtifactResolutionException e) {
			logFailedArtifactResovle("Javadoc jar");
		}

		try {
			artifacts.add(aether.getArtifact(this.expandedTokens.get(PromotionBuildTokens.GROUP_ID),
					this.expandedTokens.get(PromotionBuildTokens.ARTIFACT_ID), "sources", "jar",
					this.expandedTokens.get(PromotionBuildTokens.VERSION), stagingRepository));
		} catch (ArtifactResolutionException e) {
			logFailedArtifactResovle("Sources jar");
		}
		return artifacts;
	}

	private void logFailedArtifactResovle(String classifier) {
		listener.getLogger().println(
				"Failed to resolve " + classifier + " for artifact "
						+ this.expandedTokens.get(PromotionBuildTokens.GROUP_ID) + ":"
						+ this.expandedTokens.get(PromotionBuildTokens.ARTIFACT_ID) + ":"
						+ this.expandedTokens.get(PromotionBuildTokens.EXTENSION) + ":"
						+ this.expandedTokens.get(PromotionBuildTokens.VERSION));
	}
}
