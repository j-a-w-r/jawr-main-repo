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
package net.jawr.web.resource.bundle.global.processor;

import java.util.List;

import net.jawr.web.resource.bundle.JoinableResourceBundle;

/**
 * This class defines the wrapper, which wraps a custom global processor to a chained global processor.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class CustomGlobalProcessorChainedWrapper<T extends AbstractGlobalProcessingContext> extends AbstractChainedGlobalProcessor<T> implements
		ChainedGlobalProcessor<T> {

	/** The global preprocessor */
	private GlobalProcessor<T> globalPreprocessor;
	
	/**
	 * Constructor
	 * @param id the ID
	 * @param globalPreprocessor the global preprocessor
	 */
	public CustomGlobalProcessorChainedWrapper(String id, GlobalProcessor<T> globalPreprocessor) {
		super(id);
		this.globalPreprocessor = globalPreprocessor;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.global.preprocessor.GlobalPreprocessor#processBundles(net.jawr.web.resource.bundle.global.preprocessor.GlobalPreprocessingContext, java.util.List)
	 */
	public void processBundles(T ctx, List<JoinableResourceBundle> bundles) {
		globalPreprocessor.processBundles(ctx, bundles);
	}
	
}
