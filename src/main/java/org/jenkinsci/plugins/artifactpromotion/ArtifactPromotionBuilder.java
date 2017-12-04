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

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.PrintStream;

/**
 * Executes a promotion configured via the Jenkins UI.
 *
 * @author Halil-Cem Guersoy (hcguersoy@gmail.com)
 *
 */
public class ArtifactPromotionBuilder extends Builder implements SimpleBuildStep {

    /**
     * The POM extension.
     */
    public static final String POMTYPE = "pom";

    private ArtifactPromotionHelper artifactPromotionHelper;

    /**
     * The default constructor. The parameters are injected by jenkins builder
     * and are the same as the (private) fields.
     *
     * @param groupId
     *            The groupId of the artifact
     * @param artifactId
     *            The artifactId of the artifact.
     * @param classifier
     *            The classifier of the artifact.
     * @param version
     *            The version of the artifact.
     * @param extension
     *            The file extension of the artifact.
     * @param stagingRepository
     *            The URL of the staging repository.
     * @param stagingUser
     *            User to be used on staging repo.
     * @param stagingPW
     *            Password to be used on staging repo.
     * @param releaseUser
     *            User to be used on release repo.
     * @param releasePW
     *            Password to be used on release repo.
     * @param releaseRepository
     *            The URL of the staging repository
     * @param promoterClass
     * 			  The vendor specific class which is used for the promotion, e.g. for NexusOSS
     * @param debug
     *            Flag for debug output. Currently not used.
     */
    @DataBoundConstructor
    public ArtifactPromotionBuilder(String groupId, String artifactId, String classifier,
                                    String version, String extension, String stagingRepository,
                                    String stagingUser, String stagingPW, String releaseUser,
                                    String releasePW, String releaseRepository, String promoterClass,
                                    boolean debug, boolean skipDeletion) {
        artifactPromotionHelper = new ArtifactPromotionHelper(groupId, artifactId, classifier,
                version, extension, stagingRepository,
                stagingUser, stagingPW, releaseUser,
                releasePW, releaseRepository, promoterClass,
                debug, skipDeletion);
    }

    @Override
    public void perform(@Nonnull Run<?, ?> build, @Nonnull FilePath workspace, @Nonnull Launcher launcher,
                        @Nonnull TaskListener listener) {
        PrintStream logger = listener.getLogger();
        artifactPromotionHelper.perform(logger, build, workspace, launcher, listener);
    }

    @Override
    public ArtifactPromotionDescriptorImpl getDescriptor() {
        return (ArtifactPromotionDescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link ArtifactPromotionBuilder}.
     */
    @Extension
    public static final class ArtifactPromotionDescriptorImpl extends
            BuildStepDescriptor<Builder> implements FormValidator {

        /**
         * In order to load the persisted global configuration, you have to call
         * load() in the constructor.
         */
        public ArtifactPromotionDescriptorImpl() {
            load();
        }

        // TODO connectivity tests

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Single Artifact Promotion";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData)
                throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            // useFrench = formData.getBoolean("useFrench");
            // ^Can also use req.bindJSON(this, formData);
            // (easier when there are many fields; need set* methods for this,
            // like setUseFrench)
            // save();
            return super.configure(req, formData);
        }

    }

    public String getGroupId() {
        return artifactPromotionHelper.groupId;
    }

    public String getArtifactId() {
        return artifactPromotionHelper.artifactId;
    }

    public String getClassifier() {
        return artifactPromotionHelper.classifier;
    }

    public String getVersion() {
        return artifactPromotionHelper.version;
    }

    public String getExtension() {
        return artifactPromotionHelper.extension;
    }

    public String getStagingRepository() {
        return artifactPromotionHelper.stagingRepository;
    }

    public String getStagingUser() {
        return artifactPromotionHelper.stagingUser;
    }

    public Secret getStagingPW() {
        return artifactPromotionHelper.stagingPW;
    }

    public String getReleaseUser() {
        return artifactPromotionHelper.releaseUser;
    }

    public Secret getReleasePW() {
        return artifactPromotionHelper.releasePW;
    }

    public String getReleaseRepository() {
        return artifactPromotionHelper.releaseRepository;
    }

    public boolean isDebug() {
        return artifactPromotionHelper.debug;
    }

    public boolean isSkipDeletion() {
        return artifactPromotionHelper.skipDeletion;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ArtifactPromotionBuilder [POMTYPE=");
        builder.append(POMTYPE);
        builder.append(", groupId=");
        builder.append(artifactPromotionHelper.groupId);
        builder.append(", artifactId=");
        builder.append(artifactPromotionHelper.artifactId);
        builder.append(", classifier=");
        builder.append(artifactPromotionHelper.classifier);
        builder.append(", version=");
        builder.append(artifactPromotionHelper.version);
        builder.append(", extension=");
        builder.append(artifactPromotionHelper.extension);
        builder.append(", localRepoLocation=");
        builder.append(artifactPromotionHelper.localRepoLocation);
        builder.append(", stagingRepository=");
        builder.append(artifactPromotionHelper.stagingRepository);
        builder.append(", stagingUser=");
        builder.append(artifactPromotionHelper.stagingUser);
        builder.append(", releaseUser=");
        builder.append(artifactPromotionHelper.releaseUser);
        builder.append(", releaseRepository=");
        builder.append(artifactPromotionHelper.releaseRepository);
        builder.append(", debug=");
        builder.append(artifactPromotionHelper.debug);
        builder.append(", skipDeletion=");
        builder.append(artifactPromotionHelper.skipDeletion);
        builder.append("]");
        return builder.toString();
    }
}
