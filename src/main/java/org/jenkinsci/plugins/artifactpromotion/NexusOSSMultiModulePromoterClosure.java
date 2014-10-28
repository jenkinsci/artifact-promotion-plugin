package org.jenkinsci.plugins.artifactpromotion;

import hudson.model.BuildListener;
import hudson.util.Secret;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.deployment.DeployResult;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.jenkinsci.plugins.artifactpromotion.aether.AetherInteraction;
import org.jenkinsci.plugins.artifactpromotion.exception.ArtifactResolveException;
import org.jenkinsci.plugins.artifactpromotion.exception.PromotionException;
import org.jenkinsci.plugins.artifactpromotion.helper.PomHelper;

/**
 * 
 * @author Timo "timii" Paananen
 * 
 */
public class NexusOSSMultiModulePromoterClosure extends
		AbstractNexusOSSPromoterClosure {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private PomHelper pomHelper = new PomHelper();
	private AetherInteraction aether;
	private RepositorySystem system;
	private RepositorySystemSession session;
	private RemoteRepository stagingRepository;

	public NexusOSSMultiModulePromoterClosure(BuildListener listener,
			String localRepositoryURL,
			Map<PromotionBuildTokens, String> expandedTokens,
			String releaseUser, Secret releasePassword, String stagingUser,
			Secret stagingPassword) {
		super(listener, localRepositoryURL, expandedTokens, releaseUser,
				releasePassword, stagingUser, stagingPassword);
	}

	@Override
	public void promote() throws PromotionException {
		this.listener.getLogger().println("Started with promotion");

		aether = new AetherInteraction(this.listener);
		system = aether.getNewRepositorySystem();
		session = aether.getRepositorySystemSession(system, localRepositoryURL);

		stagingRepository = aether.getRepository(stagingUser, stagingPassword,
				"stagingrepo", this.expandedTokens
						.get(PromotionBuildTokens.STAGING_REPOSITORY));

		List<ArtifactWrapper> artifacts;
		try {
			ArtifactWrapper mainArtifact = new ArtifactWrapper(null,
					getPom(this.expandedTokens
							.get(PromotionBuildTokens.ARTIFACT_ID)));
			artifacts = getAllArtifactsFor(mainArtifact);
		} catch (ArtifactResolutionException e) {
			throw new PromotionException(
					"Could not fetch artifacts for promotion", e);
		}

		catch (ArtifactResolveException e) {
			throw new PromotionException(
					"Unexpected exception when resolving modules!", e);
		}
		doPromotion(artifacts);
	}

	private List<ArtifactWrapper> getAllArtifactsFor(
			ArtifactWrapper mainArtifact) throws ArtifactResolveException {
		List<String> modules = pomHelper.getModuleNames(mainArtifact.getPom());
		List<ArtifactWrapper> artifacts = new ArrayList<ArtifactWrapper>();
		artifacts.add(mainArtifact);

		for (String module : modules) {
			artifacts.add(getArtifactWithPom(module));
		}

		return artifacts;
	}

	private void doPromotion(List<ArtifactWrapper> artifacts)
			throws PromotionException {
		// upload the artifacts and theirs pom files to the release repos
		for (ArtifactWrapper artifact : artifacts) {
			if (artifact == null) {
				continue;
			}
			DeployResult result = deployPromotionArtifact(aether, system,
					session, artifact);
			if (result == null) {
				throw new PromotionException(
						"Could not deploy artifacts to release repository");
			}
		}
		// Remove staging artifacts if all artifacts have been promoted to
		// release repository
		for (ArtifactWrapper artifact : artifacts) {
			deleteArtifact(stagingRepository, artifact);
		}
	}

	private ArtifactWrapper getArtifactWithPom(String artifactId)
			throws ArtifactResolveException {

		this.listener.getLogger().println("Get Artifact and corresponding POM");
		Artifact artifact = null;
		Artifact pom = null;
		Artifact sources = null;
		Artifact javadocs = null;
		try {
			pom = getPom(artifactId);
			String packaging = pomHelper.getModulesPackaging(pom);
			artifact = getArtifact(artifactId, packaging);
			if(!classifiers.isEmpty()) {
				
			}

		} catch (ArtifactResolutionException e) {
			this.listener.getLogger().println(
					"Could not resolve artifact: " + e.getMessage());
			throw new ArtifactResolveException(e);
		}

		return new ArtifactWrapper(artifact, pom);
	}

	private Artifact getArtifact(String artifactId, String packaging)
			throws ArtifactResolutionException {
		return aether.getArtifact(
				session,
				system,
				stagingRepository,
				this.expandedTokens.get(PromotionBuildTokens.GROUP_ID),
				artifactId,
				packaging == null ? this.expandedTokens
						.get(PromotionBuildTokens.EXTENSION) : packaging,
				this.expandedTokens.get(PromotionBuildTokens.VERSION));
	}

	private Artifact getPom(String artifactId)
			throws ArtifactResolutionException {
		return getArtifact(artifactId, ArtifactPromotionBuilder.POMTYPE);
	}	
	
	private Artifact getArtifactWithClassifier(String artifactId, ClassifierEnum classifier) throws ArtifactResolutionException {
		return aether.getArtifact(
				session,
				system,
				stagingRepository,
				this.expandedTokens.get(PromotionBuildTokens.GROUP_ID),
				artifactId,
				"jar",
				this.expandedTokens.get(PromotionBuildTokens.VERSION) + classifier.getValue());
	}
}
