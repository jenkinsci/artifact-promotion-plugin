/**
 * The MIT License
 * Copyright (c) 2014 Halil-Cem Guersoy
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
package org.jenkinsci.plugins.artifactpromotion;

import hudson.util.Secret;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeployResult;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.metadata.Metadata;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.util.repository.AuthenticationBuilder;


/**
 * Interactions with aether.
 * 
 * @author Halil-Cem Guersoy
 * 
 */
public class AetherInteraction {
        
    private final PrintStream logger;
    
   
    public AetherInteraction(PrintStream logger) {
        super();
        this.logger = logger;
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
            final RemoteRepository releaseRepo, final Artifact artifact, final Artifact pom) throws DeploymentException {
        
        DeployRequest deployRequest = new DeployRequest();
        deployRequest.addArtifact(artifact).addArtifact(pom);
        deployRequest.setRepository(releaseRepo);
        return system.deploy(session, deployRequest);
    }

    /** Get ('resolve') the artifact from a repository server.
     * If the artifact is in local repository used by the plugin it will not
     * download it from the server and use the local silently. This local repo
     * will be cleaned by 'mvn clean' as the default location is target/local-repo. 
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
        return RepositorySystemFactory.getNewRepositorySystem(this.logger);
    }

    public DefaultRepositorySystemSession getRepositorySystemSession(final RepositorySystem system, final String localRepoLocation) {

        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        LocalRepository localRepo = new LocalRepository(localRepoLocation);
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
        session.setTransferListener(new JenkinsConsoleTransferListener(logger));
        return session;
    }

    /**
     * Creates a RemoteRepository object to work with. If a User or Password 
     * is given the authentication information is set, too. 
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
        
        RemoteRepository.Builder builder = new RemoteRepository.Builder(repoId, "default",
                repoURL);
        
        if (user.length() > 0 || Secret.toString(password).length() > 0 ) {
            Authentication authentication = new AuthenticationBuilder().addUsername(user)
                    .addPassword(Secret.toString(password)).build(); 
            
            builder = builder.setAuthentication(authentication);
        }
        
        return builder.build();
    }

    protected void traceArtifactInfo(Artifact artifact) {
        logger.println("-------------- artifact info");
        logger.println(artifact + " resolved to  " + artifact.getFile());
        logger.println("File :" + artifact.getFile());
        logger.println("Properties:");
        Map<String, String> props = artifact.getProperties();
        for (String key : props.keySet()) {
            logger.println("Key:" + key + " Value: " + props.get(key));
        }
        logger.println("Base-Version:" + artifact.getBaseVersion());
        logger.println("Version: " + artifact.getVersion());
        logger.println("Classifier: " + artifact.getClassifier());
        logger.println("Extension: " + artifact.getExtension());
    }

    protected void traceDeployResult(DeployResult result) {
        logger.println("-------------- Deploy result info");
        logger.println(result.getRequest().getTrace());
        Collection<Metadata> allMetadata = result.getMetadata();
        traceMetadata(allMetadata);
    }

    protected void traceMetadata(Collection<Metadata> allMetadata) {
        for (Metadata metadata : allMetadata) {

            logger.println("ArtifactID : " + metadata.getArtifactId());
            logger.println("GroupID : " + metadata.getGroupId());
            logger.println("Typ : " + metadata.getType());
            logger.println("Version : " + metadata.getVersion());
            logger.println("Nature : " + metadata.getNature());
            Map<String, String> props = metadata.getProperties();
            for (String key : props.keySet()) {
                System.out.println("Key:" + key + " Value: " + props.get(key));
            }
        }
    }

}
