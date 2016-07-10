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

import java.util.List;
import java.util.Set;

import net.jawr.web.resource.bundle.CompositeResourceBundle;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.iterator.BundlePath;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

/**
 * The bundle path mapping builder for composite bundle
 * 
 * @author Ibrahim Chaehoi
 */
public class CompositeBundlePathMappingBuilder extends BundlePathMappingBuilder {

	/**
	 * Constructor
	 * 
	 * @param bundle
	 *            the composite resource bundle
	 * @param fileExtension
	 *            the file extension
	 * @param generatorRegistry
	 *            the generator registry
	 * @param rsHandler
	 *            the resource handler
	 */
	public CompositeBundlePathMappingBuilder(CompositeResourceBundle bundle, String fileExtension,
			GeneratorRegistry generatorRegistry, ResourceReaderHandler rsHandler) {
		super(bundle, fileExtension, generatorRegistry, rsHandler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.mappings.BundlePathMappingBuilder#build(java
	 * .util.List)
	 */
	@Override
	public BundlePathMapping build(List<String> strPathMappings) {

		BundlePathMapping bundlePathMapping = new BundlePathMapping(bundle);
		for (JoinableResourceBundle child : ((CompositeResourceBundle) bundle).getChildBundles()) {
			if (!child.getInclusionPattern().isIncludeOnlyOnDebug()) {
				bundlePathMapping.getItemPathList().addAll(child.getItemPathList());
				addFilePathMapping(bundlePathMapping, child.getItemPathList());
			}

			if (!child.getInclusionPattern().isExcludeOnDebug()) {
				bundlePathMapping.getItemDebugPathList().addAll(child.getItemDebugPathList());
				addFilePathMapping(bundlePathMapping, child.getItemDebugPathList());
			}
			bundlePathMapping.getLicensesPathList().addAll(child.getLicensesPathList());
			addFilePathMapping(bundlePathMapping, child.getLicensesPathList());
		}
		return bundlePathMapping;
	}

	/**
	 * Adds paths to the file path mapping
	 * 
	 * @param bundlePathMapping
	 *            the bundle path mapping
	 * @param paths
	 *            the paths to add
	 */
	private void addFilePathMapping(BundlePathMapping bundlePathMapping, Set<String> paths) {
		for (String path : paths) {
			addFilePathMapping(bundlePathMapping, path);
		}
	}

	/**
	 * Adds bundle paths to the file path mapping
	 * 
	 * @param bundlePathMapping
	 *            the bundle path mapping
	 * @param paths
	 *            the paths to add
	 */
	private void addFilePathMapping(BundlePathMapping bundlePathMapping, List<BundlePath> itemPathList) {
		for (BundlePath bundlePath : itemPathList) {
			addFilePathMapping(bundlePathMapping, bundlePath.getPath());
		}
	}

}
