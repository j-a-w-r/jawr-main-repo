package test.net.jawr.web.resource.bundle.locale;

import java.io.Reader;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Properties;

import junit.framework.Assert;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.locale.ResourceBundleMessagesGenerator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.net.jawr.web.FileUtils;

public class ResourceBundleMessageGeneratorTestCase {

	private ResourceBundleMessagesGenerator generator;
	
	private Locale defaultLocale;
	
	@Before
	public void setUp(){
		defaultLocale = Locale.getDefault();
		generator = new ResourceBundleMessagesGenerator();
	}
	
	@After
	public void tearDown() {
		Locale.setDefault(defaultLocale);
	}
	
	@Test
	public void testGenerateMessageBundle() throws Exception{
		
		// Force default locale
		Locale.setDefault(Locale.FRENCH);

		JawrConfig config = new JawrConfig("js", new Properties());
		GeneratorContext ctx = new GeneratorContext(config, "bundleLocale.messages");
		ctx.setLocale(null);
		Reader rd = generator.createResource(ctx);
		StringWriter swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(readFile("bundleLocale/resultScript_fr.js"), FileUtils.removeCarriageReturn(swr.toString()));
	
		ctx.setLocale(Locale.FRENCH);
		rd = generator.createResource(ctx);
		swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(readFile("bundleLocale/resultScript_fr.js"), FileUtils.removeCarriageReturn(swr.toString()));
	
		ctx.setLocale(new Locale("es"));
		rd = generator.createResource(ctx);
		swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(readFile("bundleLocale/resultScript_es.js"), FileUtils.removeCarriageReturn(swr.toString()));
		
	}
	
	private String readFile(String path) throws Exception{
		
		return FileUtils.readFile(FileUtils.getClassPathFile(path), "UTF-8");
	}
	
	
	@Test
	public void testGenerateMessageBundleSystemFallback() throws Exception{
		
		// Force default locale
		Locale.setDefault(Locale.FRENCH);
		
		Properties prop = new Properties();
		prop.put("jawr.locale.generator.fallbackToSystemLocale", "false");
		JawrConfig config = new JawrConfig("js", prop);
		
		GeneratorContext ctx = new GeneratorContext(config, "bundleLocale.messages");
		ctx.setLocale(null);
		Reader rd = generator.createResource(ctx);
		StringWriter swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(readFile("bundleLocale/resultScript.js"), FileUtils.removeCarriageReturn(swr.toString()));
		
		prop.put("jawr.locale.generator.fallbackToSystemLocale", "true");
		ctx.setLocale(null);
		rd = generator.createResource(ctx);
		swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(readFile("bundleLocale/resultScript_fr.js"), FileUtils.removeCarriageReturn(swr.toString()));
		
	}
	
	@Test
	public void testBundleWithFilter() throws Exception{
		// Force default locale
		Locale.setDefault(Locale.FRENCH);
		
		Properties prop = new Properties();
		prop.put("jawr.locale.generator.fallbackToSystemLocale", "false");
		JawrConfig config = new JawrConfig("js", prop);
		
		GeneratorContext ctx = new GeneratorContext(config, "bundleLocale.messages|bundleLocale.errors[ui|error]");
		ctx.setLocale(null);
		Reader rd = generator.createResource(ctx);
		StringWriter swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(readFile("bundleLocale/resultScriptWithFilter.js"), FileUtils.removeCarriageReturn(swr.toString()));
		
		ctx.setLocale(Locale.FRENCH);
		rd = generator.createResource(ctx);
		swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(readFile("bundleLocale/resultScriptWithFilter_fr.js"), FileUtils.removeCarriageReturn(swr.toString()));
	
		ctx.setLocale(new Locale("es"));
		rd = generator.createResource(ctx);
		swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(readFile("bundleLocale/resultScriptWithFilter_es.js"), FileUtils.removeCarriageReturn(swr.toString()));
	}
	
	@Test
	public void testBundleWithNamespace() throws Exception{
		// Force default locale
		Locale.setDefault(Locale.FRENCH);
		
		Properties prop = new Properties();
		prop.put("jawr.locale.generator.fallbackToSystemLocale", "false");
		JawrConfig config = new JawrConfig("js", prop);
		
		GeneratorContext ctx = new GeneratorContext(config, "bundleLocale.messages|bundleLocale.errors(myMessages)");
		ctx.setLocale(null);
		Reader rd = generator.createResource(ctx);
		StringWriter swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(readFile("bundleLocale/resultScriptWithNamespace.js"), FileUtils.removeCarriageReturn(swr.toString()));
		
		ctx.setLocale(Locale.FRENCH);
		rd = generator.createResource(ctx);
		swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(readFile("bundleLocale/resultScriptWithNamespace_fr.js"), FileUtils.removeCarriageReturn(swr.toString()));
	
		ctx.setLocale(new Locale("es"));
		rd = generator.createResource(ctx);
		swr = new StringWriter();
		IOUtils.copy(rd, swr);
		Assert.assertEquals(readFile("bundleLocale/resultScriptWithNamespace_es.js"), FileUtils.removeCarriageReturn(swr.toString()));
	}
}
