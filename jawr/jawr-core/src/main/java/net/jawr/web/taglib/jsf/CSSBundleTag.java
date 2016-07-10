/**
 * Copyright 2008-2016 Jordi Hernández Sellés, Ibrahim Chaehoi
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

import javax.faces.context.FacesContext;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.BundleRenderer;
import net.jawr.web.resource.bundle.renderer.RendererFactory;

/**
 * Facelets taglib which uses a CSSHTMLBundleLinkRenderer to render links for
 * CSS bundles.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class CSSBundleTag extends AbstractResourceBundleTag {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.taglib.jsf.AbstractResourceBundleTag#createRenderer(javax.
	 * faces.context.FacesContext)
	 */
	@Override
	protected BundleRenderer createRenderer(FacesContext context) {

		ResourceBundlesHandler rsHandler = getResourceBundlesHandler(context);

		String media = (String) getAttributes().get(JawrConstant.MEDIA_ATTR);
		boolean alternate = Boolean.parseBoolean((String) getAttributes().get(JawrConstant.ALTERNATE_ATTR));
		boolean displayAlternate = Boolean
				.parseBoolean((String) getAttributes().get(JawrConstant.DISPLAY_ALTERNATE_ATTR));
		String title = (String) getAttributes().get(JawrConstant.TITLE_ATTR);

		return RendererFactory.getCssBundleRenderer(rsHandler, getUseRandomParamFlag(rsHandler.getConfig()), media,
				alternate, displayAlternate, title);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.taglib.jsf.AbstractResourceBundleTag#
	 * getResourceHandlerAttributeName()
	 */
	@Override
	protected String getResourceBundlesHandlerAttributeName() {
		return JawrConstant.CSS_CONTEXT_ATTRIBUTE;
	}

}
