package org.jenkinsci.plugins.artifactpromotion.jobdsl;

import org.jenkinsci.plugins.artifactpromotion.ArtifactPromotionBuilder;
import org.jenkinsci.plugins.artifactpromotion.NexusOSSPromotor;

import hudson.Extension;
import javaposse.jobdsl.dsl.helpers.step.StepContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;

/**
 * This class provides the artifactionPromotion DSL extension method.
 * 
 * The artifactPromotion keyword can be used with a closure as a build step.
 * The further vocabulary for the closure is defined in the {@link ArtifactPromotionDslContext}.
 * 
 * @author Patrick Schlebusch
 */
@Extension(optional = true)
public class ArtifactPromotionJobDslExtension extends ContextExtensionPoint {

	@DslExtensionMethod(context = StepContext.class)
	public Object artifactPromotion(Runnable closure) {
		ArtifactPromotionDslContext context = new ArtifactPromotionDslContext();
		executeInContext(closure, context);
		
		return new ArtifactPromotionBuilder(
				context.getGroupId(), context.getArtifactId(), context.getClassifier(), context.getVersion(),
				context.getExtension(), context.getStagingRepository(), context.getStagingUser(),
				context.getStagingPassword(), context.getReleaseUser(), context.getReleasePassword(),
				context.getReleaseRepository(), context.getPromoterClass(), context.isDebugEnabled(),
				context.isSkipDeletionEnabled());
	}
	
	public enum RepositorySystem {
		NexusOSS(NexusOSSPromotor.class.getName());
		
		private String className;
		
		private RepositorySystem(String className) {
			this.className = className;
		}
		
		public String getClassName() {
			return this.className;
		}
	}
}
