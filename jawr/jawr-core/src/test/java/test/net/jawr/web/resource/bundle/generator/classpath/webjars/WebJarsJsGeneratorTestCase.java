package test.net.jawr.web.resource.bundle.generator.classpath.webjars;

import java.io.Reader;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.classpath.webjars.WebJarsJSGenerator;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import test.net.jawr.web.FileUtils;
import test.net.jawr.web.servlet.mock.MockServletContext;

@RunWith(MockitoJUnitRunner.class)
public class WebJarsJsGeneratorTestCase {

	private JawrConfig config;
	private GeneratorContext ctx;
	private WebJarsJSGenerator generator;

	@Mock
	private ResourceReaderHandler rsReaderHandler;

	@Mock
	private GeneratorRegistry generatorRegistry;

	@Before
	public void setUp() throws Exception {

		config = new JawrConfig(JawrConstant.JS_TYPE, new Properties());
		ServletContext servletContext = new MockServletContext();

		config.setContext(servletContext);
		config.setServletMapping("/js");
		config.setCharsetName("UTF-8");

		config.setGeneratorRegistry(generatorRegistry);

		generator = createGenerator();
		ctx = new GeneratorContext(config, generator.getResolver().getResourcePath(getResourceName()));
		ctx.setResourceReaderHandler(rsReaderHandler);
	}

	protected String getResourceName() {
		return "webjars:/bootstrap/3.2.0/js/bootstrap.js";
	}

	protected WebJarsJSGenerator createGenerator() {
		return new WebJarsJSGenerator();
	}

	@Test
	public void testWebJarsJsBundleGeneratorInProdMode() throws Exception {

		// Check result in Production mode
		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		String result = IOUtils.toString(rd);
		Assert.assertEquals(FileUtils
				.readClassPathFile("generator/webjars/bootstrap_expected.js"),
				result);

	}

	@Test
	public void testWebJarsJsBundleGeneratorInDebugMode() throws Exception {
		// Check result in debug mode
		ctx.setProcessingBundle(false);
		Reader rd = generator.createResource(ctx);
		String result = IOUtils.toString(rd);
		assertEquals(
				FileUtils
						.readClassPathFile("generator/webjars/bootstrap_expected.js"),
						result);
	}

	@Test
	public void testIsDirectory() throws Exception {
		assertTrue(generator.isDirectory("webjars:/bootstrap/3.2.0/"));
		assertFalse(generator.isDirectory("webjars:/bootstrap/3.2.0/js/bootstrap.js"));
	}

	@Test
	public void testGetResourceNames() throws Exception {
		
		Set<String> resources = generator.getResourceNames("webjars:/bootstrap/3.2.0/");
		assertEquals(5, resources.size());
		assertTrue(resources.contains("css/"));
		assertTrue(resources.contains("fonts/"));
		assertTrue(resources.contains("js/"));
		assertTrue(resources.contains("less/"));
		assertTrue(resources.contains("webjars-requirejs.js"));
	}
	
	@Test
	public void testGetFilePathFromJar(){
		assertNull(generator.getFilePath("webjars:/bootstrap/3.2.0/js/bootstrap.js"));
	}
}
