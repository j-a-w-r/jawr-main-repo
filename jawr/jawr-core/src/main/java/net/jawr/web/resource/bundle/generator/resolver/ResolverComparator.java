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
package net.jawr.web.resource.bundle.generator.resolver;

import static net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver.ResolverType.PREFIXED;
import static net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver.ResolverType.SUFFIXED;

import java.util.Comparator;

/**
 * The resolver comparator
 * 
 * @author Ibrahim Chaehoi
 */
public class ResolverComparator implements Comparator<ResourceGeneratorResolver> {

	/**
	 * Constructor
	 */
	public ResolverComparator() {

	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(ResourceGeneratorResolver o1, ResourceGeneratorResolver o2) {
		
		int result = 0;
		if(o1.getType().equals(SUFFIXED) && o2.getType().equals(PREFIXED)){
			result = -1;
		}else if(o1.getType().equals(PREFIXED) && o2.getType().equals(SUFFIXED)){
			result = 1;
		}
		
		return result;
	}

}
