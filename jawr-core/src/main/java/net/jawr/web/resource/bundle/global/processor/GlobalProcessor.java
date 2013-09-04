/**
 * Copyright 2009-2012 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.global.processor;

import java.util.List;

import net.jawr.web.resource.bundle.JoinableResourceBundle;

/**
 * This interface defines the interface for the global preprocessor.
 * 
 * @author Ibrahim Chaehoi
 */
public interface GlobalProcessor<T extends AbstractGlobalProcessingContext> {

	/**
	 * Process the bundles for a type of resources.
	 *  
	 * @param ctx the processing context
	 * @param bundles the list of bundles to process.
	 */
	public void processBundles(T ctx, List<JoinableResourceBundle> bundles);
	
}
