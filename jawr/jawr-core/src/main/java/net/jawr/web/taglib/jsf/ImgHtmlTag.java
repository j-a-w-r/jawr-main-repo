/**
 * Copyright 2009-2016 Ibrahim Chaehoi
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
 * The JSF tag for HTML image
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class ImgHtmlTag extends AbstractHtmlImageTag {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.taglib.jsf.AbstractImageTag#render(javax.faces.context.
	 * FacesContext)
	 */
	@Override
	protected void render(FacesContext context) throws IOException {

		// Generate the name definition or image element
		StringBuffer results = new StringBuffer("<img");
		prepareImageUrl(context, results);

		prepareAttribute(results, "name", getAttribute("name"));
		prepareAttribute(results, "height", getAttribute("height"));
		prepareAttribute(results, "width", getAttribute("width"));
		prepareAttribute(results, "align", getAttribute("align"));
		prepareAttribute(results, "border", getAttribute("border"));
		prepareAttribute(results, "hspace", getAttribute("hspace"));
		prepareAttribute(results, "vspace", getAttribute("vspace"));
		prepareAttribute(results, "ismap", getAttribute("ismap"));
		prepareAttribute(results, "usemap", getAttribute("usemap"));
		results.append(prepareStyles());
		results.append(prepareEventHandlers());
		results.append(" />");

		ResponseWriter writer = context.getResponseWriter();
		writer.write(results.toString());

	}

}
