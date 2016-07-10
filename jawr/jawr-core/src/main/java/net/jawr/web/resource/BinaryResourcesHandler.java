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
package net.jawr.web.resource;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.CheckSumUtils;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.handler.BundleHashcodeType;
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
	private final Map<String, String> binaryResourcePathMap = new ConcurrentHashMap<>();

	/** The Jawr config */
	private final JawrConfig jawrConfig;

	/** The resource handler */
	private final ResourceReaderHandler rsHandler;

	/** The resource bundle handler */
	private final ResourceBundleHandler rsBundleHandler;

	/**
	 * Constructor
	 * 
	 * @param config
	 *            the Jawr config
	 * @param rsHandler
	 *            the <tt>ResourceReaderHandler</tt>
	 * @param rsBundleHandler
	 *            the <tt>ResourceBundleHandler</tt>
	 */
	public BinaryResourcesHandler(JawrConfig config, ResourceReaderHandler rsHandler,
			ResourceBundleHandler rsBundleHandler) {
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
	 * 
	 * @return the resource handler
	 */
	public ResourceReaderHandler getRsReaderHandler() {
		return rsHandler;
	}

	/**
	 * Returns the resource handler
	 * 
	 * @return the resource handler
	 */
	public ResourceBundleHandler getRsBundleHandler() {
		return rsBundleHandler;
	}

	/**
	 * Returns the binary map
	 * 
	 * @return the binary Map
	 */
	public Map<String, String> getBinaryPathMap() {
		return binaryResourcePathMap;
	}

	/**
	 * Add a binary mapping
	 * 
	 * @param binaryUrl
	 *            the original url
	 * @param cacheUrl
	 *            the cache url
	 */
	public void addMapping(String binaryUrl, String cacheUrl) {
		binaryResourcePathMap.put(binaryUrl, cacheUrl);
	}

	/**
	 * Return the cache image URL
	 * 
	 * @param binaryUrl
	 *            the binary url
	 * @return the cache image URL
	 */
	public String getCacheUrl(String binaryUrl) {
		return binaryResourcePathMap.get(binaryUrl);
	}

	/**
	 * Clears the binary map
	 */
	public void clear() {

		binaryResourcePathMap.clear();
	}

	/**
	 * Checks the bundle hashcode type of the requested binary resource
	 * 
	 * @param requestedPath
	 *            the requested path
	 * @return true if the requested image is a valid one or not
	 */
	public BundleHashcodeType getBundleHashcodeType(String requestedPath) {

		if (binaryResourcePathMap.containsValue(requestedPath)) {
			return BundleHashcodeType.VALID_HASHCODE;
		}

		BundleHashcodeType bundleHashcodeType = BundleHashcodeType.UNKNOW_BUNDLE;
		String[] resourceInfo = PathNormalizer.extractBinaryResourceInfo(requestedPath);
		String binaryRequest = resourceInfo[0];
		if (resourceInfo[1] != null) { // an hashcode is defined in the path
			try {
				String cacheBustedPath = CheckSumUtils.getCacheBustedUrl(binaryRequest, getRsReaderHandler(),
						jawrConfig);
				addMapping(binaryRequest, cacheBustedPath);

				if (requestedPath.equals(cacheBustedPath)) {
					bundleHashcodeType = BundleHashcodeType.VALID_HASHCODE;
				} else {
					bundleHashcodeType = BundleHashcodeType.INVALID_HASHCODE;
				}
			} catch (IOException | ResourceNotFoundException e) {
				// Nothing to do
			}

		}
		return bundleHashcodeType;
	}

}
