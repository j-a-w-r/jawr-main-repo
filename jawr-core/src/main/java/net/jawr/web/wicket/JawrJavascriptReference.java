/**
 * Copyright 2009-2012 Ibrahim Chaehoi
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
package net.jawr.web.wicket;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.BundleRenderer;
import net.jawr.web.resource.bundle.renderer.JavascriptHTMLBundleLinkRenderer;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.util.value.IValueMap;

/**
 * The abstract class for the CSS and Stylesheet reference for Wicket.
 * 
 * @autor Robert Kopaczewski (Original author) 
 * @author Ibrahim Chaehoi
 */
public class JawrJavascriptReference extends AbstractJawrReference {

    /** The serial version UID */
	private static final long serialVersionUID = -2767038866259367402L;

	/**
	 * Constructor
	 * @param id the ID
	 */
	public JawrJavascriptReference(String id) {
        super(id);
    }

	/* (non-Javadoc)
	 * @see net.jawr.web.wicket.JawrAbstractReference#getReferencePath(org.apache.wicket.util.value.IValueMap)
	 */
	protected String getReferencePath(final IValueMap attributes) {
		return (String) attributes.get("src");
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.wicket.AbstractJawrReference#getResourceHandlerAttributeName()
	 */
	@Override
	protected String getResourceHandlerAttributeName() {
		return JawrConstant.JS_CONTEXT_ATTRIBUTE;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.wicket.AbstractJawrReference#createRenderer(net.jawr.web.resource.bundle.handler.ResourceBundlesHandler, boolean, org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected BundleRenderer createRenderer(ResourceBundlesHandler rsHandler,
			Boolean useRandomParam, ComponentTag tag) {
		
		final IValueMap attributes = tag.getAttributes();
		boolean defer = attributes.getBoolean(JawrConstant.DEFER_ATTR);
        return new JavascriptHTMLBundleLinkRenderer(rsHandler, useRandomParam, defer);
	}
}
