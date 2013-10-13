/**
 * Copyright 2010-2013 Gerben Jorna, Ibrahim Chaehoi
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

import net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.PostProcessFactoryConstant;
import net.jawr.web.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines the post processor which combines style sheets for
 * different media types into a single file, surrounding them with a @media
 * block.
 * 
 * Information about css media types: http://www.w3.org/TR/CSS21/media.html
 * 
 * @author Gerben Jorna
 * @author Ibrahim Chaehoi
 */
public class CSSCombineMediaPostProcessor extends
		AbstractChainedResourceBundlePostProcessor {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CSSCombineMediaPostProcessor.class);

	/** The media rule */
	protected static final String CSS_MEDIA_RULE = "@media";

	/** The media rule start */
	protected static final String CSS_MEDIA_RULE_OPEN = "{";

	/** The media rule end */
	protected static final String CSS_MEDIA_RULE_CLOSE = "}";

	/**
	 * Constructor
	 * 
	 * @param id
	 *            the Id of the post processor
	 */
	public CSSCombineMediaPostProcessor() {
		super(PostProcessFactoryConstant.CSS_COMBINE_MEDIA);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.postprocess.
	 * AbstractChainedResourceBundlePostProcessor
	 * #doPostProcessBundle(net.jawr.web.resource.bundle.postprocess
	 * .BundleProcessingStatus, java.lang.StringBuffer)
	 */
	protected StringBuffer doPostProcessBundle(BundleProcessingStatus status,
			StringBuffer bundleData) throws IOException {
		LOGGER.info("Post processing file '" + status.getLastPathAdded() + "'");

		String bundleMediaTypePropertyName = "jawr.css.bundle."
				+ status.getCurrentBundle().getName() + ".media";
		String bundleMediaType = (String) status.getJawrConfig().getProperty(
				bundleMediaTypePropertyName);
		if (bundleMediaType == null) {
			LOGGER.warn("no bundle media type provided; use 'screen'");
			bundleMediaType = "screen";
		}

		LOGGER.info("bundle media type: " + bundleMediaType);

		StringBuffer sb = new StringBuffer(CSS_MEDIA_RULE + " "
				+ bundleMediaType + " " + CSS_MEDIA_RULE_OPEN
				+ StringUtils.LINE_SEPARATOR);
		sb.append(bundleData);
		sb.append(CSS_MEDIA_RULE_CLOSE + StringUtils.LINE_SEPARATOR
				+ StringUtils.LINE_SEPARATOR);

		LOGGER.info("Postprocessing finished");
		return sb;
	}
}
