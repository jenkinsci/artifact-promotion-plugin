package org.jenkinsci.plugins.artifactpromotion;

import hudson.ExtensionList;
import hudson.model.BuildListener;
import hudson.util.Secret;

import java.util.HashMap;
import java.util.Map;

import jenkins.model.Jenkins;

import org.apache.tools.ant.ExtensionPoint;
import org.eclipse.aether.repository.RemoteRepository;

/**
 * 
 * @author Timo "timii" Paananen
 *
 */
public abstract class AbstractPromotor extends ExtensionPoint implements Promotor {

	private transient BuildListener listener;
	private Map<PromotionBuildTokens, String> expandedTokens;
	private String localRepositoryURL;	
	
	private transient AetherInteraction aether;
	
	private String stagingUser;
	private Secret stagingPassword;
	
	private String releaseUser;
	private Secret releasePassword;
	
	
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
	
	protected RemoteRepository getStagingRepository(String stagingRepositoryURL) {
		return getRepository(stagingUser, stagingPassword, "noid",
				stagingRepositoryURL);
	}

	protected RemoteRepository getReleaseRepository(String releaseRepositoryURL) {
		return getRepository(releaseUser, releasePassword, "noid",
				releaseRepositoryURL);
	}

	private RemoteRepository getRepository(String user, Secret password, String repositoryId,
			String repositoryURL) {
		return getAether().getRepository(user, password, repositoryId, repositoryURL);
	}

	protected AetherInteraction getAether() {
		if(this.aether == null) {
			this.aether =  new AetherInteraction(getListener().getLogger());
		}
		return aether;
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
	
}
