/**
 * Copyright 2012-2016 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.postprocess.impl;

import java.io.IOException;

import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;

/**
 * Wrapper class for custom JS user made postprocessors. Adds chaining
 * functionality to the custom postprocessor.
 * 
 * @author Ibrahim Chaehoi
 */
public class CustomJsPostProcessorChainWrapper extends AbstractJsChainedResourceBundlePostProcessor {

	/** The custom post processor */
	private final ResourceBundlePostProcessor customPostProcessor;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the ID of the postprocessor
	 * @param customPostProcessor
	 *            Custom implementation of ResourceBundlePostProcessor to wrap
	 *            with chaining.
	 * @param isVariantPostProcessor
	 *            the flag indicating if it's a variant bundle
	 */
	public CustomJsPostProcessorChainWrapper(String id, ResourceBundlePostProcessor customPostProcessor,
			boolean isVariantPostProcessor) {

		super(id);
		this.customPostProcessor = customPostProcessor;
		this.isVariantPostProcessor = isVariantPostProcessor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.postprocess.
	 * AbstractChainedResourceBundlePostProcessor#doPostProcessBundle(net.jawr.
	 * web.resource.bundle.postprocess.BundleProcessingStatus,
	 * java.lang.StringBuffer)
	 */
	@Override
	protected StringBuffer doPostProcessBundle(BundleProcessingStatus status, StringBuffer bundleData)
			throws IOException {
		return customPostProcessor.postProcessBundle(status, bundleData);
	}

}
