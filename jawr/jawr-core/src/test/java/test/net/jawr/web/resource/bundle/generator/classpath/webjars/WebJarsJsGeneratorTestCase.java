package test.net.jawr.web.resource.bundle.generator.classpath.webjars;

import java.io.File;
import java.io.Reader;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;

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

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.classpath.webjars.WebJarsJSGenerator;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import test.net.jawr.web.FileUtils;
import test.net.jawr.web.servlet.mock.MockServletContext;

@RunWith(MockitoJUnitRunner.class)
public class WebJarsJsGeneratorTestCase {

	private static final String WORK_DIR = "workDirWebJars";

	private JawrConfig config;
	private GeneratorContext ctx;
	private WebJarsJSGenerator generator;

	@Mock
	private ResourceReaderHandler rsReaderHandler;

	@Mock
	private GeneratorRegistry generatorRegistry;

	@Mock
	private JoinableResourceBundle bundle;
	
	@Before
	public void setUp() throws Exception {

		FileUtils.clearDirectory(FileUtils.getClasspathRootDir() + "/" + WORK_DIR);
		FileUtils.createDir(WORK_DIR);

		config = new JawrConfig(JawrConstant.JS_TYPE, new Properties());
		ServletContext servletContext = new MockServletContext();

		config.setContext(servletContext);
		config.setServletMapping("/js");
		config.setCharsetName("UTF-8");

		config.setGeneratorRegistry(generatorRegistry);

		generator = createGenerator();
		ctx = new GeneratorContext(bundle, config, generator.getResolver().getResourcePath(getResourceName()));
		generator.setResourceReaderHandler(rsReaderHandler);
		ctx.setResourceReaderHandler(rsReaderHandler);
		
		Mockito.doAnswer(new Answer<Long>() {
			@Override
			public Long answer(InvocationOnMock invocation) throws Throwable {
				File f = new File((String) invocation.getArguments()[0]);
				return f.lastModified();
			}
		}).when(rsReaderHandler).getLastModified(Matchers.anyString());

		generator.setWorkingDirectory(FileUtils.getClasspathRootDir() + "/" + WORK_DIR);
		generator.setConfig(config);
		generator.afterPropertiesSet();
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
		
		// Check retrieve from cache
		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
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
		
		// Check retrieve from cache
		rd = generator.createResource(ctx);
		result = IOUtils.toString(rd);
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
		String filePath = generator.getFilePath("webjars:/bootstrap/3.2.0/css/bootstrap.css");
		assertTrue(filePath.replace('\\', '/').endsWith("org/webjars/bootstrap/3.2.0/bootstrap-3.2.0.jar"));
	}
}
