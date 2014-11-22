/**
 * Copyright 2007-2012 Jordi Hernández Sellés, Ibrahim Chaehoi
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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.context.ThreadLocalJawrContext;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.BundleRenderer;
import net.jawr.web.resource.bundle.renderer.BundleRendererContext;
import net.jawr.web.servlet.RendererRequestUtils;
import net.jawr.web.util.StringUtils;

/**
 * Abstract implementation of a tag lib component which will retrieve a Jawr
 * config object from the servlet context and use it to render bundles of
 * resources according to its src attribute.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public abstract class AbstractResourceBundleTag extends TagSupport {

	/** The serial version UID */
	private static final long serialVersionUID = -9114179136913388470L;

	/** The source path */
	protected String src;

	/** The flag to use random param */
	protected String useRandomParam = null;

	/** The flag indicating if we should use the random parameter */
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag() throws JspException {

		HttpServletRequest request = (HttpServletRequest) pageContext
			.getRequest();
		
		if(null == pageContext.getServletContext().getAttribute(getResourceHandlerAttributeName()))
			throw new IllegalStateException("ResourceBundlesHandler not present in servlet context. Initialization of Jawr either failed or never occurred.");

		ResourceBundlesHandler rsHandler = (ResourceBundlesHandler) pageContext.getServletContext().getAttribute(getResourceHandlerAttributeName());
		JawrConfig jawrConfig = rsHandler.getConfig(); 
		
		if(RendererRequestUtils.refreshConfigIfNeeded(request, jawrConfig)){
			rsHandler = (ResourceBundlesHandler) pageContext.getServletContext().getAttribute(getResourceHandlerAttributeName());
			jawrConfig = rsHandler.getConfig(); 
		}
				
		Boolean useRandomFlag = null;
		if(StringUtils.isNotEmpty(useRandomParam)){
			useRandomFlag = Boolean.valueOf(useRandomParam);
		}
		// Renderer istance which takes care of generating the response
		BundleRenderer renderer = createRenderer(rsHandler, useRandomFlag);
		
		// set the debug override
		RendererRequestUtils.setRequestDebuggable(request, jawrConfig);

		try {
			BundleRendererContext ctx = RendererRequestUtils
					.getBundleRendererContext(request, renderer);
			renderer.renderBundleLinks(src, ctx, pageContext.getOut());

		} catch (IOException ex) {
			throw new JspException(
					"Unexpected IOException when writing script tags for path "
							+ src, ex);
		}finally{
			// Reset the Thread local for the Jawr context
			ThreadLocalJawrContext.reset();
		}
		
		return SKIP_BODY;
	}

	/**
	 * Set the source of the resource or bundle to retrieve.
	 * 
	 * @param src
	 */
	public void setSrc(String src) {
		this.src = src;
	}

	/**
	 * Set wether random param will be added in development mode to generated
	 * urls.
	 * 
	 * @param useRandomParam
	 */
	public void setUseRandomParam(String useRandomParam) {
		this.useRandomParam = useRandomParam;
	}

	/**
	 * Returns the resource bundle attribute name
	 * @return the resource bundle attribute name
	 */
	protected abstract String getResourceHandlerAttributeName();
	
	/**
	 * Returns the bundle renderer
	 * @param rsHandler the resource bundle handler
	 * @param useRandomParam the flag indicating if we must add the random param in debug mode or not
	 * @return the bundle renderer
	 */
	protected abstract BundleRenderer createRenderer(ResourceBundlesHandler rsHandler, Boolean useRandomParam);
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.jsp.tagext.TagSupport#release()
	 */
	public void release() {

		src = null;
		useRandomParam = null;
		
		// Reset the Thread local for the Jawr context
		ThreadLocalJawrContext.reset();
	}

}
