package test.net.jawr.web.util.js.rhino;

import org.junit.Assert;
import org.junit.Test;
import org.mozilla.javascript.ScriptableObject;

import net.jawr.web.util.js.JavascriptEngine;
import test.net.jawr.web.FileUtils;

public class RhinoEngineTest {

	@Test
	public void testScriptMessage() throws Exception{
		
		RhinoEngine engine = new RhinoEngine();
		String script = readFile("bundleLocale/resultScriptWithNamespace.js");
		Object global = engine.evaluate("init.js", "eval(this)");
		ScriptableObject scope = engine.getScope();
		scope.put("window", scope, global);
		engine.evaluate("msg.js", script);
		Object result = engine.evaluate("tmp", "myMessages.error.login()");
		Assert.assertEquals(result, "Login failed");
	}
	
	@Test
	public void testScriptMessageJsEngine() throws Exception {
		
		JavascriptEngine engine = new JavascriptEngine(true);
		String script = readFile("bundleLocale/resultScriptWithNamespace.js");
		engine.evaluate("msg.js", script);
		Object result = engine.evaluate("tmp", "myMessages.error.login()");
		Assert.assertEquals(result, "Login failed");
	}
	
	private String readFile(String path) throws Exception {

		return readFile(path, "UTF-8");
	}
	
	private String readFile(String path, String charset) throws Exception {

		return FileUtils.readFile(FileUtils.getClassPathFile(path), charset);
	}
}
