package org.jenkinsci.plugins.artifactpromotion.aether;

import hudson.util.Secret;

/**
 * 
 * @author Timo "timii" Paananen
 * 
 */
public class RepositoryLogin {
	private String repositoryURL;
	private String repositoryId;
	private String username;
	private Secret password;

	public RepositoryLogin(String repositoryURL, String repositoryId, String username, Secret password) {
		if (repositoryURL == null || repositoryId == null) {
			throw new IllegalArgumentException("You cant provide null repositoryURL or repositoryId here.");
		}
		this.repositoryURL = repositoryURL;
		this.username = username;
		this.password = password;
	}

	public String getRepositoryURL() {
		return repositoryURL;
	}

	public void setRepositoryURL(String repositoryURL) {
		this.repositoryURL = repositoryURL;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Secret getPassword() {
		return password;
	}

	public void setPassword(Secret password) {
		this.password = password;
	}
	
	public String getRepositoryId() {
		return repositoryId;
	}
	
	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}
}
