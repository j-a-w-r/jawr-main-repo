/**
 * Copyright 2012-2013 Ibrahim Chaehoi
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;

/**
 * This class defines the abstract JS postprocessor
 * 
 * @author Ibrahim Chaehoi
 */
public abstract class AbstractJsChainedResourceBundlePostProcessor extends
		AbstractChainedResourceBundlePostProcessor {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AbstractChainedResourceBundlePostProcessor.class);

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the post processor ID
	 */
	public AbstractJsChainedResourceBundlePostProcessor(String id) {
		super(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor#
	 * postProcessBundle(java.lang.StringBuffer)
	 */
	public StringBuffer postProcessBundle(BundleProcessingStatus status,
			StringBuffer bundleData) {
		StringBuffer processedBundle = null;
		try {
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("postprocessing bundle:"
						+ status.getCurrentBundle().getId());
			processedBundle = doPostProcessBundle(status, bundleData);
			if (processedBundle.toString().endsWith(")")) {
				processedBundle.append(";");
			}
		} catch (IOException e) {
			throw new BundlingProcessException(
					"Unexpected IOException during execution of a postprocessor.",
					e);
		}
		if (null != nextProcessor) {
			processedBundle = nextProcessor.postProcessBundle(status,
					processedBundle);
		}
		return processedBundle;
	}

}
