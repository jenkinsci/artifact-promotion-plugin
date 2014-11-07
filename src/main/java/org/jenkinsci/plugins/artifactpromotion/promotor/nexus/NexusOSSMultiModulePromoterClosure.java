package org.jenkinsci.plugins.artifactpromotion.promotor.nexus;

import hudson.model.BuildListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.jenkinsci.plugins.artifactpromotion.ClassifierEnum;
import org.jenkinsci.plugins.artifactpromotion.PromotionBuildTokens;
import org.jenkinsci.plugins.artifactpromotion.Repository;
import org.jenkinsci.plugins.artifactpromotion.exception.ArtifactResolveException;
import org.jenkinsci.plugins.artifactpromotion.exception.PromotionException;
import org.jenkinsci.plugins.artifactpromotion.helper.PomHelper;

/**
 * 
 * @author Timo "timii" Paananen
 * 
 */
public class NexusOSSMultiModulePromoterClosure extends AbstractNexusOSSPromoterClosure {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private PomHelper pomHelper = new PomHelper();
	private static final List<ClassifierEnum> defaultClassifiers = new ArrayList<ClassifierEnum>();

	public NexusOSSMultiModulePromoterClosure(BuildListener listener, String localRepositoryURL,
			Map<PromotionBuildTokens, String> expandedTokens, Repository stagingRepository, Repository releaseRepository, boolean generatePom) {
		super(listener, localRepositoryURL, expandedTokens, stagingRepository, releaseRepository, generatePom);
		defaultClassifiers.add(ClassifierEnum.JAVADOC);
		defaultClassifiers.add(ClassifierEnum.SOURCES);
	}

	@Override
	public void promote() throws PromotionException {
		this.listener.getLogger().println("Started with promotion");
		List<Artifact> artifacts = null;
		try {
			artifacts = getAllArtifacts();
		} catch (ArtifactResolveException e) {
			throw new PromotionException("Could not fetch artifacts for promotion", e);
		}
		doPromotion(artifacts);
	}

	private List<Artifact> getAllArtifacts() throws ArtifactResolveException {
		Artifact artifact = null;
		try {
			artifact = getArtifact(this.expandedTokens.get(PromotionBuildTokens.ARTIFACT_ID), "pom");
		} catch (ArtifactResolutionException e) {
			throw new ArtifactResolveException(e);
		}
		List<String> modules = pomHelper.getModuleNames(artifact);
		List<Artifact> artifacts = new ArrayList<Artifact>();
		artifacts.add(artifact);
		for (String module : modules) {
			artifacts.addAll(getModuleArtifacts(module));
		}

		return artifacts;
	}

	private void doPromotion(List<Artifact> artifacts) throws PromotionException {
		// upload the artifacts and theirs pom files to the release repos
		Artifact[] artifactArray = artifacts.toArray(new Artifact[artifacts.size()]);
		deployPromotionArtifact(artifactArray);

		// Remove staging artifacts if all artifacts have been promoted to
		// release repository
		deleteArtifact(artifactArray);

	}

	private List<Artifact> getModuleArtifacts(String artifactId) throws ArtifactResolveException {

		this.listener.getLogger().println("Get Artifact and corresponding POM");
		List<Artifact> artifacts = new ArrayList<Artifact>();
		Artifact pom;
		try {
			pom = getArtifact(artifactId, "pom");
			String packaging = pomHelper.getModulesPackaging(pom);
			if(!this.generatePom) {
				artifacts.add(getArtifact(artifactId, "pom"));
			}
			artifacts.add(getArtifact(artifactId, packaging));

		} catch (ArtifactResolutionException e) {
			this.listener.getLogger().println("Could not resolve artifact: " + e.getMessage());
			throw new ArtifactResolveException(e);
		}
		// Try to fetch source and javadoc artifacts. It's not fatal if fetching
		// fails
		try {
			artifacts.add(aether.getArtifact(this.expandedTokens.get(PromotionBuildTokens.GROUP_ID), artifactId,
					ClassifierEnum.JAVADOC.getValue(), "jar", this.expandedTokens.get(PromotionBuildTokens.VERSION),
					stagingRepository));
		} catch (ArtifactResolutionException e) {
			listener.getLogger().println("Failed to resolve Javadoc jar for artifact: " + pom.toString());
		}
		try {
			artifacts.add(aether.getArtifact(this.expandedTokens.get(PromotionBuildTokens.GROUP_ID), artifactId,
					ClassifierEnum.SOURCES.getValue(), "jar", this.expandedTokens.get(PromotionBuildTokens.VERSION),
					stagingRepository));
		} catch (ArtifactResolutionException e) {
			listener.getLogger().println("Failed to resolve Sources jar for artifact: " + pom.toString());
		}
		return artifacts;
	}

	private Artifact getArtifact(String artifactId, String packaging) throws ArtifactResolutionException {
		return aether.getArtifact(this.expandedTokens.get(PromotionBuildTokens.GROUP_ID), artifactId, null, packaging,
				this.expandedTokens.get(PromotionBuildTokens.VERSION), stagingRepository);
	}
}
