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

import hudson.remoting.Callable;

import org.jenkinsci.plugins.artifactpromotion.exception.PromotionException;
import org.jenkinsci.remoting.RoleChecker;

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

    @Override
    public void checkRoles(RoleChecker roleChecker) throws SecurityException {
        // TODO
    }
}
