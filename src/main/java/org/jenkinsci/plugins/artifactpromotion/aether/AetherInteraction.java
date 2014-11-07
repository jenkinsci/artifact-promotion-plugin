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
package org.jenkinsci.plugins.artifactpromotion.aether;

import hudson.model.BuildListener;
import hudson.util.Secret;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeployResult;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.jenkinsci.plugins.artifactpromotion.JenkinsConsoleTransferListener;
import org.jenkinsci.plugins.artifactpromotion.Repository;

/**
 * Interactions with aether.
 * 
 * @author Halil-Cem Guersoy
 * @author Timo "timii" Paananen
 * 
 */
public class AetherInteraction {

	private BuildListener listener;
	private RepositorySystem repositorySystem;
	private RepositorySystemSession session;

	public AetherInteraction(BuildListener listener) {
		super();
		this.listener = listener;
	}

	public AetherInteraction(BuildListener listener, String localRepositoryURL) {
		this(listener);
		this.repositorySystem = getNewRepositorySystem();
		this.session = createSession(localRepositoryURL);
		
	}

	private RepositorySystemSession createSession(String localRepositoryURL) {
		LocalRepository localRepository = new LocalRepository(localRepositoryURL);
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
		session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(session, localRepository));
		session.setTransferListener(new JenkinsConsoleTransferListener(this.listener.getLogger()));
		return session;
	}

	/**
	 * Deploy the aertifact to a repository server.
	 * 
	 * @param session
	 * @param system
	 * @param releaseRepo
	 * @param artifact
	 * @param pom
	 * @return
	 * @throws DeploymentException
	 */
	public DeployResult deployArtifacts(Repository repository, Artifact... artifacts) throws DeploymentException {
		DeployRequest deployRequest = new DeployRequest();
		deployRequest.setRepository(this.getRepository(repository));
		for (Artifact artifact : artifacts) {
			listener.getLogger().println(
					"Deploying artifact " + artifact.getGroupId() + ":" + artifact.getArtifactId() + ":"
							+ artifact.getVersion()+":"+artifact.getClassifier()+":"+artifact.getExtension());

			deployRequest.addArtifact(artifact);
		}
		return repositorySystem.deploy(session, deployRequest);
	}

	/**
	 * Get ('resolve') the artifact from a repository server. If the artifact is
	 * in local repository used by the plugin it will not download it from the
	 * server and use the local silently. This local repo will be cleaned by
	 * 'mvn clean' as the default location is target/local-repo.
	 * 
	 * @param groupId
	 * @param artifactId
	 * @param classfier
	 * @param type
	 * @param version
	 * @param repository
	 * @return artifact
	 * @throws ArtifactResolutionException
	 */
	public Artifact getArtifact(final String groupId, final String artifactId, final String classifier,
			final String type, final String version, Repository repository) throws ArtifactResolutionException {
		ArtifactRequest jarRequest = new ArtifactRequestBuilder().createRequest()
				.setArtifact(groupId, artifactId, classifier, type, version).setRepository(getRepository(repository)).build();
		ArtifactResult artifactResult = repositorySystem.resolveArtifact(session, jarRequest);

		return artifactResult.getArtifact();
	}

	/**
	 * Get ('resolve') the artifact with pom, sources and javadoc from a repository server. If the artifact is
	 * in local repository used by the plugin it will not download it from the
	 * server and use the local silently. This local repo will be cleaned by
	 * 'mvn clean' as the default location is target/local-repo.
	 * 
	 * @param groupId
	 * @param artifactId
	 * @param classfier
	 * @param type
	 * @param version
	 * @param repository
	 * @return List of artifacts
	 * @throws ArtifactResolutionException
	 */
	public List<Artifact> getArtifacts(final Repository repository, final String groupId, final String artifactId,
			final String type, final String version) throws ArtifactResolutionException {
		RemoteRepository repo = getRepository(repository);
		ArtifactRequest pomRequest = new ArtifactRequestBuilder().createRequest()
				.setArtifact(groupId, artifactId, null, "pom", version).setRepository(repo).build();
		ArtifactRequest jarRequest = new ArtifactRequestBuilder().createRequest()
				.setArtifact(groupId, artifactId, null, type, version).setRepository(repo).build();
		ArtifactRequest javadocRequest = new ArtifactRequestBuilder().createRequest()
				.setArtifact(groupId, artifactId, "javadoc", type, version).setRepository(repo).build();
		ArtifactRequest sourcesRequest = new ArtifactRequestBuilder().createRequest()
				.setArtifact(groupId, artifactId, "sources", type, version).setRepository(repo).build();

		List<ArtifactRequest> requests = new ArrayList<ArtifactRequest>();
		requests.add(pomRequest);
		requests.add(jarRequest);
		requests.add(sourcesRequest);
		requests.add(javadocRequest);

		List<ArtifactResult> results = repositorySystem.resolveArtifacts(session, requests);

		List<Artifact> artifacts = new ArrayList<Artifact>();
		for (ArtifactResult result : results) {
			artifacts.add(result.getArtifact());
		}

		return artifacts;
	}

	private RepositorySystem getNewRepositorySystem() {
		return RepositorySystemFactory.getNewRepositorySystem(listener.getLogger());
	}

	private RemoteRepository getRepository(Repository repository) {

		if (repository.getURL() == null) {
			throw new IllegalArgumentException("You cant provide null objects here.");
		}

		RemoteRepository.Builder builder = new RemoteRepository.Builder(repository.getId(), "default",
				repository.getURL());

		if (isAuthenticationData(repository)) {
			Authentication authentication = new AuthenticationBuilder().addUsername(repository.getUsername())
					.addPassword(Secret.toString(repository.getPassword())).build();

			builder = builder.setAuthentication(authentication);
		}

		return builder.build();
	}

	private boolean isAuthenticationData(Repository repository) {
		boolean usernameGiven = repository.getUsername() != null && !repository.getUsername().isEmpty();
		boolean passwordGiven = repository.getPassword() != null 
				&& !Secret.toString(repository.getPassword()).isEmpty();
		return usernameGiven && passwordGiven;
	}
}
