package org.jenkinsci.plugins.artifactpromotion.exception;

public class PromotionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public PromotionException(String message) {
		super(message);
	}
	
	public PromotionException(String message, Throwable e) {
		super(message, e);
	}
}
