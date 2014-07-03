package org.jenkinsci.plugins.artifactpromotion;

import org.jenkinsci.plugins.artifactpromotion.exception.PromotionException;

public interface IPromotorClosure {

	public abstract void promote() throws PromotionException;

}