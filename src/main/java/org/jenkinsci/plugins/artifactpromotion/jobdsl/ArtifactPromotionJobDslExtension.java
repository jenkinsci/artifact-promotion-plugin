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
package org.jenkinsci.plugins.artifactpromotion.jobdsl;

import org.jenkinsci.plugins.artifactpromotion.ArtifactPromotionBuilder;
import org.jenkinsci.plugins.artifactpromotion.NexusOSSPromotor;

import hudson.Extension;
import javaposse.jobdsl.dsl.helpers.step.StepContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;

/**
 * This class provides the artifactionPromotion DSL extension method.
 * 
 * The artifactPromotion keyword can be used with a closure as a build step.
 * The further vocabulary for the closure is defined in the {@link ArtifactPromotionDslContext}.
 * 
 * @author Patrick Schlebusch
 */
@Extension(optional = true)
public class ArtifactPromotionJobDslExtension extends ContextExtensionPoint {

    @DslExtensionMethod(context = StepContext.class)
    public Object artifactPromotion(Runnable closure) {
        ArtifactPromotionDslContext context = new ArtifactPromotionDslContext();
        executeInContext(closure, context);

        return new ArtifactPromotionBuilder(
                context.getGroupId(), context.getArtifactId(), context.getClassifier(), context.getVersion(),
                context.getExtension(), context.getStagingRepository(), context.getStagingUser(),
                context.getStagingPassword(), context.getReleaseUser(), context.getReleasePassword(),
                context.getReleaseRepository(), context.getPromoterClass(), context.isDebugEnabled(),
                context.isSkipDeletionEnabled());
    }

    public enum RepositorySystem {
        NexusOSS(NexusOSSPromotor.class.getName());

        private String className;

        private RepositorySystem(String className) {
            this.className = className;
        }

        public String getClassName() {
            return this.className;
        }
    }
}
