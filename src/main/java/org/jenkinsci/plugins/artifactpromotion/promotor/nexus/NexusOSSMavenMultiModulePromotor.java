package org.jenkinsci.plugins.artifactpromotion.promotor.nexus;

import org.jenkinsci.plugins.artifactpromotion.AbstractPromotor;
import org.jenkinsci.plugins.artifactpromotion.promotor.AbstractPromotorDescription;
import org.jenkinsci.plugins.artifactpromotion.promotor.IPromotorClosure;
import org.jenkinsci.plugins.artifactpromotion.promotor.Promotor;

import hudson.Extension;
import hudson.model.Descriptor;

/**
 * Sonatype Nexus OSS specific multimodule project {@link Promotor}
 * implementation.
 * 
 * @author Timo "timii" Paananen
 * 
 */
@Extension
public class NexusOSSMavenMultiModulePromotor extends AbstractPromotor {

	@SuppressWarnings("unchecked")
	public Descriptor<Promotor> getDescriptor() {
		return new AbstractPromotorDescription() {
			@Override
			public String getDisplayName() {
				return "Nexus OSS - Maven Multimodule";
			}
		};
	}

	/**
	 * This method creates the Nexus OSS MultiModule promoter which is encapsulated into a
	 * 'closure' to make this plugin run on slaves, too.
	 * 
	 * @see org.jenkinsci.plugins.artifactpromotion.promotor.Promotor#callPromotor(hudson.remoting.VirtualChannel)
	 * 
	 * @return NexusOSSMultiModulePromoterClosure
	 */
	@Override
	protected IPromotorClosure getPromotor() {
		return new NexusOSSMavenMultiModulePromoterClosure(this.getListener(),
				this.getLocalRepositoryURL(), this.getExpandedTokens(), this.getStagingRepository(), this.getReleaseRepository(), this.getGeneratePom());
	}

}
