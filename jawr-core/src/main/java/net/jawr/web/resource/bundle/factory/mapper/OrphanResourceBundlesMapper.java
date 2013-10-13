/**
 * Copyright 2007-2012 Jordi Hernández Sellés, Ibrahim Chaehoi
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.jawr.web.JawrConstant;
import net.jawr.web.exception.DuplicateBundlePathException;
import net.jawr.web.resource.bundle.InclusionPattern;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.JoinableResourceBundleImpl;
import net.jawr.web.resource.bundle.JoinableResourceOrphanBundleImpl;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Instances of this class will find all the resources which don't belong to any
 * defined bundle. Will return a mapping for each of them.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 * 
 */
public class OrphanResourceBundlesMapper {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(OrphanResourceBundlesMapper.class);

	/** The base directory */
	protected String baseDir;

	/** The resource handler */
	protected ResourceReaderHandler rsHandler;

	/** The generator registry */
	protected GeneratorRegistry generatorRegistry;

	/** The list of current bundles */
	protected List<JoinableResourceBundle> currentBundles;

	/** The resource file extension */
	protected String resourceExtension;

	/** The bundle mapping */
	private List<String> bundleMapping;

	/**
	 * Constructor
	 * 
	 * @param baseDir
	 *            the base directory of the resource mapper
	 * @param rsHandler
	 *            the resource handler
	 * @param generatorRegistry
	 *            the generator registry
	 * @param currentBundles
	 *            the list of current bundles
	 * @param resourceExtension
	 *            the resource file extension
	 */
	public OrphanResourceBundlesMapper(String baseDir,
			ResourceReaderHandler rsHandler,
			GeneratorRegistry generatorRegistry,
			List<JoinableResourceBundle> currentBundles,
			String resourceExtension) {

		this.baseDir = "/**";
		if (!"".equals(baseDir)) {
			if (generatorRegistry.isPathGenerated(baseDir)) {
				this.baseDir = PathNormalizer.normalizePath(baseDir) + "/**";
			} else if (!"/".equals(baseDir)) {
				this.baseDir = "/" + PathNormalizer.normalizePath(baseDir)
						+ "/**";
			}
		}

		this.rsHandler = rsHandler;
		this.generatorRegistry = generatorRegistry;
		this.currentBundles = new ArrayList<JoinableResourceBundle>();
		if (null != currentBundles)
			this.currentBundles.addAll(currentBundles);
		this.resourceExtension = resourceExtension;
		this.bundleMapping = new ArrayList<String>();
	}

	/**
	 * Scan all dirs starting at baseDir, and add each orphan resource to the
	 * resources map.
	 * 
	 * @return
	 */
	public List<String> getOrphansList() throws DuplicateBundlePathException {

		// Create a mapping for every resource available
		JoinableResourceBundleImpl tempBundle = new JoinableResourceOrphanBundleImpl(
				"orphansTemp", "orphansTemp", this.resourceExtension,
				new InclusionPattern(),
				Collections.singletonList(this.baseDir), rsHandler,
				generatorRegistry);

		// Add licenses
		Set<String> licensesPathList = tempBundle.getLicensesPathList();
		for (Iterator<String> it = licensesPathList.iterator(); it.hasNext();) {
			addFileIfNotMapped(it.next());
		}

		// Add resources
		List<String> allPaths = tempBundle.getItemPathList();
		for (Iterator<String> it = allPaths.iterator(); it.hasNext();) {
			addFileIfNotMapped(it.next());
		}
		return this.bundleMapping;
	}

	/**
	 * Determine wether a resource is already added to some bundle, add it to
	 * the list if it is not.
	 * 
	 * @param filePath
	 * @param currentMappedResources
	 */
	private void addFileIfNotMapped(String filePath)
			throws DuplicateBundlePathException {

		for (Iterator<JoinableResourceBundle> it = currentBundles.iterator(); it
				.hasNext();) {
			JoinableResourceBundle bundle = it.next();
			List<String> items = bundle.getItemPathList();
			List<String> itemsDebug = bundle.getItemDebugPathList();
			Set<String> licenses = bundle.getLicensesPathList();

			if (items.contains(filePath) || itemsDebug.contains(filePath))
				return;
			else if (licenses.contains(filePath))
				return;
			else if (filePath.equals(bundle.getId())) {
				Marker fatal = MarkerFactory.getMarker("FATAL");
				LOGGER.error(fatal,
						"Duplicate bundle id resulted from orphan mapping of:"
								+ filePath);
				throw new DuplicateBundlePathException(filePath);
			}
		}

		if (!filePath.startsWith(JawrConstant.WEB_INF_DIR_PREFIX)
				&& !filePath.startsWith(JawrConstant.META_INF_DIR_PREFIX)) {

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Adding orphan resource: " + filePath);

			// If we got here, the resource belongs to no other bundle.
			bundleMapping.add(filePath);
		}
	}

}
