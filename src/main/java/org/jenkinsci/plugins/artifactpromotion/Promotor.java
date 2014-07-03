/**
 * The MIT License
 * Copyright (c) 2014 Halil-Cem Guersoy and all contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.artifactpromotion;

import hudson.model.Describable;
import hudson.remoting.VirtualChannel;

import org.jenkinsci.plugins.artifactpromotion.exception.PromotionException;


/**
 * Interface which a server specific implementation has to implement to
 * provide product specific handling of artifact promotion.  
 *
 */
public interface Promotor extends Describable<Promotor> {
	
	/**
	 * This method calls the 'real' promotion stuff or may it do it self. 
	 * 
	 * @param channel - a hudson.Channel to enable runs on slaves.
	 * @throws PromotionException thrown then the promotion has gone wrong 
	 */
	public void callPromotor(VirtualChannel channel) throws PromotionException;
	
}
