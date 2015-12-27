package org.jenkinsci.plugins.artifactpromotion.jobdsl;

import hudson.util.Secret;
import javaposse.jobdsl.dsl.Context;

import org.jenkinsci.plugins.artifactpromotion.jobdsl.ArtifactPromotionJobDslExtension.RepositorySystem;

/**
 * Provides the DSL context to execute the artifactionPromotion closure in.
 * The public methods of this class can be called from the closure and thus define the DSL vocabulary
 * inside the artifactPromotion element.
 *  
 * @author Patrick Schlebusch
 */
public class ArtifactPromotionDslContext implements Context {
	private String groupId;
	private String artifactId;
	private String classifier;
	private String version;
	private String extension = "jar";
	
	private String stagingRepository;
	private String stagingUser;
	private Secret stagingPassword;
	
	private String releaseRepository;
	private String releaseUser;
	private Secret releasePassword;
	
	private String promoterClass = RepositorySystem.NexusOSS.getClassName();
	private boolean debug = false;
	private boolean skipDeletion = true;

	public void groupId(String groupId) {
		this.groupId = groupId;
	}
	String getGroupId() {
		return groupId;
	}

	public void artifactId(String artifactId) {
		this.artifactId = artifactId;
	}
	String getArtifactId() {
		return artifactId;
	}

	public void classifier(String classifier) {
		this.classifier = classifier;
	}
	String getClassifier() {
		return classifier;
	}

	public void version(String version) {
		this.version = version;
	}
	String getVersion() {
		return version;
	}

	public void extension(String extension) {
		this.extension = extension;
	}
	String getExtension() {
		return extension;
	}

	public void stagingRepository(String repository, String user, String password) {
		this.stagingRepository(repository, user, password, true);
	}
	public void stagingRepository(String repository, String user, String password, boolean skipDeletion) {
		this.stagingRepository = repository;
		this.stagingUser = user;
		this.stagingPassword = Secret.fromString(password);
		this.skipDeletion = skipDeletion;
	}
	String getStagingRepository() {
		return stagingRepository;
	}

	String getStagingUser() {
		return stagingUser;
	}

	Secret getStagingPassword() {
		return stagingPassword;
	}

	public void releaseRepository(String repository, String user, String password) {
		this.releaseRepository = repository;
		this.releaseUser = user;
		this.releasePassword = Secret.fromString(password);
	}
	String getReleaseRepository() {
		return releaseRepository;
	}
	
	String getReleaseUser() {
		return releaseUser;
	}

	Secret getReleasePassword() {
		return releasePassword;
	}

	String getPromoterClass() {
		return promoterClass;
	}

	public void debug(boolean debug) {
		this.debug = debug;
	}
	boolean isDebugEnabled() {
		return debug;
	}

	boolean isSkipDeletionEnabled() {
		return skipDeletion;
	}
	
}
