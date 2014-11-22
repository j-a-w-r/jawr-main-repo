/**
 * Copyright 2012  Ibrahim Chaehoi
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


/**
 * This class defines the factory for prefix and suffix resolver
 * 
 * @author ibrahim Chaehoi
 */
public class ResourceGeneratorResolverFactory {

	/**
	 * Create the prefix generator resolver 
	 * @param prefix the prefix
	 * @return the prefix generator resolver
	 */
	public static ResourceGeneratorResolver createPrefixResolver(String prefix){
		return new PrefixedPathResolver(prefix);
	}
	
	/**
	 * Create the suffix generator resolver 
	 * @param suffix the prefix
	 * @return the suffix generator resolver
	 */
	public static ResourceGeneratorResolver createSuffixResolver(String suffix){
		return new SuffixedPathResolver(suffix);
	}
}
