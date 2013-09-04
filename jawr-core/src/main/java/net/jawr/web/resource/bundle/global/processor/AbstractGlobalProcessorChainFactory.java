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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;

/**
 * This class defines the global preprocessor factory.
 * 
 * @author Ibrahim Chaehoi
 * 
 */
public abstract class AbstractGlobalProcessorChainFactory<T extends AbstractGlobalProcessingContext> implements
		GlobalProcessorChainFactory<T> {

	/** The user-defined postprocessors */
	private Map<String, ChainedGlobalProcessor<T>> customPostprocessors = new HashMap<String, ChainedGlobalProcessor<T>>();

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.global.processor.GlobalProcessorChainFactory#setCustomGlobalProcessors(java.util.Map)
	 */
	@SuppressWarnings("unchecked")
	public void setCustomGlobalProcessors(Map<String, String> keysClassNames) {
		
		for(Iterator<Entry<String, String>> it = keysClassNames.entrySet().iterator(); it.hasNext();){
			
			Entry<String, String> entry = it.next();
			GlobalProcessor<T> customGlobalPreprocessor = 
				(GlobalProcessor<T>) ClassLoaderResourceUtils.buildObjectInstance((String) entry.getValue());
			
			String key = (String) entry.getKey();			
			customPostprocessors.put(key, new CustomGlobalProcessorChainedWrapper<T>(key, customGlobalPreprocessor));
		}		
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.global.processor.GlobalProcessorChainFactory#buildDefaultProcessorChain()
	 */
	public GlobalProcessor<T> buildDefaultProcessorChain() {
		
		return new EmptyGlobalProcessor<T>();
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.global.processor.GlobalProcessorChainFactory#buildProcessorChain(java.lang.String)
	 */
	public GlobalProcessor<T> buildProcessorChain(String processorKeys) {
		
		if (null == processorKeys)
			return null;
		else if (JawrConstant.EMPTY_GLOBAL_PREPROCESSOR_ID
				.equals(processorKeys))
			return new EmptyGlobalProcessor<T>();

		StringTokenizer tk = new StringTokenizer(processorKeys, ",");

		AbstractChainedGlobalProcessor<T> chain = null;
		while (tk.hasMoreTokens())
			chain = addOrCreateChain(chain, tk.nextToken());

		return chain;
	}

	/**
	 * Creates an AbstractChainedGlobalProcessor. If the supplied
	 * chain is null, the new chain is returned. Otherwise it is added to the
	 * existing chain.
	 * 
	 * @param chain
	 *            the chained post processor
	 * @param key
	 *            the id of the post processor
	 * @return the chained post processor, with the new post processor.
	 */
	private AbstractChainedGlobalProcessor<T> addOrCreateChain(
			AbstractChainedGlobalProcessor<T> chain, String key) {

		AbstractChainedGlobalProcessor<T> toAdd;

		if (customPostprocessors.get(key) == null) {
			toAdd = buildProcessorByKey(key);
		} else{
			toAdd = (AbstractChainedGlobalProcessor<T>) customPostprocessors
				.get(key);
		}
		
		AbstractChainedGlobalProcessor<T> newChainResult = null;
		if (chain == null) {
			newChainResult = toAdd;
		}else{
			chain.addNextProcessor(toAdd);
			newChainResult = chain;
		}

		return newChainResult;
	}

	/**
	 * Build the global preprocessor from the ID given in parameter
	 * 
	 * @param key the ID of the preprocessor
	 * @return a global preprocessor
	 */
	protected abstract AbstractChainedGlobalProcessor<T> buildProcessorByKey(String key);

}
