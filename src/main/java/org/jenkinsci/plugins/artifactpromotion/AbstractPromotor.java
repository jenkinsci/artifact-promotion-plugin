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

import hudson.ExtensionList;
import hudson.model.BuildListener;
import hudson.util.Secret;

import java.util.HashMap;
import java.util.Map;

import jenkins.model.Jenkins;

import org.apache.tools.ant.ExtensionPoint;

/**
 * 
 * Wraps needed informations and supply accessors to them.
 * 
 * @author Timo "timii" Paananen
 *
 */
public abstract class AbstractPromotor extends ExtensionPoint implements Promotor {

	private BuildListener listener;
	private Map<PromotionBuildTokens, String> expandedTokens;
	private String localRepositoryURL;	
	
	private String stagingUser;
	private Secret stagingPassword;
	
	private String releaseUser;
	private Secret releasePassword;
	
	private boolean skipDeletion;
	
	
	public void setLocalRepositoryURL(String localRepositoryURL) {
		this.localRepositoryURL = localRepositoryURL;
	}
	
	protected String getLocalRepositoryURL() {
		return localRepositoryURL;
	}
	
	public void setExpandedTokens(
			Map<PromotionBuildTokens, String> expandedTokens) {
		this.expandedTokens = expandedTokens;
	}
	
	protected Map<PromotionBuildTokens, String> getExpandedTokens() {
		if(expandedTokens == null) {
			expandedTokens = new HashMap<PromotionBuildTokens, String>(0);
		}
		return expandedTokens;
	}
	
	public void setListener(BuildListener listener) {
		this.listener = listener;
	}
	
	protected BuildListener getListener() {
		return listener;
	}
	
	public static ExtensionList<Promotor> getAllPromoters() {
		return Jenkins.getInstance().getExtensionList(Promotor.class);
	}
	
	protected String getReleaseUser() {
		return releaseUser;
	}
	
	public void setReleaseUser(String releaseUser) {
		this.releaseUser = releaseUser;
	}
	
	protected Secret getReleasePassword() {
		return releasePassword;
	}
	
	public void setReleasePassword(Secret releasePassword) {
		this.releasePassword = releasePassword;
	}
	
	protected String getStagingUser() {
		return stagingUser;
	}

	public void setStagingUser(String stagingUser) {
		this.stagingUser = stagingUser;
	}
	
	protected Secret getStagingPassword() {
		return stagingPassword;
	}
	
	public void setStagingPassword(Secret stagingPassword) {
		this.stagingPassword = stagingPassword;
	}

	protected boolean isSkipDeletion() {
		return skipDeletion;
	}

	public void setSkipDeletion(Boolean skipDeletion) {
		this.skipDeletion = skipDeletion;
	}
	
}
