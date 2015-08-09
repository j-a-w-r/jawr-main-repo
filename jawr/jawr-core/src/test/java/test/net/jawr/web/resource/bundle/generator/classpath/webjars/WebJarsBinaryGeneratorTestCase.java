package test.net.jawr.web.resource.bundle.generator.classpath.webjars;

import java.io.InputStream;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.classpath.webjars.WebJarsBinaryResourceGenerator;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WebJarsBinaryGeneratorTestCase {

	private JawrConfig config;
	private GeneratorContext ctx;
	private WebJarsBinaryResourceGenerator generator;

	@Mock
	private ResourceReaderHandler rsReaderHandler;

	@Mock
	private GeneratorRegistry generatorRegistry;

	@Before
	public void setUp() throws Exception {

		generator = createGenerator();
		ctx = new GeneratorContext(config, generator.getResolver().getResourcePath(getResourceName()));
		ctx.setResourceReaderHandler(rsReaderHandler);
	}

	protected String getResourceName() {
		return "webjars:/bootstrap/3.2.0/fonts/glyphicons-halflings-regular.eot";
	}

	protected WebJarsBinaryResourceGenerator createGenerator() {
		return new WebJarsBinaryResourceGenerator();
	}

	@Test
	public void testWebJarsBinaryBundleGeneratorInProdMode() throws Exception {

		// Check result in Production mode
		ctx.setProcessingBundle(true);
		InputStream is = generator.createResourceAsStream(ctx);
		InputStream expectedResult = ClassLoaderResourceUtils.getResourceAsStream("generator/webjars/glyphicons-halflings-regular.eot", this);
		boolean equals = org.apache.commons.io.IOUtils.contentEquals(is, expectedResult);
		Assert.assertTrue(equals);

	}

	@Test
	public void testWebJarsBinaryBundleGeneratorInDebugMode() throws Exception {
		// Check result in debug mode
		ctx.setProcessingBundle(false);
		InputStream is = generator.createResourceAsStream(ctx);
		InputStream expectedResult = ClassLoaderResourceUtils.getResourceAsStream("generator/webjars/glyphicons-halflings-regular.eot", this);
		boolean equals = org.apache.commons.io.IOUtils.contentEquals(is, expectedResult);
		Assert.assertTrue(equals);
	}

}
