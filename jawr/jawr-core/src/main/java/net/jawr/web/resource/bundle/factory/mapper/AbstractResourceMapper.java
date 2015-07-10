/**
 * Copyright 2007-2009 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.factory.mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.jawr.web.exception.DuplicateBundlePathException;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Base class to implement map-based automatic bundles generators. The generated
 * bundles are added to a Map instance in which the keys are bundles ids and the
 * values are the bundles.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 * 
 */
public abstract class AbstractResourceMapper {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AbstractResourceMapper.class);

	/** The base directory */
	protected String baseDir;

	/** The resource handler */
	protected ResourceReaderHandler rsHandler;

	/** The list of current bundles */
	protected List<JoinableResourceBundle> currentBundles;

	/** The resource extension */
	protected String resourceExtension;

	/** The bundle mapping */
	private Map<String, String> bundleMapping;

	/**
	 * Constructor
	 * 
	 * @param baseDir
	 *            the base directory of the resource mapper
	 * @param rsHandler
	 *            the resource handler
	 * @param currentBundles
	 *            the list of current bundles
	 * @param resourceExtension
	 *            the resource file extension
	 */
	public AbstractResourceMapper(String baseDir,
			ResourceReaderHandler rsHandler,
			List<JoinableResourceBundle> currentBundles,
			String resourceExtension) {
		super();
		this.baseDir = baseDir;
		this.rsHandler = rsHandler;
		this.currentBundles = new ArrayList<JoinableResourceBundle>();
		if (null != currentBundles)
			this.currentBundles.addAll(currentBundles);
		this.resourceExtension = resourceExtension;
		this.bundleMapping = new HashMap<String, String>();
	}

	/**
	 * Find the required files to add to the mapping. Subclasses must use the
	 * addBundleToMap method.
	 * 
	 * @throws DuplicateBundlePathException
	 *             if we try to add a bundle with a name, which already exists.
	 */
	protected abstract void addBundlesToMapping()
			throws DuplicateBundlePathException;

	public final Map<String, String> getBundleMapping()
			throws DuplicateBundlePathException {
		addBundlesToMapping();
		return bundleMapping;
	}

	/**
	 * Add a bundle and its mapping to the resulting Map.
	 * 
	 * @param bundleId
	 *            the bundle Id
	 * @param mapping
	 *            the mapping
	 * @throws DuplicateBundlePathException
	 *             if we try to add a bundle with a name, which already exists.
	 */
	protected final void addBundleToMap(String bundleId, String mapping)
			throws DuplicateBundlePathException {

		for (Iterator<JoinableResourceBundle> it = currentBundles.iterator(); it
				.hasNext();) {
			JoinableResourceBundle bundle = it.next();
			if (bundleId.equals(bundle.getId())
					|| this.bundleMapping.containsKey(bundleId)) {
				Marker fatal = MarkerFactory.getMarker("FATAL");
				LOGGER.error(fatal, "Duplicate bundle id resulted from mapping:"
						+ bundleId);
				throw new DuplicateBundlePathException(bundleId);
			}
		}

		bundleMapping.put(bundleId, mapping);
	}

}
