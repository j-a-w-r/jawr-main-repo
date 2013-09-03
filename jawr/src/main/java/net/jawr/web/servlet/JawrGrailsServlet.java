/**
 * Copyright 2008-2012 Jordi Hern�ndez Sell�s, Ibrahim Chaehoi
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
package net.jawr.web.servlet;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jawr.web.JawrConstant;

import org.apache.log4j.Logger;

/**
 * Specialized subclass of the JawrServlet which s automatically mapped to the servlet context 
 * in grails applications.  
 * 
 * @author Jordi Hern�ndez Sell�s
 * @author Ibrahim Chaehoi
 */
public class JawrGrailsServlet extends JawrServlet {

	private static final Logger LOGGER = Logger.getLogger(JawrGrailsServlet.class);
	
	private static final long serialVersionUID = -7749799838520309579L;
	private Integer configHash;
	
	/* (non-Javadoc)
	 * @see net.jawr.web.servlet.JawrServlet#init()
	 */
	@SuppressWarnings("unchecked")
	public void init() throws ServletException {
		Map<String, Object> config = null;
		String type = getServletConfig().getInitParameter("type");
		
		ServletContext servletContext = getServletContext();
		configHash = (Integer)servletContext.getAttribute(JawrConstant.JAWR_GRAILS_CONFIG_HASH);
		
		if(JawrConstant.CSS_TYPE.equals(type))
			config = (Map<String, Object>) servletContext.getAttribute(JawrConstant.JAWR_GRAILS_CSS_CONFIG);
		else if(JawrConstant.IMG_TYPE.equals(type))
			config = (Map<String, Object>) servletContext.getAttribute(JawrConstant.JAWR_GRAILS_IMG_CONFIG);
		else
			config = (Map<String, Object>) servletContext.getAttribute(JawrConstant.JAWR_GRAILS_JS_CONFIG);
		
		Properties jawrProps = (Properties)config.get(JawrConstant.JAWR_GRAILS_CONFIG_PROPERTIES_KEY);
		try {
			if(JawrConstant.IMG_TYPE.equals(type)){
				this.requestHandler = new JawrImageRequestHandler(servletContext , config, jawrProps );
			}else{
				this.requestHandler = new JawrRequestHandler(servletContext , config, jawrProps );
			}
			
			if(JawrConstant.JS_TYPE.equals(type)){
				servletContext.setAttribute(JawrConstant.JAWR_GRAILS_JS_REQUEST_HANDLER, requestHandler);
			}else if(JawrConstant.CSS_TYPE.equals(type)){
				servletContext.setAttribute(JawrConstant.JAWR_GRAILS_CSS_REQUEST_HANDLER, requestHandler);
			}else if(JawrConstant.IMG_TYPE.equals(type)){
				servletContext.setAttribute(JawrConstant.JAWR_GRAILS_IMG_REQUEST_HANDLER, requestHandler);
			}
		}catch (ServletException e) {
			LOGGER.fatal("Jawr servlet with name" +  getServletConfig().getServletName() +" failed to initialize properly. ");
			LOGGER.fatal("Cause:");
			LOGGER.fatal(e.getMessage(),e);
			throw e;
		}catch (RuntimeException e) {
			LOGGER.fatal("Jawr servlet with name" +  getServletConfig().getServletName() +" failed to initialize properly. ");
			LOGGER.fatal("Cause: ");
			LOGGER.fatal(e.getMessage(),e);
			throw new ServletException(e);
		}
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.servlet.JawrServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// Reload config if applies
		if(!configHash.equals((Integer)getServletContext().getAttribute(JawrConstant.JAWR_GRAILS_CONFIG_HASH)))
			this.init();
		
		super.doGet(req, resp);
	}

	
}
