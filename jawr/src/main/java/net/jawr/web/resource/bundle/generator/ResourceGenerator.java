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
package net.jawr.web.resource.bundle.generator;

import java.io.Reader;

/**
 * A ResourceGenerator is acomponent that generates script or CSS dynamically, instead of reading 
 * it from the contents of a WAR file. It is used for creating resources programatically or to 
 * retrieve them from sources outside the scope of a WAR file. 
 * 
 * @author  Jordi Hernández Sellés, Ibrahim Chaehoi
 *
 */
public interface ResourceGenerator extends PrefixedResourceGenerator{

	/** The javascript debug path */
	public static final String JAVASCRIPT_DEBUGPATH = "/jawr_generator.js";
	
	/** The css debug path */
	public static final String CSS_DEBUGPATH = "/jawr_generator.css";
	
	/** The javascript debug path */
	public static final String IMG_DEBUGPATH = "/jawr_generator.jpg";
	
	/**
	 * Create a reader on a generated resource (any script not read from the war file 
	 * structure). 
	 * 
	 * @param path
	 * @param servletContext
	 * @param charset
	 * @return the reader for the generated resource
	 */
	public Reader createResource(GeneratorContext context);
	
	
	/**
	 * Returns the request path to use when generating a URL to this generator. 
	 * Normally it's OK to return either ResourceGenerator.JAVASCRIPT_DEBUGPATH 
	 * or  ResourceGenerator.CSS_DEBUGPATH, but this can be modified to suit an 
	 * application's path needs. Note that any prefix specified in the servlet mapping 
	 * does not need to be included in the returned value. 
	 *   
	 * @return the request path to use in debug mode
	 */
	public String getDebugModeRequestPath();
	
}
