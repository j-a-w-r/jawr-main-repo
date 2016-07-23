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
package net.jawr.web.resource.bundle.postprocess;

import java.io.IOException;

import net.jawr.web.JawrConstant;

/**
 * Empty implementation of a bundle processor, which actually does nothing to a
 * bundle.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class EmptyResourceBundlePostProcessor extends AbstractChainedResourceBundlePostProcessor {

	/**
	 * Constructor
	 */
	public EmptyResourceBundlePostProcessor() {
		super(PostProcessFactoryConstant.NO_POSTPROCESSING_KEY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.postprocess.
	 * AbstractChainedResourceBundlePostProcessor#doPostProcessBundle(net.jawr.
	 * web.resource.bundle.postprocess .BundleProcessingStatus,
	 * java.lang.StringBuffer)
	 */
	@Override
	protected StringBuffer doPostProcessBundle(BundleProcessingStatus status, StringBuffer bundleData)
			throws IOException {
		
		if(status.getProcessingType().equals(BundleProcessingStatus.FILE_PROCESSING_TYPE)
				&& status.getJawrConfig().getResourceType().equals(JawrConstant.JS_TYPE)){
			if (bundleData.toString().trim().endsWith(")")) {
				bundleData.append(";");
			}
		}
		
		return bundleData;
	}

	/**
	 * Set the next post processor in the chain.
	 * 
	 * @param nextProcessor
	 *            the next post processor to set
	 */
	@Override
	public void addNextProcessor(ChainedResourceBundlePostProcessor nextProcessor) {
		throw new UnsupportedOperationException(
				"The empty resource bundle processor can't have a next post processord");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.postprocess.
	 * ChainedResourceBundlePostProcessor#containsCompositeBundleProcessor()
	 */
	public boolean containsCompositeBundleProcessor() {
		return false;
	}

}
