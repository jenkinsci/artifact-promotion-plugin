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

import com.google.common.collect.ImmutableSet;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Set;

/**
 * Executes a promotion configured via a pipeline.
 *
 * @author Julian Sauer (julian_sauer@mx.net)
 */
public class ArtifactPromotionStep extends Step implements Serializable {

    private ArtifactPromotionHelper artifactPromotionHelper;

    /**
     * The default constructor. The parameters are injected by jenkins builder
     * and are the same as the (private) fields.
     *
     * @param groupId           The groupId of the artifact
     * @param artifactId        The artifactId of the artifact.
     * @param classifier        The classifier of the artifact.
     * @param version           The version of the artifact.
     * @param stagingRepository The URL of the staging repository.
     * @param stagingUser       User to be used on staging repo.
     * @param stagingPW         Password to be used on staging repo.
     * @param releaseUser       User to be used on release repo.
     * @param releasePW         Password to be used on release repo.
     * @param releaseRepository The URL of the staging repository
     * @param promoterClass     The vendor specific class which is used for the promotion, e.g. for NexusOSS
     * @param debug             Flag for debug output. Currently not used.
     */
    @DataBoundConstructor
    public ArtifactPromotionStep(String groupId, String artifactId, String classifier,
                                 String version, String stagingRepository,
                                 String stagingUser, String stagingPW, String releaseUser,
                                 String releasePW, String releaseRepository, String promoterClass,
                                 boolean debug) {
        artifactPromotionHelper = new ArtifactPromotionHelper(groupId, artifactId, classifier,
                version, "jar", stagingRepository,
                stagingUser, stagingPW, releaseUser,
                releasePW, releaseRepository, promoterClass,
                debug, true);
    }

    @Override
    public StepExecution start(StepContext stepContext) throws Exception {
        return new ArtifactPromotionExecution(stepContext, this);
    }

    private static final class ArtifactPromotionExecution extends SynchronousNonBlockingStepExecution<Void> {

        private StepContext context;

        private ArtifactPromotionStep step;

        protected ArtifactPromotionExecution(@Nonnull StepContext context, ArtifactPromotionStep step) {
            super(context);
            this.context = context;
            this.step = step;
        }

        @Override
        protected Void run() throws Exception {

            PrintStream logger = context.get(TaskListener.class).getLogger();

            Run<?, ?> build = context.get(Run.class);
            FilePath workspace = context.get(FilePath.class);
            Launcher launcher = context.get(Launcher.class);
            TaskListener listener = context.get(TaskListener.class);

            step.artifactPromotionHelper.perform(logger, build, workspace, launcher, listener);
            return null;

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

    @DataBoundSetter
    public void setExtension(String extension) {
        artifactPromotionHelper.extension = extension;
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

    public String getStagingPW() {
        return artifactPromotionHelper.stagingPW.getPlainText();
    }

    public String getReleaseUser() {
        return artifactPromotionHelper.releaseUser;
    }

    public String getReleasePW() {
        return artifactPromotionHelper.releasePW.toString();
    }

    public String getReleaseRepository() {
        return artifactPromotionHelper.releaseRepository;
    }

    public boolean isDebug() {
        return artifactPromotionHelper.debug;
    }

    @DataBoundSetter
    public void setSkipDeletion(boolean skipDeletion) {
        artifactPromotionHelper.skipDeletion = skipDeletion;
    }

    public boolean isSkipDeletion() {
        return artifactPromotionHelper.skipDeletion;
    }

    public String getPromoterClass() {
        return artifactPromotionHelper.promoterClass;
    }

    @Extension
    public static final class ArtifactPromotionStepDescriptorImpl extends
            StepDescriptor implements FormValidator {

        public ArtifactPromotionStepDescriptorImpl() {
            load();
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, FilePath.class, Launcher.class, TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return "artifactPromotion";
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

}
