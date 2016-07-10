/**
 * Copyright 2015-2016 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.mappings;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.iterator.BundlePath;

/**
 * This class defines the resource mapping for a bundle
 * 
 * @author Ibrahim Chaehoi
 */
public class BundlePathMapping {

	/** The resource bundle */
	private final JoinableResourceBundle bundle;

	/**
	 * The list of path mappings. It could contains directory mapping like
	 * 'myPath/**'
	 */
	private final List<PathMapping> pathMappings;

	/**
	 * The final item path list containing all the resource linked to this
	 * bundle
	 */
	private List<BundlePath> itemPathList;

	/**
	 * The list of file path mappings. It could only contains file mapping to
	 * resources used by the bundle
	 */
	private final List<FilePathMapping> filePathMappings;

	/**
	 * The list of file path mappings for linked resources. It could only
	 * contains file mapping to resources used by the bundle
	 */
	private List<FilePathMapping> linkedFilePathMappings;

	/**
	 * The final item path list containing all the resource linked to this
	 * bundle for debug mode
	 */
	private List<BundlePath> itemDebugPathList;

	/**
	 * The license path list
	 */
	private Set<String> licensesPathList;

	/**
	 * Constructor
	 * 
	 * @param bundle
	 *            the bundle
	 */
	public BundlePathMapping(JoinableResourceBundle bundle) {

		this.bundle = bundle;
		this.itemPathList = new CopyOnWriteArrayList<>();
		this.itemDebugPathList = new CopyOnWriteArrayList<>();
		this.licensesPathList = new HashSet<>();
		this.pathMappings = new CopyOnWriteArrayList<>();
		this.filePathMappings = new CopyOnWriteArrayList<>();
		this.linkedFilePathMappings = new CopyOnWriteArrayList<>();
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

	/**
	 * @return the filePathMappings
	 */
	public List<FilePathMapping> getFilePathMappings() {
		return filePathMappings;
	}

	/**
	 * @return the linkedFilePathMappings
	 */
	public List<FilePathMapping> getLinkedFilePathMappings() {
		return linkedFilePathMappings;
	}

	/**
	 * Sets the file path mappings for linked resources
	 * 
	 * @param mappings
	 *            the mapping to set
	 */
	public void setLinkedFilePathMappings(List<FilePathMapping> mappings) {
		this.linkedFilePathMappings = mappings;
	}
}