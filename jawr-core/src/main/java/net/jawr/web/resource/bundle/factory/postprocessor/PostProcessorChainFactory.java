/**
 * Copyright 2007-2012 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.factory.postprocessor;

import java.util.Map;

import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;

/**
 * Interface for a factory of chained PostProcessor objects. It is meant to ease 
 * configuration implementations. 
 * 
 * @author Jordi Hernández Sellés
 * @author ibrahim Chaehoi
 */
public interface PostProcessorChainFactory {

	/**
	 * Builds the default chain for resources, be it javascript or css ones. 
	 * @return
	 */
	public ResourceBundlePostProcessor buildDefaultProcessorChain();

	/**
	 * Builds the default unitary resource chain for resources. 
	 * @return
	 */
	public ResourceBundlePostProcessor buildDefaultUnitProcessorChain();

	/**
	 * Returns the default composite bundle processor chain
	 * @return the default composite bundle processor chain
	 */
	public ResourceBundlePostProcessor buildDefaultCompositeProcessorChain();
	
	/**
	 * Returns the default composite bundle unit processor chain
	 * @return the default composite bundle unit processor chain
	 */
	public ResourceBundlePostProcessor buildDefaultUnitCompositeProcessorChain();
	
	/**
	 * Builds a chain based on a comma-separated list of postprocessor keys. 
	 * @param processorKeys
	 * @return
	 */
	public ResourceBundlePostProcessor buildPostProcessorChain(String processorKeys);
	
	/**
	 * Sets a map of custom postprocessors to use. 
	 * The map has a key to name a postprocessor (to be used in bundle definitions), and 
	 * the classname of a custom postprocessor class which must implement 
	 * net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor. 
	 * 
	 * @param keysClassNames
	 */
	public void setCustomPostprocessors(Map<String, String> keysClassNames);

}