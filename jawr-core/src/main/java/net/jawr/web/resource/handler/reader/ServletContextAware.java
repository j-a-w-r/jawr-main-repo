/**
 * Copyright 2009 Ibrahim Chaehoi
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
package net.jawr.web.resource.handler.reader;

import javax.servlet.ServletContext;

/**
 * This interface defines the interface to be implemented by any object that
 * wants to be notified of the ServletContext that it runs in.
 * 
 * @author Ibrahim Chaehoi
 */
public interface ServletContextAware {

	/**
	 * Set the ServletContext that this object runs in.
	 * 
	 * @param servletContext
	 *            ServletContext object to be used by this object
	 */
	void setServletContext(ServletContext servletContext);

}
