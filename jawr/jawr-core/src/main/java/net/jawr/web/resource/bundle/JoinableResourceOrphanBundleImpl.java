/**
 * Copyright 2012-2016 Ibrahim Chaehoi
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

import java.util.List;

import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.mappings.BundlePathMappingBuilder;
import net.jawr.web.resource.bundle.mappings.OrphanBundlePathMappingBuilder;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

/**
 * This class defines the joinable resource bundle for orphan
 * 
 * @author Ibrahim Chaehoi
 */
public class JoinableResourceOrphanBundleImpl extends JoinableResourceBundleImpl {

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the ID of this bundle
	 * @param name
	 *            Unique name for this bundle.
	 * @param fileExtension
	 *            File extensions for this bundle.
	 * @param inclusionPattern
	 *            Strategy for including this bundle.
	 * @param pathMappings
	 *            Set Strings representing the folders or files to include,
	 *            possibly with wildcards.
	 * @param resourceReaderHandler
	 *            Used to access the files and folders.
	 * @param generatorRegistry
	 *            the generator registry
	 */
	public JoinableResourceOrphanBundleImpl(String id, String name, String fileExtension,
			InclusionPattern inclusionPattern, List<String> pathMappings, ResourceReaderHandler resourceReaderHandler,
			GeneratorRegistry generatorRegistry) {
		super(id, name, null, fileExtension, inclusionPattern, pathMappings, resourceReaderHandler, generatorRegistry);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundleImpl#createBundlePathMappingBuilder(java.lang.String, net.jawr.web.resource.handler.reader.ResourceReaderHandler, net.jawr.web.resource.bundle.generator.GeneratorRegistry)
	 */
	@Override
	protected BundlePathMappingBuilder createBundlePathMappingBuilder(String fileExtension,
			ResourceReaderHandler resourceReaderHandler, GeneratorRegistry generatorRegistry) {
		
		return new OrphanBundlePathMappingBuilder(this, fileExtension, generatorRegistry, resourceReaderHandler);
	}

	
}
