package test.net.jawr.web.resource.bundle.generator.js.coffee;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;

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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import test.net.jawr.web.FileUtils;
import test.net.jawr.web.servlet.mock.MockServletContext;
import test.net.jawr.web.util.js.rhino.JSEngineUtils;

@RunWith(Parameterized.class)
public class CoffeeScriptGeneratorTestCase {

	private static Logger LOGGER = LoggerFactory
			.getLogger(CoffeeScriptGeneratorTestCase.class);

	@Parameter
	public String jsEngineName;

	@Parameters
	public static List<Object[]> jsEnginesToTestWith() {
		return Arrays.asList(new Object[][] {
				{ JawrConstant.JS_ENGINE_DEFAULT }, { "nashorn" } });
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

		when(pathIterator.next()).thenReturn(
				new BundlePath(null, "/temp.coffee"));
		when(pathIterator.nextPath()).thenReturn(
				new BundlePath(null, "/temp.coffee"));
		when(pathIterator.hasNext()).thenReturn(true, false, false);
		when(
				resourceBundlesHandler.getBundlePaths(Matchers.anyString(),
						Matchers.any(ConditionalCommentCallbackHandler.class),
						Matchers.anyMapOf(String.class, String.class)))
				.thenReturn(pathIterator);

		when(resourceBundlesHandler.resolveBundleForPath("/temp.coffe"))
				.thenReturn(bundle);

		servletContext.setAttribute(JawrConstant.JS_CONTEXT_ATTRIBUTE,
				resourceBundlesHandler);
		// getMockBundlesHandler(config, paths));
		config.setContext(servletContext);
		config.setServletMapping("/js");
		config.setCharsetName("UTF-8");
		GeneratorRegistry generatorRegistry = addGeneratorRegistryToConfig(
				config, JawrConstant.JS_TYPE);
		generator = new CoffeeScriptGenerator();

		ctx = new GeneratorContext(config, bundlePath);

		Reader tempReader = new StringReader(
				FileUtils
						.readClassPathFile("generator/js/coffeescript/temp.coffee"));
		when(
				rsReaderHandler.getResource(Matchers.anyString(),
						Matchers.anyBoolean(), Matchers.anyList())).thenReturn(
				tempReader);

		ctx.setResourceReaderHandler(rsReaderHandler);
		generatorRegistry.setResourceReaderHandler(ctx
				.getResourceReaderHandler());

		generator.setConfig(config);
		generator.afterPropertiesSet();
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
			StringWriter writer = new StringWriter();
			IOUtils.copy(rd, writer);
			Assert.assertEquals(
					FileUtils
							.readClassPathFile("generator/js/coffeescript/expected.js"),
					writer.getBuffer().toString());
		}
	}

	private GeneratorRegistry addGeneratorRegistryToConfig(JawrConfig config,
			String type) {
			GeneratorRegistry generatorRegistry = new GeneratorRegistry(type);
			generatorRegistry.setConfig(config);
			config.setGeneratorRegistry(generatorRegistry);
			return generatorRegistry;
	}
}
