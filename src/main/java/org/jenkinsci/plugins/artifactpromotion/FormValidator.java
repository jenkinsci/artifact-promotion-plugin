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

import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.QueryParameter;

/**
 * Perform validations for StepDescriptor implementations.
 *
 * @author Halil-Cem Guersoy (hcguersoy@gmail.com)
 * @author Julian Sauer (julian_sauer@mx.net)
 */
public interface FormValidator {

    /**
     * Performs on-the-fly validation of the form field 'name'.
     *
     * @param value This parameter receives the value that the user has typed.
     * @return Indicates the outcome of the validation. This is sent to the
     * browser.
     */
    default FormValidation doCheckArtifactId(@QueryParameter String value) {
        if (value.length() == 0)
            return FormValidation.error("Please set an ArtifactId!");
        return FormValidation.ok();
    }

    default FormValidation doCheckGroupId(@QueryParameter String value) {
        if (value.length() == 0)
            return FormValidation.error("Please set a GroupId!");
        return FormValidation.ok();
    }

    default FormValidation doCheckVersion(@QueryParameter String value) {
        if (value.length() == 0)
            return FormValidation
                    .error("Please set a Version for your artifact!");
        return FormValidation.ok();
    }

    default FormValidation doCheckStagingRepository(
            @QueryParameter String value) {
        return checkURI(value);
    }

    default FormValidation doCheckReleaseRepository(
            @QueryParameter String value) {
        return checkURI(value);
    }

    /**
     * This method checks originally the URL if it is valid. On the way to
     * support tokens this behavior is build out. It will be reactivated
     * after a general refactoring for better token macro support.
     * <p>
     * TODO implment a URL validation which works with token macro plugin
     *
     * @param value
     * @return
     */
    default FormValidation checkURI(String value) {
        if (value.length() == 0) {
            return FormValidation
                    .error("Please set an URL for the staging repository!");
        }
        return FormValidation.ok();
    }

    /**
     * Generates LisBoxModel for available Repository systems
     *
     * @return available Promoters as ListBoxModel
     */
    default ListBoxModel doFillPromoterClassItems() {
        ListBoxModel promoterModel = new ListBoxModel();
        for (Promotor promotor : Jenkins.getInstance()
                .getExtensionList(Promotor.class)) {
            promoterModel.add(promotor.getDescriptor().getDisplayName(), promotor
                    .getClass().getCanonicalName());
        }

        return promoterModel;
    }

}
