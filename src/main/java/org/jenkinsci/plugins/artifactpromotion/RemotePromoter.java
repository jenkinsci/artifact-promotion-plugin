package org.jenkinsci.plugins.artifactpromotion;

import hudson.remoting.Callable;

import org.jenkinsci.plugins.artifactpromotion.exception.PromotionException;

/**
 * This class represents a kind of interface to
 * work this hudsons/jenkins remote closures.
 * 
 * @author guersoy
 *
 */
public class RemotePromoter implements Callable<Void, PromotionException> {
	
	private static final long serialVersionUID = 1L;
	
	private IPromotorClosure promotor = null;

	/**
	 * @param closure The specific promotor which has to be Serializable.
	 */
	public RemotePromoter(IPromotorClosure closure) {
		super();
		this.promotor = closure;
	}

	/** 
	 * Execute the promotor, either on the master or on a slave.
	 * 
	 * @see hudson.remoting.Callable#call()
	 */
	public Void call() throws PromotionException {
		this.promotor.promote();
		//satisfy Void
		return null;
	}
	
}
