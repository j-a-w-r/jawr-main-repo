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
package net.jawr.web.resource.bundle.factory.global.preprocessor;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.global.preprocessor.css.smartsprites.CssSmartSpritesGlobalPreprocessor;
import net.jawr.web.resource.bundle.global.processor.AbstractChainedGlobalProcessor;
import net.jawr.web.resource.bundle.global.processor.AbstractGlobalProcessorChainFactory;

/**
 * This class defines the global preprocessor factory.
 * 
 * @author Ibrahim Chaehoi
 * 
 */
public class BasicGlobalPreprocessorChainFactory extends AbstractGlobalProcessorChainFactory<GlobalPreprocessingContext>
		implements GlobalPreprocessorChainFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.global.processor.
	 * AbstractGlobalProcessorChainFactory#buildProcessorByKey(java.lang.String)
	 */
	@Override
	protected AbstractChainedGlobalProcessor<GlobalPreprocessingContext> buildProcessorByKey(String key) {

		AbstractChainedGlobalProcessor<GlobalPreprocessingContext> processor = null;

		if (key.equals(JawrConstant.GLOBAL_CSS_SMARTSPRITES_PREPROCESSOR_ID)) {
			processor = new CssSmartSpritesGlobalPreprocessor();
		}

		return processor;
	}

}
