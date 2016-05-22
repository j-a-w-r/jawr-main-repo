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
package net.jawr.web.resource.bundle.generator;

import java.util.Comparator;

import net.jawr.web.resource.bundle.generator.resolver.ResolverComparator;

/**
 * This class defines the comparator for resource generator
 * 
 * @author Ibrahim Chaehoi
 */
public class GeneratorComparator implements Comparator<ResourceGenerator> {

	/**
	 * Constructor
	 */
	public GeneratorComparator() {
	
	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(ResourceGenerator o1, ResourceGenerator o2) {
		
		ResolverComparator rComparator = new ResolverComparator();
		return rComparator.compare(o1.getResolver(), o2.getResolver());
	}

}
