/**
 * Copyright 2011 Ibrahim Chaehoi
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
package net.jawr.web.config;

/**
 * The interface for Jawr config properties resolver
 * 
 * @author Ibrahim Chaehoi
 */
public interface ConfigPropertyResolver {

	/**
	 * Resolve the property. If the property has not been resolved <b>null</b> should be return
	 * @param property the property to resolve
	 * @return the property value or null if not resolved
	 */
	public String resolve(String property);
	
}
