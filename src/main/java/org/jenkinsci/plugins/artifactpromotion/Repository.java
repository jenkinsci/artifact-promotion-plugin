package org.jenkinsci.plugins.artifactpromotion;

import hudson.util.Secret;

import org.apache.commons.lang.builder.ToStringBuilder;
/**
 * 
 * @author Timo "timii" Paananen
 *
 */
public class Repository {
	
	public Repository( String username, Secret password, String repositoryURL, String repositoryId) {
		this.username = username;
		this.password = password;
		this.id = repositoryId;
		this.url = repositoryURL;
	}

	private String username;
	private Secret password;
	private String url;
	private String id;
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
	public String getURL() {
		return url;
	}
	public void setURL(String repositoryURL) {
		if(repositoryURL != null && !repositoryURL.endsWith("/")) {
			repositoryURL = repositoryURL + "/";
		}
		this.url = repositoryURL;
	}
	public String getId() {
		return id;
	}
	public void setId(String repositoryId) {
		this.id = repositoryId;
	}
	
	@Override
	public String toString() {
		StringBuilder toStringBuilder = new StringBuilder();
		toStringBuilder.append(this.getClass().getCanonicalName());
		toStringBuilder.append("[id=");
		toStringBuilder.append(this.id);
		toStringBuilder.append(" URL=");
		toStringBuilder.append(this.url);
		toStringBuilder.append(" username=");
		toStringBuilder.append(this.username);
		toStringBuilder.append(" hasPassword=");
		toStringBuilder.append(this.password != null);
		toStringBuilder.append("]");
		return toStringBuilder.toString();
	}
	
}
