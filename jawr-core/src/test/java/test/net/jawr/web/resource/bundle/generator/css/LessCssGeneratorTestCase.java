package test.net.jawr.web.resource.bundle.generator.css;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.BinaryResourcesHandler;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.css.less.LessCssGenerator;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

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

import test.net.jawr.web.FileUtils;
import test.net.jawr.web.servlet.mock.MockServletContext;

@RunWith(MockitoJUnitRunner.class)
public class LessCssGeneratorTestCase {

	private JawrConfig config;
	private GeneratorContext ctx;
	private LessCssGenerator generator;

	@Mock
	private ResourceReaderHandler rsReaderHandler;
	
	@Mock
	private ResourceReaderHandler binaryRsReaderHandler;
	
	@Mock
	private ResourceBundlesHandler cssBundleHandler;
	
	@Mock
	private GeneratorRegistry generatorRegistry;
	
	@Before
	public void setUp() throws Exception {
		
		// Bundle path (full url would be: /servletMapping/prefix/css/bundle.css
		final String bundlePath = "/temp.less";
		
		config = new JawrConfig("css", new Properties());
		ServletContext servletContext = new MockServletContext();
		
		//String[] paths = new String[]{"/temp.less", "jar:/style.less"};
		servletContext.setAttribute(JawrConstant.CSS_CONTEXT_ATTRIBUTE, cssBundleHandler);
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		config.setGeneratorRegistry(generatorRegistry);
		//GeneratorRegistry generatorRegistry = addGeneratorRegistryToConfig(config, JawrConstant.CSS_TYPE);
		generator = new LessCssGenerator();
		ctx = new GeneratorContext(config, bundlePath);
		ctx.setResourceReaderHandler(rsReaderHandler);
		//generatorRegistry.setResourceReaderHandler(rsReaderHandler);
		
		// Set up the Image servlet Jawr config
		JawrConfig binaryServletJawrConfig = new JawrConfig("img", new Properties());
		binaryServletJawrConfig.setGeneratorRegistry(generatorRegistry);
		// addGeneratorRegistryToConfig(imgServletJawrConfig, "img");
		when(binaryRsReaderHandler.getResourceAsStream(anyString())).thenReturn(new ByteArrayInputStream("fakeData".getBytes()));
		//generatorRegistry.setResourceReaderHandler(imgRsReaderHandler);
		BinaryResourcesHandler binaryRsHandler = new BinaryResourcesHandler(binaryServletJawrConfig, binaryRsReaderHandler, null);
		servletContext.setAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE, binaryRsHandler);
		
		generator.setConfig(config);
		generator.setResourceReaderHandler(rsReaderHandler);
		generator.afterPropertiesSet();
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testLessCssBundleGeneratorInProductionMode() throws Exception{
		
		String tempLessContent = FileUtils.readClassPathFile("generator/css/less/temp.less");
		when(rsReaderHandler.getResource(Matchers.eq("/temp.less"), Matchers.anyBoolean(), (List<Class<?>>) Matchers.any())).thenReturn(new StringReader(tempLessContent));
		when(rsReaderHandler.getResourceAsStream(anyString())).thenReturn(new ByteArrayInputStream("fakeData".getBytes()));
		
		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/less/expected_prod.css"), writer.getBuffer().toString());
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testLessCssBundleGeneratorInDebugMode() throws Exception{
		
		String tempLessContent = FileUtils.readClassPathFile("generator/css/less/temp.less");
		when(rsReaderHandler.getResource(Matchers.eq("/temp.less"), Matchers.anyBoolean(), (List<Class<?>>) Matchers.any())).thenReturn(new StringReader(tempLessContent));
		when(rsReaderHandler.getResourceAsStream(anyString())).thenReturn(new ByteArrayInputStream("fakeData".getBytes()));
		
		ctx.setProcessingBundle(false);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/less/expected_debug.css"), writer.getBuffer().toString());
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testLessCssBundleGeneratorInProductionModeWithImport() throws Exception{
		
		ctx = new GeneratorContext(config, "/import.less");
		ctx.setResourceReaderHandler(rsReaderHandler);
		
		String tempLessContent = FileUtils.readClassPathFile("generator/css/less/import.less");
		when(rsReaderHandler.getResource(Matchers.eq("/import.less"), Matchers.anyBoolean(), (List<Class<?>>) Matchers.any())).thenReturn(new StringReader(tempLessContent));
		when(rsReaderHandler.getResourceAsStream(anyString())).thenReturn(new ByteArrayInputStream("fakeData".getBytes()));
		
		initRsReaderHandler("/import1.less", "generator/css/less/import1.less");
		initRsReaderHandler("/import1/import1a.less", "generator/css/less/import1/import1a.less");
		initRsReaderHandler("/import1/import1b.less", "generator/css/less/import1/import1b.less");
		initRsReaderHandler("/import1/import1c.less", "generator/css/less/import1/import1c.less");
		
		initRsReaderHandler("/import4.less", "generator/css/less/import4.less");
		initRsReaderHandler("/import5.less", "generator/css/less/import5.less");
		
		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/less/expected_import.css"), writer.getBuffer().toString());
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testLessCssBundleGeneratorInProductionModeWithImportAndQuotes() throws Exception{
		
		ctx = new GeneratorContext(config, "/import_quotes.less");
		ctx.setResourceReaderHandler(rsReaderHandler);
		
		String tempLessContent = FileUtils.readClassPathFile("generator/css/less/import_quotes.less");
		when(rsReaderHandler.getResource(Matchers.eq("/import_quotes.less"), Matchers.anyBoolean(), (List<Class<?>>) Matchers.any())).thenReturn(new StringReader(tempLessContent));
		when(rsReaderHandler.getResourceAsStream(anyString())).thenReturn(new ByteArrayInputStream("fakeData".getBytes()));
		
		initRsReaderHandler("/import1.less", "generator/css/less/import1.less");
		initRsReaderHandler("/import1/import1a.less", "generator/css/less/import1/import1a.less");
		initRsReaderHandler("/import1/import1b.less", "generator/css/less/import1/import1b.less");
		initRsReaderHandler("/import1/import1c.less", "generator/css/less/import1/import1c.less");
		
		initRsReaderHandler("/import4.less", "generator/css/less/import4.less");
		initRsReaderHandler("/import5.less", "generator/css/less/import5.less");
		
		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/less/expected_import.css"), writer.getBuffer().toString());
	}
	
	@SuppressWarnings("unchecked")
	private void initRsReaderHandler(String resourceName, String resourcePath) throws Exception{
		final String lessContent = FileUtils.readClassPathFile(resourcePath);
		Mockito.doAnswer(new Answer<Reader>() {

			@Override
			public Reader answer(InvocationOnMock invocation) throws Throwable {
				return new StringReader(lessContent);
			}
		}).when(rsReaderHandler).getResource(Matchers.eq(resourceName), Matchers.anyBoolean(), (List<Class<?>>) Matchers.any());
		//when(rsReaderHandler.getResource(Matchers.eq(resourceName), Matchers.anyBoolean(), (List<Class<?>>) Matchers.any())).thenReturn(new StringReader(lessContent));
		
	}
}
