/**
 * Copyright 2007-2016 Jordi Hernández Sellés, Ibrahim Chaehoi
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

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.BundleRenderer;
import net.jawr.web.resource.bundle.renderer.RendererFactory;

/**
 * JSP taglib which uses a CSSHTMLBundleLinkRenderer to render links for CSS
 * bundles.
 * 
 * @author Jordi Hern�ndez Sell�s
 * @author Ibrahim Chaehoi
 */
public class CSSBundleTag extends AbstractResourceBundleTag {

	/** The serial version UID */
	private static final long serialVersionUID = 5087323727715427592L;

	/** The media */
	private String media;

	/** The flag indicating if it's an alternate stylesheet */
	private boolean alternate;

	/** The flag indicating if they must display the alternate styles */
	private boolean displayAlternate;

	/** The title */
	private String title;

	/**
	 * Set the media type to use in the css tag
	 * 
	 * @param media
	 */
	public void setMedia(String media) {
		this.media = media;
	}

	/**
	 * Sets the alternate flag
	 * 
	 * @param alternate
	 *            the alternate to set
	 */
	public void setAlternate(boolean alternate) {
		this.alternate = alternate;
	}

	/**
	 * Sets the flag indicating if the styles must display the alternate styles
	 * 
	 * @param displayAlternate
	 *            the falg indicating if we must display alternate skin styles
	 *            or not
	 */
	public void setDisplayAlternate(boolean displayAlternate) {
		this.displayAlternate = displayAlternate;
	}

	/**
	 * Sets the title
	 * 
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.taglib.AbstractResourceBundleTag#createRenderer(boolean)
	 */
	@Override
	protected BundleRenderer createRenderer(ResourceBundlesHandler rsHandler, Boolean useRandomParam) {
		return RendererFactory.getCssBundleRenderer(rsHandler, useRandomParam, this.media, this.alternate,
				this.displayAlternate, this.title);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.taglib.AbstractResourceBundleTag#
	 * getResourceHandlerAttributeName()
	 */
	@Override
	protected String getResourceHandlerAttributeName() {
		return JawrConstant.CSS_CONTEXT_ATTRIBUTE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.jsp.tagext.TagSupport#release()
	 */
	@Override
	public void release() {
		super.release();
		alternate = false;
		displayAlternate = false;
		title = null;
		media = null;
	}

}