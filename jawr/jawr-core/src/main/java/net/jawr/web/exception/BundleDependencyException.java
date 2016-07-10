/**
 * Copyright 2009-2016 Ibrahim Chaehoi
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
 * Exception to throw during configuration process, if the mappings result in a
 * circular dependency definition for a given path.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class BundleDependencyException extends Exception {

	/** The serial version UID */
	private static final long serialVersionUID = 5973452875314885576L;

	/** The bundle name */
	private final String bundleName;

	/** The message */
	private final String message;

	/**
	 * Constructor
	 * 
	 * @param bundleName
	 *            the bundle name
	 * @param message
	 *            the message
	 */
	public BundleDependencyException(String bundleName, String message) {
		super();
		this.bundleName = bundleName;
		this.message = message;
	}

	/**
	 * Returns the bundle name
	 * 
	 * @return the bundle name
	 */
	public String getBundleName() {
		return bundleName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return "An error occured during the dependency resolution of the bundle '" + bundleName + "'. " + message;
	}
}
