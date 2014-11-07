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
import hudson.remoting.VirtualChannel;

import java.util.HashMap;
import java.util.Map;

import jenkins.model.Jenkins;

import org.apache.tools.ant.ExtensionPoint;
import org.jenkinsci.plugins.artifactpromotion.exception.PromotionException;
import org.jenkinsci.plugins.artifactpromotion.promotor.IPromotorClosure;
import org.jenkinsci.plugins.artifactpromotion.promotor.Promotor;
import org.jenkinsci.plugins.artifactpromotion.promotor.RemotePromoter;

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
	private Repository releaseRepository;
	private Repository stagingRepository;
	private boolean generatePom;
	
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
	
	public void setReleaseRepository(Repository releaseRepository) {
		this.releaseRepository = releaseRepository;
	}
	
	public void setStagingRepository(Repository stagingRepository) {
		this.stagingRepository = stagingRepository;
	}
	
	protected Repository getReleaseRepository() {
		return this.releaseRepository;
	}
	
	protected Repository getStagingRepository() {
		return this.stagingRepository;
	}
	
	
	protected boolean getGeneratePom() {
		return this.generatePom;
	}
	public void setGeneratePom(boolean generatePom) {
		this.generatePom = generatePom;
	}
	/**
	 * This method calls promoter which is encapsulated into a
	 * 'closure' to make this plugin run on slaves, too.
	 * 
	 * @see org.jenkinsci.plugins.artifactpromotion.promotor.Promotor#callPromotor(hudson.remoting.VirtualChannel)
	 */
	public void callPromotor(VirtualChannel channel) throws PromotionException {	

		RemotePromoter promotorTask = new RemotePromoter(getPromotor());
			
		try {
			channel.call(promotorTask);
		} catch (Exception e) {
			getListener().getLogger().println("Promotion could not be executed");
			e.printStackTrace(getListener().getLogger());
			throw new PromotionException("Promotion could not be executed: " + e.getMessage());
		}
	}

	/**
	 * This method needs to create repository system specific promoter which is encapsulated into a
	 * 'closure' to make this plugin run on slaves, too.
	 * 
	 * @see org.jenkinsci.plugins.artifactpromotion.promotor.Promotor#callPromotor(hudson.remoting.VirtualChannel)
	 * 
	 * @return IPromotorClosure
	 */
	protected abstract IPromotorClosure getPromotor();
}
