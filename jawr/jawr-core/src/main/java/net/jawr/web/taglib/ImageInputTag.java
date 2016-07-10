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
package net.jawr.web.taglib;

/**
 * This class defines the image tag.
 * 
 * This implementation is based on the Struts image tag.
 * 
 * @author Ibrahim Chaehoi
 */
public class ImageInputTag extends AbstractImageTag {

	// ------------------------------------------------------------- Properties

	/**
	 * The serial version UID
	 */
	private static final long serialVersionUID = -3608810516737758870L;

	/**
	 * Constructor
	 */
	public ImageInputTag() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.taglib.AbstractImageTag#isPlainImage()
	 */
	@Override
	protected boolean isPlainImage() {
		return false;
	}

	/**
	 * @param accesskey
	 *            the accesskey to set
	 */
	public void setAccesskey(String accesskey) {
		getAttributeMap().put("accesskey", accesskey);
	}

	/**
	 * @param tabindex
	 *            the tabindex to set
	 */
	public void setTabindex(String tabindex) {
		getAttributeMap().put("tabindex", tabindex);
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		getAttributeMap().put("value", value);
	}

	/**
	 * @param disabled
	 *            the disabled to set
	 */
	public void setDisabled(boolean disabled) {
		getAttributeMap().put("disabled", disabled);
	}

	/**
	 * @param onfocus
	 *            the onfocus to set
	 */
	public void setOnfocus(String onfocus) {
		getAttributeMap().put("onfocus", onfocus);
	}

	/**
	 * @param onblur
	 *            the onblur to set
	 */
	public void setOnblur(String onblur) {
		getAttributeMap().put("onblur", onblur);
	}

	/**
	 * @param onchange
	 *            the onchange to set
	 */
	public void setOnchange(String onchange) {
		getAttributeMap().put("onchange", onchange);
	}

}
