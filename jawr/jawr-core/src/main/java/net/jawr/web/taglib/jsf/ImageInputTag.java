/**
 * Copyright 2009 Ibrahim Chaehoi
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package net.jawr.web.taglib.jsf;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * This class defines the JSF image input tag.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class ImageInputTag extends AbstractHtmlImageTag {

	/* (non-Javadoc)
	 * @see net.jawr.web.taglib.jsf.AbstractImageTag#render(javax.faces.context.FacesContext)
	 */
	protected void render(FacesContext context) throws IOException {

		// Generate the name definition or image element
		StringBuffer results = new StringBuffer(getElementOpen());

		prepareImageUrl(context, results);

		prepareAttribute(results, "name", getAttribute("name"));
		prepareAttribute(results, "align", getAttribute("align"));
		prepareAttribute(results, "border", getAttribute("border"));
		prepareAttribute(results, "value", getAttribute("value"));
		prepareAttribute(results, "accesskey", getAttribute("accesskey"));
		prepareAttribute(results, "tabindex", getAttribute("tabindex"));
		prepareAttribute(results, "disabled", getAttribute("disabled"));
		results.append(prepareStyles());
		results.append(prepareEventHandlers());
		results.append(" />");

		ResponseWriter writer = context.getResponseWriter();
		writer.write(results.toString());
	}

	/**
     * Prepares the keyboard event handlers, appending them to the the given
     * StringBuffer.
     *
     * @param handlers The StringBuffer that output will be appended to.
     */
    protected void prepareKeyEvents(StringBuffer handlers) {
        
    	super.prepareKeyEvents(handlers);
        prepareAttribute(handlers, "onchange", getAttribute("onchange"));
        prepareAttribute(handlers, "onfocus",  getAttribute("onfocus"));
        prepareAttribute(handlers, "onblur", getAttribute("onblur"));
    }

	/**
	 * Render the opening element.
	 * 
	 * @return The opening part of the element.
	 */
	protected String getElementOpen() {
		return "<input type=\"image\"";
	}
}
