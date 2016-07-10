/**
 * Copyright 2009-2016 Ibrahim Chaehoi
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
 * The interface for ResourceGenerator which specify the debug path for the CDN.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public interface SpecificCDNDebugPathResourceGenerator extends ResourceGenerator {

	/**
	 * Returns the path to use when generating a resource for the "build time
	 * processor". The path should just take in account the parameter used.
	 * 
	 * @param parameter
	 *            the parameter
	 * @return the path to use when generating a resource for the "build time
	 *         processor".
	 */
	public String getDebugModeBuildTimeGenerationPath(String parameter);

}
