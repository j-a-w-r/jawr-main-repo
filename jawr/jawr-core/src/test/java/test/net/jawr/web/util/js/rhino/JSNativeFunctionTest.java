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

package test.net.jawr.web.util.js.rhino;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Assert;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ScriptableObject;

/**
 * Unit test for JS Native function
 * 
 * @author Ibrahim Chaehoi
 */
public class JSNativeFunctionTest {

	private String testScript = "var FRUITS = 'apple orange banana';\n"
			+ "var COLORS = 'red green yellow';\n"
			+ "var RESERVED_WORDS = FRUITS + ' ' + COLORS;\n"
			+" (function(){\n"
			+ "var words = RESERVED_WORDS.split(' ');\n"
			+ "return words.length})()";
	
	@Test
	public void testScriptWithGlobalNativeRhinoObject() throws ScriptException{
		
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine scriptEngine = manager.getEngineByName("mozilla.rhino");
		Object result = scriptEngine.eval(testScript);
		
		Assert.assertEquals(6, ((Number)result).intValue());
	}
	
	@Test
	public void testWithStdRhinoScriptWithGlobalNativeRhinoObject() throws ScriptException{
		
		Context context = Context.enter();
		context.setOptimizationLevel(-1);
		ScriptableObject scope = context.initStandardObjects();
		Object result = null;
		try {
			Context ctx = Context.getCurrentContext();
			result = ctx.evaluateString(scope, testScript, "test", 0,
					null);
		} finally {
			if (Context.getCurrentContext() != null) {
				Context.exit();
			}
		}
		
		Assert.assertEquals(6, ((Number)result).intValue());
	}
}
