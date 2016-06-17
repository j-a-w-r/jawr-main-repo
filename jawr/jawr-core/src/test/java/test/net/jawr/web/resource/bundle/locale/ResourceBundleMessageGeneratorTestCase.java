package test.net.jawr.web.resource.bundle.locale;

import static org.mockito.Mockito.when;
import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.locale.ResourceBundleMessagesGenerator;
import net.jawr.web.resource.bundle.mappings.FilePathMapping;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.util.js.JavascriptEngine;
import test.net.jawr.web.FileUtils;

@RunWith(MockitoJUnitRunner.class)
public class ResourceBundleMessageGeneratorTestCase {

	private static final String WORK_DIR = "workDirResourceMessages";

	private ResourceBundleMessagesGenerator generator;

	private Locale defaultLocale;

	@Mock
	private JoinableResourceBundle bundle;

	private final List<FilePathMapping> fMappings = new ArrayList<>();

	@Mock
	private ResourceReaderHandler rsReaderHandler;

	@Before
	public void setUp() throws Exception {

		FileUtils.clearDirectory(FileUtils.getClasspathRootDir() + "/" + WORK_DIR);
		FileUtils.createDir(WORK_DIR);
		FileUtils.copyFile("bundleLocale/messages_fr.properties.backup", "bundleLocale/messages_fr.properties");

		defaultLocale = Locale.getDefault();
		generator = new ResourceBundleMessagesGenerator();

		Mockito.doAnswer(new Answer<Long>() {
			@Override
			public Long answer(InvocationOnMock invocation) throws Throwable {
				File f = new File((String) invocation.getArguments()[0]);
				return f.lastModified();
			}
		}).when(rsReaderHandler).getLastModified(Matchers.anyString());

		generator.setWorkingDirectory(FileUtils.getClasspathRootDir() + "/" + WORK_DIR);
		generator.setResourceReaderHandler(rsReaderHandler);

		fMappings.clear();
		when(bundle.getLinkedFilePathMappings()).thenReturn(fMappings);
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteDirectory(FileUtils.getClasspathRootDir() + "/" + WORK_DIR);
		FileUtils.copyFile("bundleLocale/messages_fr.properties.backup", "bundleLocale/messages_fr.properties");

		Locale.setDefault(defaultLocale);
	}

	@Test
	public void testGenerateMessageBundle() throws Exception {

		// Force default locale
		Locale.setDefault(Locale.FRENCH);

		Properties prop = new Properties();
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_ADD_QUOTE_TO_MSG_KEY, "false");
		JawrConfig config = new JawrConfig("js", prop);
		generator.setConfig(config);
		generator.afterPropertiesSet();
		GeneratorContext ctx = new GeneratorContext(bundle, config, "bundleLocale.messages");
		ctx.setLocale(null);
		Reader rd = generator.createResource(ctx);
		String result = IOUtils.toString(rd);
		assertEquals(readFile("bundleLocale/resultScript_fr.js"), FileUtils.removeCarriageReturn(result));

		assertFalse(ctx.isRetrievedFromCache());
		assertEquals(fMappings.size(), 2);

		// Check linked resources
		File f = FileUtils.getClassPathFile("bundleLocale/messages.properties");
		FilePathMapping fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));
		f = FileUtils.getClassPathFile("bundleLocale/messages_fr.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));

		ctx.setLocale(Locale.FRENCH);
		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
		assertEquals(readFile("bundleLocale/resultScript_fr.js"), FileUtils.removeCarriageReturn(result));

		assertFalse(ctx.isRetrievedFromCache());

		ctx.setLocale(new Locale("es"));
		ctx.setRetrievedFromCache(false);
		fMappings.clear();
		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
		assertEquals(readFile("bundleLocale/resultScript_es.js"), FileUtils.removeCarriageReturn(result));

		assertFalse(ctx.isRetrievedFromCache());

		// Check linked resources
		f = FileUtils.getClassPathFile("bundleLocale/messages.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));

		f = FileUtils.getClassPathFile("bundleLocale/messages_es.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));
	}

	@Test
	@Ignore
	public void testGenerateMessageBundleWithCharset() throws Exception {

		// Force default locale
		Locale.setDefault(Locale.FRENCH);

		Properties prop = new Properties();
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_RESOURCE_BUNDLE_CHARSET, "UTF-8");
		JawrConfig config = new JawrConfig("js", prop);
		generator.setConfig(config);
		generator.afterPropertiesSet();

		GeneratorContext ctx = new GeneratorContext(bundle, config, "bundleLocale.messagesResourceBundleUTF8");
		ctx.setLocale(null);
		Reader rd = generator.createResource(ctx);
		String result = IOUtils.toString(rd);
		assertEquals(readFile("bundleLocale/resultScriptResourceBundleUTF8_fr.js"),
				FileUtils.removeCarriageReturn(result));

		assertFalse(ctx.isRetrievedFromCache());
		assertEquals(fMappings.size(), 2);

		// Check linked resources
		File f = FileUtils.getClassPathFile("bundleLocale/messagesResourceBundleUTF8.properties");
		FilePathMapping fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));
		f = FileUtils.getClassPathFile("bundleLocale/messagesResourceBundleUTF8_fr.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));

		ctx.setLocale(Locale.FRENCH);
		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
		assertEquals(readFile("bundleLocale/resultScriptResourceBundleUTF8_fr.js"),
				FileUtils.removeCarriageReturn(result));

		assertTrue(ctx.isRetrievedFromCache());
		assertEquals(fMappings.size(), 2);

		ctx.setLocale(new Locale("es"));
		fMappings.clear();

		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
		assertEquals(readFile("bundleLocale/resultScriptResourceBundleUTF8_es.js"),
				FileUtils.removeCarriageReturn(result));

		assertFalse(ctx.isRetrievedFromCache());
		assertEquals(fMappings.size(), 2);

		// Check linked resources
		f = FileUtils.getClassPathFile("bundleLocale/messagesResourceBundleUTF8.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));
		f = FileUtils.getClassPathFile("bundleLocale/messagesResourceBundleUTF8_es.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));

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
		generator.setConfig(config);
		generator.afterPropertiesSet();

		GeneratorContext ctx = new GeneratorContext(bundle, config, "bundleLocale.messages");
		ctx.setLocale(null);
		Reader rd = generator.createResource(ctx);
		String result = IOUtils.toString(rd);
		assertEquals(readFile("bundleLocale/resultScript.js"), FileUtils.removeCarriageReturn(result));

		assertFalse(ctx.isRetrievedFromCache());
		assertEquals(fMappings.size(), 1);

		// Check linked resources
		File f = FileUtils.getClassPathFile("bundleLocale/messages.properties");
		FilePathMapping fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));

		// Change fallback to system locale property to true
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_FALLBACK_TO_SYSTEM_LOCALE, "true");
		generator.setConfig(config);
		generator.afterPropertiesSet();
		ctx.setLocale(null);
		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
		assertEquals(readFile("bundleLocale/resultScript_fr.js"), FileUtils.removeCarriageReturn(result));

		assertFalse(ctx.isRetrievedFromCache());
		assertEquals(fMappings.size(), 2);

		// Check linked resources
		f = FileUtils.getClassPathFile("bundleLocale/messages.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));
		f = FileUtils.getClassPathFile("bundleLocale/messages_fr.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));

	}

	@Test
	public void testGenerateMessageBundleAddQuoteToKey() throws Exception {

		// Force default locale
		Locale.setDefault(Locale.FRENCH);

		Properties prop = new Properties();
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_FALLBACK_TO_SYSTEM_LOCALE, "false");
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_ADD_QUOTE_TO_MSG_KEY, "true");
		JawrConfig config = new JawrConfig("js", prop);
		generator.setConfig(config);
		generator.afterPropertiesSet();

		GeneratorContext ctx = new GeneratorContext(bundle, config, "bundleLocale.messages");
		ctx.setLocale(null);
		Reader rd = generator.createResource(ctx);
		String result = IOUtils.toString(rd);
		assertEquals(readFile("bundleLocale/resultScriptWithQuoteForKeys.js"), FileUtils.removeCarriageReturn(result));

		assertFalse(ctx.isRetrievedFromCache());
		assertEquals(1, fMappings.size());

		// Check linked resources
		File f = FileUtils.getClassPathFile("bundleLocale/messages.properties");
		FilePathMapping fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));

	}

	@Test
	public void testBundleWithFilter() throws Exception {
		// Force default locale
		Locale.setDefault(Locale.FRENCH);

		Properties prop = new Properties();
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_FALLBACK_TO_SYSTEM_LOCALE, "false");
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_ADD_QUOTE_TO_MSG_KEY, "false");
		JawrConfig config = new JawrConfig("js", prop);
		generator.setConfig(config);
		generator.afterPropertiesSet();

		GeneratorContext ctx = new GeneratorContext(bundle, config,
				"bundleLocale.messages|bundleLocale.errors[ui|error]");
		ctx.setLocale(null);
		Reader rd = generator.createResource(ctx);
		String result = IOUtils.toString(rd);
		// Checks result content instead of file to overcome the difference
		// between JDK < 8 and JDK >= 8
		// where the order of the message definition changed
		Map<String, String> expectedMsg = new HashMap<String, String>();
		expectedMsg.put("messages.error.login", "Login failed");
		expectedMsg.put("messages.ui.msg.hello.world", "Hello $ world!");
		expectedMsg.put("messages.ui.msg.salut", "Mr.");
		checkGeneratedMsgContent(result, expectedMsg);

		assertFalse(ctx.isRetrievedFromCache());
		assertEquals(fMappings.size(), 2);

		// Check linked resources
		File f = FileUtils.getClassPathFile("bundleLocale/messages.properties");
		FilePathMapping fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));
		f = FileUtils.getClassPathFile("bundleLocale/errors.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));

		ctx.setLocale(Locale.FRENCH);
		fMappings.clear();
		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
		expectedMsg.clear();
		expectedMsg.put("messages.error.login", "Erreur lors de la connection");
		expectedMsg.put("messages.ui.msg.hello.world", "Â¡Bonjour $ š tout le monde!");
		expectedMsg.put("messages.ui.msg.salut", "Mr.");
		expectedMsg.put("messages.ui.error.panel.title", "Erreur");

		assertFalse(ctx.isRetrievedFromCache());
		assertEquals(fMappings.size(), 4);

		// Check linked resources
		f = FileUtils.getClassPathFile("bundleLocale/messages.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));
		f = FileUtils.getClassPathFile("bundleLocale/messages_fr.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));
		f = FileUtils.getClassPathFile("bundleLocale/errors.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));
		f = FileUtils.getClassPathFile("bundleLocale/errors_fr.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));

		ctx.setLocale(new Locale("es"));
		fMappings.clear();
		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
		expectedMsg.clear();
		expectedMsg.put("messages.error.login", "Login failed");
		expectedMsg.put("messages.ui.msg.hello.world", "Â¡Hola $ Mundo!");
		expectedMsg.put("messages.ui.msg.salut", "Mr.");
		checkGeneratedMsgContent(result, expectedMsg);

		assertFalse(ctx.isRetrievedFromCache());
		assertEquals(fMappings.size(), 4);

		// Check linked resources
		f = FileUtils.getClassPathFile("bundleLocale/messages.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));
		f = FileUtils.getClassPathFile("bundleLocale/messages_es.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));
		f = FileUtils.getClassPathFile("bundleLocale/errors.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));
		f = FileUtils.getClassPathFile("bundleLocale/errors_es.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));
	}

	@Test
	public void testBundleWithNamespace() throws Exception {
		// Force default locale
		Locale.setDefault(Locale.FRENCH);

		Properties prop = new Properties();
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_FALLBACK_TO_SYSTEM_LOCALE, "false");
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_ADD_QUOTE_TO_MSG_KEY, "false");
		JawrConfig config = new JawrConfig("js", prop);
		generator.setConfig(config);
		generator.afterPropertiesSet();

		GeneratorContext ctx = new GeneratorContext(bundle, config,
				"bundleLocale.messages|bundleLocale.errors(myMessages)");
		ctx.setLocale(null);
		Reader rd = generator.createResource(ctx);
		String result = IOUtils.toString(rd);

		// Checks result content instead of file to overcome the difference
		// between JDK < 8 and JDK >= 8
		// where the order of the message definition changed
		Map<String, String> expectedMsg = new HashMap<>();
		expectedMsg.put("myMessages.error.login", "Login failed");
		expectedMsg.put("myMessages.ui.msg.hello.world", "Hello $ world!");
		expectedMsg.put("myMessages.ui.msg.salut", "Mr.");
		expectedMsg.put("myMessages.warning.password.expired", "Password expired");
		checkGeneratedMsgContent(result, expectedMsg);

		assertFalse(ctx.isRetrievedFromCache());
		assertEquals(2, fMappings.size());

		// Check linked resources
		File f = FileUtils.getClassPathFile("bundleLocale/messages.properties");
		FilePathMapping fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));
		f = FileUtils.getClassPathFile("bundleLocale/errors.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));

		ctx.setLocale(Locale.FRENCH);
		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
		expectedMsg.clear();
		expectedMsg.put("myMessages.error.login", "Erreur lors de la connection");
		expectedMsg.put("myMessages.ui.msg.hello.world", "Â¡Bonjour $ š tout le monde!");
		expectedMsg.put("myMessages.ui.msg.salut", "Mr.");
		expectedMsg.put("myMessages.warning.password.expired", "Password expiré");
		expectedMsg.put("myMessages.ui.error.panel.title", "Erreur");
		checkGeneratedMsgContent(result, expectedMsg);

		assertFalse(ctx.isRetrievedFromCache());
		assertEquals(fMappings.size(), 4);

		// Check linked resources
		f = FileUtils.getClassPathFile("bundleLocale/messages.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));
		f = FileUtils.getClassPathFile("bundleLocale/messages_fr.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));
		f = FileUtils.getClassPathFile("bundleLocale/errors.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));
		f = FileUtils.getClassPathFile("bundleLocale/errors_fr.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));

		ctx.setLocale(new Locale("es"));
		fMappings.clear();
		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
		expectedMsg.clear();
		expectedMsg.put("myMessages.error.login", "Login failed");
		expectedMsg.put("myMessages.ui.msg.hello.world", "Â¡Hola $ Mundo!");
		expectedMsg.put("myMessages.ui.msg.salut", "Mr.");
		expectedMsg.put("myMessages.warning.password.expired", "Password expired");
		checkGeneratedMsgContent(result, expectedMsg);

		assertFalse(ctx.isRetrievedFromCache());
		assertEquals(fMappings.size(), 4);

		// Check linked resources
		f = FileUtils.getClassPathFile("bundleLocale/messages.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));
		f = FileUtils.getClassPathFile("bundleLocale/messages_es.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));
		f = FileUtils.getClassPathFile("bundleLocale/errors.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));
		f = FileUtils.getClassPathFile("bundleLocale/errors_es.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));
	}

	@Test
	public void testCacheReload() throws Exception {

		// Force default locale
		Locale.setDefault(Locale.FRENCH);

		Properties prop = new Properties();
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_ADD_QUOTE_TO_MSG_KEY, "false");
		JawrConfig config = new JawrConfig("js", prop);
		generator.setConfig(config);
		generator.afterPropertiesSet();
		GeneratorContext ctx = new GeneratorContext(bundle, config, "bundleLocale.messages");
		ctx.setLocale(Locale.FRENCH);
		Reader rd = generator.createResource(ctx);
		String result = IOUtils.toString(rd);
		assertEquals(readFile("bundleLocale/resultScript_fr.js"), FileUtils.removeCarriageReturn(result));

		assertFalse(ctx.isRetrievedFromCache());
		assertEquals(fMappings.size(), 2);

		// Check linked resources
		File f = FileUtils.getClassPathFile("bundleLocale/messages.properties");
		FilePathMapping fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));
		f = FileUtils.getClassPathFile("bundleLocale/messages_fr.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));

		// Update the resource bundle
		FileUtils.copyFile("bundleLocale/messages_new_fr.properties", "bundleLocale/messages_fr.properties");
		ctx.setLocale(Locale.FRENCH);
		fMappings.clear();
		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
		assertEquals(readFile("bundleLocale/resultScript_fr_updated.js"), FileUtils.removeCarriageReturn(result));

		assertFalse(ctx.isRetrievedFromCache());
		assertEquals(2, fMappings.size());

		// Check linked resources
		f = FileUtils.getClassPathFile("bundleLocale/messages.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));
		f = FileUtils.getClassPathFile("bundleLocale/messages_fr.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));

		// Check retrieve from cache
		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
		assertEquals(readFile("bundleLocale/resultScript_fr_updated.js"), FileUtils.removeCarriageReturn(result));

		assertTrue(ctx.isRetrievedFromCache());
		assertEquals(2, fMappings.size());

		// Check linked resources
		f = FileUtils.getClassPathFile("bundleLocale/messages.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));
		f = FileUtils.getClassPathFile("bundleLocale/messages_fr.properties");
		fMapping = new FilePathMapping(f);
		assertTrue(fMappings.contains(fMapping));

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

		for (Map.Entry<String, String> entries : expectedMsg.entrySet()) {
			String msg = (String) engine.evaluate(entries.getKey() + "()");
			assertEquals(entries.getValue(), msg);
		}
	}
}
