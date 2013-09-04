/**
 * Copyright 2008-2012 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.postprocess.impl.yui;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.PostProcessFactoryConstant;
import net.jawr.web.resource.bundle.postprocess.impl.AbstractJsChainedResourceBundlePostProcessor;

import org.apache.log4j.Logger;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * This postprocessor will minify a javascript bundle or file using Julien Lecomte's YUICompressor. See http://developer.yahoo.com/yui/compressor/ for
 * more information.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class YUIJSCompressor extends AbstractJsChainedResourceBundlePostProcessor {
	
	/** The logger */
	private static final Logger LOGGER = Logger.getLogger(YUIJSCompressor.class);
	
	/** The flag indicating if the content should be obfuscated or not */
	private boolean obfuscate;

	/**
	 * Constructor.
	 * 
	 * @param obfuscate Wether obfuscation should be applied.
	 */
	public YUIJSCompressor(boolean obfuscate) {
		super(obfuscate ? PostProcessFactoryConstant.YUI_COMPRESSOR_OBFUSCATOR
				: PostProcessFactoryConstant.YUI_COMPRESSOR);
		this.obfuscate = obfuscate;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor#doPostProcessBundle(net.jawr.web.resource.bundle.postprocess
	 * .BundleProcessingStatus, java.lang.StringBuffer)
	 */
	protected StringBuffer doPostProcessBundle(BundleProcessingStatus status,
			StringBuffer bundleData) throws IOException {

		Reader rd = new StringReader(bundleData.toString());
		JavaScriptCompressor compressor = new JavaScriptCompressor(rd,
				new YUIErrorReporter(status, bundleData));

		StringWriter wr = new StringWriter();
		/*
		 * Params for this call: Writer: the StringWriter. int linebreak: always -1... boolean munge: obfuscated or not. boolean verbose: logging
		 * level boolean preserveAllSemiColons: leave to false. boolean disableOptimizations: leave to true. *
		 */
		compressor.compress(wr, -1, obfuscate, LOGGER.isDebugEnabled(), false,
				false);

		return wr.getBuffer();
	}

}
