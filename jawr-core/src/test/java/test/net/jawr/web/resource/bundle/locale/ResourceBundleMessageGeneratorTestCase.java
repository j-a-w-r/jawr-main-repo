package test.net.jawr.web.resource.bundle.locale;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mozilla.javascript.ScriptableObject;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.locale.ResourceBundleMessagesGenerator;
import net.jawr.web.util.js.JavascriptEngine;
import test.net.jawr.web.FileUtils;
import test.net.jawr.web.util.js.rhino.RhinoEngine;

public class ResourceBundleMessageGeneratorTestCase {

	private ResourceBundleMessagesGenerator generator;

	private Locale defaultLocale;

	@Before
	public void setUp() {
		defaultLocale = Locale.getDefault();
		generator = new ResourceBundleMessagesGenerator();
	}

	@After
	public void tearDown() {
		Locale.setDefault(defaultLocale);
	}

	@Test
	public void testGenerateMessageBundle() throws Exception {

		// Force default locale
		Locale.setDefault(Locale.FRENCH);

		Properties prop = new Properties();
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_ADD_QUOTE_TO_MSG_KEY, "false");
		JawrConfig config = new JawrConfig("js", prop);
		GeneratorContext ctx = new GeneratorContext(config, "bundleLocale.messages");
		ctx.setLocale(null);
		Reader rd = generator.createResource(ctx);
		String result = IOUtils.toString(rd);
		assertEquals(readFile("bundleLocale/resultScript_fr.js"), FileUtils.removeCarriageReturn(result));

		ctx.setLocale(Locale.FRENCH);
		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
		assertEquals(readFile("bundleLocale/resultScript_fr.js"), FileUtils.removeCarriageReturn(result));

		ctx.setLocale(new Locale("es"));
		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
		assertEquals(readFile("bundleLocale/resultScript_es.js"), FileUtils.removeCarriageReturn(result));

	}

	@Test
	@Ignore
	public void testGenerateMessageBundleWithCharset() throws Exception {

		// Force default locale
		Locale.setDefault(Locale.FRENCH);

		Properties prop = new Properties();
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_RESOURCE_BUNDLE_CHARSET, "UTF-8");
		JawrConfig config = new JawrConfig("js", prop);

		GeneratorContext ctx = new GeneratorContext(config, "bundleLocale.messagesResourceBundleUTF8");
		ctx.setLocale(null);
		Reader rd = generator.createResource(ctx);
		String result = IOUtils.toString(rd);
		assertEquals(readFile("bundleLocale/resultScriptResourceBundleUTF8_fr.js"),
				FileUtils.removeCarriageReturn(result));

		ctx.setLocale(Locale.FRENCH);
		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
		assertEquals(readFile("bundleLocale/resultScriptResourceBundleUTF8_fr.js"),
				FileUtils.removeCarriageReturn(result));

		ctx.setLocale(new Locale("es"));
		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
		assertEquals(readFile("bundleLocale/resultScriptResourceBundleUTF8_es.js"),
				FileUtils.removeCarriageReturn(result));

	}

	private String readFile(String path) throws Exception {

		return readFile(path, "UTF-8");
	}

	private String readFile(String path, String charset) throws Exception {

		return FileUtils.readFile(FileUtils.getClassPathFile(path), charset);
	}

	@Test
	public void testGenerateMessageBundleSystemFallback() throws Exception {

		// Force default locale
		Locale.setDefault(Locale.FRENCH);

		Properties prop = new Properties();
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_FALLBACK_TO_SYSTEM_LOCALE, "false");
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_ADD_QUOTE_TO_MSG_KEY, "false");
		JawrConfig config = new JawrConfig("js", prop);

		GeneratorContext ctx = new GeneratorContext(config, "bundleLocale.messages");
		ctx.setLocale(null);
		Reader rd = generator.createResource(ctx);
		String result = IOUtils.toString(rd);
		assertEquals(readFile("bundleLocale/resultScript.js"), FileUtils.removeCarriageReturn(result));

		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_FALLBACK_TO_SYSTEM_LOCALE, "true");
		ctx.setLocale(null);
		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
		assertEquals(readFile("bundleLocale/resultScript_fr.js"), FileUtils.removeCarriageReturn(result));

	}

	@Test
	public void testGenerateMessageBundleAddQuoteToKey() throws Exception {

		// Force default locale
		Locale.setDefault(Locale.FRENCH);

		Properties prop = new Properties();
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_FALLBACK_TO_SYSTEM_LOCALE, "false");
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_ADD_QUOTE_TO_MSG_KEY, "true");
		JawrConfig config = new JawrConfig("js", prop);

		GeneratorContext ctx = new GeneratorContext(config, "bundleLocale.messages");
		ctx.setLocale(null);
		Reader rd = generator.createResource(ctx);
		String result = IOUtils.toString(rd);
		assertEquals(readFile("bundleLocale/resultScriptWithQuoteForKeys.js"), FileUtils.removeCarriageReturn(result));
	}

	@Test
	public void testBundleWithFilter() throws Exception {
		// Force default locale
		Locale.setDefault(Locale.FRENCH);

		Properties prop = new Properties();
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_FALLBACK_TO_SYSTEM_LOCALE, "false");
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_ADD_QUOTE_TO_MSG_KEY, "false");
		JawrConfig config = new JawrConfig("js", prop);

		GeneratorContext ctx = new GeneratorContext(config, "bundleLocale.messages|bundleLocale.errors[ui|error]");
		ctx.setLocale(null);
		Reader rd = generator.createResource(ctx);
		String result = IOUtils.toString(rd);
		assertEquals(readFile("bundleLocale/resultScriptWithFilter.js"), FileUtils.removeCarriageReturn(result));

		ctx.setLocale(Locale.FRENCH);
		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
		assertEquals(readFile("bundleLocale/resultScriptWithFilter_fr.js"), FileUtils.removeCarriageReturn(result));

		ctx.setLocale(new Locale("es"));
		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
		assertEquals(readFile("bundleLocale/resultScriptWithFilter_es.js"), FileUtils.removeCarriageReturn(result));
	}

	@Test
	public void testBundleWithNamespace() throws Exception {
		// Force default locale
		Locale.setDefault(Locale.FRENCH);

		Properties prop = new Properties();
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_FALLBACK_TO_SYSTEM_LOCALE, "false");
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_ADD_QUOTE_TO_MSG_KEY, "false");
		JawrConfig config = new JawrConfig("js", prop);

		GeneratorContext ctx = new GeneratorContext(config,
				"bundleLocale.messages|bundleLocale.errors(myMessages)");
		ctx.setLocale(null);
		Reader rd = generator.createResource(ctx);
		String result = IOUtils.toString(rd);
		
		// Checks result content instead of file to overcome the difference between JDK < 8 and JDK >= 8 
		// where the order of the message definition change 
		Map<String, String> expectedMsg = new HashMap<String, String>();
		expectedMsg.put("myMessages.error.login", "Login failed");
		expectedMsg.put("myMessages.ui.msg.hello.world", "Hello $ world!");
		expectedMsg.put("myMessages.ui.msg.salut","Mr.");
		expectedMsg.put("myMessages.warning.password.expired","Password expired");
		checkGeneratedMsgContent(result, expectedMsg);
		
		ctx.setLocale(Locale.FRENCH);
		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
		expectedMsg.clear();
		expectedMsg.put("myMessages.error.login", "Erreur lors de la connection");
		expectedMsg.put("myMessages.ui.msg.hello.world", "Â¡Bonjour $ š tout le monde!");
		expectedMsg.put("myMessages.ui.msg.salut","Mr.");
		expectedMsg.put("myMessages.warning.password.expired","Password expiré");
		expectedMsg.put("myMessages.ui.error.panel.title","Erreur");
		checkGeneratedMsgContent(result, expectedMsg);

		ctx.setLocale(new Locale("es"));
		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
		expectedMsg.clear();
		expectedMsg.put("myMessages.error.login", "Login failed");
		expectedMsg.put("myMessages.ui.msg.hello.world", "Â¡Hola $ Mundo!");
		expectedMsg.put("myMessages.ui.msg.salut","Mr.");
		expectedMsg.put("myMessages.warning.password.expired","Password expired");
		checkGeneratedMsgContent(result, expectedMsg);
//		assertEquals(
//				readFile("bundleLocale/resultScriptWithNamespace_es.js"),
//				FileUtils.removeCarriageReturn(result));
	}

	/**
	 * Checks if the generated script contains the expected messages Here we
	 * check result content instead of generated file content to overcome the
	 * difference between JDK < 8 and JDK >= 8 where the order of the message
	 * definition change
	 * 
	 * @param generatedScript
	 *            the generated script
	 * @param expectedMsg
	 *            the expected message
	 */
	private void checkGeneratedMsgContent(String generatedScript, Map<String, String> expectedMsg) {

		// Checks result content instead of file to overcome the difference
		// between JDK < 8 and JDK >= 8
		// where the order of the message definition change
		JavascriptEngine engine = new JavascriptEngine(true);
		engine.evaluate("msg.js", generatedScript);
		
		for(Map.Entry<String, String> entries : expectedMsg.entrySet()){
			String msg = (String) engine.evaluate(entries.getKey()+"()");
			assertEquals(entries.getValue(), msg);
		}		
	}
}
