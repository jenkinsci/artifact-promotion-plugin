package org.jenkinsci.plugins.artifactpromotion.aether;

import hudson.model.BuildListener;

import java.util.Collection;
import java.util.Map;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.deployment.DeployResult;
import org.eclipse.aether.metadata.Metadata;


/**
 * Provides helper methods for logging artifact info
 * 
 * @author Halil-Cem Guersoy
 * @author Timo "timii" Paananen
 * 
 */
@Deprecated
public class AetherTracer {

	private BuildListener listener;

	public AetherTracer(BuildListener listener) {
		this.listener = listener;
	}

	public void traceArtifactInfo(Artifact artifact) {
		this.listener.getLogger().println("-------------- artifact info");
		this.listener.getLogger().println(artifact + " resolved to  " + artifact.getFile());
		this.listener.getLogger().println("File :" + artifact.getFile());
		this.listener.getLogger().println("Properties:");
		Map<String, String> props = artifact.getProperties();
		for (String key : props.keySet()) {
			this.listener.getLogger().println("Key:" + key + " Value: " + props.get(key));
		}
		this.listener.getLogger().println("Base-Version:" + artifact.getBaseVersion());
		this.listener.getLogger().println("Version: " + artifact.getVersion());
		this.listener.getLogger().println("Classifier: " + artifact.getClassifier());
		this.listener.getLogger().println("Extension: " + artifact.getExtension());
	}

	public void traceDeployResult(DeployResult result) {
		this.listener.getLogger().println("-------------- Deploy result info");
		this.listener.getLogger().println(result.getRequest().getTrace());
		Collection<Metadata> allMetadata = result.getMetadata();
		traceMetadata(allMetadata);
	}

	public void traceMetadata(Collection<Metadata> allMetadata) {
		for (Metadata metadata : allMetadata) {

			this.listener.getLogger().println("ArtifactID : " + metadata.getArtifactId());
			this.listener.getLogger().println("GroupID : " + metadata.getGroupId());
			this.listener.getLogger().println("Typ : " + metadata.getType());
			this.listener.getLogger().println("Version : " + metadata.getVersion());
			this.listener.getLogger().println("Nature : " + metadata.getNature());
			Map<String, String> props = metadata.getProperties();
			for (String key : props.keySet()) {
				this.listener.getLogger().println("Key:" + key + " Value: " + props.get(key));
			}
		}
	}
}
