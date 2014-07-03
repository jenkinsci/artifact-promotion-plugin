package org.jenkinsci.plugins.artifactpromotion;

import hudson.remoting.Callable;

import org.jenkinsci.plugins.artifactpromotion.exception.PromotionException;

public class RemotePromoter implements Callable<Void, PromotionException> {
	
	private static final long serialVersionUID = 1L;
	
	private IPromotorClosure promotor = null;

	/**
	 * Take the specific promoter.
	 * 
	 * @param promotor
	 */
	public RemotePromoter(IPromotorClosure promotor) {
		super();
		this.promotor = promotor;
	}

	public Void call() throws PromotionException {
		this.promotor.promote();
		//satisfy Void
		return null;
	}
	
}
