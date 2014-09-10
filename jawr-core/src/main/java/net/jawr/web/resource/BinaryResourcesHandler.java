/**
 * Copyright 2009-2014 Ibrahim Chaehoi
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
package net.jawr.web.resource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.handler.bundle.ResourceBundleHandler;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

/**
 * This class defines the binary web resource handler.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class BinaryResourcesHandler {

	/** The binary web resource map */
	private final Map<String, String> binaryResourceMap = new ConcurrentHashMap<String, String>();
	
	/** The Jawr config */
	private final JawrConfig jawrConfig;
	
	/** The resource handler */
	private final ResourceReaderHandler rsHandler;
	
	/** The resource bundle handler */
	private final ResourceBundleHandler rsBundleHandler;
	
	/**
	 * Constructor
	 * @param config the Jawr config
	 * @param rsHandler the <tt>ResourceReaderHandler</tt>
	 * @param rsBundleHandler the <tt>ResourceBundleHandler</tt>
	 */
	public BinaryResourcesHandler(JawrConfig config, ResourceReaderHandler rsHandler, ResourceBundleHandler rsBundleHandler){
		this.jawrConfig = config;
		this.rsHandler = rsHandler;
		this.rsBundleHandler = rsBundleHandler;
	}
	
	/**
	 * @return the jawrConfig
	 */
	public JawrConfig getConfig() {
		return jawrConfig;
	}

	/**
	 * Returns the resource handler
	 * @return the resource handler
	 */
	public ResourceReaderHandler getRsReaderHandler() {
		return rsHandler;
	}
	
	/**
	 * Returns the resource handler
	 * @return the resource handler
	 */
	public ResourceBundleHandler getRsBundleHandler() {
		return rsBundleHandler;
	}
	
	/**
	 * Returns the image map
	 * @return the image Map
	 */
	public Map<String, String> getImageMap() {
		return binaryResourceMap;
	}

	/**
	 * Add an image mapping
	 * @param imgUrl the original url
	 * @param cacheUrl the cache url
	 */
	public void addMapping(String imgUrl, String cacheUrl){
		binaryResourceMap.put(imgUrl, cacheUrl);
	}
	
	/**
	 * Return the cache image URL
	 * @param imgUrl the image url
	 * @return the cache image URL
	 */
	public String getCacheUrl(String imgUrl){
		return binaryResourceMap.get(imgUrl);
	}
	
	/**
	 * Clears the image map 
	 */
	public void clear(){
	
		binaryResourceMap.clear();
	}

	/**
	 * Checks if the requested image is a valid one or not
	 * @param requestedPath the requested path
	 * @return true if the requested image is a valid one or not
	 */
	public boolean containsValidBundleHashcode(String requestedPath) {
		return binaryResourceMap.containsValue(requestedPath);
	}
	
}
