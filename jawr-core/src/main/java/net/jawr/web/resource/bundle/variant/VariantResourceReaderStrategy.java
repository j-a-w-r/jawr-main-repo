/**
 * Copyright 2010 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.variant;

import java.util.Map;

import net.jawr.web.resource.bundle.generator.GeneratorContext;

/**
 * This interface is implemented by object which are able to define a fallback strategy 
 * for the variant resource provider.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public interface VariantResourceReaderStrategy {

	/**
	 * Initialize the variant resource provider strategy
	 * @param context the generator context
	 * @param variantsSetMap the variant set map for the current context path
	 */
	void initVariantProviderStrategy(GeneratorContext context, Map<String, VariantSet> variantsSetMap);
	
	/**
	 * Returns the new variant map combination to use.
	 * If the map returned is null, it means that no more strategy is available
	 * @return the new variant map combination to use
	 */
	Map<String, String> nextVariantMapConbination();
	
}
