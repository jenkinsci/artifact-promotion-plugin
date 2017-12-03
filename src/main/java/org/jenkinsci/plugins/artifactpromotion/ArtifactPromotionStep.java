package org.jenkinsci.plugins.artifactpromotion;

import com.google.common.collect.ImmutableSet;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.Secret;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;

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
     * @param extension         The file extension of the artifact.
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

    public String getPromoterClass() {
        return artifactPromotionHelper.promoterClass;
    }

    @Extension
    public static final class ArtiactPromotionStepDescriptorImpl extends
            StepDescriptor {

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return ImmutableSet.of(Run.class, FilePath.class, Launcher.class, TaskListener.class);
        }

        @Override
        public String getFunctionName() {
            return "artifactPromotion";
        }
    }

}
