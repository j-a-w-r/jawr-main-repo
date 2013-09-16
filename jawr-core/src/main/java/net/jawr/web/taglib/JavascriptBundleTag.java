/**
 * Copyright 2007-2012 Jordi Hern�ndez Sell�s, Ibrahim Chaehoi
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
 * Implementation of a jsp taglib AbstractResourceBundleTag used to render javascript bundles. 
 * 
 * @author Jordi Hern�ndez Sell�s
 * @author Ibrahim Chaehoi
 */
public class JavascriptBundleTag extends AbstractResourceBundleTag {

	/** The serial version UID */
	private static final long serialVersionUID = 5087323727715427593L;

	/** The defer attribute */
	protected boolean defer;
	
	/* (non-Javadoc)
	 * @see net.jawr.web.taglib.AbstractResourceBundleTag#getResourceHandlerAttributeName()
	 */
	@Override
	protected String getResourceHandlerAttributeName() {
		return JawrConstant.JS_CONTEXT_ATTRIBUTE;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.taglib.AbstractResourceBundleTag#createRenderer(net.jawr.web.resource.bundle.handler.ResourceBundlesHandler, boolean)
	 */
	@Override
	protected BundleRenderer createRenderer(ResourceBundlesHandler rsHandler,
			Boolean useRandomParam) {
		
		return  RendererFactory.getJsBundleRenderer(rsHandler, useRandomParam, defer);
	}

}
