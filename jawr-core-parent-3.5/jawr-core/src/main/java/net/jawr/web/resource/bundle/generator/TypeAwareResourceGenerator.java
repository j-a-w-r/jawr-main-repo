/**
 * Copyright 2009 Ibrahim Chaehoi
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

/**
 * This interface is implemented by all resource generator, which wants to be informed of 
 * the resource type which they are managing. This property will be set after the initialization
 * of the resource generator.
 * This interface is helpful for the generator, which are able to handle different resource type.
 * This interface will help them to determine which path must be used in debug mode.
 * 
 * For example : For a resource generator which is abl to handle Javascript files and CSS files.
 * if the type is "js" then the debugModePath will be "/jawr_generator.js"    
 * else if the type is "css" the debugModePath will be "/jawr_generator.css"
 *   
 * @author Ibrahim Chaehoi
 *
 */
public interface TypeAwareResourceGenerator extends InitializingResourceGenerator {

	/**
	 * Set the resource type managed by the resource generator.
	 * @param resourceType the resource type.
	 */
	public void setResourceType(String resourceType);
	
}
