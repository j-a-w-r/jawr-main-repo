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
package net.jawr.web.taglib;

import net.jawr.web.resource.bundle.renderer.image.ImgRenderer;

/**
 * This class defines the image tag.
 * 
 * This implementation is based on the Struts image tag.
 * 
 * @author Ibrahim Chaehoi
 */
public class ImgHtmlTag extends AbstractImageTag {

	// -------------------------------------------------------------
	/**
	 * The serial version UID
	 */
	private static final long serialVersionUID = -6048102958207543073L;

	// ----------------------------------------------------- Constructor

	public ImgHtmlTag() {
		super(new ImgRenderer(true));
	}

	public void setHeight(String height) {
		getAttributeMap().put("height", height);
	}

	public void setHspace(String hspace) {
		getAttributeMap().put("hspace", hspace);
	}

	public void setIsmap(String ismap) {
		getAttributeMap().put("ismap", ismap);
	}

	public void setUsemap(String usemap) {
		getAttributeMap().put("usemap", usemap);
	}

	public void setVspace(String vspace) {
		getAttributeMap().put("vspace", vspace);
	}

	public void setWidth(String width) {
		getAttributeMap().put("width", width);
	}
	
}
