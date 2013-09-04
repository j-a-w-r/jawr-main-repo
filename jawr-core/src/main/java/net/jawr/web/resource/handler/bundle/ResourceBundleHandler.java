/**
 * Copyright 2009-2011 Ibrahim Chaehoi
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
package net.jawr.web.resource.handler.bundle;

import java.io.InputStream;
import java.io.Reader;
import java.nio.channels.ReadableByteChannel;
import java.util.Properties;

import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.JoinableResourceBundleContent;

/**
 * This interface is implemented by the object which handle resource bundle.
 *   
 * @author Ibrahim Chaehoi
 *
 */
public interface ResourceBundleHandler {

	/**
	 * Returns the resource type managed by the resource handler
	 * @return the resource type
	 */
	public String getResourceType();
	
	/**
	 * Stores a collected group of resources with the specified name. 
	 * Creates a text version, a gzipped binary version. 
	 * @param bundleName the bundle name.
	 * @param bundleResources the bundle resources
	 */
	public void storeBundle(String bundleName,
			JoinableResourceBundleContent bundleResourcesContent);
	
	/**
	 * Retrieves a reader for a bundle from the store. 
	 * @param bundleName the bundle name.
	 * @return a reader for a bundle from the store. 
	 */
	public Reader getResourceBundleReader(String bundleName) throws ResourceNotFoundException;
	
	/**
	 * Retrieves ReadableByteChannel on a resource bundle.
	 * @param bundleName the bundle name
	 * @return ReadableByteChannel channel to read the file where the bundle is stored. 
	 */
	public ReadableByteChannel getResourceBundleChannel(String bundleName) throws ResourceNotFoundException;
	
	/**
	 * Retrieves the input stream of a resource bundle. 
	 * @param bundleName the bundle name
	 * @return a input stream of the resource bundle
	 */
	public InputStream getResourceBundleAsStream(String bundleName) throws ResourceNotFoundException;
	
	/**
	 * Checks if the mapping file exists in the working directory 
	 * @return true if the mapping file exists in the working directory 
	 */
	public boolean isExistingMappingFile();
	
	/**
	 * Returns the jawr bundle mapping from the working directory. 
	 * @return the jawr bundle mapping.
	 */
	public Properties getJawrBundleMapping();
	
	/**
	 * Store the bundle mapping.
	 * @param bundleMapping the bundle mapping to store
	 */
	public void storeJawrBundleMapping(Properties bundleMapping);
	
	/**
     * Returns the path of the directory containing the generated text bundles 
     * @return the path of the directory containing the generated text bundles
     */
    public String getBundleTextDirPath();
}
