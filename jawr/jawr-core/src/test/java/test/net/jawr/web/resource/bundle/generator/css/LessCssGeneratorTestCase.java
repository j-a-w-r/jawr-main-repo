package test.net.jawr.web.resource.bundle.generator.css;

import static org.mockito.Matchers.anyString;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.junit.After;
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

import static org.mockito.Mockito.when;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.BinaryResourcesHandler;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.css.less.LessCssGenerator;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.mappings.FilePathMapping;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import test.net.jawr.web.FileUtils;
import test.net.jawr.web.servlet.mock.MockServletContext;

@RunWith(MockitoJUnitRunner.class)
public class LessCssGeneratorTestCase {

	private static String WORK_DIR = "workDirLess";

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

	@Mock
	private JoinableResourceBundle bundle;

	private List<FilePathMapping> filePathMappings;

	@Before
	public void setUp() throws Exception {

		// Bundle path (full url would be: /servletMapping/prefix/css/bundle.css
		final String bundlePath = "/temp.less";

		config = new JawrConfig("css", new Properties());
		ServletContext servletContext = new MockServletContext();

		// String[] paths = new String[]{"/temp.less", "jar:/style.less"};
		servletContext.setAttribute(JawrConstant.CSS_CONTEXT_ATTRIBUTE, cssBundleHandler);
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		when(generatorRegistry.isGeneratedBinaryResource(Matchers.startsWith("jar:"))).thenReturn(true);
		when(generatorRegistry.isHandlingCssImage(Matchers.startsWith("jar:"))).thenReturn(true);

		config.setGeneratorRegistry(generatorRegistry);

		// GeneratorRegistry generatorRegistry =
		// addGeneratorRegistryToConfig(config, JawrConstant.CSS_TYPE);
		generator = new LessCssGenerator();
		FileUtils.clearDirectory(FileUtils.getClasspathRootDir() + File.separator + WORK_DIR);
		FileUtils.createDir(WORK_DIR);

		generator.setWorkingDirectory(FileUtils.getClasspathRootDir() + "/" + WORK_DIR);
		ctx = new GeneratorContext(bundle, config, bundlePath);
		ctx.setResourceReaderHandler(rsReaderHandler);

		// Set up the Image servlet Jawr config
		JawrConfig binaryServletJawrConfig = new JawrConfig(JawrConstant.BINARY_TYPE, new Properties());
		binaryServletJawrConfig.setGeneratorRegistry(generatorRegistry);
		when(binaryRsReaderHandler.getResourceAsStream(anyString()))
				.thenReturn(new ByteArrayInputStream("fakeData".getBytes()));
		BinaryResourcesHandler binaryRsHandler = new BinaryResourcesHandler(binaryServletJawrConfig,
				binaryRsReaderHandler, null);
		servletContext.setAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE, binaryRsHandler);
		generator.setResourceReaderHandler(rsReaderHandler);
		generator.afterPropertiesSet();

		filePathMappings = new ArrayList<>();
		when(bundle.getFilePathMappings()).thenReturn(filePathMappings);

		// Make sure that importa.less is restored
		Reader rd = new FileReader(FileUtils.getClassPathFile("generator/css/less/import1/import1a.less.backup"));
		Writer wr = new FileWriter(FileUtils.getClassPathFile("generator/css/less/import1/import1a.less"));
		IOUtils.copy(rd, wr, true);
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteDirectory(FileUtils.getClasspathRootDir() + "/" + WORK_DIR);

		// Make sure that importa.less is restored
		Reader rd = new FileReader(FileUtils.getClassPathFile("generator/css/less/import1/import1a.less.backup"));
		Writer wr = new FileWriter(FileUtils.getClassPathFile("generator/css/less/import1/import1a.less"));
		IOUtils.copy(rd, wr, true);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testLessCssBundleGeneratorInProductionMode() throws Exception {

		String tempLessContent = FileUtils.readClassPathFile("generator/css/less/temp.less");
		when(rsReaderHandler.getResource(Matchers.any(JoinableResourceBundle.class), Matchers.eq("/temp.less"),
				Matchers.anyBoolean(), (List<Class<?>>) Matchers.any())).thenReturn(new StringReader(tempLessContent));
		when(rsReaderHandler.getResourceAsStream(anyString()))
				.thenReturn(new ByteArrayInputStream("fakeData".getBytes()));

		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/less/expected_prod.css"),
				writer.getBuffer().toString());

		assertEquals(0, filePathMappings.size());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testLessCssBundleGeneratorInDebugMode() throws Exception {

		String tempLessContent = FileUtils.readClassPathFile("generator/css/less/temp.less");
		when(rsReaderHandler.getResource(Matchers.any(JoinableResourceBundle.class), Matchers.eq("/temp.less"),
				Matchers.anyBoolean(), (List<Class<?>>) Matchers.any())).thenReturn(new StringReader(tempLessContent));
		when(rsReaderHandler.getResourceAsStream(anyString()))
				.thenReturn(new ByteArrayInputStream("fakeData".getBytes()));

		ctx.setProcessingBundle(false);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/less/expected_debug.css"),
				writer.getBuffer().toString());

		assertEquals(0, filePathMappings.size());

		// Checks retrieve from cache
		when(rsReaderHandler.getResource(Matchers.any(JoinableResourceBundle.class), Matchers.eq("/temp.less"),
				Matchers.anyBoolean(), (List<Class<?>>) Matchers.any())).thenReturn(new StringReader(tempLessContent));
		
		ctx.setProcessingBundle(false);
		rd = generator.createResource(ctx);
		writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/less/expected_debug.css"),
				writer.getBuffer().toString());

	}

	@Test
	public void testLessCssBundleGeneratorInProductionModeWithImport() throws Exception {

		ctx = new GeneratorContext(bundle, config, "/import.less");
		ctx.setResourceReaderHandler(rsReaderHandler);

		when(rsReaderHandler.getResourceAsStream(anyString()))
				.thenReturn(new ByteArrayInputStream("fakeData".getBytes()));

		initRsReaderHandler("/import.less", "generator/css/less/import.less");
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
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/less/expected_import.css"),
				writer.getBuffer().toString());

		assertEquals(5, filePathMappings.size());

		assertEquals(filePathMappings.get(0).getPath(),
				FileUtils.getClassPathFileAbsolutePath("generator/css/less/import1.less"));
		assertEquals(filePathMappings.get(1).getPath(),
				FileUtils.getClassPathFileAbsolutePath("generator/css/less/import1/import1a.less"));
		assertEquals(filePathMappings.get(2).getPath(),
				FileUtils.getClassPathFileAbsolutePath("generator/css/less/import1/import1b.less"));
		assertEquals(filePathMappings.get(3).getPath(),
				FileUtils.getClassPathFileAbsolutePath("generator/css/less/import4.less"));
		assertEquals(filePathMappings.get(4).getPath(),
				FileUtils.getClassPathFileAbsolutePath("generator/css/less/import5.less"));

		// Checks retrieve from cache
		ctx.setProcessingBundle(true);
		rd = generator.createResource(ctx);
		writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/less/expected_import.css"),
				writer.getBuffer().toString());
	}

	@Test
	public void testLessCssBundleGeneratorInProductionModeFromClasspath() throws Exception {

		ctx = new GeneratorContext(bundle, config, "jar:/import.less");
		ctx.setResourceReaderHandler(rsReaderHandler);

		when(rsReaderHandler.getResourceAsStream(anyString()))
				.thenReturn(new ByteArrayInputStream("fakeData".getBytes()));

		initRsReaderHandler("jar:/import.less", "generator/css/less/import.less");
		initRsReaderHandler("jar:/import1.less", "generator/css/less/import1.less");
		initRsReaderHandler("jar:/import1/import1a.less", "generator/css/less/import1/import1a.less");
		initRsReaderHandler("jar:/import1/import1b.less", "generator/css/less/import1/import1b.less");
		initRsReaderHandler("jar:/import1/import1c.less", "generator/css/less/import1/import1c.less");

		initRsReaderHandler("jar:/import4.less", "generator/css/less/import4.less");
		initRsReaderHandler("jar:/import5.less", "generator/css/less/import5.less");

		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/less/expected_import.css"),
				writer.getBuffer().toString());

		assertEquals(5, filePathMappings.size());

		assertEquals(filePathMappings.get(0).getPath(),
				FileUtils.getClassPathFileAbsolutePath("generator/css/less/import1.less"));
		assertEquals(filePathMappings.get(1).getPath(),
				FileUtils.getClassPathFileAbsolutePath("generator/css/less/import1/import1a.less"));
		assertEquals(filePathMappings.get(2).getPath(),
				FileUtils.getClassPathFileAbsolutePath("generator/css/less/import1/import1b.less"));
		assertEquals(filePathMappings.get(3).getPath(),
				FileUtils.getClassPathFileAbsolutePath("generator/css/less/import4.less"));
		assertEquals(filePathMappings.get(4).getPath(),
				FileUtils.getClassPathFileAbsolutePath("generator/css/less/import5.less"));

		// Checks retrieve from cache
		ctx.setProcessingBundle(true);
		rd = generator.createResource(ctx);
		writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/less/expected_import.css"),
				writer.getBuffer().toString());
	}
	
	@Test
	public void testSmartBundling() throws Exception {
		testLessCssBundleGeneratorInProductionModeWithImport();

		// Simulate change on a linked resource
		File f = FileUtils.getClassPathFile("generator/css/less/import1/import1a.less");
		FileWriter fWriter = null;
		try{
			fWriter = new FileWriter(f);
		fWriter.append("@import \"import1c.less\";\nimport1a { color: blue;}");
		}finally{
			IOUtils.close(fWriter);
		}
		
		filePathMappings.clear();
		initRsReaderHandler("/import1/import1a.less", "generator/css/less/import1/import1a.less", Calendar.getInstance().getTimeInMillis()+3);

		ctx.setProcessingBundle(true);
		Reader rd = generator.createResource(ctx);
		StringWriter writer = new StringWriter();
		IOUtils.copy(rd, writer);
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/less/expected_importUpdated.css"),
				writer.getBuffer().toString());

		assertEquals(6, filePathMappings.size());

		assertEquals(filePathMappings.get(0).getPath(),
				FileUtils.getClassPathFileAbsolutePath("generator/css/less/import1.less"));
		assertEquals(filePathMappings.get(1).getPath(),
				FileUtils.getClassPathFileAbsolutePath("generator/css/less/import1/import1a.less"));
		assertEquals(filePathMappings.get(2).getPath(),
				FileUtils.getClassPathFileAbsolutePath("generator/css/less/import1/import1c.less"));
		assertEquals(filePathMappings.get(3).getPath(),
				FileUtils.getClassPathFileAbsolutePath("generator/css/less/import1/import1b.less"));
		assertEquals(filePathMappings.get(4).getPath(),
				FileUtils.getClassPathFileAbsolutePath("generator/css/less/import4.less"));
		assertEquals(filePathMappings.get(5).getPath(),
				FileUtils.getClassPathFileAbsolutePath("generator/css/less/import5.less"));

	}

	@Test
	@SuppressWarnings("unchecked")
	public void testLessCssBundleGeneratorInProductionModeWithImportAndQuotes() throws Exception {

		ctx = new GeneratorContext(bundle, config, "/import_quotes.less");
		ctx.setResourceReaderHandler(rsReaderHandler);

		String tempLessContent = FileUtils.readClassPathFile("generator/css/less/import_quotes.less");
		when(rsReaderHandler.getResource(Matchers.any(JoinableResourceBundle.class), Matchers.eq("/import_quotes.less"),
				Matchers.anyBoolean(), (List<Class<?>>) Matchers.any())).thenReturn(new StringReader(tempLessContent));
		when(rsReaderHandler.getResourceAsStream(anyString()))
				.thenReturn(new ByteArrayInputStream("fakeData".getBytes()));

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
		Assert.assertEquals(FileUtils.readClassPathFile("generator/css/less/expected_import.css"),
				writer.getBuffer().toString());
	}

	@SuppressWarnings("unchecked")
	private void initRsReaderHandler(String resourceName, String resourcePath) throws Exception {
		final String lessContent = FileUtils.readClassPathFile(resourcePath);
		File f = FileUtils.getClassPathFile(resourcePath);
		String filePath = f.getAbsolutePath();
		when(rsReaderHandler.getFilePath(resourceName)).thenReturn(filePath);
		when(rsReaderHandler.getLastModified(filePath)).thenReturn(f.lastModified());
		Mockito.doAnswer(new Answer<Reader>() {

			@Override
			public Reader answer(InvocationOnMock invocation) throws Throwable {
				return new StringReader(lessContent);
			}
		}).when(rsReaderHandler).getResource(Matchers.any(JoinableResourceBundle.class), Matchers.eq(resourceName),
				Matchers.anyBoolean(), (List<Class<?>>) Matchers.any());
	}
	
	@SuppressWarnings("unchecked")
	private void initRsReaderHandler(String resourceName, String resourcePath, long lastModified) throws Exception {
		final String lessContent = FileUtils.readClassPathFile(resourcePath);
		File f = FileUtils.getClassPathFile(resourcePath);
		String filePath = f.getAbsolutePath();
		when(rsReaderHandler.getFilePath(resourceName)).thenReturn(filePath);
		when(rsReaderHandler.getLastModified(filePath)).thenReturn(lastModified);
		Mockito.doAnswer(new Answer<Reader>() {

			@Override
			public Reader answer(InvocationOnMock invocation) throws Throwable {
				return new StringReader(lessContent);
			}
		}).when(rsReaderHandler).getResource(Matchers.any(JoinableResourceBundle.class), Matchers.eq(resourceName),
				Matchers.anyBoolean(), (List<Class<?>>) Matchers.any());
	}
}
