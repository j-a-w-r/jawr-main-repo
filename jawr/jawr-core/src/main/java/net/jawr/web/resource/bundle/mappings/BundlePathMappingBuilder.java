/**
 * Copyright 2016 Ibrahim Chaehoi
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

import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.iterator.BundlePath;
import net.jawr.web.resource.bundle.sorting.SortFileParser;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

/**
 * This class is responsible for handling the resource bundle path mapping
 * 
 * @author Ibrahim Chaehoi
 */
public class BundlePathMappingBuilder {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(BundlePathMappingBuilder.class);

	/** The licencing file name */
	public static final String LICENSES_FILENAME = ".license";

	/** The sorting file name */
	public static final String SORT_FILE_NAME = ".sorting";

	/** The bundle */
	protected JoinableResourceBundle bundle;

	/** The generator registry */
	protected GeneratorRegistry generatorRegistry;

	/** The resource reader handler */
	protected ResourceReaderHandler resourceReaderHandler;

	/** The file extensions of the bundle */
	protected String fileExtension;

	/** The list of path mappings */
	private List<String> strPathMappings;

	/**
	 * Constructor
	 * 
	 * @param bundle
	 *            the resource bundle
	 * @param fileExtension
	 *            the file extension
	 * @param generatorRegistry
	 *            the generator registry
	 * @param rsHandler
	 *            the resource handler
	 */
	public BundlePathMappingBuilder(JoinableResourceBundle bundle, String fileExtension,
			GeneratorRegistry generatorRegistry, ResourceReaderHandler rsHandler) {
		this.bundle = bundle;
		this.generatorRegistry = generatorRegistry;
		this.resourceReaderHandler = rsHandler;

		if (fileExtension != null && fileExtension.length() > 0 && fileExtension.charAt(0) != '.') {
			this.fileExtension = "." + fileExtension;
		} else {
			this.fileExtension = fileExtension;
		}
	}

	/**
	 * Detects all files that belong to the bundle and adds them to the bundle
	 * path mapping.
	 * 
	 * @param pathMappings
	 *            the list of path mappings
	 * @return the bundlePathMapping
	 */
	public BundlePathMapping build(List<String> strPathMappings) {
		this.strPathMappings = strPathMappings;
		return build();
	}

	/**
	 * Detects all files that belong to the bundle and adds them to the bundle
	 * path mapping.
	 * 
	 * @return the bundlePathMapping
	 */
	public BundlePathMapping build() {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Creating bundle path List for " + this.bundle.getId());
		}

		BundlePathMapping bundlePathMapping = new BundlePathMapping(this.bundle);
		bundlePathMapping.setPathMappings(strPathMappings);
		List<PathMapping> pathMappings = bundlePathMapping.getPathMappings();
		if (pathMappings != null) {
			for (Iterator<PathMapping> it = pathMappings.iterator(); it.hasNext();) {
				PathMapping pathMapping = it.next();
				boolean isGeneratedPath = generatorRegistry.isPathGenerated(pathMapping.getPath());

				// Handle generated resources
				// path ends in /, the folder is included without subfolders
				if (pathMapping.isDirectory()) {
					addItemsFromDir(bundlePathMapping, pathMapping, false);
				}
				// path ends in /, the folder is included with all subfolders
				else if (pathMapping.isRecursive()) {
					addItemsFromDir(bundlePathMapping, pathMapping, true);
				} else if (pathMapping.getPath().endsWith(fileExtension)) {
					addPathMapping(bundlePathMapping, asPath(pathMapping.getPath(), isGeneratedPath));
				} else if (generatorRegistry.isPathGenerated(pathMapping.getPath())) {
					addPathMapping(bundlePathMapping, pathMapping.getPath());
				} else if (pathMapping.getPath().endsWith(LICENSES_FILENAME)) {
					bundlePathMapping.getLicensesPathList().add(asPath(pathMapping.getPath(), isGeneratedPath));
				} else
					throw new BundlingProcessException("Wrong mapping [" + pathMapping + "] for bundle ["
							+ this.bundle.getName() + "]. Please check configuration. ");
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Finished creating bundle path List for " + this.bundle.getId());
		}

		return bundlePathMapping;
	}

	/**
	 * Adds a path mapping to the bundle
	 * 
	 * @param pathMapping
	 *            the path mapping to add
	 */
	private void addPathMapping(BundlePathMapping bundlePathMapping, String pathMapping) {

		addFilePathMapping(bundlePathMapping, pathMapping);

		if (!bundle.getInclusionPattern().isIncludeOnlyOnDebug()) {
			bundlePathMapping.getItemPathList().add(new BundlePath(bundle.getBundlePrefix(), pathMapping));
		}

		if (!bundle.getInclusionPattern().isExcludeOnDebug()) {
			bundlePathMapping.getItemDebugPathList().add(new BundlePath(bundle.getBundlePrefix(), pathMapping));
		}

	}

	/**
	 * Adds the path mapping to the file path mapping
	 * 
	 * @param bundlePathMapping
	 *            the bundle path mapping
	 * @param pathMapping
	 *            the path mapping to add
	 */
	protected void addFilePathMapping(BundlePathMapping bundlePathMapping, String pathMapping) {
		long timestamp = 0;
		String filePath = resourceReaderHandler.getFilePath(pathMapping);
		if (filePath != null) {
			timestamp = resourceReaderHandler.getLastModified(filePath);
			List<FilePathMapping> filePathMappings = bundlePathMapping.getFilePathMappings();
			boolean found = false;
			for (FilePathMapping filePathMapping : filePathMappings) {
				if (filePathMapping.getPath().equals(filePath)) {
					found = true;
					break;
				}
			}
			if (!found) {
				filePathMappings.add(new FilePathMapping(bundle, filePath, timestamp));
			}
		}

	}

	/**
	 * Adds all the resources within a path to the item path list.
	 * 
	 * @param bundlePathMapping
	 *            the bundle path mapping
	 * @param dirName
	 *            the directory name
	 * @param addSubDirs
	 *            boolean If subfolders will be included. In such case, every
	 *            folder below the path is included.
	 */
	protected void addItemsFromDir(BundlePathMapping bundlePathMapping, PathMapping dirName, boolean addSubDirs) {

		Set<String> resources = resourceReaderHandler.getResourceNames(dirName.getPath());
		boolean isGeneratedPath = generatorRegistry.isPathGenerated(dirName.getPath());
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Adding " + resources.size() + " resources from path [" + dirName.getPath() + "] to bundle "
					+ bundle.getId());
		}

		// If the directory contains a sorting file, it is used to order the
		// resources.
		if (resources.contains(SORT_FILE_NAME) || resources.contains("/" + SORT_FILE_NAME)) {

			String sortFilePath = joinPaths(dirName.getPath(), SORT_FILE_NAME, isGeneratedPath);

			addFilePathMapping(bundlePathMapping, sortFilePath);

			Reader reader;
			try {
				reader = resourceReaderHandler.getResource(bundle, sortFilePath);
			} catch (ResourceNotFoundException e) {
				throw new BundlingProcessException(
						"Unexpected ResourceNotFoundException when reading a sorting file[" + sortFilePath + "]", e);
			}

			SortFileParser parser = new SortFileParser(reader, resources, dirName.getPath());

			List<String> sortedResources = parser.getSortedResources();
			for (Iterator<String> it = sortedResources.iterator(); it.hasNext();) {
				String resourceName = (String) it.next();

				// Add subfolders or files
				if (resourceName.endsWith(fileExtension) || generatorRegistry.isPathGenerated(resourceName)) {
					addPathMapping(bundlePathMapping, asPath(resourceName, isGeneratedPath));

					if (LOGGER.isDebugEnabled())
						LOGGER.debug("Added to item path list from the sorting file:" + resourceName);
				} else if (dirName.isRecursive() && resourceReaderHandler.isDirectory(resourceName))
					addItemsFromDir(bundlePathMapping, new PathMapping(bundle, resourceName + "/**"), true);
			}
		}

		// Add licenses file
		if (resources.contains(LICENSES_FILENAME) || resources.contains("/" + LICENSES_FILENAME)) {
			String licencePath = joinPaths(dirName.getPath(), LICENSES_FILENAME, isGeneratedPath);
			bundlePathMapping.getLicensesPathList().add(licencePath);
			addFilePathMapping(bundlePathMapping, licencePath);
		}

		// Add remaining resources (remaining after sorting, or all if no sort
		// file present)
		List<String> folders = new ArrayList<String>();
		for (Iterator<String> it = resources.iterator(); it.hasNext();) {
			String resourceName = (String) it.next();
			String resourcePath = joinPaths(dirName.getPath(), resourceName, isGeneratedPath);

			boolean resourceIsDir = resourceReaderHandler.isDirectory(resourcePath);
			if (addSubDirs && resourceIsDir) {
				folders.add(resourceName);
			} else if (resourcePath.endsWith(fileExtension)
					|| (generatorRegistry.isPathGenerated(resourcePath) && !resourceIsDir)) {
				addPathMapping(bundlePathMapping, asPath(resourcePath, isGeneratedPath));

				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Added to item path list:" + asPath(resourcePath, isGeneratedPath));
			}
		}

		// Add subfolders if requested. Subfolders are added last unless
		// specified in sorting file.
		if (addSubDirs) {
			for (Iterator<String> it = folders.iterator(); it.hasNext();) {
				String folderName = joinPaths(dirName.getPath(), it.next(), isGeneratedPath);
				addItemsFromDir(bundlePathMapping, new PathMapping(bundle, folderName + "/**"), true);
			}
		}
	}

	/**
	 * Normalizes a path and adds a separator at its start, if it's not a
	 * generated resource.
	 * 
	 * @param path
	 *            the path
	 * @param generatedResource
	 *            the flag indicating if the resource has been generated
	 * @return the normalized path
	 */
	private String asPath(String path, boolean generatedResource) {

		String result = path;
		if (!generatedResource) {
			result = PathNormalizer.asPath(path);
		}
		return result;
	}

	/**
	 * Normalizes two paths and joins them as a single path.
	 * 
	 * @param prefix
	 *            the path prefix
	 * @param path
	 *            the path
	 * @param generatedResource
	 *            the flag indicating if the resource has been generated
	 * @return the normalized path
	 */
	private String joinPaths(String dirName, String folderName, boolean generatedResource) {

		return PathNormalizer.joinPaths(dirName, folderName, generatedResource);
	}

}
