/**
 * Copyright 2007-2011 Jordi Hernández Sellés, Ibrahim Chaehoi
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

import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.servlet.http.HttpServletRequest;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.BundleRenderer;
import net.jawr.web.resource.bundle.renderer.BundleRendererContext;
import net.jawr.web.servlet.RendererRequestUtils;

/**
 * Abstract implementation of a facelets taglib component which will retrieve a Jawr config
 * object from the servlet context and use it to render bundles of resources according 
 * to its src attribute.  
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 *
 */
public abstract class AbstractResourceBundleTag extends UIOutput {

	/** The bundle renderer */
	protected BundleRenderer renderer;
	
	/** The flag indicating if we use the random parameter or not */
	protected String useRandomParam = null;    
	
	/* (non-Javadoc)
	 * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
	 */
	public void encodeBegin(FacesContext context) throws IOException {
		
		// Initialize attributes
		this.useRandomParam = (String)getAttributes().get("useRandomParam");
        
        String src = (String)getAttributes().get("src"); 
        
        ResponseWriter writer = context.getResponseWriter();
        HttpServletRequest request = ((HttpServletRequest)context.getExternalContext().getRequest());
        
        ResourceBundlesHandler bundler = getResourceBundlesHandler(context);
        
        // src is mandatory
        if(null == src)
        	throw new IllegalStateException("The src attribute is mandatory for this Jawr tag. ");
        
        //Refresh the config if needed
        if(RendererRequestUtils.refreshConfigIfNeeded(request, bundler.getConfig())){
        	bundler = getResourceBundlesHandler(context);
        }
        
        // Get an instance of the renderer. 
        if(null == this.renderer || !this.renderer.getBundler().getConfig().isValid())
             this.renderer = createRenderer(context);		
        
        
        // 
        RendererRequestUtils.setRequestDebuggable(request,renderer.getBundler().getConfig());
        
        BundleRendererContext ctx = RendererRequestUtils.getBundleRendererContext(request, renderer);
        renderer.renderBundleLinks( src,
                ctx, writer);

		super.encodeBegin(context);
	}

	/**
	 * Returns the flag for the use of random param in debug mode
	 * @param config the Jawr config
	 * @return the flag for the use of random param in debug mode
	 */
	protected boolean getUseRandomParamFlag(JawrConfig config){
		
		boolean useRandomParamFlag = config.isDebugUseRandomParam(); 
		if(useRandomParam != null){
			useRandomParamFlag = Boolean.parseBoolean(useRandomParam);
		}
		return useRandomParamFlag;
	}
	
	/**
	 * Retrieve the renderer. 
	 * @param context the FacesContext
	 * @return the renderer for the tag
	 */
	protected abstract BundleRenderer createRenderer(FacesContext context);
	
	/**
	 * Returns the resource handler
	 * @param context the FacesContext
	 * @return the resource handler
	 */
	protected ResourceBundlesHandler getResourceBundlesHandler(FacesContext context) {
		Object handler = context.getExternalContext().getApplicationMap().get(getResourceBundlesHandlerAttributeName());
		if(null == handler)
			throw new IllegalStateException("ResourceBundlesHandler not present in servlet context. Initialization of Jawr either failed or never occurred.");

		ResourceBundlesHandler rsHandler = (ResourceBundlesHandler) handler;
		return rsHandler;
	}
	
	/**
	 * Returns the resource handler of the tag
	 * @return the resource handler of the tag
	 */
	protected abstract String getResourceBundlesHandlerAttributeName();
	
}
