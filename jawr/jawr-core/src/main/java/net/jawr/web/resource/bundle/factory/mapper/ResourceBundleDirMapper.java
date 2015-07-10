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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.jawr.web.exception.DuplicateBundlePathException;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to generate ResourceBundles by reading files under a certain
 * path. Each subdir will generate a RasourceBundle that includes every file and
 * directory below it.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 * 
 */
public class ResourceBundleDirMapper extends AbstractResourceMapper {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ResourceBundleDirMapper.class);

	/** The Set of path to exclude */
	private Set<String> excludedPaths;

	/**
	 * Constructor
	 * 
	 * @param baseDir
	 *            Root dir from which to define the paths.
	 * @param rsHandler
	 *            Resource handler to resolve the file structure
	 * @param currentBundles
	 *            Bundles created so far
	 * @param resourceExtension
	 *            Expected resource extension
	 * @param excludedPaths
	 *            Paths to exclude from the mappings.
	 */
	public ResourceBundleDirMapper(String baseDir,
			ResourceReaderHandler rsHandler,
			List<JoinableResourceBundle> currentBundles,
			String resourceExtension, Set<String> excludedPaths) {
		super(baseDir, rsHandler, currentBundles, resourceExtension);
		this.excludedPaths = initExcludedPathList(excludedPaths);
	}

	/**
	 * Determine which paths are to be excluded based on a set of path mappings
	 * from the configuration.
	 * 
	 * @param paths
	 *            the Set of path to exclude
	 * @return the Set of path to exclude
	 */
	private Set<String> initExcludedPathList(Set<String> paths) {
		Set<String> toExclude = new HashSet<String>();
		if (null == paths)
			return toExclude;

		for (Iterator<String> it = paths.iterator(); it.hasNext();) {
			String path = it.next();
			path = PathNormalizer.asPath(path);
			toExclude.add(path);
		}
		return toExclude;
	}

	/**
	 * Generates the resource bunles mapping expressions.
	 * 
	 * @return Map A map with the resource bundle key and the mapping for it as
	 *         a value.
	 */
	protected void addBundlesToMapping() throws DuplicateBundlePathException {
		Set<String> paths = rsHandler.getResourceNames(baseDir);

		for (Iterator<String> it = paths.iterator(); it.hasNext();) {
			String path = it.next();
			path = PathNormalizer.joinPaths(baseDir, path);
			if (!excludedPaths.contains(path) && rsHandler.isDirectory(path)) {
				String bundleKey = path + resourceExtension;
				addBundleToMap(bundleKey, path + "/**");
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Added [" + bundleKey + "] with value ["
							+ path + "/**] to a generated path list");
			}
		}
	}

}
