package test.net.jawr.web.resource.bundle.locale;

import java.io.Reader;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Properties;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.locale.ResourceBundleMessagesGenerator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import test.net.jawr.web.FileUtils;

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
		GeneratorContext ctx = new GeneratorContext(config,
				"bundleLocale.messages");
		ctx.setLocale(null);
		Reader rd = generator.createResource(ctx);
		StringWriter swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(readFile("bundleLocale/resultScript_fr.js"),
				FileUtils.removeCarriageReturn(swr.toString()));

		ctx.setLocale(Locale.FRENCH);
		rd = generator.createResource(ctx);
		swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(readFile("bundleLocale/resultScript_fr.js"),
				FileUtils.removeCarriageReturn(swr.toString()));

		ctx.setLocale(new Locale("es"));
		rd = generator.createResource(ctx);
		swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(readFile("bundleLocale/resultScript_es.js"),
				FileUtils.removeCarriageReturn(swr.toString()));

	}

	@Test
	@Ignore
	public void testGenerateMessageBundleWithCharset() throws Exception {

		// Force default locale
		Locale.setDefault(Locale.FRENCH);

		Properties prop = new Properties();
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_RESOURCE_BUNDLE_CHARSET, "UTF-8");
		JawrConfig config = new JawrConfig("js", prop);
		
		GeneratorContext ctx = new GeneratorContext(config,
				"bundleLocale.messagesResourceBundleUTF8");
		ctx.setLocale(null);
		Reader rd = generator.createResource(ctx);
		StringWriter swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(
				readFile("bundleLocale/resultScriptResourceBundleUTF8_fr.js"),
				FileUtils.removeCarriageReturn(swr.toString()));

		ctx.setLocale(Locale.FRENCH);
		rd = generator.createResource(ctx);
		swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(
				readFile("bundleLocale/resultScriptResourceBundleUTF8_fr.js"),
				FileUtils.removeCarriageReturn(swr.toString()));

		ctx.setLocale(new Locale("es"));
		rd = generator.createResource(ctx);
		swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(
				readFile("bundleLocale/resultScriptResourceBundleUTF8_es.js"),
				FileUtils.removeCarriageReturn(swr.toString()));

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

		GeneratorContext ctx = new GeneratorContext(config,
				"bundleLocale.messages");
		ctx.setLocale(null);
		Reader rd = generator.createResource(ctx);
		StringWriter swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(readFile("bundleLocale/resultScript.js"),
				FileUtils.removeCarriageReturn(swr.toString()));

		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_FALLBACK_TO_SYSTEM_LOCALE, "true");
		ctx.setLocale(null);
		rd = generator.createResource(ctx);
		swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(readFile("bundleLocale/resultScript_fr.js"),
				FileUtils.removeCarriageReturn(swr.toString()));

	}
	
	@Test
	public void testGenerateMessageBundleAddQuoteToKey() throws Exception {

		// Force default locale
		Locale.setDefault(Locale.FRENCH);

		Properties prop = new Properties();
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_FALLBACK_TO_SYSTEM_LOCALE, "false");
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_ADD_QUOTE_TO_MSG_KEY, "true");
		JawrConfig config = new JawrConfig("js", prop);

		GeneratorContext ctx = new GeneratorContext(config,
				"bundleLocale.messages");
		ctx.setLocale(null);
		Reader rd = generator.createResource(ctx);
		StringWriter swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(readFile("bundleLocale/resultScriptWithQuoteForKeys.js"),
				FileUtils.removeCarriageReturn(swr.toString()));
	}

	@Test
	public void testBundleWithFilter() throws Exception {
		// Force default locale
		Locale.setDefault(Locale.FRENCH);

		Properties prop = new Properties();
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_FALLBACK_TO_SYSTEM_LOCALE, "false");
		prop.put(JawrConstant.JAWR_LOCALE_GENERATOR_ADD_QUOTE_TO_MSG_KEY, "false");
		JawrConfig config = new JawrConfig("js", prop);

		GeneratorContext ctx = new GeneratorContext(config,
				"bundleLocale.messages|bundleLocale.errors[ui|error]");
		ctx.setLocale(null);
		Reader rd = generator.createResource(ctx);
		StringWriter swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(readFile("bundleLocale/resultScriptWithFilter.js"),
				FileUtils.removeCarriageReturn(swr.toString()));

		ctx.setLocale(Locale.FRENCH);
		rd = generator.createResource(ctx);
		swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(
				readFile("bundleLocale/resultScriptWithFilter_fr.js"),
				FileUtils.removeCarriageReturn(swr.toString()));

		ctx.setLocale(new Locale("es"));
		rd = generator.createResource(ctx);
		swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(
				readFile("bundleLocale/resultScriptWithFilter_es.js"),
				FileUtils.removeCarriageReturn(swr.toString()));
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
		StringWriter swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(
				readFile("bundleLocale/resultScriptWithNamespace.js"),
				FileUtils.removeCarriageReturn(swr.toString()));

		ctx.setLocale(Locale.FRENCH);
		rd = generator.createResource(ctx);
		swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(
				readFile("bundleLocale/resultScriptWithNamespace_fr.js"),
				FileUtils.removeCarriageReturn(swr.toString()));

		ctx.setLocale(new Locale("es"));
		rd = generator.createResource(ctx);
		swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(
				readFile("bundleLocale/resultScriptWithNamespace_es.js"),
				FileUtils.removeCarriageReturn(swr.toString()));
	}
}
