package test.net.jawr.web.resource.bundle.generator.classpath.webjars;

import java.io.Reader;
import java.util.Properties;

import javax.servlet.ServletContext;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.classpath.webjars.WebJarsJSGenerator;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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

		final String bundlePath = "/bootstrap/3.2.0/js/bootstrap.js";

		config = new JawrConfig(JawrConstant.JS_TYPE, new Properties());
		ServletContext servletContext = new MockServletContext();

		config.setContext(servletContext);
		config.setServletMapping("/js");
		config.setCharsetName("UTF-8");

		config.setGeneratorRegistry(generatorRegistry);

		generator = new WebJarsJSGenerator();
		ctx = new GeneratorContext(config, bundlePath);
		ctx.setResourceReaderHandler(rsReaderHandler);
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
		Assert.assertEquals(
				FileUtils
						.readClassPathFile("generator/webjars/bootstrap_expected.js"),
						result);
	}

}
