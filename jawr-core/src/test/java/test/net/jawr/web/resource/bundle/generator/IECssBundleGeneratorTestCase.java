/**
 * 
 */
package test.net.jawr.web.resource.bundle.generator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.BinaryResourcesHandler;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.IECssBundleGenerator;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.iterator.BundlePath;
import net.jawr.web.resource.bundle.iterator.ConditionalCommentCallbackHandler;
import net.jawr.web.resource.bundle.iterator.ResourceBundlePathsIterator;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import test.net.jawr.web.FileUtils;
import test.net.jawr.web.servlet.mock.MockServletContext;

/**
 * IECssBundleGeneratorTestCase
 * 
 * @author Ibrahim Chaehoi
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class IECssBundleGeneratorTestCase {

	private JawrConfig config;
	private GeneratorContext ctx;
	private IECssBundleGenerator generator;

	@Mock
	private GeneratorRegistry generatorRegistry;

	@Mock
	private GeneratorRegistry binaryGeneratorRegistry;

	@Mock
	private ResourceBundlesHandler resourceBundlesHandler;

	@Mock
	private ResourceBundlePathsIterator pathIterator;

	@Mock
	private JoinableResourceBundle bundle;
	
	@Mock
	private ResourceReaderHandler rsReaderHandler;
	
	@Mock
	private ResourceReaderHandler binaryRsReaderHandler;
	
	@Before
	public void setUp() throws Exception {

		// Bundle path (full url would be: /servletMapping/prefix/css/bundle.css
		final String bundlePath = "/css/bundle.css";

		config = new JawrConfig("css", new Properties());
		ServletContext servletContext = new MockServletContext();

		Map<String, VariantSet> emptyVariants = Collections.emptyMap();
		when(bundle.getVariants()).thenReturn(emptyVariants);
		
		when(pathIterator.next()).thenReturn(new BundlePath(null, "/temp.css"), new BundlePath(null, "jar:/style.css"));
		when(pathIterator.nextPath()).thenReturn(new BundlePath(null, "/temp.css"), new BundlePath(null, "jar:/style.css"));
		when(pathIterator.hasNext()).thenReturn(true, true, true, false, false);
		when(
				resourceBundlesHandler.getBundlePaths(Matchers.anyString(),
						Matchers.any(ConditionalCommentCallbackHandler.class),
						Matchers.anyMapOf(String.class, String.class)))
				.thenReturn(pathIterator);

		when(resourceBundlesHandler.resolveBundleForPath("/bundle.css")).thenReturn(bundle);
		servletContext.setAttribute(JawrConstant.CSS_CONTEXT_ATTRIBUTE, resourceBundlesHandler);
				//getMockBundlesHandler(config, paths));
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");

		when(generatorRegistry.isGeneratedBinaryResource(Matchers.startsWith("jar:")))
				.thenReturn(true);
		when(generatorRegistry.isHandlingCssImage(Matchers.startsWith("jar:")))
				.thenReturn(true);
		config.setGeneratorRegistry(generatorRegistry);

		generator = new IECssBundleGenerator();
		ctx = new GeneratorContext(config, bundlePath);
		
		Reader tempReader = new StringReader(FileUtils
						.readClassPathFile("generator/ieCssBundle/temp.css"));
		when(rsReaderHandler.getResource("/temp.css", true)).thenReturn(tempReader);
		
		Reader jarStyleReader = new StringReader(FileUtils
				.readClassPathFile("generator/ieCssBundle/jar_style.css"));
		when(rsReaderHandler.getResource("jar:/style.css", true)).thenReturn(jarStyleReader);
		
		ctx.setResourceReaderHandler(rsReaderHandler);
		generatorRegistry.setResourceReaderHandler(ctx
				.getResourceReaderHandler());

		// Set up the Image servlet Jawr config
		JawrConfig binaryServletJawrConfig = new JawrConfig(JawrConstant.BINARY_TYPE,
				new Properties());
		
		when(binaryGeneratorRegistry.isGeneratedBinaryResource(Matchers.anyString()))
			.thenReturn(true);
		when(binaryGeneratorRegistry.isHandlingCssImage(Matchers.anyString()))
			.thenReturn(true);

		binaryServletJawrConfig.setGeneratorRegistry(generatorRegistry);
		
		Mockito.doAnswer(new Answer<InputStream>() {

			@Override
			public InputStream answer(InvocationOnMock invocation) throws Throwable {
				return new ByteArrayInputStream("fakeData".getBytes());
			}
		}).when(binaryRsReaderHandler).getResourceAsStream(Matchers.anyString());
		
		generatorRegistry.setResourceReaderHandler(binaryRsReaderHandler);

		BinaryResourcesHandler binaryRsHandler = new BinaryResourcesHandler(
				binaryServletJawrConfig, binaryRsReaderHandler, null);
		servletContext.setAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE,
				binaryRsHandler);
	}

	@Test
	public void testIeCssBundleGenerator() throws Exception {

		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		assertEquals(
				FileUtils
						.readClassPathFile("generator/ieCssBundle/expected.css"),
				writer.getBuffer().toString());
	}

}
