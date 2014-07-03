package org.jenkinsci.plugins.artifactpromotion;

import hudson.model.BuildListener;
import hudson.util.Secret;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.deployment.DeployResult;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.jenkinsci.plugins.artifactpromotion.exception.PromotionException;

public class NexusOSSPromoterClosure implements Serializable, IPromotorClosure {
	
	private static final long serialVersionUID = 1L;
	
	private String localRepositoryURL; 
	private Map<PromotionBuildTokens, String> expandedTokens;
	private String releaseUser;
	private Secret releasePassword;
	private String stagingUser;
	private Secret stagingPassword;
	
	/**
	 * @param localRepositoryURL
	 * @param listener
	 * @param expandedTokens
	 * @param releaseUser
	 * @param releasePassword
	 * @param stagingUser
	 * @param stagingPassword
	 */
	public NexusOSSPromoterClosure(
			String localRepositoryURL,
			BuildListener listener,
			Map<PromotionBuildTokens, String> expandedTokens,
			String releaseUser, Secret releasePassword,
			String stagingUser, Secret stagingPassword) {
		super();
		
		this.localRepositoryURL = localRepositoryURL;
		
		this.expandedTokens = expandedTokens;
		
		this.releaseUser = releaseUser;
		this.releasePassword = releasePassword;
		this.stagingUser = stagingUser;
		this.stagingPassword = stagingPassword;
		
	}

	/* (non-Javadoc)
	 * @see org.jenkinsci.plugins.artifactpromotion.IPromotorClosure#promote()
	 */
	public void promote() throws PromotionException {
		
		
		AetherInteraction aether = new AetherInteraction();
		RepositorySystem system = aether.getNewRepositorySystem();
		RepositorySystemSession session = aether.getRepositorySystemSession(
				system, localRepositoryURL);

		//only debug
		System.err.println("stagingUser [" + stagingUser + "]");
		System.err.println("stagingPassword [" + stagingPassword + "]");
		System.err.println("stagingRepo [" + this.expandedTokens
				.get(PromotionBuildTokens.STAGING_REPOSITORY) + "]");
		
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

		deleteArtifact(stagingRepository, artifact);
	}

	private ArtifactWrapper getArtifact(AetherInteraction aether,
			RepositorySystem system, RepositorySystemSession session,
			RemoteRepository stagingRepo) {

		System.out.println("Get Artifact and corresponding POM");
		Artifact artifact = null;
		Artifact pom = null;
		try {
			artifact = aether.getArtifact(session, system, stagingRepo,
					this.expandedTokens.get(PromotionBuildTokens.GROUP_ID),
					this.expandedTokens.get(PromotionBuildTokens.ARTIFACT_ID),
					this.expandedTokens.get(PromotionBuildTokens.EXTENSION),
					this.expandedTokens.get(PromotionBuildTokens.VERSION));
			pom = aether.getArtifact(session, system, stagingRepo,
					this.expandedTokens.get(PromotionBuildTokens.GROUP_ID),
					this.expandedTokens.get(PromotionBuildTokens.ARTIFACT_ID),
					ArtifactPromotionBuilder.POMTYPE,
					this.expandedTokens.get(PromotionBuildTokens.VERSION));
		} catch (ArtifactResolutionException e) {
			System.out.println(
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
			System.out.println(
					"Could not deploy artifact to " + releaseRepository
							+ " using User " + releaseUser + ":"
							+ e.getMessage());
			return null;
		}
	}

	private void deleteArtifact(RemoteRepository aetherStagingRepo,
			ArtifactWrapper artifact) {
		IDeleteArtifact deleter = new DeleteArtifactNexusOSS(this.stagingUser,
				this.stagingPassword, false);
		deleter.deleteArtifact(aetherStagingRepo, artifact.getArtifact());
	}

}
