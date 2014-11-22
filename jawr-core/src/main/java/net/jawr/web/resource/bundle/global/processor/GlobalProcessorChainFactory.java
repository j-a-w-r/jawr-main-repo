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
package net.jawr.web.resource.bundle.global.processor;

import java.util.Map;

import net.jawr.web.resource.bundle.global.processor.GlobalProcessor;

/**
 * Interface for a factory of chained preprocessor objects. It is meant to ease 
 * configuration implementations. 
 * 
 * @author Ibrahim Chaehoi
 *
 */
public interface GlobalProcessorChainFactory<T extends AbstractGlobalProcessingContext> {

	/**
	 * Builds the default chain of processors for resources, be it javascript or css ones. 
	 * @return the default chain of processors
	 */
	public abstract GlobalProcessor<T> buildDefaultProcessorChain();

	/**
	 * Builds a chain of processors based on a comma-separated list of processor keys. 
	 * @param processorKeys the comma-separated list of processor keys.
	 * @return a chain of processors
	 */
	public abstract GlobalProcessor<T> buildProcessorChain(String processorKeys);
	
	
	/**
	 * Sets a map of custom processors to use. 
	 * The map has a key to name a processor (to be used in bundle definitions), and 
	 * the class name of a custom processor class which must implement 
	 * net.jawr.web.resource.bundle.global.preprocessor.GlobalProcessor 
	 * 
	 * @param keysClassNames the map associated the keys and the class names.
	 */
	public abstract void setCustomGlobalProcessors(Map<String, String> keysClassNames);

}
