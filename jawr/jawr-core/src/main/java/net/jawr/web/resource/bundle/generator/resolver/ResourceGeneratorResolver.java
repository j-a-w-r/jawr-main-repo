/**
 * Copyright 2012-2016  Ibrahim Chaehoi
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
 * This interface defines the matcher which will be used to determine 
 * if the ResourceGenerator is responsible to handle a path
 * 
 * @author ibrahim Chaehoi
 *
 */
public interface ResourceGeneratorResolver {

	/**
	 * The resolver type (prefixed or suffixed)
	 */
	public enum ResolverType {
		PREFIXED,
		
		SUFFIXED
	}
	
	/**
	 * Returns the type of the resource generator (Prefixed or suffixed)
	 * 
	 * @return the type of the resource generator
	 */
	ResolverType getType();
	
	/**
	 * Checks if the path matches the ResourceGenerator
	 *  
	 * @param path the path
	 * 
	 * @return true if the path matches
	 */
	boolean matchPath(String path);
	
	/**
	 * Returns the resource path to be handled by the ResourceGenerator
	 *  
	 * @param path the requested path
	 * 
	 * @return the resource path
	 */
	String getResourcePath(String requestedPath);
	
	/**
	 * Checks if the matcher is same as the one passed in parameter
	 * @param matcher the matcher to test
	 * @return true if the matcher is same as the one passed in parameter
	 */
	boolean isSameAs(ResourceGeneratorResolver matcher);
	
}
