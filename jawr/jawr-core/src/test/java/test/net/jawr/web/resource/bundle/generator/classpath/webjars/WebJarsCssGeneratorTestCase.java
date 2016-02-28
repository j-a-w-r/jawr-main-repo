package test.net.jawr.web.resource.bundle.generator.classpath.webjars;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.when;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.BinaryResourcesHandler;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.classpath.webjars.WebJarsCssGenerator;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import test.net.jawr.web.FileUtils;
import test.net.jawr.web.servlet.mock.MockServletContext;

@RunWith(MockitoJUnitRunner.class)
public class WebJarsCssGeneratorTestCase {

	private static final String WORK_DIR = "workDirWebJars";

	private JawrConfig config;
	private GeneratorContext ctx;
	private WebJarsCssGenerator generator;

	@Mock
	private ResourceReaderHandler rsReaderHandler;

	@Mock
	private ResourceReaderHandler binaryRsReaderHandler;

	@Mock
	private ResourceBundlesHandler cssBundleHandler;

	@Mock
	private GeneratorRegistry generatorRegistry;

	@Mock
	private JoinableResourceBundle bundle;

	@Before
	public void setUp() throws Exception {

		FileUtils.clearDirectory(FileUtils.getClasspathRootDir() + "/" + WORK_DIR);
		FileUtils.createDir(WORK_DIR);

		Properties props = new Properties();
		props.put("jawr.css.classpath.handle.image", "true");
		config = new JawrConfig(JawrConstant.CSS_TYPE, props);
		ServletContext servletContext = new MockServletContext();

		servletContext.setAttribute(JawrConstant.CSS_CONTEXT_ATTRIBUTE, cssBundleHandler);
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");

		config.setGeneratorRegistry(generatorRegistry);

		generator = createGenerator();
		ctx = new GeneratorContext(bundle, config, generator.getResolver().getResourcePath(getResourceName()));
		ctx.setResourceReaderHandler(rsReaderHandler);

		// Set up the Image servlet Jawr config
		JawrConfig binaryServletJawrConfig = new JawrConfig(JawrConstant.BINARY_TYPE, new Properties());
		binaryServletJawrConfig.setGeneratorRegistry(generatorRegistry);

		// Return a new ByteArrayInputStream each time where calling
		// getResourceAsStream
		Mockito.doAnswer(new Answer<InputStream>() {

			@Override
			public InputStream answer(InvocationOnMock invocation) throws Throwable {
				return new ByteArrayInputStream("fakeData".getBytes());
			}
		}).when(binaryRsReaderHandler).getResourceAsStream(Matchers.anyString());

		BinaryResourcesHandler binaryRsHandler = new BinaryResourcesHandler(binaryServletJawrConfig,
				binaryRsReaderHandler, null);
		servletContext.setAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE, binaryRsHandler);
		generator.setConfig(config);

		when(generatorRegistry
				.isGeneratedBinaryResource("webjars:/bootstrap/3.2.0/fonts/glyphicons-halflings-regular.eot"))
						.thenReturn(true);
		when(generatorRegistry
				.isGeneratedBinaryResource("webjars:/bootstrap/3.2.0/fonts/glyphicons-halflings-regular.woff"))
						.thenReturn(true);
		when(generatorRegistry
				.isGeneratedBinaryResource("webjars:/bootstrap/3.2.0/fonts/glyphicons-halflings-regular.ttf"))
						.thenReturn(true);
		when(generatorRegistry
				.isGeneratedBinaryResource("webjars:/bootstrap/3.2.0/fonts/glyphicons-halflings-regular.svg"))
						.thenReturn(true);
		when(generatorRegistry.isHandlingCssImage(Matchers.anyString())).thenReturn(true);

		generator.setWorkingDirectory(FileUtils.getClasspathRootDir() + "/" + WORK_DIR);
		generator.setResourceReaderHandler(rsReaderHandler);
		generator.afterPropertiesSet();
	}

	protected String getResourceName() {
		return "webjars:/bootstrap/3.2.0/css/bootstrap.css";
	}

	protected WebJarsCssGenerator createGenerator() {
		return new WebJarsCssGenerator();
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteDirectory(FileUtils.getClasspathRootDir() + "/" + WORK_DIR);
	}

	@Test
	public void testWebJarsCssBundleGenerator() throws Exception {

		// Check result in Production mode
		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		String result = IOUtils.toString(rd);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/webjars/bootstrap_prod_expected.css"), result);

		// Check result in Production mode from cache
		ctx.setProcessingBundle(true);
		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/webjars/bootstrap_prod_expected.css"), result);

		// Check result in debug mode
		ctx.setProcessingBundle(false);
		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/webjars/bootstrap_debug_expected.css"), result);

		// Check result in debug mode
		ctx.setProcessingBundle(false);
		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/webjars/bootstrap_debug_expected.css"), result);

	}

	@Test
	public void testIsDirectory() throws Exception {
		assertTrue(generator.isDirectory("webjars:/bootstrap/3.2.0/"));
		assertFalse(generator.isDirectory("webjars:/bootstrap/3.2.0/css/bootstrap.css"));
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
	public void testGetFilePathFromJar() {
		String filePath = generator.getFilePath("webjars:/bootstrap/3.2.0/css/bootstrap.css");
		assertTrue(filePath.replace('\\', '/').endsWith("org/webjars/bootstrap/3.2.0/bootstrap-3.2.0.jar"));
	}

}
