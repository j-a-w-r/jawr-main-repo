/**
 * Copyright 2007-2016 Jordi Hernández Sellés, Ibrahim Chaehoi
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
 * duplicate mapping for a given path.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class DuplicateBundlePathException extends Exception {

	/** The serial version UID */
	private static final long serialVersionUID = 1848915729978060353L;

	/** The bundle path */
	private final String bundlePath;

	/**
	 * Constructor
	 * 
	 * @param bundleName
	 *            the bundle nam
	 */
	public DuplicateBundlePathException(String bundleName) {
		super();
		this.bundlePath = bundleName;
	}

	/**
	 * Returns the bundle path
	 * 
	 * @return the bundle path
	 */
	public String getBundlePath() {
		return bundlePath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return "At least two bundles share the pathname: " + getBundlePath();
	}
}
