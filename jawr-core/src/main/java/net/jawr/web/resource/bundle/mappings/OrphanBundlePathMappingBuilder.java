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

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

/**
 * The bundle path mapping builder for orphan bundle
 * 
 * @author Ibrahim Chaehoi
 */
public class OrphanBundlePathMappingBuilder extends BundlePathMappingBuilder {

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
	 *            the resource handker
	 */
	public OrphanBundlePathMappingBuilder(JoinableResourceBundle bundle, String fileExtension,
			GeneratorRegistry generatorRegistry, ResourceReaderHandler rsHandler) {
		super(bundle, fileExtension, generatorRegistry, rsHandler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.JoinableResourceBundleImpl#addItemsFromDir(
	 * java.lang.String, boolean)
	 */
	@Override
	protected void addItemsFromDir(BundlePathMapping bundlePathMapping, PathMapping dir, boolean addSubDirs) {
		String dirName = dir.getPath();

		if (!dirName.startsWith(JawrConstant.WEB_INF_DIR) && !dirName.startsWith(JawrConstant.META_INF_DIR)) {
			super.addItemsFromDir(bundlePathMapping, dir, addSubDirs);
		}
	}

}
