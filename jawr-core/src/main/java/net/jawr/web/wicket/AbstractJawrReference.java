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

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;

import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.BundleRenderer;
import net.jawr.web.resource.bundle.renderer.BundleRendererContext;
import net.jawr.web.servlet.RendererRequestUtils;
import net.jawr.web.util.StringUtils;

import org.apache.log4j.Logger;
import org.apache.wicket.Response;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.util.value.IValueMap;

/**
 * This class defines the abstract class for the Jawr CSS and Stylesheet references 
 * for Wicket.
 * 
 * @autor Robert Kopaczewski (Original author) 
 * @author Ibrahim Chaehoi
 */
public abstract class AbstractJawrReference extends WebMarkupContainer {

	/** The serial version UID */
	private static final long serialVersionUID = 6483803210055728200L;
	
	/** The logger */
	private static final Logger LOGGER = Logger.getLogger(AbstractJawrReference.class);

	/** The bundle renderer */
	protected BundleRenderer renderer;
	
	/** The flag indicating if we must use the random parameter */
    protected String useRandomParam;
    
    /**
     * Constructor
     * @param id the ID
     */
    public AbstractJawrReference(String id) {
        super(id);
    }

    /* (non-Javadoc)
     * @see org.apache.wicket.MarkupContainer#onRender(org.apache.wicket.markup.MarkupStream)
     */
    protected void onRender(MarkupStream markupStream) {
        try {
            final ComponentTag openTag = markupStream.getTag();
            final ComponentTag tag = openTag.mutable();
            final IValueMap attributes = tag.getAttributes();

            // Initialize attributes
            String src = getReferencePath(attributes);
            
            // src is mandatory
            if (null == src) {
                throw new IllegalStateException("The src attribute is mandatory for this Jawr reference tag. ");
            }

            ServletWebRequest servletWebRequest = (ServletWebRequest) getRequest();
            HttpServletRequest request = servletWebRequest.getHttpServletRequest();

            // Get an instance of the renderer.
            ResourceBundlesHandler rsHandler = (ResourceBundlesHandler) request.getSession().getServletContext().getAttribute(getResourceHandlerAttributeName());
            if (null == rsHandler) {
                throw new IllegalStateException("ResourceBundlesHandler not present in servlet context. Initialization of Jawr either failed or never occurred.");
            }

            // Refresh the config if needed
            if(RendererRequestUtils.refreshConfigIfNeeded(request, rsHandler.getConfig())){
            	rsHandler = (ResourceBundlesHandler) request.getSession().getServletContext().getAttribute(getResourceHandlerAttributeName());
            }
            
            Boolean useRandomFlag = null;
    		if(StringUtils.isNotEmpty(useRandomParam)){
    			useRandomFlag = Boolean.valueOf(useRandomParam);
    		}
            BundleRenderer renderer = createRenderer(rsHandler, useRandomFlag, tag);
            
            // set the debug override
 	       	RendererRequestUtils.setRequestDebuggable(request,renderer.getBundler().getConfig());
 		   
            final Response response = getResponse();
            Writer writer = new RedirectWriter(response);
            BundleRendererContext ctx =  RendererRequestUtils.getBundleRendererContext(request, renderer);
			   
            renderer.renderBundleLinks(src,
            		ctx,
                    writer);
        } catch (IOException ex) {
            LOGGER.error("onRender() error : ", ex);
        }

        markupStream.skipComponent();
    }

    /**
     * Returns the reference path
     * @param attributes the attributes
     * @return the reference path
     */
    protected abstract String getReferencePath(final IValueMap attributes);
    
    /**
	 * Returns the resource bundle attribute name
	 * @return the resource bundle attribute name
	 */
	protected abstract String getResourceHandlerAttributeName();
	
	/**
     * Create the tag renderer.
	 * @param rsHandler the resource bundle handler 
     * @param useRandomParam the flag indicating if we must use random parameter in debug mode
	 * @param tag the tag
     * @return the tag renderer.
     */
    protected abstract BundleRenderer createRenderer(ResourceBundlesHandler rsHandler, Boolean useRandomParam, ComponentTag tag);
    
}
