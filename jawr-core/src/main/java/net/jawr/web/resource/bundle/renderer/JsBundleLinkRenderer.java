/**
 * Copyright 2013 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.renderer;

import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;

/**
 * The interface of JS bundle renderer. 
 * 
 * @author Ibrahim Chaehoi
 */
public interface JsBundleLinkRenderer extends BundleRenderer {

	/**
	 * Initializes the JS Bundle renderer
	 * @param bundler the resource bundle handler
	 * @param useRandomParam the flag indicating if it should use random param
	 * @param async the flag indicating if the link has an async attribute
	 * @param defer the flag indicating if the link has a deferred attribute
	 */
	public void init(ResourceBundlesHandler bundler, Boolean useRandomParam, Boolean async, Boolean defer);
	
}
