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

import org.eclipse.aether.artifact.Artifact;

/**
 * Wraps the information about an artifact, in this case
 * it puts the artifact itself together with its corresponding pom.
 *
 */
public class ArtifactWrapper {

	private Artifact artifact = null;
	private Artifact pom = null;
	
	public ArtifactWrapper(Artifact artifact, Artifact pom) {
		this.artifact = artifact;
		this.pom = pom;
	}
	
	public Artifact getArtifact() {
		return artifact;
	}
	
	public Artifact getPom() {
		return pom;
	}
}
