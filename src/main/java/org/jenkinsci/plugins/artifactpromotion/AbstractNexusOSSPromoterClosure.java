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
import org.jenkinsci.plugins.artifactpromotion.exception.PromotionException;

/**
 * 
 * @author Timo "timii" Paananen
 * 
 */
public abstract class AbstractNexusOSSPromoterClosure implements Serializable,
		IPromotorClosure {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String localRepositoryURL;
	protected Map<PromotionBuildTokens, String> expandedTokens;
	protected String releaseUser;
	protected Secret releasePassword;
	protected String stagingUser;
	protected Secret stagingPassword;
	protected BuildListener listener;

	public AbstractNexusOSSPromoterClosure(BuildListener listener,
			String localRepositoryURL,
			Map<PromotionBuildTokens, String> expandedTokens,
			String releaseUser, Secret releasePassword, String stagingUser,
			Secret stagingPassword) {

		this.expandedTokens = expandedTokens;
		this.listener = listener;
		this.releaseUser = releaseUser;
		this.releasePassword = releasePassword;
		this.stagingUser = stagingUser;
		this.stagingPassword = stagingPassword;
		this.localRepositoryURL = localRepositoryURL;
	}

	protected DeployResult deployPromotionArtifact(AetherInteraction aether,
			RepositorySystem system, RepositorySystemSession session,
			ArtifactWrapper artifact) {
		if (artifact.getArtifact() != null) {
			listener.getLogger().println(
					"Deploying artifact " + artifact.getArtifact().getGroupId() + ":"
							+ artifact.getArtifact().getArtifactId() + ":" + artifact.getArtifact().getVersion());
		}
		listener.getLogger().println(
				"Deploying pom " + artifact.getPom().getGroupId() + ":"
						+ artifact.getPom().getArtifactId() + ":" + artifact.getPom().getVersion());
		
		RemoteRepository releaseRepository = aether.getRepository(releaseUser,
				releasePassword, "releaserepo", this.expandedTokens
						.get(PromotionBuildTokens.RELEASE_REPOSITORY));
		try {
			
			return aether.deployArtifact(session, system, releaseRepository,
					artifact.getArtifact(), artifact.getPom());
		} catch (DeploymentException e) {
			this.listener.getLogger().println(
					"Could not deploy artifact to " + releaseRepository
							+ " using User " + releaseUser + ":"
							+ e.getMessage());
			e.printStackTrace();
			//FIXME: we should just rethrow
			return null;
		}
	}

	protected void deleteArtifact(RemoteRepository aetherStagingRepo,
			ArtifactWrapper artifact) {
		IDeleteArtifact deleter = new DeleteArtifactNexusOSS(this.listener,
				this.stagingUser, this.stagingPassword, false);
		listener.getLogger().println("artifact to delete: " + artifact.toString());
		Artifact ar = artifact.getArtifact() == null ? artifact.getPom() : artifact.getArtifact();		
		deleter.deleteArtifact(aetherStagingRepo, ar);
	}

	public abstract void promote() throws PromotionException;

}