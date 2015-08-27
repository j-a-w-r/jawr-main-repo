/**
 * Copyright 2014-2015 Ibrahim Chaehoi
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

import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.minification.CompressionResult;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.util.StopWatch;
import net.jawr.web.util.StringUtils;
import net.jawr.web.util.js.JavascriptEngine;

/**
 * The Uglify JS engine. This compressor is using UglifyJS
 * (https://github.com/mishoo/UglifyJS2)
 * 
 * @author ibrahim Chaehoi
 */
public class UglifyJS {

	/** The logger */
	private static Logger PERF_LOGGER = LoggerFactory.getLogger(JawrConstant.PERF_PROCESSING_LOGGER);
	
	/** The Uglify scripts to load */
	private static final String[] UGLIFY_SCRIPTS = { "utils.js", "ast.js",
			"parse.js", "transform.js", "scope.js", "output.js", "compress.js",
			"sourcemap.js", "uglify.js" };

	/** The rhino engine */
	boolean baseEngine = true;
	private JavascriptEngine jsEngine;

	/** The Jawr configuration */
	private final JawrConfig config;

	/** The Uglify options in JSON format */
	private Object options;

	/**
	 * Constructor
	 * 
	 * @param config
	 *            the JAWR config
	 * @param scriptDirLocation
	 *            the directory where the Uglify source file are located
	 * @param optionsInJson
	 *            the uglify options
	 */
	public UglifyJS(JawrConfig config, String scriptDirLocation,
			String optionsInJson) {

		StopWatch stopWatch = new StopWatch();
		stopWatch.start("initializing JS engine for Uglify");

		String jsEngineName = config.getJavascriptEngineName(JawrConstant.UGLIFY_POSTPROCESSOR_JS_ENGINE);
		this.jsEngine = new JavascriptEngine(jsEngineName);
		this.config = config;
		this.options = jsEngine.execEval(optionsInJson);
		String baseJsLocation = StringUtils.isNotEmpty(scriptDirLocation) ? scriptDirLocation
				: JawrConstant.UGLIFY_POSTPROCESSOR_DEFAULT_JS_BASE_LOCATION;
		for (String script : UGLIFY_SCRIPTS) {
			jsEngine.evaluate(script, getResourceInputStream(baseJsLocation
					+ script));

		}
		stopWatch.stop();
		if(PERF_LOGGER.isDebugEnabled()){
			PERF_LOGGER.debug(stopWatch.prettyPrint());
		}
	}

	/**
	 * Returns the resource input stream
	 * 
	 * @param path
	 *            the resource path
	 * @return the resource input stream
	 */
	private InputStream getResourceInputStream(String path) {
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

	/**
	 * Invoke the uglyfication process on the given script code
	 * 
	 * @param scriptSource
	 *            the JS code to process
	 * @return the uglified code
	 */
	public CompressionResult compress(String scriptSource) {

		Object result = null;
		StopWatch stopWatch = new StopWatch();
		stopWatch.start("Compressing using Uglify");
		try {
			result = jsEngine.invokeFunction("minify", scriptSource, options);
		} catch (NoSuchMethodException | ScriptException e) {
			throw new BundlingProcessException(e);
		}

		stopWatch.stop();
		if(PERF_LOGGER.isDebugEnabled()){
			PERF_LOGGER.debug(stopWatch.prettyPrint());
		}
	
		return (CompressionResult) result;

	}

}
