/**
 * Copyright 2014-2016 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.iterator;

/**
 * This class defines the resource bundle path which will be rendered by the
 * BundleRenderer
 * 
 * @author ibrahim Chaehoi
 *
 */
public class BundlePath {

	/** The bundle prefix */
	private String bundlePrefix;

	/** The bundle path */
	private String path;

	/** The flag indicating if it's an external URL or not */
	private boolean externalURL;

	/**
	 * Constructor
	 * 
	 * @param bundlePrefix
	 *            the bundle prefix
	 * @param path
	 *            the bundle path, which is not a production URL
	 */
	public BundlePath(String bundlePrefix, String path) {
		this(bundlePrefix, path, false);
	}

	/**
	 * Constructor
	 * 
	 * @param prefix
	 *            the bundle prefix
	 * @param path
	 *            the bundle path
	 * @param isExternalURL
	 *            flag indicating if it's an external URL or not
	 */
	public BundlePath(String prefix, String path, boolean isExternalURL) {
		super();
		this.bundlePrefix = prefix;
		this.path = path;
		this.externalURL = isExternalURL;
	}

	/**
	 * Returns the bundle prefix
	 * 
	 * @return the bundle prefix
	 */
	public String getBundlePrefix() {
		return bundlePrefix;
	}

	/**
	 * Sets the bundle prefix
	 * 
	 * @param bundlePrefix
	 *            the bundle prefix to set
	 */
	public void setBundlePrefix(String bundlePrefix) {
		this.bundlePrefix = bundlePrefix;
	}

	/**
	 * Returns the bundle path
	 * 
	 * @return the bundle path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Sets the bundle path
	 * 
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Returns true if the path is the path for an external URL
	 * 
	 * @return true if the path is the path for a external URL
	 */
	public boolean isExternalURL() {
		return externalURL;
	}

	/**
	 * Sets the production URL
	 * 
	 * @param externalURL
	 *            the flag indicating if it's an external URL
	 */
	public void setExternalURL(boolean externalURL) {
		this.externalURL = externalURL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Bundle Path : [" + bundlePrefix + "; " + path + " ; "
				+ (externalURL ? "alternale external URL" : "Not external URL") + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bundlePrefix == null) ? 0 : bundlePrefix.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + (externalURL ? 1231 : 1237);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BundlePath other = (BundlePath) obj;
		if (bundlePrefix == null) {
			if (other.bundlePrefix != null)
				return false;
		} else if (!bundlePrefix.equals(other.bundlePrefix))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (externalURL != other.externalURL)
			return false;
		return true;
	}

}
