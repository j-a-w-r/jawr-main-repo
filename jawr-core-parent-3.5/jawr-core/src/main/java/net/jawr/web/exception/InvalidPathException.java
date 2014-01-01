/**
 * Copyright 2007 Jordi Hernández Sellés
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
 * An exception to be thrown if an invalid path is requested from the filesystem. 
 * 
 * @author Jordi Hernández Sellés
 *
 */
public class InvalidPathException extends RuntimeException {
	
	private String invalidPath;

	public InvalidPathException(String invalidPath) {
		super();
		this.invalidPath = invalidPath;
	}

	public String getMessage() {
		return "An invalid path mapping was used. The resulting path [" + invalidPath +"] does not exist";
	}

	private static final long serialVersionUID = -2061741852994690500L;
	
}
