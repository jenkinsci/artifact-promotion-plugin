package org.jenkinsci.plugins.artifactpromotion;

import hudson.model.Describable;

import org.jenkinsci.plugins.artifactpromotion.exception.PromotionException;

public interface Promotor extends Describable<Promotor> {

	public void promote() throws PromotionException;
}
