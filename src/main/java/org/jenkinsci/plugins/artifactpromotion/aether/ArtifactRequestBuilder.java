package org.jenkinsci.plugins.artifactpromotion.aether;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;

/**
 * Builder for building Aether ArtifactRequests
 *  
 * @author Timo "timii" Paananen
 *
 */
public class ArtifactRequestBuilder {
	private ArtifactRequest request;

	public ArtifactRequestBuilder createRequest() {
		this.request = new ArtifactRequest();
		return this;
	}
	
	public ArtifactRequestBuilder setRepository(RemoteRepository repository) {
		request.addRepository(repository);
		return this;
	}
	
	public ArtifactRequestBuilder setArtifact(String groupId, String artifactId, String classifier, String extension, String version) {
		DefaultArtifact artifact = new DefaultArtifact(groupId,artifactId,classifier, extension, version);
		request.setArtifact(artifact);
		return this;
	}
	
	public ArtifactRequest build() {
		return request;
	}
}
