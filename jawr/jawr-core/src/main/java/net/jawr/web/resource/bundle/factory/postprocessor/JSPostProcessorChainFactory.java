/**
 * Copyright 2007-2016 Jordi Hernández Sellés, Ibrahim Chaehoi
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

import net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.ChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.PostProcessFactoryConstant;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.impl.CustomJsPostProcessorChainWrapper;
import net.jawr.web.resource.bundle.postprocess.impl.JSMinPostProcessor;
import net.jawr.web.resource.bundle.postprocess.impl.js.uglify.UglifyPostProcessor;
import net.jawr.web.resource.bundle.postprocess.impl.yui.YUIJSCompressor;

/**
 * PostProcessorChainFactory for javascript resources.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 *
 */
public class JSPostProcessorChainFactory extends AbstractPostProcessorChainFactory
		implements PostProcessorChainFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.factory.postprocessor.
	 * AbstractPostProcessorChainFactory#getCustomProcessorWrapper(net.jawr.web.
	 * resource.bundle.postprocess.ResourceBundlePostProcessor,
	 * java.lang.String)
	 */
	@Override
	protected ChainedResourceBundlePostProcessor getCustomProcessorWrapper(ResourceBundlePostProcessor customProcessor,
			String key, boolean isVariantPostProcessor) {

		return new CustomJsPostProcessorChainWrapper(key, customProcessor, isVariantPostProcessor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.factory.processor.PostProcessorChainFactory#
	 * buildDefaultProcessor()
	 */
	@Override
	public ResourceBundlePostProcessor buildDefaultProcessorChain() {
		AbstractChainedResourceBundlePostProcessor processor = buildJSMinPostProcessor();
		processor.addNextProcessor(buildLicensesProcessor());
		return processor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.factory.processor.PostProcessorChainFactory#
	 * buildDefaultUnitProcessor()
	 */
	@Override
	public ResourceBundlePostProcessor buildDefaultUnitProcessorChain() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.factory.processor.PostProcessorChainFactory#
	 * getPostProcessor(java.lang.String)
	 */
	@Override
	protected AbstractChainedResourceBundlePostProcessor buildProcessorByKey(String procesorKey) {
		if (PostProcessFactoryConstant.JSMIN.equals(procesorKey))
			return buildJSMinPostProcessor();
		else if (PostProcessFactoryConstant.LICENSE_INCLUDER.equals(procesorKey))
			return buildLicensesProcessor();
		else if (PostProcessFactoryConstant.UGLIFY_JS.equals(procesorKey))
			return buildUglifyJSProcessor();
		else if (PostProcessFactoryConstant.YUI_COMPRESSOR.equals(procesorKey))
			return new YUIJSCompressor(false);
		else if (PostProcessFactoryConstant.YUI_COMPRESSOR_OBFUSCATOR.equals(procesorKey))
			return new YUIJSCompressor(true);
		else
			throw new IllegalArgumentException("The supplied key [" + procesorKey
					+ "] is not bound to any ResourceBundlePostProcessor. Please check the documentation for valid keys. ");
	}

	/**
	 * Creates the Uglify postprocessor
	 * 
	 * @return the Uglify postprocessor
	 */
	private AbstractChainedResourceBundlePostProcessor buildUglifyJSProcessor() {
		return new UglifyPostProcessor();
	}

	/**
	 * Creates the JSMin postprocessor
	 * 
	 * @return the JSMin postprocessor
	 */
	private AbstractChainedResourceBundlePostProcessor buildJSMinPostProcessor() {
		return new JSMinPostProcessor();
	}

}
