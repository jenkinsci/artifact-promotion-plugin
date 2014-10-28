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
import java.util.Arrays;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
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
import org.jenkinsci.plugins.artifactpromotion.ArtifactWrapper;
import org.jenkinsci.plugins.artifactpromotion.JenkinsConsoleTransferListener;
import org.jenkinsci.plugins.artifactpromotion.RepositorySystemFactory;

/**
 * Interactions with aether.
 * 
 * @author Halil-Cem Guersoy
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

	private String createArtifactCoordsString(String groupId, String artifactId, String classifier, String version) {
		StringBuilder coordsBuilder = new StringBuilder();
		coordsBuilder.append(groupId);
		coordsBuilder.append(":");
		coordsBuilder.append(artifactId);
		coordsBuilder.append(":");
		if (classifier != null) {
			coordsBuilder.append(classifier);
			coordsBuilder.append(":");
		}
		coordsBuilder.append(version);
		return coordsBuilder.toString();
	}

	public DeployResult deployArtifact(ArtifactWrapper artifact, RepositoryLogin repositoryLogin)
			throws DeploymentException {
		DeployRequest deployRequest = new DeployRequest();
		deployRequest.setRepository(getRepository(repositoryLogin));

		deployRequest.addArtifact(artifact.getArtifact());
		deployRequest.addArtifact(artifact.getPom());
		for (Artifact classierArtifact : artifact.getClassifierArtifacts()) {
			deployRequest.addArtifact(classierArtifact);
		}
		return repositorySystem.deploy(session, deployRequest);
	}

	public ArtifactWrapper getArtifact(final String groupId, final String artifactId, final String classifier,
			final String type, final String version, RepositoryLogin logins) throws ArtifactResolutionException {
		ArtifactRequest artifactRequest = new ArtifactRequest();
		artifactRequest.setRepositories(new ArrayList<RemoteRepository>(Arrays.asList(getRepository(logins))));

		Artifact artifact = new DefaultArtifact(createArtifactCoordsString(groupId, artifactId, classifier, version));
		artifactRequest.setArtifact(artifact);

		ArtifactResult artifactResult = repositorySystem.resolveArtifact(session, artifactRequest);

		artifact = artifactResult.getArtifact();
		return new ArtifactWrapper(artifact, null);
	}

	private RemoteRepository getRepository(final RepositoryLogin logins) {
		RemoteRepository.Builder repositoryBuilder = new RemoteRepository.Builder(logins.getRepositoryId(), "default",
				logins.getRepositoryURL());
		if (logins.getUsername() != null && logins.getPassword() != null) {
			Authentication authentication = new AuthenticationBuilder().addUsername(logins.getUsername())
					.addPassword(Secret.toString(logins.getPassword())).build();

			repositoryBuilder = repositoryBuilder.setAuthentication(authentication);
		}
		return repositoryBuilder.build();
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
	protected DeployResult deployArtifact(final RepositorySystemSession session, final RepositorySystem system,
			final RemoteRepository releaseRepo, final ArtifactWrapper wrapped) throws DeploymentException {

		DeployRequest deployRequest = new DeployRequest();
		deployRequest.addArtifact(wrapped.getArtifact());
		deployRequest.addArtifact(wrapped.getPom());
		deployRequest.setRepository(releaseRepo);
		return system.deploy(session, deployRequest);
	}

	/**
	 * Get ('resolve') the artifact from a repository server. If the artifact is
	 * in local repository used by the plugin it will not download it from the
	 * server and use the local silently. This local repo will be cleaned by
	 * 'mvn clean' as the default location is target/local-repo.
	 * 
	 * @param session
	 * @param system
	 * @param remoteRepos
	 * @param groupId
	 * @param artifactId
	 * @param type
	 * @param version
	 * @return
	 * @throws ArtifactResolutionException
	 */
	protected Artifact getArtifact(final RepositorySystemSession session, RepositorySystem system,
			final RemoteRepository remoteRepo, final String groupId, final String artifactId, final String type,
			final String version) throws ArtifactResolutionException {

		Artifact artifact = new DefaultArtifact(groupId + ":" + artifactId + ":" + type + ":" + version);
		ArtifactRequest artifactRequest = new ArtifactRequest();
		artifactRequest.setArtifact(artifact);
		artifactRequest.setRepositories(new ArrayList<RemoteRepository>(Arrays.asList(remoteRepo)));
		ArtifactResult artifactResult = system.resolveArtifact(session, artifactRequest);
		artifact = artifactResult.getArtifact();
		return artifact;
	}

	public RepositorySystem getNewRepositorySystem() {
		return RepositorySystemFactory.getNewRepositorySystem(listener.getLogger());
	}

	public DefaultRepositorySystemSession getRepositorySystemSession(final RepositorySystem system,
			final String localRepoLocation) {

		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
		LocalRepository localRepo = new LocalRepository(localRepoLocation);
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
		session.setTransferListener(new JenkinsConsoleTransferListener(listener.getLogger()));
		return session;
	}

	/**
	 * Creates a RemoteRepository object to work with. If a User or Password is
	 * given the authentication information is set, too.
	 * 
	 * @param user
	 * @param password
	 * @param repoId
	 * @param repoURL
	 * @return The remote repository to connect to.
	 */
	protected RemoteRepository getRepository(final String user, final Secret password, final String repoId,
			final String repoURL) {

		if (user == null || password == null || repoId == null || repoURL == null)
			throw new IllegalArgumentException("You cant provide null objects here.");

		RemoteRepository.Builder builder = new RemoteRepository.Builder(repoId, "default", repoURL);

		if (user.length() > 0 || Secret.toString(password).length() > 0) {
			Authentication authentication = new AuthenticationBuilder().addUsername(user)
					.addPassword(Secret.toString(password)).build();

			builder = builder.setAuthentication(authentication);
		}

		return builder.build();
	}
}
