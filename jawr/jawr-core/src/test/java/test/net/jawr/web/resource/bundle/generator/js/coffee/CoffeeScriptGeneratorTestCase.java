package test.net.jawr.web.resource.bundle.generator.js.coffee;

import static org.mockito.MockitoAnnotations.initMocks;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.when;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.js.coffee.CoffeeScriptGenerator;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.iterator.BundlePath;
import net.jawr.web.resource.bundle.iterator.ConditionalCommentCallbackHandler;
import net.jawr.web.resource.bundle.iterator.ResourceBundlePathsIterator;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import test.net.jawr.web.FileUtils;
import test.net.jawr.web.servlet.mock.MockServletContext;
import test.net.jawr.web.util.js.rhino.JSEngineUtils;

@RunWith(Parameterized.class)
public class CoffeeScriptGeneratorTestCase {

	private static Logger LOGGER = LoggerFactory.getLogger(CoffeeScriptGeneratorTestCase.class);

	private static String WORK_DIR = "workDirCoffee";

	@Parameter
	public String jsEngineName;

	@Parameters
	public static List<Object[]> jsEnginesToTestWith() {
		return Arrays.asList(new Object[][] { { JawrConstant.DEFAULT_JS_ENGINE }, { "nashorn" } });
	}

	private JawrConfig config;
	private GeneratorContext ctx;
	private CoffeeScriptGenerator generator;

	@Mock
	private ResourceBundlesHandler resourceBundlesHandler;

	@Mock
	private ResourceBundlePathsIterator pathIterator;

	@Mock
	private JoinableResourceBundle bundle;

	@Mock
	private ResourceReaderHandler rsReaderHandler;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {

		initMocks(this);

		final String bundlePath = "/temp.coffee";

		config = new JawrConfig("js", new Properties());
		ServletContext servletContext = new MockServletContext();

		when(pathIterator.next()).thenReturn(new BundlePath(null, bundlePath));
		when(pathIterator.nextPath()).thenReturn(new BundlePath(null, bundlePath));
		when(pathIterator.hasNext()).thenReturn(true, false, false);
		when(resourceBundlesHandler.getBundlePaths(Matchers.anyString(),
				Matchers.any(ConditionalCommentCallbackHandler.class), Matchers.anyMapOf(String.class, String.class)))
						.thenReturn(pathIterator);

		when(resourceBundlesHandler.resolveBundleForPath(bundlePath)).thenReturn(bundle);

		servletContext.setAttribute(JawrConstant.JS_CONTEXT_ATTRIBUTE, resourceBundlesHandler);
		// getMockBundlesHandler(config, paths));
		config.setContext(servletContext);
		config.setServletMapping("/js");
		config.setCharsetName("UTF-8");
		GeneratorRegistry generatorRegistry = addGeneratorRegistryToConfig(config, JawrConstant.JS_TYPE);
		generator = new CoffeeScriptGenerator();
		FileUtils.clearDirectory(FileUtils.getClasspathRootDir() + File.separator + WORK_DIR);
		FileUtils.createDir(WORK_DIR);
		generator.setWorkingDirectory(FileUtils.getClasspathRootDir() + "/" + WORK_DIR);

		ctx = new GeneratorContext(bundle, config, bundlePath);

		ctx.setResourceReaderHandler(rsReaderHandler);
		generatorRegistry.setResourceReaderHandler(ctx.getResourceReaderHandler());

		// Make sure that the version of temp.coffee is the right one
		FileUtils.copyFile("generator/js/coffeescript/temp.coffee.backup", "generator/js/coffeescript/temp.coffee");

		when(rsReaderHandler.getFilePath(Matchers.anyString()))
				.thenReturn(FileUtils.getClassPathFileAbsolutePath("generator/js/coffeescript/temp.coffee"));
		Mockito.doAnswer(new Answer<Long>() {

			@Override
			public Long answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				File f = new File((String) args[0]);
				Long result = f.lastModified();
				return result;
			}
		}).when(rsReaderHandler).getLastModified(Matchers.anyString());

		Mockito.doAnswer(new Answer<Reader>() {

			@Override
			public Reader answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				Reader rd = null;
				try {
					final String content = FileUtils.readClassPathFile("generator/js/coffeescript" + args[1]);
					rd = new StringReader(content);
				} catch (IOException ex) {
					// Do nothing
				}
				return rd;
			}
		}).when(rsReaderHandler).getResource(Matchers.any(JoinableResourceBundle.class), Matchers.anyString(),
				Matchers.anyBoolean(), (List<Class<?>>) Matchers.any());

		generator.setResourceReaderHandler(rsReaderHandler);
		generator.setConfig(config);
		generator.afterPropertiesSet();
	}

	@After
	public void tearDown() throws Exception {
		// Make sure that the version of temp.coffee is the right one
		FileUtils.copyFile("generator/js/coffeescript/temp.coffee.backup", "generator/js/coffeescript/temp.coffee");
	}

	/**
	 * Checks if the JS engine is available
	 * 
	 * @return true if JS engine is available
	 */
	private boolean isJsEngineAvailable() {
		return JSEngineUtils.isJsEngineAvailable(jsEngineName, LOGGER);
	}

	@Test
	public void testBundleGenerator() throws Exception {
		if (isJsEngineAvailable()) {

			Reader rd = generator.createResource(ctx);
			String content = IOUtils.toString(rd);
			Assert.assertEquals(FileUtils.readClassPathFile("generator/js/coffeescript/expected.js"), content);

			// Retrieve from cache in production mode
			rd = generator.createResource(ctx);
			content = IOUtils.toString(rd);
			Assert.assertEquals(FileUtils.readClassPathFile("generator/js/coffeescript/expected.js"), content);

			// Retrieve in debug mode (should be read from cache)
			ctx.setProcessingBundle(false);
			rd = generator.createResource(ctx);
			content = IOUtils.toString(rd);
			Assert.assertEquals(FileUtils.readClassPathFile("generator/js/coffeescript/expected.js"), content);

			// Retrieve in debug mode (should be read from cache)
			rd = generator.createResource(ctx);
			content = IOUtils.toString(rd);
			Assert.assertEquals(FileUtils.readClassPathFile("generator/js/coffeescript/expected.js"), content);

			FileUtils.copyFile("generator/js/coffeescript/temp2.coffee", "generator/js/coffeescript/temp.coffee");
			when(rsReaderHandler
					.getLastModified(FileUtils.getClassPathFileAbsolutePath("generator/js/coffeescript/temp.coffee")))
							.thenReturn(Calendar.getInstance().getTimeInMillis() + 3);

			ctx.setProcessingBundle(true);

			// Retrieve updated version in production mode
			rd = generator.createResource(ctx);
			content = IOUtils.toString(rd);
			Assert.assertEquals(FileUtils.readClassPathFile("generator/js/coffeescript/expected_updated.js"), content);

			// Retrieve updated version from cache in production mode
			rd = generator.createResource(ctx);
			content = IOUtils.toString(rd);
			Assert.assertEquals(FileUtils.readClassPathFile("generator/js/coffeescript/expected_updated.js"), content);

			// Retrieve updated version in debug mode from cache
			rd = generator.createResource(ctx);
			content = IOUtils.toString(rd);
			Assert.assertEquals(FileUtils.readClassPathFile("generator/js/coffeescript/expected_updated.js"), content);

			// Retrieve updated version in debug mode from cache
			rd = generator.createResource(ctx);
			content = IOUtils.toString(rd);
			Assert.assertEquals(FileUtils.readClassPathFile("generator/js/coffeescript/expected_updated.js"), content);

		}
	}

	private GeneratorRegistry addGeneratorRegistryToConfig(JawrConfig config, String type) {
		GeneratorRegistry generatorRegistry = new GeneratorRegistry(type);
		generatorRegistry.setConfig(config);
		config.setGeneratorRegistry(generatorRegistry);
		return generatorRegistry;
	}
}
