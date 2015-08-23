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
package test.net.jawr.web.util.js.rhino;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.IOUtils;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * This class is intended to provide facility method for Rhino.
 * 
 * @author ibrahim Chaehoi
 */
public class RhinoEngine {

	/** The current scope */
	private final ScriptableObject scope;

	/**
	 * Constructor 
	 */
	public RhinoEngine() {
		
		Context context = Context.enter();
		context.setOptimizationLevel(-1); // Without this, Rhino hits a 64K
											// bytecode limit and fails
		scope = context.initStandardObjects();
	}

	/**
	 * Initialize the context if it doesn't exists
	 */
	private void initContext() {
		if (Context.getCurrentContext() == null) {
			Context.enter();
		}
	}

	/**
	 * Returns the current context
	 * 
	 * @return the current context
	 */
	private Context getContext() {
		initContext();
		return Context.getCurrentContext();
	}

	/**
	 * @return the context
	 */
	public ScriptableObject getScope() {
		return this.scope;
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
			return getContext().evaluateReader(scope, reader, scriptName, 0,
					null);
		} catch (IOException e) {
			throw new BundlingProcessException(
					"Error while evaluating script : " + scriptName, e);
		} finally {
			if (Context.getCurrentContext() != null) {
				Context.exit();
			}
		}
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
			return getContext().evaluateString(scope, script, scriptName, 0,
					null);
		} finally {
			if (Context.getCurrentContext() != null) {
				Context.exit();
			}
		}
	}

	/**
	 * Evaluates the JS passed in parameter
	 * 
	 * @param currentScope
	 *            the scope to use
	 * @param source
	 *            the JS script
	 * @param scriptName
	 *            the script name
	 * @return the result
	 */
	public Object evaluateString(Scriptable currentScope, String source,
			String scriptName) {
		try {
			return getContext().evaluateString(currentScope, source,
					scriptName, 0, null);
		} finally {
			if (Context.getCurrentContext() != null) {
				Context.exit();
			}
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
		try {
			return getContext().evaluateReader(scope,
					new InputStreamReader(stream), scriptName, 1, null);
		} catch (IOException e) {
			throw new BundlingProcessException(
					"Error while evaluating script : " + scriptName, e);
		} finally {
			IOUtils.close(stream);
		}
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
	public RhinoEngine evaluateInChain(String scriptName, InputStream stream) {
		evaluate(scriptName, stream);
		return this;
	}

	/**
	 * Returns a new Object from the current context
	 * 
	 * @return a new Object from the current context
	 */
	public Scriptable newObject() {
		Scriptable obj = getContext().newObject(scope);
		obj.setParentScope(scope);
		return obj;
	}

}
