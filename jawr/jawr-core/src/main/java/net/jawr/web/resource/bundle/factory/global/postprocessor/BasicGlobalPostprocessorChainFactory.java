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
package net.jawr.web.resource.bundle.factory.global.postprocessor;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.global.postprocessor.google.closure.ClosureGlobalPostProcessor;
import net.jawr.web.resource.bundle.global.processor.AbstractChainedGlobalProcessor;
import net.jawr.web.resource.bundle.global.processor.AbstractGlobalProcessorChainFactory;

/**
 * This class defines the global preprocessor factory.
 * 
 * @author Ibrahim Chaehoi
 * 
 */
public class BasicGlobalPostprocessorChainFactory extends AbstractGlobalProcessorChainFactory<GlobalPostProcessingContext> implements
		GlobalPostprocessorChainFactory {

	/**
	 * Build the global preprocessor from the ID given in parameter
	 * 
	 * @param key the ID of the preprocessor
	 * @return a global preprocessor
	 */
	protected AbstractChainedGlobalProcessor<GlobalPostProcessingContext> buildProcessorByKey(String key) {

		AbstractChainedGlobalProcessor<GlobalPostProcessingContext> processor = null;

		if (key.equals(JawrConstant.GLOBAL_GOOGLE_CLOSURE_POSTPROCESSOR_ID)) {
			processor = new ClosureGlobalPostProcessor();
		}

		return processor;
	}

}
