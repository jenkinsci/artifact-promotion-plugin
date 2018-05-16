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
package org.jenkinsci.plugins.artifactpromotion.jobdsl;

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
    private String stagingPassword;

    private String releaseRepository;
    private String releaseUser;
    private String releasePassword;

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
        this.stagingPassword = password;
        this.skipDeletion = skipDeletion;
    }
    String getStagingRepository() {
        return stagingRepository;
    }

    String getStagingUser() {
        return stagingUser;
    }

    String getStagingPassword() {
        return stagingPassword;
    }

    public void releaseRepository(String repository, String user, String password) {
        this.releaseRepository = repository;
        this.releaseUser = user;
        this.releasePassword = password;
    }
    String getReleaseRepository() {
        return releaseRepository;
    }

    String getReleaseUser() {
        return releaseUser;
    }

    String getReleasePassword() {
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
