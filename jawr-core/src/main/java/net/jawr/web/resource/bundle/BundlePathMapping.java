/**
 * Copyright 2015 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import net.jawr.web.resource.bundle.iterator.BundlePath;

/**
 * This class defines the resource mapping for a bundle
 * 
 * @author Ibrahim Chaehoi
 */
public class BundlePathMapping {

	/** The resource bundle */
	private JoinableResourceBundle bundle;

	/**
	 * The list of path mappings. It could contains directory mapping like
	 * 'myPath/**'
	 */
	private List<PathMapping> pathMappings;
	/**
	 * The final item path list containing all the resource linked to this
	 * bundle
	 */
	private List<BundlePath> itemPathList;
	/**
	 * The final item path list containing all the resource linked to this
	 * bundle for debug mode
	 */
	private List<BundlePath> itemDebugPathList;

	/** The license path list */
	private Set<String> licensesPathList;

	/**
	 * Constructor
	 */
	public BundlePathMapping(JoinableResourceBundle bundle) {

		this.bundle = bundle;
		this.itemPathList = new CopyOnWriteArrayList<BundlePath>();
		this.itemDebugPathList = new CopyOnWriteArrayList<BundlePath>();
		this.licensesPathList = new HashSet<String>();
		this.pathMappings = new CopyOnWriteArrayList<PathMapping>();
	}

	/**
	 * @return the pathMappings
	 */
	public List<PathMapping> getPathMappings() {
		return pathMappings;
	}

	/**
	 * Sets the path mapping
	 * 
	 * @param pathMappings
	 *            the pathMappings to set
	 */
	public void setPathMappings(List<String> pathMappings) {

		this.pathMappings.clear();
		if (pathMappings != null) {
			for (String mapping : pathMappings) {
				this.pathMappings.add(new PathMapping(bundle, mapping));
			}
		}

	}

	/**
	 * @return the itemPathList
	 */
	public List<BundlePath> getItemPathList() {
		return itemPathList;
	}

	/**
	 * @param itemPathList
	 *            the itemPathList to set
	 */
	public void setItemPathList(List<BundlePath> itemPathList) {
		this.itemPathList = itemPathList;
	}

	/**
	 * @return the itemDebugPathList
	 */
	public List<BundlePath> getItemDebugPathList() {
		return itemDebugPathList;
	}

	/**
	 * @param itemDebugPathList
	 *            the itemDebugPathList to set
	 */
	public void setItemDebugPathList(List<BundlePath> itemDebugPathList) {
		this.itemDebugPathList = itemDebugPathList;
	}

	/**
	 * @return the licensesPathList
	 */
	public Set<String> getLicensesPathList() {
		return licensesPathList;
	}

	/**
	 * @param licensesPathList
	 *            the licensesPathList to set
	 */
	public void setLicensesPathList(Set<String> licensesPathList) {
		this.licensesPathList = licensesPathList;
	}
}