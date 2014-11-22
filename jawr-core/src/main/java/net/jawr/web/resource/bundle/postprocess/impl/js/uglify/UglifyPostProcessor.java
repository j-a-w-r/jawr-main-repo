/**
 * Copyright 2014 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.postprocess.impl.js.uglify;

import java.io.IOException;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.minification.CompressionResult;
import net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.PostProcessFactoryConstant;

/**
 * This class defines the UglifyJS postprocessor used for JS compression
 * 
 * @author Ibrahim Chaehoi
 */
public class UglifyPostProcessor extends
		AbstractChainedResourceBundlePostProcessor {

	/** The UglifyJS compressor */
	private UglifyJS uglifyJS;

	/**
	 * Constructor
	 */
	public UglifyPostProcessor() {
		super(PostProcessFactoryConstant.UGLIFY_JS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.postprocess.
	 * AbstractChainedResourceBundlePostProcessor
	 * #doPostProcessBundle(net.jawr.web
	 * .resource.bundle.postprocess.BundleProcessingStatus,
	 * java.lang.StringBuffer)
	 */
	@Override
	protected StringBuffer doPostProcessBundle(BundleProcessingStatus status,
			StringBuffer bundleData) throws IOException {

		if (uglifyJS == null) {
			JawrConfig config = status.getJawrConfig();
			String scriptDirLocation = config.getProperty(
					JawrConstant.UGLIFY_POSTPROCESSOR_SCRIPT_LOCATION,
					JawrConstant.UGLIFY_POSTPROCESSOR_DEFAULT_JS_BASE_LOCATION);

			String optionsInJson = config.getProperty(JawrConstant.UGLIFY_POSTPROCESSOR_OPTIONS, "{}");
			uglifyJS = new UglifyJS(config, scriptDirLocation, optionsInJson);
		}

		CompressionResult result = uglifyJS.compress(bundleData.toString());
		return new StringBuffer(result.getCode());
	}

}
