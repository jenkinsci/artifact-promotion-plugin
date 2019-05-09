/**
 * The MIT License
 * Copyright (c) 2014 Halil-Cem Guersoy and all contributors
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
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
import hudson.model.TaskListener;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.apache.tools.ant.ExtensionPoint;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Wraps needed informations and supply accessors to them.
 *
 * @author Timo "timii" Paananen
 *
 */
public abstract class AbstractPromotor extends ExtensionPoint implements Promotor {

    private TaskListener listener;
    private Map<PromotionBuildTokens, String> expandedTokens;
    private String localRepositoryURL;

    private String stagingUser;
    private Secret stagingPassword;

    private String releaseUser;
    private Secret releasePassword;

    private boolean skipDeletion;
    private boolean debug;

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
        if (expandedTokens == null) {
            expandedTokens = new HashMap<PromotionBuildTokens, String>(0);
        }
        return expandedTokens;
    }

    public void setListener(TaskListener listener) {
        this.listener = listener;
    }

    protected TaskListener getListener() {
        return listener;
    }

    public static ExtensionList<Promotor> getAllPromoters() {
        ExtensionList<Promotor> exList = Jenkins.get().getExtensionList(Promotor.class);
        if (exList == null) {
            throw new IllegalStateException("None promoter extension found!");
        }
        return exList;
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

    protected boolean isDebug() {
        return debug;
    }

    public void setSkipDeletion(Boolean skipDeletion) {
        this.skipDeletion = skipDeletion;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

}
