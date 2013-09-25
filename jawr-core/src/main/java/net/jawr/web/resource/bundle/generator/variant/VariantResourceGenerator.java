/**
 * Copyright 2010-2012 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator.variant;

import java.util.Map;

import net.jawr.web.resource.bundle.generator.ResourceGenerator;
import net.jawr.web.resource.bundle.variant.VariantSet;

/**
 * This interface defines the interface for Resource generator which are able to generate resources with different variant.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public interface VariantResourceGenerator extends ResourceGenerator{

	/**
	 * Returns the map of available variant for a resource.
	 * The key of the map is the type of variant (for ex: locale, skin...)
	 * The values associated are the list of variant for the type. 
	 * @param resource the resource name
	 * @return the map of available variant for a resource 
	 */
	Map<String, VariantSet> getAvailableVariants(String resource);
	
}
