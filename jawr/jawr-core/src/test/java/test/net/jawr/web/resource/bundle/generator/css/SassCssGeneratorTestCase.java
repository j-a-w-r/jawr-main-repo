package test.net.jawr.web.resource.bundle.generator.css;

import static net.jawr.web.resource.bundle.generator.css.sass.SassGenerator.SAAS_GENERATOR_URL_MODE;
import static net.jawr.web.resource.bundle.generator.css.sass.SassGenerator.SASS_GENERATOR_DEFAULT_URL_MODE;
import static org.mockito.Matchers.anyString;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.junit.After;
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

import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.when;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.BinaryResourcesHandler;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.css.sass.SassGenerator;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.mappings.FilePathMapping;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import test.net.jawr.web.FileUtils;
import test.net.jawr.web.servlet.mock.MockServletContext;

@RunWith(MockitoJUnitRunner.class)
public class SassCssGeneratorTestCase {

	private static String WORK_DIR = "workDirSass";

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

	@Mock
	private JoinableResourceBundle bundle;

	private List<FilePathMapping> filePathMappings;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {

		ServletContext servletContext = new MockServletContext();

		// String[] paths = new String[]{"/temp.sass", "jar:/style.sass"};
		servletContext.setAttribute(JawrConstant.CSS_CONTEXT_ATTRIBUTE, cssBundleHandler);
		when(config.getContext()).thenReturn(servletContext);
		when(config.getResourceCharset()).thenReturn(Charset.forName("UTF-8"));
		when(config.getServletMapping()).thenReturn("/css");
		when(config.getProperty(SAAS_GENERATOR_URL_MODE, SASS_GENERATOR_DEFAULT_URL_MODE))
				.thenReturn(SASS_GENERATOR_DEFAULT_URL_MODE);

		when(generatorRegistry.isGeneratedBinaryResource(Matchers.startsWith("jar:"))).thenReturn(true);
		when(generatorRegistry.isHandlingCssImage(Matchers.startsWith("jar:"))).thenReturn(true);

		when(config.getGeneratorRegistry()).thenReturn(generatorRegistry);

		filePathMappings = new ArrayList<>();
		when(bundle.getFilePathMappings()).thenReturn(filePathMappings);

		// GeneratorRegistry generatorRegistry =
		// addGeneratorRegistryToConfig(config, JawrConstant.CSS_TYPE);
		generator = new SassGenerator();
		FileUtils.clearDirectory(FileUtils.getClasspathRootDir() + File.separator + WORK_DIR);
		FileUtils.createDir(WORK_DIR);

		generator.setWorkingDirectory(FileUtils.getClasspathRootDir() + "/" + WORK_DIR);

		when(ctx.getResourceReaderHandler()).thenReturn(rsReaderHandler);
		when(ctx.getConfig()).thenReturn(config);
		when(ctx.getCharset()).thenReturn(Charset.forName("UTF-8"));
		when(ctx.getBundle()).thenReturn(bundle);

		// Set up the Image servlet Jawr config
		JawrConfig binaryServletJawrConfig = new JawrConfig(JawrConstant.BINARY_TYPE, new Properties());
		binaryServletJawrConfig.setGeneratorRegistry(generatorRegistry);
		when(binaryRsReaderHandler.getResourceAsStream(anyString()))
				.thenReturn(new ByteArrayInputStream("fakeData".getBytes()));
		BinaryResourcesHandler binaryRsHandler = new BinaryResourcesHandler(binaryServletJawrConfig,
				binaryRsReaderHandler, null);
		servletContext.setAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE, binaryRsHandler);
		generator.setResourceReaderHandler(rsReaderHandler);
		generator.setConfig(config);
		Mockito.doAnswer(new Answer<Reader>() {

			@Override
			public Reader answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				Reader rd = null;
				try {
					final String sassContent = FileUtils.readClassPathFile("generator/css/sass" + args[1]);
					rd = new StringReader(sassContent);
				} catch (IOException ex) {
					// Do nothing
				}
				return rd;
			}
		}).when(rsReaderHandler).getResource(Matchers.any(JoinableResourceBundle.class), Matchers.anyString(),
				Matchers.anyBoolean(), (List<Class<?>>) Matchers.any());

		Mockito.doAnswer(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				String result = null;
				try {
					result = FileUtils.getClassPathFileAbsolutePath("generator/css/sass" + (String) args[0]);
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}

				return result;
			}
		}).when(rsReaderHandler).getFilePath(Matchers.anyString());

		Mockito.doAnswer(new Answer<Long>() {

			@Override
			public Long answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				File f = new File((String) args[0]);
				Long result = f.lastModified();
				return result;
			}
		}).when(rsReaderHandler).getLastModified(Matchers.anyString());

		// Make sure that _partial-import.scss is restored
		Reader rd = new FileReader(FileUtils.getClassPathFile("generator/css/sass/_partial-for-import.scss.backup"));
		Writer wr = new FileWriter(FileUtils.getClassPathFile("generator/css/sass/_partial-for-import.scss"));
		IOUtils.copy(rd, wr, true);
	}

	@After
	public void tearDown() throws Exception {
		// Make sure that _partial-import.scss is restored
		Reader rd = new FileReader(FileUtils.getClassPathFile("generator/css/sass/_partial-for-import.scss.backup"));
		Writer wr = new FileWriter(FileUtils.getClassPathFile("generator/css/sass/_partial-for-import.scss"));
		IOUtils.copy(rd, wr, true);
	}

	@Test
	public void testSassFunctions() throws Exception {

		when(ctx.getPath()).thenReturn("/functions.scss");
		when(rsReaderHandler.getResourceAsStream(anyString()))
				.thenReturn(new ByteArrayInputStream("fakeData".getBytes()));

		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		assertEquals(FileUtils.readClassPathFile("generator/css/sass/functions_expected.css"),
				writer.getBuffer().toString());
	}

	@Test
	public void testSassCssBundleWithImports() throws Exception {

		when(ctx.getPath()).thenReturn("/imports.scss");
		// initRsReaderHandler("/imports.scss");
		// initRsReaderHandler("/_partial-for-import.scss");

		when(rsReaderHandler.getResourceAsStream(anyString()))
				.thenReturn(new ByteArrayInputStream("fakeData".getBytes()));

		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/sass/imports_expected.css"),
				writer.getBuffer().toString());

		assertEquals(2, filePathMappings.size());

		assertEquals(FileUtils.getClassPathFileAbsolutePath("generator/css/sass/imports.scss"),
				filePathMappings.get(0).getPath());
		assertEquals(FileUtils.getClassPathFileAbsolutePath("generator/css/sass/_partial-for-import.scss"),
				filePathMappings.get(1).getPath());

		// Checks retrieve from cache
		rd = generator.createResource(ctx);
		writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/sass/imports_expected.css"),
				writer.getBuffer().toString());

	}

	@Test
	public void testSmartBundling() throws Exception {

		testSassCssBundleWithImports();

		// Simulate change on a linked resource
		File f = FileUtils.getClassPathFile("generator/css/sass/_partial-for-import.scss");
		FileWriter fWriter = new FileWriter(f);
		System.out.println("Sass Smartbundling - file last modified before change : "+f.lastModified());
		fWriter.append("@import \"./folder-test2/variables.scss\"; \n" + "$foo : red; \n" + "@mixin caption {\n"
				+ ".caption { \n" + "$side: right;\n" + "border: 1px solid red;\n" + "background: #ff0000;\n"
				+ "padding: 5px;\n" + "margin: 5px;" + "}}\n" + "@include caption;\n");

		fWriter.close();
		
		f.setLastModified(Calendar.getInstance().getTimeInMillis());
		System.out.println("Sass Smartbundling - file last modified after change : "+f.lastModified());
		
		filePathMappings.clear();

		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/sass/imports_updated_expected.css"),
				writer.getBuffer().toString());

		assertEquals(3, filePathMappings.size());
		assertEquals(FileUtils.getClassPathFileAbsolutePath("generator/css/sass/imports.scss"),
				filePathMappings.get(0).getPath());
		assertEquals(FileUtils.getClassPathFileAbsolutePath("generator/css/sass/_partial-for-import.scss"),
				filePathMappings.get(1).getPath());
		assertEquals(FileUtils.getClassPathFileAbsolutePath("generator/css/sass/folder-test2/variables.scss"),
				filePathMappings.get(2).getPath());

	}

	@Test
	public void testSassCssBundleWithMixins() throws Exception {

		when(ctx.getPath()).thenReturn("/mixins.scss");
		// initRsReaderHandler("/mixins.scss");
		// initRsReaderHandler("/_partial-for-import.scss");

		when(rsReaderHandler.getResourceAsStream(anyString()))
				.thenReturn(new ByteArrayInputStream("fakeData".getBytes()));

		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/sass/mixins_expected.css"),
				writer.getBuffer().toString());
	}

	@Test
	public void testSassCssBundleWithParentImport() throws Exception {

		when(ctx.getPath()).thenReturn("/folder-test/parent-import.scss");
		// initRsReaderHandler("/folder-test/parent-import.scss");
		// initRsReaderHandler("/folder-test2/base-imported.scss");
		// initRsReaderHandler("/folder-test2/base.scss");
		// initRsReaderHandler("/folder-test2/url.scss");
		// initRsReaderHandler("/folder-test2/variables.scss");

		when(rsReaderHandler.getResourceAsStream(anyString()))
				.thenReturn(new ByteArrayInputStream("fakeData".getBytes()));

		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/sass/folder-test/parent-import-expected.css"),
				writer.getBuffer().toString());
	}

	@Test
	public void testSassCompass() throws Exception {

		when(ctx.getPath()).thenReturn("/compass-test/compass-import.scss");
		when(rsReaderHandler.getResourceAsStream(anyString()))
				.thenReturn(new ByteArrayInputStream("fakeData".getBytes()));

		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/sass/compass-test/compass-import-expected.css"),
				writer.getBuffer().toString());
	}

	@Test
	public void testSassCompassWithUrlRelativeMode() throws Exception {

		when(config.getProperty(SAAS_GENERATOR_URL_MODE, SASS_GENERATOR_DEFAULT_URL_MODE)).thenReturn("RELATIVE");
		when(ctx.getPath()).thenReturn("/compass-test/compass-import.scss");
		when(rsReaderHandler.getResourceAsStream(anyString()))
				.thenReturn(new ByteArrayInputStream("fakeData".getBytes()));

		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/sass/compass-test/compass-import-expected.css"),
				writer.getBuffer().toString());
	}

	@Test
	public void testSassCompassWithUrlAbsoluteMode() throws Exception {

		when(config.getProperty(SAAS_GENERATOR_URL_MODE, SASS_GENERATOR_DEFAULT_URL_MODE)).thenReturn("ABSOLUTE");
		generator.setConfig(config);

		when(ctx.getPath()).thenReturn("/compass-test/compass-import.scss");
		when(rsReaderHandler.getResourceAsStream(anyString()))
				.thenReturn(new ByteArrayInputStream("fakeData".getBytes()));

		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(
				FileUtils.readClassPathFile("generator/css/sass/compass-test/compass-import-absolute-url-expected.css"),
				writer.getBuffer().toString());
	}

	@Test
	@SuppressWarnings("unchecked")
	@Ignore
	public void testSassCssBundleGeneratorInDebugMode() throws Exception {

		String tempsassContent = FileUtils.readClassPathFile("generator/css/sass/temp.sass");
		when(rsReaderHandler.getResource(Matchers.any(JoinableResourceBundle.class), Matchers.eq("/temp.sass"),
				Matchers.anyBoolean(), (List<Class<?>>) Matchers.any())).thenReturn(new StringReader(tempsassContent));
		when(rsReaderHandler.getResourceAsStream(anyString()))
				.thenReturn(new ByteArrayInputStream("fakeData".getBytes()));

		ctx.setProcessingBundle(false);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/sass/expected_debug.css"),
				writer.getBuffer().toString());
	}

}
