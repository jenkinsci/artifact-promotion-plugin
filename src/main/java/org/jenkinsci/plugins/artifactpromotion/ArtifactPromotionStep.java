package org.jenkinsci.plugins.artifactpromotion;

import com.google.common.collect.ImmutableSet;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.artifactpromotion.exception.PromotionException;
import org.jenkinsci.plugins.artifactpromotion.jobdsl.ArtifactPromotionJobDslExtension;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * TODO: Use ArtifactPromotionBuilder for actually moving artifacts
 *
 * @author Julian Sauer (julian_sauer@mx.net)
 */
public class ArtifactPromotionStep extends Step implements Serializable {

    private final String groupId;
    private final String artifactId;
    private final String classifier;
    private final String version;
    private final String extension;

    /**
     * The location of the local repository system relative to the workspace. In
     * this repository the downloaded artifact will be saved.
     */
    private final String localRepoLocation = "target" + File.separator
            + "local-repo";

    /**
     * Name of the promoter class.
     */
    private final String promoterClass;

    // Fields for UI
    /**
     * The repository there the artifact is. In a normal case a staging
     * repository.
     */
    private final String stagingRepository;

    /**
     * The User for the staging Repository
     */
    private final String stagingUser;

    /**
     * The staging secret. We should still save the passwords using the
     * credentials plugin but its so bad documented :-(
     */
    private final Secret stagingPW;

    /**
     * The user for the release repo.
     */
    private final String releaseUser;

    /**
     * The release repo secret
     */
    private final Secret releasePW;

    /**
     * The repository into the artifact has to be moved.
     */
    private final String releaseRepository;

    /**
     * Flag to write more info in the job console.
     */
    private final boolean debug;

    /**
     * If true don't delete the artifact from the source repository.
     */
    private boolean skipDeletion;

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
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.classifier = classifier;
        this.version = version;
        this.extension = extension == null ? "jar" : extension;
        this.stagingRepository = stagingRepository;
        this.stagingUser = stagingUser;
        this.stagingPW = Secret.fromString(stagingPW);
        this.releaseUser = releaseUser;
        this.releasePW = Secret.fromString(releasePW);
        this.releaseRepository = releaseRepository;
        this.debug = debug;
        this.promoterClass = promoterClass;
        this.skipDeletion = skipDeletion;
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
            AbstractPromotor artifactPromotor = null;

            Run<?, ?> build = context.get(Run.class);
            FilePath workspace = context.get(FilePath.class);
            Launcher launcher = context.get(Launcher.class);
            TaskListener listener = context.get(TaskListener.class);

            // Initialize the promoter class
            // moved to here as the constructor of the builder is bypassed by stapler
            try {

                if (step.debug) {
                    logger.println("Used promoter class: " + step.promoterClass);
                }

                // TODO: Use this.promoterClass
                artifactPromotor = (AbstractPromotor) Jenkins.getInstance()
                        .getExtensionList(ArtifactPromotionJobDslExtension.RepositorySystem.NexusOSS.getClassName()).iterator().next();

            } catch (ClassNotFoundException e) {
                logger.println("ClassNotFoundException - unable to pick correct promotor class: " + e);
                throw new RuntimeException(e);
            }

            if (artifactPromotor == null) {
                logger.println("artifactPromotor is null - ABORTING!");
                return null;
            }
            artifactPromotor.setListener(listener);

            Map<PromotionBuildTokens, String> expandedTokens = expandTokens(build, workspace,
                    listener);
            if (expandedTokens == null) {
                logger.println("Could not expand tokens - ABORTING!");
                return null;
            }
            artifactPromotor.setExpandedTokens(expandedTokens);
            artifactPromotor.setReleasePassword(step.releasePW);
            artifactPromotor.setReleaseUser(step.releaseUser);
            artifactPromotor.setStagingPassword(step.stagingPW);
            artifactPromotor.setStagingUser(step.stagingUser);
            artifactPromotor.setSkipDeletion(step.skipDeletion);

            String localRepoPath = workspace.getRemote() + File.separator
                    + step.localRepoLocation;
            artifactPromotor.setLocalRepositoryURL(localRepoPath);

            if (step.debug) {
                logger.println("Local repository path: [" + localRepoPath + "]");
            }

            try {
                artifactPromotor.callPromotor(launcher.getChannel());
            } catch (PromotionException e) {
                logger.println(e.getMessage());
            }
            return null;
        }

        /**
         * Expands needed build tokens
         *
         * @param build
         * @param listener
         * @return Map<PromotionBuildTokens, String> of expanded tokens
         */
        private Map<PromotionBuildTokens, String> expandTokens(
                Run<?, ?> build, FilePath workspace, TaskListener listener) {

            PrintStream logger = listener.getLogger();
            Map<PromotionBuildTokens, String> tokens = new HashMap<PromotionBuildTokens, String>();
            try {
                tokens.put(PromotionBuildTokens.GROUP_ID,
                        TokenMacro.expandAll(build, workspace, listener, step.groupId));
                tokens.put(PromotionBuildTokens.ARTIFACT_ID,
                        TokenMacro.expandAll(build, workspace, listener, step.artifactId));
                tokens.put(PromotionBuildTokens.CLASSIFIER,
                        TokenMacro.expandAll(build, workspace, listener, step.classifier));
                tokens.put(PromotionBuildTokens.VERSION,
                        TokenMacro.expandAll(build, workspace, listener, step.version));
                tokens.put(PromotionBuildTokens.EXTENSION, "".equals(step.extension) ? "jar" :
                        TokenMacro.expandAll(build, workspace, listener, step.extension));
                tokens.put(PromotionBuildTokens.STAGING_REPOSITORY,
                        TokenMacro.expandAll(build, workspace, listener, step.stagingRepository));
                tokens.put(PromotionBuildTokens.RELEASE_REPOSITORY,
                        TokenMacro.expandAll(build, workspace, listener, step.releaseRepository));
            } catch (MacroEvaluationException mee) {
                logger.println("Could not evaluate a makro" + mee);
                return null;

            } catch (IOException ioe) {
                logger.println("Got an IOException during evaluation of a makro token"
                        + ioe);
                return null;
            } catch (InterruptedException ie) {
                logger.println("Got an InterruptedException during avaluating a makro token"
                        + ie);
                return null;
            }
            return tokens;
        }

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
