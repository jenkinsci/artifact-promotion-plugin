package org.jenkinsci.plugins.artifactpromotion.jobdsl;

import org.jenkinsci.plugins.artifactpromotion.ArtifactPromotionBuilder;

import hudson.Extension;
import javaposse.jobdsl.dsl.helpers.step.StepContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;

@Extension(optional = true)
public class ArtifactPromotionJobDslExtension extends ContextExtensionPoint {

	@DslExtensionMethod(context = StepContext.class)
	public Object artifactPromotion(Runnable closure) {
		ArtifactPromotionDslContext context = new ArtifactPromotionDslContext();
		executeInContext(closure, context);
		
		return new ArtifactPromotionBuilder(
				context.getGroupId(), context.getArtifactId(), context.getVersion(), context.getExtension(),
				context.getStagingRepository(), context.getStagingUser(), context.getStagingPassword(),
				context.getReleaseUser(), context.getReleasePassword(), context.getReleaseRepository(),
				context.getPromoterClass(), context.isDebugEnabled(), context.isSkipDeletionEnabled());
	}
	
	public enum RepositorySystem {
		NexusOSS("org.jenkinsci.artifactpromotion.NexusOSSPromotor");
		
		private String className;
		
		private RepositorySystem(String className) {
			this.className = className;
		}
		
		public String getClassName() {
			return this.className;
		}
	}
}
