/**
 * Copyright 2009-2013 Ibrahim Chaehoi
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


/**
 * This class defines the image tag.
 * 
 * This implementation is based on the Struts image tag.
 * 
 * @author Ibrahim Chaehoi
 */
public class ImgHtmlTag extends AbstractImageTag {

	/**
	 * The serial version UID
	 */
	private static final long serialVersionUID = -6048102958207543073L;

	
	/**
	 * Constructor
	 */
	public ImgHtmlTag() {
	
	}


	/* (non-Javadoc)
	 * @see net.jawr.web.taglib.AbstractImageTag#isPlainImage()
	 */
	@Override
	protected boolean isPlainImage() {
		return true;
	}
	
	/**
	 * Sets the height attribute
	 * @param height the value to set
	 */
	public void setHeight(String height) {
		getAttributeMap().put("height", height);
	}

	/**
	 * Sets the hspace attribute
	 * @param hspace the value to set
	 */
	public void setHspace(String hspace) {
		getAttributeMap().put("hspace", hspace);
	}

	/**
	 * Sets the ismap attribute
	 * @param ismap the value to set
	 */
	public void setIsmap(String ismap) {
		getAttributeMap().put("ismap", ismap);
	}

	/**
	 * Sets the usemap attribute
	 * @param usemap the value to set
	 */
	public void setUsemap(String usemap) {
		getAttributeMap().put("usemap", usemap);
	}

	/**
	 * Sets the vspace attribute
	 * @param vspace the value to set
	 */
	public void setVspace(String vspace) {
		getAttributeMap().put("vspace", vspace);
	}

	/**
	 * Sets the width attribute
	 * @param width the value to set
	 */
	public void setWidth(String width) {
		getAttributeMap().put("width", width);
	}

	
}
