/**
 * Copyright 2013-2016 Ibrahim Chaehoi
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

import net.jawr.web.config.JawrConfig;

/**
 * The interface for the servletContext resource reader
 * 
 * @author ibrahim Chaehoi
 */
public interface ServletContextResourceReader extends TextResourceReader, StreamResourceReader, ResourceBrowser {

	/**
	 * Initializes the ServletContextResourceReader
	 * 
	 * @param context
	 *            the servlet context
	 * @param config
	 *            the Jawr config
	 */
	public void init(ServletContext context, JawrConfig config);

}
