package org.jenkinsci.plugins.artifactpromotion;

import org.jenkinsci.plugins.artifactpromotion.exception.PromotionException;

/**
 * Interface to provide a method which should be implemented by 
 * 'closures' which should run on slaves (and masters).
 * The needed parameters should be provided by the constructor of
 * the implementation.
 * 
 * @author hcguersoy
 *
 */
public interface IPromotorClosure {

	/**
	 * This methods does the real 'promotion'.
	 * 
	 * @throws PromotionException
	 */
	public abstract void promote() throws PromotionException;

}