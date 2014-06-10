/**
 * The MIT License
 * Copyright (c) 2014 Halil-Cem Guersoy
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



import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.Secret;

import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;

import net.sf.json.JSONObject;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.deployment.DeployResult;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * In this class we encapsulate the process of moving an artifact from one repository into another one.
 * 
 * 
 * TODO make me more flexible to so support different repository servers, at least nexus pro, artifactory and archiva.
 * 
 * @author Halil-Cem Guersoy (hcguersoy@gmail.com)
 * 
 */
public class ArtifactPromotionBuilder extends Builder {
    
    /**
     * The POM extension.
     */
    private final String POMTYPE = "pom";
        
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String extension;
        
    /**
     * The location of the local repository system. In this repository the downloaded
     * artifact will be saved.
     */
    private final String localRepoLocation = "target/local-repo";
    
    // Fields for UI
    
    /**
     * The repository there the artifact is. In a normal case a staging repository.
     */
    private final String stagingRepository; 
    
    /**
     * The User for the staging Repository
     */
    private final String stagingUser;
    
    /**
     * The staging secret. 
     * We should still save the passwords using the credentials plugin
     * but its so bad documented :-( 
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
     * The dafeault constructor.
     * The parameters are injected by jenkins builder and are the same as the (private) fields.
     * 
     * @param groupId The groupId of the artifact
     * @param artifactId The artifactId of the artifact.
     * @param version The version of the artifact.
     * @param extension The file extension of the artifact.
     * @param stagingRepository The URL of the staging repository.
     * @param stagingUser User to be used on staging repo. 
     * @param stagingPW Password to be used on staging repo.
     * @param releaseUser User to be used on release repo.
     * @param releasePW Password to be used on release repo.
     * @param releaseRepository The URL of the staging repository
     * @param debug Flag for debug output. Currently not used.
     */
    @DataBoundConstructor
    public ArtifactPromotionBuilder(String groupId, String artifactId, String version, String extension,
            String stagingRepository, String stagingUser, Secret stagingPW,
            String releaseUser, Secret releasePW, String releaseRepository, boolean debug) {
        super();
        
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.extension = extension;
        this.stagingRepository = stagingRepository;
        this.stagingUser = stagingUser;
        this.stagingPW = stagingPW;
        this.releaseUser = releaseUser;
        this.releasePW = releasePW;
        this.releaseRepository = releaseRepository;
        this.debug = debug;
        
    }
    
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {

        PrintStream logger = listener.getLogger();

        AetherInteraction aether = new AetherInteraction(logger);
        
        logger.println("Initialising aether");
        RepositorySystem system = aether.getNewRepositorySystem();
        RepositorySystemSession session = aether.getRepositorySystemSession(system, this.localRepoLocation);

        //the staging is done here in a nexus oss specific way, moving an artifact by a copy/delete pattern.
        //this is (maybe) different on other repository servers. Due to that this part should be refactored 
        //to make it more flexible:
        // TODO refactor me to support different repository servers
        
        //pull the artifact and its pom from the staging repository 
        logger.println("Get Artifact and corresponding POM");
        RemoteRepository aetherStagingRepo = aether.getRepository(stagingUser, stagingPW, "noid", stagingRepository);
        Artifact artifact;
        Artifact artifactpom;
        try {
            artifact = aether.getArtifact(session, system, aetherStagingRepo, groupId, artifactId, extension, version);
            artifactpom = aether.getArtifact(session, system, aetherStagingRepo, groupId, artifactId, POMTYPE,
                    version);
        } catch (ArtifactResolutionException e) {
            logger.println("Could not resolve artifact: " + e.getMessage());
            return false;
        }
       
        if (debug) {
            aether.traceArtifactInfo(artifact);
            aether.traceArtifactInfo(artifactpom);
        }
        
        //upload the artifact and its pom to the release repos
        RemoteRepository aetherReleaseRepo = aether.getRepository(releaseUser, releasePW, "noid", releaseRepository);
        DeployResult result = null;
        try {
            result = aether.deployArtifact(session, system, aetherReleaseRepo, artifact, artifactpom);
        } catch (DeploymentException e) {
            logger.println("Could not deploy artifact to " + releaseRepository + " using User " + releaseUser + ":" + e.getMessage());
            return false;
        }
        
        if (debug) {
            aether.traceDeployResult(result);            
        }
        
        // remove the artifact from the statging repository
        // TODO This is specific for Nexus OSS and should be done in a changable/configurable way.
        IDeleteArtifact deleter = new DeleteArtifactNexusOSS(logger, debug);
        deleter.deleteArtifact(aetherStagingRepo, artifact);

        return true;
    }
        
    @Override
    public ArtifactPromotionDescriptorImpl getDescriptor() {
        return (ArtifactPromotionDescriptorImpl)super.getDescriptor();
    }
    
    /**
     * Descriptor for {@link ArtifactPromotionBuilder}.
     */
    @Extension
    public static final class ArtifactPromotionDescriptorImpl extends BuildStepDescriptor<Builder> {

        /**
         * In order to load the persisted global configuration, you have to 
         * call load() in the constructor.
         */
        public ArtifactPromotionDescriptorImpl() {
            load();
        }

        // Form validation
        
        /**
         * Performs on-the-fly validation of the form field 'name'.
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckArtifactId(@QueryParameter String value) {
            if (value.length() == 0)
                return FormValidation.error("Please set an ArtifactId!");
            return FormValidation.ok();
        }
        
        public FormValidation doCheckGroupId(@QueryParameter String value) {
            if (value.length() == 0)
                return FormValidation.error("Please set a GroupId!");
            return FormValidation.ok();
        }
        
        public FormValidation doCheckVersion(@QueryParameter String value) {
            if (value.length() == 0)
                return FormValidation.error("Please set a Version for your artifact!");
            return FormValidation.ok();
        }
        
        public FormValidation doCheckStagingRepository(@QueryParameter String value) {
            return checkURI(value);
        }
        
        public FormValidation doCheckReleaseRepository(@QueryParameter String value) {
            return checkURI(value);
        }        
        
        /**
         * Checks using java.net.URI constructor if a given URL is valid.
         * This is a poor mans solution. An alternative would be using apache commons URIValidator but
         * don't wanna import additional lib for this.
         * 
         * @param value
         * @return
         */
        private FormValidation checkURI(String value) {
            if (value.length() == 0) {
                return FormValidation.error("Please set an URL for the staging repository!");
            } else {
                try {
                    new URI(value);
                } catch (URISyntaxException e) {
                    return FormValidation.error("The URI is malformed. Please consult RFC2396 for a correct URI.");
                }
            }
            return FormValidation.ok();
        }        
        
        //TODO connectivity tests

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
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            //useFrench = formData.getBoolean("useFrench");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            //save();
            return super.configure(req,formData);
        }
    }
    

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getExtension() {
        return extension;
    }

    public String getStagingRepository() {
        return stagingRepository;
    }

    public String getStagingUser() {
        return stagingUser;
    }

    public Secret getStagingPW() {
        return stagingPW;
    }

    public String getReleaseUser() {
        return releaseUser;
    }

    public Secret getReleasePW() {
        return releasePW;
    }

    public String getReleaseRepository() {
        return releaseRepository;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ArtifactPromotionBuilder [POMTYPE=");
        builder.append(POMTYPE);
        builder.append(", groupId=");
        builder.append(groupId);
        builder.append(", artifactId=");
        builder.append(artifactId);
        builder.append(", version=");
        builder.append(version);
        builder.append(", extension=");
        builder.append(extension);
        builder.append(", localRepoLocation=");
        builder.append(localRepoLocation);
        builder.append(", stagingRepository=");
        builder.append(stagingRepository);
        builder.append(", stagingUser=");
        builder.append(stagingUser);
        builder.append(", releaseUser=");
        builder.append(releaseUser);
        builder.append(", releaseRepository=");
        builder.append(releaseRepository);
        builder.append(", debug=");
        builder.append(debug);
        builder.append("]");
        return builder.toString();
    }   
}
