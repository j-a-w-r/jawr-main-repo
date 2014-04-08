/**
 * Copyright 2012-2014 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator.css.less;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.ConsString;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.tools.shell.Global;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines the less engine
 * 
 * Original code from LessCssEngine
 * 
 * @author (Original) Rostislav Hristov
 * @author (Original) Uriah Carpenter
 * @author (Original) Noah Sloan
 */
public class LessEngine {

	/** The logger */
	private final Logger logger = LoggerFactory.getLogger(getClass());

	/** The scope */
	private Scriptable scope;
	
	/** The compileString function */
	private Function cs;
	
	/**
	 * Constructor 
	 */
	public LessEngine() {
		this(LessEngine.class.getResourceAsStream("less.js"), LessEngine.class.getResourceAsStream("engine.js"), LessEngine.class.getResourceAsStream("browser.js"));
	}

	/**
	 * Constructor
	 * @param less the URL to the less.js file
	 */
	public LessEngine(InputStream less, InputStream engine, InputStream browser) {
		
		try {
			logger.debug("Initializing LESS Engine.");
			Context cx = Context.enter();
			logger.warn("Using implementation version: "
					+ cx.getImplementationVersion());
			cx.setOptimizationLevel(9);
			Global global = new Global();
			global.init(cx);
			scope = cx.initStandardObjects(global);
			cx.evaluateReader(scope, new InputStreamReader(browser), "browser.js", 1,
					null);
			cx.evaluateReader(scope, new InputStreamReader(less), "less.js", 1,
					null);
			cx.evaluateReader(scope, new InputStreamReader(engine), "engine.js", 1,
					null);
			cs = (Function) scope.get("compileString", scope);
			Context.exit();
		} catch (Exception e) {
			logger.error("LESS Engine intialization failed.", e);
		}
	}

	/**
	 * Compiles the input string
	 * @param input the input string
	 * @return the compiled string
	 * @throws LessException if an error occurs during the compilation 
	 */
	public String compile(String input) throws LessException {
		try {
			long time = System.currentTimeMillis();
			String result = call(cs, new Object[] { input });
			logger.debug("The compilation of '" + input + "' took "
					+ (System.currentTimeMillis() - time) + " ms.");
			return result;
		} catch (Exception e) {
			throw parseLessException(e);
		}
	}

	/**
	 * Calls the function using the argument array
	 * @param fn the function
	 * @param args the arguments
	 * @return the result of the function call
	 */
	private synchronized String call(Function fn, Object[] args) {
		return (String) Context.call(null, fn, scope, scope, args);
	}

	/**
	 * Parse the exception and return a LessException
	 * @param root the root exception
	 * @return a LessException 
	 * @throws LessException if an exception occurs 
	 */
	private LessException parseLessException(Exception root)
			throws LessException {

		logger.debug("Parsing LESS Exception", root);

		if (root instanceof JavaScriptException) {

			Scriptable value = (Scriptable) ((JavaScriptException) root)
					.getValue();

			boolean hasName = ScriptableObject.hasProperty(value, "name");
			boolean hasType = ScriptableObject.hasProperty(value, "type");

			if (hasName || hasType) {
				String errorType = "Error";

				if (hasName) {
					String type = (String) ScriptableObject.getProperty(value,
							"name");
					if ("ParseError".equals(type)) {
						errorType = "Parse Error";
					} else {
						errorType = type + " Error";
					}
				} else if (hasType) {
					Object prop = ScriptableObject.getProperty(value, "type");
					if (prop instanceof String) {
						errorType = (String) prop + " Error";
					}
				}

				String message = ScriptableObject.getProperty(value,
						"message").toString();

				String filename = "";
				
				if (ScriptableObject.getProperty(value, "filename") != null) { 
					filename = ScriptableObject.getProperty(value, "filename").toString(); 
				}
				
				int line = -1;
				if (ScriptableObject.hasProperty(value, "line")) {
					line = ((Double) ScriptableObject
							.getProperty(value, "line")).intValue();
				}

				int column = -1;
				if (ScriptableObject.hasProperty(value, "column")) {
					column = ((Double) ScriptableObject.getProperty(value,
							"column")).intValue();
				}

				List<String> extractList = new ArrayList<String>();
				if (ScriptableObject.hasProperty(value, "extract")) {
					NativeArray extract = (NativeArray) ScriptableObject
							.getProperty(value, "extract");
					for (int i = 0; i < extract.getLength(); i++) {
						if (extract.get(i, extract) instanceof String) {
							extractList.add(((String) extract.get(i, extract))
									.replace("\t", " "));
						}
					}
				}

				throw new LessException(message.toString(), errorType, filename, line,
						column, extractList);
			}
		}

		throw new LessException(root);
	}
	
}