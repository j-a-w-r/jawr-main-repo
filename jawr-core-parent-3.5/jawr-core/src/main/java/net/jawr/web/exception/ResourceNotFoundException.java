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
package net.jawr.web.exception;

/**
 * Exception thrown when trying to access a non existing resource, be it from the store or the servlet context. 
 * 
 * @author Jordi Hernández Sellés
 * @author ibrahim Chaehoi
 */
public class ResourceNotFoundException extends Exception {
	
	/** The serial version UID */
	private static final long serialVersionUID = -8792768175785512913L;

	/** The requested path */
	private String requestedPath;
	
	/**
	 * Constructor
	 * @param requestedPath the requested path
	 */
	public ResourceNotFoundException(String requestedPath) {
		super();
		this.requestedPath = requestedPath;
	}

	/**
	 * Returns the requested path
	 * @return teh requested path
	 */
	public String getRequestedPath() {
		return requestedPath;
	}

}
