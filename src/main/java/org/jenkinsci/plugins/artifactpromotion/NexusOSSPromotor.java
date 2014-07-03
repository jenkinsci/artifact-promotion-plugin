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

import java.io.IOException;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.remoting.VirtualChannel;

import org.jenkinsci.plugins.artifactpromotion.exception.PromotionException;

/**
 * 
 * @author Timo "timii" Paananen
 * @author Halil-Cem Guersoy
 * 
 */
@Extension
public class NexusOSSPromotor extends AbstractPromotor {

	public void callPromotor(VirtualChannel channel) throws PromotionException {

		IPromotorClosure promotor = new NexusOSSPromoterClosure(
				getLocalRepositoryURL(), 
				getListener(), 
				getExpandedTokens(),
				getReleaseUser(), 
				getReleasePassword(), 
				getStagingUser(),
				getStagingPassword());

		RemotePromoter promotorTask = new RemotePromoter(promotor);
		
		//only debugging
//		DebuggingObjectOutputStream out = null;
//			try {
//				out =
//						new DebuggingObjectOutputStream(getListener().getLogger());
//			  out.writeObject(promotorTask);
//			  getListener().getLogger().println("######### promotortask serialises fine");
//			} catch (Exception e) {
//				getListener().getLogger().println("Serialization error. Path to bad object: " + out.getStack());
//				e.printStackTrace(getListener().getLogger());
//			  throw new RuntimeException(
//			      "Serialization error. Path to bad object: " 
//			          + out.getStack(), e);
//			} finally {
//				try {
//					out.close();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace(getListener().getLogger());
//				}
//			}
			
		try {
			channel.call(promotorTask);
		} catch (Exception e) {
			getListener().getLogger().println("Promotion could not be executed");
			e.printStackTrace(getListener().getLogger());
			throw new PromotionException("Promotion could not be executed: " + e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	public Descriptor<Promotor> getDescriptor() {
		return new AbstractPromotorDescription() {

			@Override
			public String getDisplayName() {
				return "Nexus OSS";
			}
		};
	}

}
