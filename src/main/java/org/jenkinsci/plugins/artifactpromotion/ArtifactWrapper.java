package org.jenkinsci.plugins.artifactpromotion;

import org.eclipse.aether.artifact.Artifact;

public class ArtifactWrapper {

	private Artifact artifact = null;
	private Artifact pom = null;
	
	public ArtifactWrapper(Artifact artifact, Artifact pom) {
		this.artifact = artifact;
		this.pom = pom;
	}
	
	public Artifact getArtifact() {
		return artifact;
	}
	
	public Artifact getPom() {
		return pom;
	}
}
