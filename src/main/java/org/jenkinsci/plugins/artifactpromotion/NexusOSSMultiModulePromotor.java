package org.jenkinsci.plugins.artifactpromotion;

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
public class NexusOSSMultiModulePromotor extends AbstractPromotor {

	@SuppressWarnings("unchecked")
	public Descriptor<Promotor> getDescriptor() {
		return new AbstractPromotorDescription() {
			@Override
			public String getDisplayName() {
				return "Nexus OSS - Multimodule";
			}
		};
	}

	/**
	 * This method creates the Nexus OSS MultiModule promoter which is encapsulated into a
	 * 'closure' to make this plugin run on slaves, too.
	 * 
	 * @see org.jenkinsci.plugins.artifactpromotion.Promotor#callPromotor(hudson.remoting.VirtualChannel)
	 * 
	 * @return NexusOSSMultiModulePromoterClosure
	 */
	@Override
	protected IPromotorClosure getPromotor() {
		return new NexusOSSMultiModulePromoterClosure(getListener(),
				getLocalRepositoryURL(), getExpandedTokens(), getReleaseUser(),
				getReleasePassword(), getStagingUser(), getStagingPassword());
	}

}
