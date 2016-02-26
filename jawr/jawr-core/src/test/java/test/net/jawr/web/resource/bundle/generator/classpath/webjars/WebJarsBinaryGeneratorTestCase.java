package test.net.jawr.web.resource.bundle.generator.classpath.webjars;

import java.io.InputStream;
import java.util.Set;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class WebJarsBinaryGeneratorTestCase {

	private JawrConfig config;
	private GeneratorContext ctx;
	private WebJarsBinaryResourceGenerator generator;

	@Mock
	private ResourceReaderHandler rsReaderHandler;

	@Mock
	private GeneratorRegistry generatorRegistry;

	@Mock
	private JoinableResourceBundle bundle;
	
	@Before
	public void setUp() throws Exception {

		generator = createGenerator();
		ctx = new GeneratorContext(bundle, config, generator.getResolver().getResourcePath(getResourceName()));
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
		assertNull(generator.getFilePath("webjars:/bootstrap/3.2.0/fonts/glyphicons-halflings-regular.eot"));
	}
}
