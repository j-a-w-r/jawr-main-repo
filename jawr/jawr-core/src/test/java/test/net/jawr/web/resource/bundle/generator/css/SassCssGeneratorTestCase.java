package test.net.jawr.web.resource.bundle.generator.css;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static net.jawr.web.resource.bundle.generator.css.sass.SassGenerator.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.BinaryResourcesHandler;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.css.sass.SassGenerator;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import test.net.jawr.web.FileUtils;
import test.net.jawr.web.servlet.mock.MockServletContext;

@RunWith(MockitoJUnitRunner.class)
public class SassCssGeneratorTestCase {

	private SassGenerator generator;

	@Mock
	private JawrConfig config;
	
	@Mock
	private GeneratorContext ctx;
	
	@Mock
	private ResourceReaderHandler rsReaderHandler;
	
	@Mock
	private ResourceReaderHandler binaryRsReaderHandler;
	
	@Mock
	private ResourceBundlesHandler cssBundleHandler;
	
	@Mock
	private GeneratorRegistry generatorRegistry;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		
		ServletContext servletContext = new MockServletContext();
		
		//String[] paths = new String[]{"/temp.sass", "jar:/style.sass"};
		servletContext.setAttribute(JawrConstant.CSS_CONTEXT_ATTRIBUTE, cssBundleHandler);
		when(config.getContext()).thenReturn(servletContext);
		when(config.getResourceCharset()).thenReturn(Charset.forName("UTF-8"));
		when(config.getServletMapping()).thenReturn("/css");
		when(config.getProperty(SAAS_GENERATOR_URL_MODE, SASS_GENERATOR_DEFAULT_URL_MODE)).thenReturn(SASS_GENERATOR_DEFAULT_URL_MODE);
		
		when(generatorRegistry.isGeneratedBinaryResource(Matchers.startsWith("jar:"))).thenReturn(true);
		when(generatorRegistry.isHandlingCssImage(Matchers.startsWith("jar:")))
			.thenReturn(true);
		
		when(config.getGeneratorRegistry()).thenReturn(generatorRegistry);
		
		//GeneratorRegistry generatorRegistry = addGeneratorRegistryToConfig(config, JawrConstant.CSS_TYPE);
		generator = new SassGenerator();
		when(ctx.getResourceReaderHandler()).thenReturn(rsReaderHandler);
		when(ctx.getConfig()).thenReturn(config);
		when(ctx.getCharset()).thenReturn(Charset.forName("UTF-8"));
		
		
		// Set up the Image servlet Jawr config
		JawrConfig binaryServletJawrConfig = new JawrConfig(JawrConstant.BINARY_TYPE, new Properties());
		binaryServletJawrConfig.setGeneratorRegistry(generatorRegistry);
		when(binaryRsReaderHandler.getResourceAsStream(anyString())).thenReturn(new ByteArrayInputStream("fakeData".getBytes()));
		BinaryResourcesHandler binaryRsHandler = new BinaryResourcesHandler(binaryServletJawrConfig, binaryRsReaderHandler, null);
		servletContext.setAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE, binaryRsHandler);
		generator.setResourceReaderHandler(rsReaderHandler);
		generator.setConfig(config);
		Mockito.doAnswer(new Answer<Reader>() {

			@Override
			public Reader answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				Reader rd = null;
				try{
					final String sassContent = FileUtils.readClassPathFile("generator/css/sass"+args[0]);
					rd = new StringReader(sassContent);	
				}catch(IOException ex){
					// Do nothing
				}
				return rd;
			}
		}).when(rsReaderHandler).getResource(Matchers.anyString(), Matchers.anyBoolean(), (List<Class<?>>) Matchers.any());

	}
	
	@Test
	public void testSassFunctions() throws Exception{
		
		when(ctx.getPath()).thenReturn("/functions.scss");
		when(rsReaderHandler.getResourceAsStream(anyString())).thenReturn(new ByteArrayInputStream("fakeData".getBytes()));
		
		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		assertEquals(FileUtils.readClassPathFile("generator/css/sass/functions_expected.css"), writer.getBuffer().toString());
	}
	
	@Test
	public void testSassCssBundleWithImports() throws Exception{
		
		when(ctx.getPath()).thenReturn("/imports.scss");
//		initRsReaderHandler("/imports.scss");
//		initRsReaderHandler("/_partial-for-import.scss");
		
		when(rsReaderHandler.getResourceAsStream(anyString())).thenReturn(new ByteArrayInputStream("fakeData".getBytes()));
		
		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/sass/imports_expected.css"), writer.getBuffer().toString());
	}
	
	@Test
	public void testSassCssBundleWithMixins() throws Exception{
		
		when(ctx.getPath()).thenReturn("/mixins.scss");
//		initRsReaderHandler("/mixins.scss");
		//initRsReaderHandler("/_partial-for-import.scss");
		
		when(rsReaderHandler.getResourceAsStream(anyString())).thenReturn(new ByteArrayInputStream("fakeData".getBytes()));
		
		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/sass/mixins_expected.css"), writer.getBuffer().toString());
	}
	
	@Test
	public void testSassCssBundleWithParentImport() throws Exception{
		
		when(ctx.getPath()).thenReturn("/folder-test/parent-import.scss");
//		initRsReaderHandler("/folder-test/parent-import.scss");
//		initRsReaderHandler("/folder-test2/base-imported.scss");
//		initRsReaderHandler("/folder-test2/base.scss");
//		initRsReaderHandler("/folder-test2/url.scss");
//		initRsReaderHandler("/folder-test2/variables.scss");
		
		when(rsReaderHandler.getResourceAsStream(anyString())).thenReturn(new ByteArrayInputStream("fakeData".getBytes()));
		
		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/sass/folder-test/parent-import-expected.css"), writer.getBuffer().toString());
	}
	
	@Test
	public void testSassCompass() throws Exception{
		
		when(ctx.getPath()).thenReturn("/compass-test/compass-import.scss");
		when(rsReaderHandler.getResourceAsStream(anyString())).thenReturn(new ByteArrayInputStream("fakeData".getBytes()));
		
		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/sass/compass-test/compass-import-expected.css"), writer.getBuffer().toString());
	}
	
	@Test
	public void testSassCompassWithUrlRelativeMode() throws Exception{
		
		when(config.getProperty(SAAS_GENERATOR_URL_MODE, SASS_GENERATOR_DEFAULT_URL_MODE)).thenReturn("RELATIVE");
		when(ctx.getPath()).thenReturn("/compass-test/compass-import.scss");
		when(rsReaderHandler.getResourceAsStream(anyString())).thenReturn(new ByteArrayInputStream("fakeData".getBytes()));
		
		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/sass/compass-test/compass-import-expected.css"), writer.getBuffer().toString());
	}
	
	@Test
	public void testSassCompassWithUrlAbsoluteMode() throws Exception{
		
		when(config.getProperty(SAAS_GENERATOR_URL_MODE, SASS_GENERATOR_DEFAULT_URL_MODE)).thenReturn("ABSOLUTE");
		generator.setConfig(config);
		
		when(ctx.getPath()).thenReturn("/compass-test/compass-import.scss");
		when(rsReaderHandler.getResourceAsStream(anyString())).thenReturn(new ByteArrayInputStream("fakeData".getBytes()));
		
		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/sass/compass-test/compass-import-absolute-url-expected.css"), writer.getBuffer().toString());
	}
	
	
	@Test
	@SuppressWarnings("unchecked")
	@Ignore
	public void testSassCssBundleGeneratorInDebugMode() throws Exception{
		
		String tempsassContent = FileUtils.readClassPathFile("generator/css/sass/temp.sass");
		when(rsReaderHandler.getResource(Matchers.eq("/temp.sass"), Matchers.anyBoolean(), (List<Class<?>>) Matchers.any())).thenReturn(new StringReader(tempsassContent));
		when(rsReaderHandler.getResourceAsStream(anyString())).thenReturn(new ByteArrayInputStream("fakeData".getBytes()));
		
		ctx.setProcessingBundle(false);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/sass/expected_debug.css"), writer.getBuffer().toString());
	}
	
}
