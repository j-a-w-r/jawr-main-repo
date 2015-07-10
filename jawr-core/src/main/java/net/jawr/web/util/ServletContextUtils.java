/**
 * Copyright 2009-2014 Ibrahim Chaehoi
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

import javax.servlet.ServletContext;

/**
 * Utility class for ServeltContext
 * 
 * @author Ibrahim Chaehoi
 */
public class ServletContextUtils {

	/**
	 * The default context path used for the application if no context oath is
	 * defined
	 */
	public static final String DEFAULT_CONTEXT_PATH = "/default";

	/**
	 * Returns the context path associated to the servlet context
	 * 
	 * @param servletContext
	 *            the servlet context
	 * @return the context path associated to the servlet context
	 * @throws Exception
	 *             if an exception occurs
	 */
	public static String getContextPath(ServletContext servletContext) {
		String contextPath = DEFAULT_CONTEXT_PATH;

		// Get the context path
		if (servletContext != null) {
			contextPath = servletContext.getContextPath();
			if(StringUtils.isEmpty(contextPath)){
				contextPath = DEFAULT_CONTEXT_PATH;
			}
		}
			
		return contextPath;
	}
	
}
