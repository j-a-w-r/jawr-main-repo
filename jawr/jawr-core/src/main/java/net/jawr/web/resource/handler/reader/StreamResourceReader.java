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

import java.io.InputStream;


/**
 * This interface is implemented by objects which can read Stream resources.
 * 
 * @author Ibrahim Chaehoi
 */
public interface StreamResourceReader extends ResourceReader {

	/**
	 * Retrieves the input stream of a resource defined in the web application. 
	 * @param resourceName the name of the resource.  
	 * @return a input stream of the resource
	 */
	public InputStream getResourceAsStream(String resourceName);
	
	/**
	 * Retrieves the input stream of a resource defined in the web application. 
	 * @param resourceName the name of the resource.  
	 * @param processingBundle the flag indicating that we are currently processing the bundles
	 * @return a input stream of the resource
	 */
	public InputStream getResourceAsStream(String resourceName, boolean processingBundle);
	
	
}
