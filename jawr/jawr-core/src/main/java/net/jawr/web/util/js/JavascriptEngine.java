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
package net.jawr.web.util.js;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.jawr.web.JawrConstant;
import net.jawr.web.exception.BundlingProcessException;

/**
 * This class is intended to provide facility method for JS script engine.
 * 
 * @author ibrahim Chaehoi
 */
public class JavascriptEngine {

	private static final String UNKNOWN_SCRIPT = "Unknown script";
	/** The script engine */
	private ScriptEngine scriptEngine;

	/**
	 * Constructor
	 */
	public JavascriptEngine() {
		this(JawrConstant.DEFAULT_JS_ENGINE, false);
	}
	
	/**
	 * Constructor
	 * @param initGlobal
	 *            the flag indicating that we must initialize the global object
	 *            in the global variable
	 */
	public JavascriptEngine(boolean initGlobal) {
		this(JawrConstant.DEFAULT_JS_ENGINE, initGlobal);
	}
	
	/**
	 * Constructor
	 * @param scriptEngineName the name of the Javascript engine to use
	 */
	public JavascriptEngine(String scriptEngineName) {
		this(scriptEngineName, false);
	}

	/**
	 * Constructor
	 * 
	 * @param scriptEngineName the name of the Javascript engine to use
	 * @param initGlobal
	 *            the flag indicating that we must initialize the global object
	 *            in the global variable
	 */
	public JavascriptEngine(String scriptEngineName, boolean initGlobal) {

		ScriptEngineManager manager = new ScriptEngineManager();
		scriptEngine = manager.getEngineByName(scriptEngineName);
		if (initGlobal) {

			// get JavaScript "global" object and put it in the script engine scope
			try {
				Object global = scriptEngine.eval("eval(this)");
				scriptEngine.put("window", global);
				scriptEngine.put("global", global);
			} catch (ScriptException e) {
				throw new BundlingProcessException(e);
			}
		}
	}

	/**
	 * @return the context
	 */
	public ScriptContext getContext() {
		return this.scriptEngine.getContext();
	}

	/**
	 * Evaluates the script
	 * 
	 * @param reader
	 *            the script reader
	 * @return the result
	 */
	public Object evaluate(Reader reader) {
		return evaluate(UNKNOWN_SCRIPT, reader);
	}
	
	/**
	 * Evaluates the script
	 * 
	 * @param scriptName
	 *            the script name
	 * @param reader
	 *            the script reader
	 * @return the result
	 */
	public Object evaluate(String scriptName, Reader reader) {

		try {
			scriptEngine.put(ScriptEngine.FILENAME, scriptName);
			return scriptEngine.eval(reader);
		} catch (ScriptException e) {
			throw new BundlingProcessException(
					"Error while evaluating script : " + scriptName, e);
		}
	}

	/**
	 * Evaluates the script
	 * 
	 * @param script
	 *            the script
	 * @return the result
	 */
	public Object evaluate(String script) {
		return evaluate(UNKNOWN_SCRIPT, script);
	}
	
	/**
	 * Evaluates the script
	 * 
	 * @param scriptName
	 *            the script name
	 * @param script
	 *            the script
	 * @return the result
	 */
	public Object evaluate(String scriptName, String script) {

		try {
			scriptEngine.put(ScriptEngine.FILENAME, scriptName);
			return scriptEngine.eval(script);
		} catch (ScriptException e) {
			throw new BundlingProcessException(
					"Error while evaluating script : " + scriptName, e);
		}
	}

	/**
	 * Evaluates the JS passed in parameter
	 * @param scriptName
	 *            the script name
	 * @param source
	 *            the JS script
	 * @param bindings
	 *            the bindings to use
	 * 
	 * @return the result
	 */
	public Object evaluateString(String scriptName, String source,
			Bindings bindings) {
		try {
			scriptEngine.put(ScriptEngine.FILENAME, scriptName);
			return scriptEngine.eval(source, bindings);
		} catch (ScriptException e) {
			throw new BundlingProcessException(
					"Error while evaluating script : " + scriptName, e);
		}
	}

	/**
	 * Evaluates a script
	 * 
	 * @param stream
	 *            The inputStream of the script to evaluate.
	 * @param sourceName
	 *            the name of the script to evaluate.
	 */
	public Object evaluate(String scriptName, InputStream stream) {
		return evaluate(scriptName, new InputStreamReader(stream));
	}

	/**
	 * Evaluates a script and return the RhinoEngine for a chained script
	 * evaluation.
	 *
	 * @param stream
	 *            The inputStream of the script to evaluate.
	 * @param sourceName
	 *            the name of the script to evaluate.
	 * @return the current RhinoEngine.
	 */
	public JavascriptEngine evaluateInChain(String scriptName, InputStream stream) {
		evaluate(scriptName, stream);
		return this;
	}

	/**
	 * Returns a new Object from the current context
	 * 
	 * @return a new Object from the current context
	 */
	public Bindings getBindings() {
		Bindings bindings = getContext()
				.getBindings(ScriptContext.ENGINE_SCOPE);
		return bindings;
	}

}
