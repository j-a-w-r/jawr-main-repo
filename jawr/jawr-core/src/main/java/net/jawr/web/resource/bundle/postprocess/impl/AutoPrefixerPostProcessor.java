/**
 * Copyright 2015 Ibrahim Chaehoi
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.script.Bindings;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.PostProcessFactoryConstant;
import net.jawr.web.util.StopWatch;
import net.jawr.web.util.js.JavascriptEngine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines the autoprefixer postprocessor
 * 
 * @see <a href="https://github.com/postcss/autoprefixer">Autoprefixer official
 *      site </a>
 * @author Ibrahim Chaehoi
 */
public class AutoPrefixerPostProcessor extends
		AbstractChainedResourceBundlePostProcessor {

	/** The Logger */
	private static final Logger PERF_LOGGER = LoggerFactory
			.getLogger(JawrConstant.PERF_LOGGER);

	/** The property name of the autoprefixer options */
	public static final String AUTOPREFIXER_SCRIPT_OPTIONS = "jawr.css.autoprefixer.options";

	/** The property name of the autoprefixer script location */
	public static final String AUTOPREFIXER_SCRIPT_LOCATION = "jawr.css.autoprefixer.script";

	/** The default location of the autoprefixer script */
	public static final String AUTOPREFIXER_SCRIPT_DEFAULT_LOCATION = "/net/jawr/web/resource/bundle/postprocessor/css/autoprefixer/autoprefixer-5.2.1.js";

	/** The autoprefixer js engine property name */
	public static final String AUTOPREFIXER_JS_ENGINE = "jawr.css.autoprefixer.js.engine";

	/** The default options */
	public static final String AUTOPREFIXER_DEFAULT_OPTIONS = "{}";

	/** The coffee script options */
	private String options;

	/** The Rhino engine */
	private JavascriptEngine jsEngine;

	/**
	 * Constructor
	 */
	public AutoPrefixerPostProcessor() {
		super(PostProcessFactoryConstant.AUTOPREFIXER);
	}

	/**
	 * Initialize the postprocessor
	 */
	private void initialize(JawrConfig config) {

		StopWatch stopWatch = new StopWatch();
		stopWatch.start("Initializing JS engine for Autoprefixer");
		options = config.getProperty(AUTOPREFIXER_SCRIPT_OPTIONS,
				AUTOPREFIXER_DEFAULT_OPTIONS);

		// Load JavaScript Script Engine
		String script = config.getProperty(AUTOPREFIXER_SCRIPT_LOCATION,
				AUTOPREFIXER_SCRIPT_DEFAULT_LOCATION);
		String jsEngineName = config.getJavascriptEngineName(AUTOPREFIXER_JS_ENGINE);
		jsEngine = new JavascriptEngine(jsEngineName, true);
		jsEngine.getBindings().put("logger", PERF_LOGGER);
		InputStream inputStream = getResourceInputStream(config, script);
		jsEngine.evaluate("autoprefixer.js", inputStream);
		jsEngine.evaluate("initAutoPrefixer.js",
				String.format("processor = autoprefixer(%s);", options));
		jsEngine.evaluate("jawrAutoPrefixerProcess.js", String
				.format("function process(cssSource, opts){"
						+ "var result = processor.process(cssSource, opts);"
						+ "if(result.warnings){"
						+ "result.warnings().forEach(function(message){"
						+ "if(logger.isWarnEnabled()){"
						+ "logger.warn(message.toString());" + "}" + "});}"
						+ "return result.css;" + "}"));

		stopWatch.stop();
		if (PERF_LOGGER.isDebugEnabled()) {
			PERF_LOGGER.debug(stopWatch.prettyPrint());
		}
	}

	/**
	 * Returns the resource input stream
	 * 
	 * @param config
	 *            the Jawr config
	 * @param path
	 *            the resource path
	 * @return the resource input stream
	 */
	private InputStream getResourceInputStream(JawrConfig config, String path) {
		InputStream is = config.getContext().getResourceAsStream(path);
		if (is == null) {
			try {
				is = ClassLoaderResourceUtils.getResourceAsStream(path, this);
			} catch (FileNotFoundException e) {
				throw new BundlingProcessException(e);
			}
		}

		return is;
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

		if (jsEngine == null) {
			initialize(status.getJawrConfig());
		}

		StopWatch stopWatch = new StopWatch();
		stopWatch.start("Processing Autoprefixer on '"+status.getLastPathAdded()+"'");
		Bindings bindings = jsEngine.getBindings();
		bindings.put("cssSource", bundleData.toString());
		String res = (String) jsEngine.evaluateString("Autoprefixer",
				String.format("process(cssSource, %s);", options),
				bindings);
		
		stopWatch.stop();
		if (PERF_LOGGER.isDebugEnabled()) {
			PERF_LOGGER.debug(stopWatch.prettyPrint());
		}
		return new StringBuffer(res);
	}

}
