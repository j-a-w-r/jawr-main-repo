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
import net.jawr.web.util.StringUtils;

/**
 * Implementation of a jsp taglib AbstractResourceBundleTag used to render
 * javascript bundles.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class JavascriptBundleTag extends AbstractResourceBundleTag {

	/** The serial version UID */
	private static final long serialVersionUID = -6420832844948862285L;

	/** The type attribute */
	protected String type;

	/** The async attribute */
	protected String async;

	/** The defer attribute */
	protected String defer;

	/** The crossorigin attribute */
	protected String crossorigin;

	/**
	 * Sets the type attribute
	 * 
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Set the async attribute.
	 * 
	 * @param async
	 */
	public void setAsync(String async) {
		this.async = async;
	}

	/**
	 * Set the defer attribute.
	 * 
	 * @param defer
	 */
	public void setDefer(String defer) {
		this.defer = defer;
	}

	/**
	 * Set the crossorigin attribute.
	 * 
	 * @param crossorigin
	 */
	public void setCrossorigin(String crossorigin) {
		this.crossorigin = crossorigin;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.taglib.AbstractResourceBundleTag#
	 * getResourceHandlerAttributeName()
	 */
	@Override
	protected String getResourceHandlerAttributeName() {
		return JawrConstant.JS_CONTEXT_ATTRIBUTE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.taglib.AbstractResourceBundleTag#createRenderer(net.jawr.web
	 * .resource.bundle.handler.ResourceBundlesHandler, boolean)
	 */
	@Override
	protected BundleRenderer createRenderer(ResourceBundlesHandler rsHandler, Boolean useRandomParam) {

		Boolean asyncFlag = null;
		if (StringUtils.isNotEmpty(async)) {
			asyncFlag = Boolean.valueOf(async);
		}
		Boolean deferFlag = null;
		if (StringUtils.isNotEmpty(defer)) {
			deferFlag = Boolean.valueOf(defer);
		}
		return RendererFactory.getJsBundleRenderer(rsHandler, type, useRandomParam, asyncFlag, deferFlag, crossorigin);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.jsp.tagext.TagSupport#release()
	 */
	@Override
	public void release() {

		super.release();
		async = null;
		defer = null;
		type = null;
	}
}
