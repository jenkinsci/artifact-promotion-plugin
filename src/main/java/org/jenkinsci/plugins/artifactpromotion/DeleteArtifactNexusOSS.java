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
package org.jenkinsci.plugins.artifactpromotion;

import hudson.model.BuildListener;
import hudson.util.Secret;

import java.io.Serializable;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.RemoteRepository;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.Base64;

/**
 * This class is responsible to remove a artifact from a Nexus OSS repository.
 * As Nexus OSS does not support staging repositories nor metadata to flag artifacts
 * as release candidates or releases the promoted artifact has to be moved into a
 * release repository. As no 'move' is possible in nexus this is done by a
 * copy/delete pattern. This class handles the 'delete' part.
 *  
 * @author Halil-Cem Guersoy
 *
 */
public class DeleteArtifactNexusOSS implements IDeleteArtifact, Serializable {

	private static final long serialVersionUID = 1L;

	/**
     * The URL path delimiter.
     */
    private static final String DELI = "/";
    
    /**
     * Nexus returns status code 204 then deleted successful via REsT API.
     */
    private static final int NEXUS_DELETE_SUCESS = 204;    
    
    private boolean debug;
    
    private String user;
    
    private Secret password;
    
    private BuildListener listener;
    
    /**
     * The default constructor.
     */
    public DeleteArtifactNexusOSS(BuildListener listener, final String user, final Secret password, final boolean debug) {
        super();
        this.debug = debug;
        this.user = user;
        this.password = password;
        this.listener = listener;
    }

    /** 
     * Delete a artifact from a Nexus OSS repo using the REST interface of NexusOSS.
     * 
     * TODO change to actual jersey version. Here it is still a old version used.
     * 
     * @see org.jenkinsci.plugins.artifactpromotion.IDeleteArtifact#deleteArtifact(org.eclipse.aether.repository.RemoteRepository, org.eclipse.aether.artifact.Artifact, java.io.PrintStream)
     */
    public void deleteArtifact(final RemoteRepository stagingRepo, final Artifact artifact) throws IllegalStateException {

        String requestURL = stagingRepo.getUrl() + artifact.getGroupId().replace(".", DELI) + DELI
                + artifact.getArtifactId() + DELI + artifact.getVersion() + DELI;
        
        if (debug) listener.getLogger().println("Request URL is: [" + requestURL + "]");

        //TODO needs rework for anonymous access
        String auth = new String(Base64.encode(this.user + ":" + Secret.toString(this.password)));
        
        Client client = Client.create();
        WebResource webResource = client.resource(requestURL);
        ClientResponse response = webResource.header("Authorization", "Digest " + auth).type("application/json")
                .accept("application/json").delete(ClientResponse.class);

        int statusCode = response.getStatus();

        if (debug) listener.getLogger().println("Status code is: " + statusCode);

        if (statusCode == 401) {
            throw new IllegalStateException("Invalid Username or Password while accessing target repository.");
        } else if (statusCode != NEXUS_DELETE_SUCESS) {
            throw new IllegalStateException("The artifact is not deleted - status code is: " + statusCode);
        }
        listener.getLogger().println("Successfully deleted artifact " + artifact.getArtifactId() + " from repository " + stagingRepo.getUrl());
        
    }
 

}
