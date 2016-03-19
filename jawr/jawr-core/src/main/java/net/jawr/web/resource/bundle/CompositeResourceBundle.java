/**
 * Copyright 2007-2016 Jordi Hernández Sellés, Ibrahim Chaehoi
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

import java.util.Iterator;
import java.util.List;

import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.mappings.BundlePathMappingBuilder;
import net.jawr.web.resource.bundle.mappings.CompositeBundlePathMappingBuilder;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

/**
 * This class acts as a proxy for a group of bundles which are created
 * independently but share a common id and act as a single bundle in runtime. It
 * allows to join bundles which have different configuration, such as different
 * postprocessing filters.
 * 
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class CompositeResourceBundle extends JoinableResourceBundleImpl {

	/** The child bundles */
	private List<JoinableResourceBundle> childBundles;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the ID of this bundle
	 * @param name
	 *            Unique name for this bundle.
	 * @param childBundles
	 *            the childBundles
	 * @param inclusionPattern
	 *            Strategy for including this bundle.
	 * @param resourceHandler
	 *            Used to access the files and folders.
	 * @param bundlePrefix
	 *            the bundle prefix.
	 * @param fileExtension
	 *            File extensions for this bundle.
	 * @param generatorRegistry
	 *            the generator registry
	 */
	public CompositeResourceBundle(String id, String name, List<JoinableResourceBundle> childBundles,
			InclusionPattern inclusionPattern, ResourceReaderHandler resourceHandler, String bundlePrefix,
			String fileExtension, GeneratorRegistry generatorRegistry) {

		super(id, name, bundlePrefix, fileExtension, inclusionPattern, resourceHandler, generatorRegistry);

		this.childBundles = childBundles;
		this.bundlePathMappingBuilder = createBundlePathMappingBuilder(fileExtension, resourceHandler, generatorRegistry);
		this.bundlePathMapping = this.bundlePathMappingBuilder.build(null);
		
		for (Iterator<JoinableResourceBundle> it = this.childBundles.iterator(); it.hasNext();) {
			JoinableResourceBundle child = it.next();
			
			// If the child has no postprocessors, apply the composite's if any
			if (null == child.getBundlePostProcessor()) {
				child.setBundlePostProcessor(this.getBundlePostProcessor());
			}
			if (null == child.getUnitaryPostProcessor()) {
				child.setUnitaryPostProcessor(this.getUnitaryPostProcessor());
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundleImpl#createBundlePathMappingBuilder(java.lang.String, net.jawr.web.resource.handler.reader.ResourceReaderHandler, net.jawr.web.resource.bundle.generator.GeneratorRegistry)
	 */
	@Override
	protected BundlePathMappingBuilder createBundlePathMappingBuilder(String fileExtension, ResourceReaderHandler resourceReaderHandler, GeneratorRegistry generatorRegistry){
		return new CompositeBundlePathMappingBuilder(this, fileExtension, generatorRegistry, resourceReaderHandler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.JoinableResourceBundle#isComposite()
	 */
	public boolean isComposite() {
		return true;
	}

	/**
	 * @return List<JoinableResourceBundle> The bundles which are members of
	 *         this composite.
	 */
	public List<JoinableResourceBundle> getChildBundles() {
		return childBundles;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "CompositeResourceBundleImpl [id=" + getId() + ", name=" + getName() + "]";
	}
}
