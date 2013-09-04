/**
 * Copyright 2009-2011 Ibrahim Chaehoi
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
package net.jawr.web.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletContext;

import net.jawr.web.exception.BundlingProcessException;

import org.apache.log4j.Logger;

/**
 * Utility class for ServeltContext
 * 
 * @author Ibrahim Chaehoi
 */
public class ServletContextUtils {

	/** The logger */
	private static final Logger LOGGER = Logger.getLogger(ServletContextUtils.class);

	/** The context path parameter */
	private static final String CONTEXT_PATH_PARAM_NAME = "contextPath";
	
	/** The default context path used for the application if no context oath is defined */
	public static final String DEFAULT_CONTEXT_PATH_NAME = "default";

	/** The default context path used for the application if no context oath is defined */
	private static final String DEFAULT_CONTEXT_PATH = "/"+DEFAULT_CONTEXT_PATH_NAME;

	/** The getContextPath method name to retrieve the context path for servlet API 2.5 and above */
	private static final String GET_CONTEXT_PATH_METHOD = "getContextPath";

	/**
	 * Returns the context path associated to the servlet context
	 * @param servletContext the servlet context
	 * @return the context path associated to the servlet context
	 * @throws Exception if an exception occurs
	 */
	public static String getContextPath(ServletContext servletContext) {
		String contextPath = null;
		
		// Get the context path
		if(servletContext != null){
			// If the servlet API version is greater or equals to 2.5, use the getContextPath method
			if(servletContext.getMajorVersion() > 2 || servletContext.getMajorVersion() == 2 && servletContext.getMinorVersion() >= 5){
				
				contextPath = getContextPathForNewServletApi(servletContext);
				
			}else{ // Retrieve the context path from the init parameter or the servlet context
				contextPath = servletContext.getInitParameter(CONTEXT_PATH_PARAM_NAME);
			}
			
			if(contextPath == null){
				LOGGER.warn("No context path defined for this web application. You will face issues, if you are deploying mutiple web app, without defining the context.\n" +
						"If you are using a server with Servlet API less than 2.5, please use the context parameter 'contextPath' in your web.xml to define the context path of the application.\n" +
						"The context path of your application will be set to '/default'");
				
				contextPath = DEFAULT_CONTEXT_PATH;
			}
		}
		
		return contextPath;
	}

	/**
	 * Returns the context path using the servlet API version 2.5 method
	 * @param servletContext the servlet context
	 * @return the context path
	 */
	private static String getContextPathForNewServletApi(
			ServletContext servletContext) {
		String contextPath = null;
		try {
			Method getServletContextPathMethod = servletContext.getClass().getMethod(GET_CONTEXT_PATH_METHOD, new Class[] {});
			contextPath = (String) getServletContextPathMethod.invoke(servletContext, (Object[])null);
		} catch (SecurityException e) {
			throw new BundlingProcessException(e);
		} catch (NoSuchMethodException e) {
			throw new BundlingProcessException(e);
		} catch (IllegalArgumentException e) {
			throw new BundlingProcessException(e);
		} catch (IllegalAccessException e) {
			throw new BundlingProcessException(e);
		} catch (InvocationTargetException e) {
			throw new BundlingProcessException(e);
		}
		return contextPath;
	}
}
