package org.jenkinsci.plugins.artifactpromotion.promotor.nexus;

import hudson.model.BuildListener;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.deployment.DeployResult;
import org.eclipse.aether.deployment.DeploymentException;
import org.jenkinsci.plugins.artifactpromotion.ClassifierEnum;
import org.jenkinsci.plugins.artifactpromotion.PromotionBuildTokens;
import org.jenkinsci.plugins.artifactpromotion.Repository;
import org.jenkinsci.plugins.artifactpromotion.aether.AetherInteraction;
import org.jenkinsci.plugins.artifactpromotion.exception.PromotionException;
import org.jenkinsci.plugins.artifactpromotion.promotor.IDeleteArtifact;
import org.jenkinsci.plugins.artifactpromotion.promotor.IPromotorClosure;

/**
 * 
 * @author Timo "timii" Paananen
 * 
 */
public abstract class AbstractNexusOSSPromoterClosure implements Serializable, IPromotorClosure {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String localRepositoryURL;
	protected Map<PromotionBuildTokens, String> expandedTokens;
	protected Repository stagingRepository;
	protected Repository releaseRepository;
	protected BuildListener listener;
	protected List<ClassifierEnum> classifiers = Collections.emptyList();
	protected AetherInteraction aether;
	protected boolean generatePom;

	public AbstractNexusOSSPromoterClosure(BuildListener listener, String localRepositoryURL,
			Map<PromotionBuildTokens, String> expandedTokens, Repository stagingRepository, Repository releaseRepository, boolean generatePom) {
		this.aether = new AetherInteraction(listener, localRepositoryURL);
		this.expandedTokens = expandedTokens;
		this.listener = listener;
		this.stagingRepository = stagingRepository;
		this.releaseRepository = releaseRepository;
		this.localRepositoryURL = localRepositoryURL;
		this.generatePom = generatePom;
	}

	protected DeployResult deployPromotionArtifact(Artifact... artifact) throws PromotionException {
		try {
			return aether.deployArtifacts(this.releaseRepository, artifact);
		} catch (DeploymentException e) {
			this.listener.getLogger().println(
					"Could not deploy artifact to " + this.releaseRepository.getURL() + " using User "
							+ this.releaseRepository.getUsername() + ":" + e.getMessage());
			throw new PromotionException("Could not deploy artifacts to release repository", e);
		}
	}

	protected void deleteArtifact(Artifact... artifacts) {
		this.listener.getLogger().print("Deleting artifacts from  " + this.stagingRepository.getURL() + " repository");
		IDeleteArtifact deleter = new DeleteArtifactNexusOSS(this.listener, this.stagingRepository, false);
		Set<String> deleted = new HashSet<String>();
		for (Artifact artifact : artifacts) {
			//Rest api will delete all related artifacts at once.
			//no need to separately delete source, javadoc or pom files
			if (!deleted.contains(artifact.getArtifactId())) {
				listener.getLogger().println("artifact to delete: " + artifact.toString());
				deleter.deleteArtifact(artifact);
				deleted.add(artifact.getArtifactId());
			}
		}

	}

	public abstract void promote() throws PromotionException;

}