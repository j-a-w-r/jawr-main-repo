/**
 * 
 */
package test.net.jawr.web.resource.bundle.postprocess.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.StringReader;
import java.util.Properties;

import javax.servlet.ServletContext;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.BinaryResourcesHandler;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.impl.CSSImportPostProcessor;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import test.net.jawr.web.FileUtils;
import test.net.jawr.web.servlet.mock.MockServletContext;

/**
 * @author Ibrahim Chaehoi
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class CSSImportPostProcessorTest {

	private static String WORK_DIR = "workDirCssImport";

	@Mock
	JoinableResourceBundle bundle;
	
	@Mock
	ResourceReaderHandler rsHandler;
	
	JawrConfig config;
	BundleProcessingStatus status;
	CSSImportPostProcessor processor;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {

		// Bundle path (full url would be: /servletMapping/prefix/css/bundle.css
		final String bundlePath = "/css/bundle.css";
		// Bundle url prefix
		final String urlPrefix = "/v00";

		when(bundle.getId()).thenReturn(bundlePath);
		when(bundle.getURLPrefix(Matchers.anyMap())).thenReturn(urlPrefix);
		
		config = new JawrConfig("css", new Properties());
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/js");
		config.setCharsetName("UTF-8");
		config.setCssClasspathImageHandledByClasspathCss(true);
		GeneratorRegistry generatorRegistry = new GeneratorRegistry(JawrConstant.CSS_TYPE);
		generatorRegistry.setConfig(config);
		config.setGeneratorRegistry(generatorRegistry);
		
		JawrConfig imgConfig = new JawrConfig(JawrConstant.BINARY_TYPE, new Properties());
		GeneratorRegistry imgGeneratorRegistry = new GeneratorRegistry(JawrConstant.BINARY_TYPE);
		generatorRegistry.setConfig(imgConfig);
		imgConfig.setGeneratorRegistry(imgGeneratorRegistry);
		BinaryResourcesHandler imgRsHandler = new BinaryResourcesHandler(imgConfig, null, null);
		servletContext.setAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE, imgRsHandler);
		
		FileUtils.clearDirectory(FileUtils.getClasspathRootDir() + File.separator + WORK_DIR);
		FileUtils.createDir(WORK_DIR);
		when(rsHandler.getWorkingDirectory()).thenReturn(FileUtils.getClasspathRootDir() + File.separator + WORK_DIR);
		processor = new CSSImportPostProcessor();
	}

	@Test
	public void testImportWithHttpURL() {
		// basic test
		StringBuffer data = new StringBuffer("@import url(\"http://fonts.googleapis.com/css?family=Lobster|Cabin:400,700\");\n" +
				".blue { color : #0000FF } ");
		
		String filePath = "/css/folder/subfolder/subfolder/someCSS.css";
		String expectedContent = "\n" +
				".blue { color : #0000FF } ";
		
		status = getBundleProcessingStatus(filePath, "/css/folder/subfolder/subfolder/temp.css");
		String result = processor.postProcessBundle(status, data).toString();		
		assertEquals("Content was not rewritten properly",expectedContent, result);
	}
	
	@Test
	public void testImportWithHttpsURL() {
		// basic test
		StringBuffer data = new StringBuffer("@import url(\"https://fonts.googleapis.com/css?family=Lobster|Cabin:400,700\");\n" +
				".blue { color : #0000FF } ");
		
		String filePath = "/css/folder/subfolder/subfolder/someCSS.css";
		String expectedContent = "\n" +
				".blue { color : #0000FF } ";
		
		status = getBundleProcessingStatus(filePath, "/css/folder/subfolder/subfolder/temp.css");
		String result = processor.postProcessBundle(status, data).toString();		
		assertEquals("Content was not rewritten properly",expectedContent, result);
	}
	
	@Test
	public void testImportWithAbsoluteURL() {
		// basic test
		StringBuffer data = new StringBuffer("@import url(\"//fonts.googleapis.com/css?family=Lobster|Cabin:400,700\");\n" +
				".blue { color : #0000FF } ");
		
		String filePath = "/css/folder/subfolder/subfolder/someCSS.css";
		String expectedContent = "\n" +
				".blue { color : #0000FF } ";
		
		status = getBundleProcessingStatus(filePath, "/css/folder/subfolder/subfolder/temp.css");
		String result = processor.postProcessBundle(status, data).toString();		
		assertEquals("Content was not rewritten properly",expectedContent, result);
	}

	@Test
	public void testBasicRelativeURLImport() {
		// basic test
		StringBuffer data = new StringBuffer("@import url(temp.css);\n" +
				".blue { color : #0000FF } ");
		
		String filePath = "/css/folder/subfolder/subfolder/someCSS.css";
		String expectedContent = ".test { align : left; \n" +
						"padding : 0 7px; \n" +
						"background : url('../img/rainbow.png'); \n"+ 
				"}\n" +
				".blue { color : #0000FF } ";
		
		status = getBundleProcessingStatus(filePath, "/css/folder/subfolder/subfolder/temp.css");
		String result = processor.postProcessBundle(status, data).toString();		
		assertEquals("Content was not rewritten properly",expectedContent, result);
	}
	
	@Test
	public void testBasicRelativeURLImport1() {
		// basic test
		StringBuffer data = new StringBuffer("@import url(temp.css);\n" +
				".blue { color : rgb(0, 0, 255) } ");

		String filePath = "/css/folder/subfolder/subfolder/someCSS.css";
		String expectedContent = ".test { align : left; \n" +
						"padding : 0 7px; \n" +
						"background : url('../img/rainbow.png'); \n"+
				"}\n" +
				".blue { color : rgb(0, 0, 255) } ";

		status = getBundleProcessingStatus(filePath, "/css/folder/subfolder/subfolder/temp.css");
		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("Content was not rewritten properly",expectedContent, result);
	}

	@Test
	public void testRelativeURLImportWithSpaceAndSimpleQuote() {
		// basic test
		StringBuffer data = new StringBuffer("@import url( \n 'temp.css' \n );\n" +
				".blue { color : #0000FF } ");
		
		String filePath = "/css/folder/subfolder/subfolder/someCSS.css";
		String expectedContent = ".test { align : left; \n" +
						"padding : 0 7px; \n" +
						"background : url('../img/rainbow.png'); \n"+ 
				"}\n" +
				".blue { color : #0000FF } ";
		
		status = getBundleProcessingStatus(filePath, "/css/folder/subfolder/subfolder/temp.css");
		String result = processor.postProcessBundle(status, data).toString();		
		assertEquals("Content was not rewritten properly",expectedContent, result);
	}
	
	@Test
	public void testRelativeURLImportWithSpaceAndDoubleQuote() {
		// basic test
		StringBuffer data = new StringBuffer("@import url( \n \"temp.css\" \n );\n" +
				".blue { color : #0000FF } ");
		
		String filePath = "/css/folder/subfolder/subfolder/someCSS.css";
		String expectedContent = ".test { align : left; \n" +
						"padding : 0 7px; \n" +
						"background : url('../img/rainbow.png'); \n"+ 
				"}\n" +
				".blue { color : #0000FF } ";
		
		status = getBundleProcessingStatus(filePath, "/css/folder/subfolder/subfolder/temp.css");
		String result = processor.postProcessBundle(status, data).toString();		
		assertEquals("Content was not rewritten properly",expectedContent, result);
	}
	
	@Test
	public void testBasicAbsoluteURLImport() {
		// basic test
		StringBuffer data = new StringBuffer("@import url(/style/myStyle/temp.css);\n" +
				".blue { color : #0000FF } ");
		
		String filePath = "/css/folder/subfolder/subfolder/someCSS.css";
		String expectedContent = ".test { align : left; \n" +
						"padding : 0 7px; \n" +
						"background : url('../../../../style/img/rainbow.png'); \n"+ 
				"}\n" +
				".blue { color : #0000FF } ";
		
		status = getBundleProcessingStatus(filePath, "/style/myStyle/temp.css");
		String result = processor.postProcessBundle(status, data).toString();		
		assertEquals("Content was not rewritten properly",expectedContent, result);
	}

	@Test
	public void testClasspathCssRelativeURLImport() {
		// basic test
		StringBuffer data = new StringBuffer("@import url(../rainbow/temp.css);\n" +
				".blue { color : #0000FF } ");
		
		String filePath = "jar:cssimportprocessor/style/myStyle/someCSS.css";
		String expectedContent = ".test { align : left; \n" +
						"padding : 0 7px; \n" +
						"background : url('jar:cssimportprocessor/style/img/rainbow.png'); \n"+ 
				"}\n" +
				".blue { color : #0000FF } ";
		
		status = getBundleProcessingStatus(filePath, "jar:cssimportprocessor/style/rainbow/temp.css");
		String result = processor.postProcessBundle(status, data).toString().replaceAll("\r", "");		
		assertEquals("Content was not rewritten properly",expectedContent, result);
	}
	
	@Test
	public void testClasspathCssAbsoluteURLImport() {
		// basic test
		StringBuffer data = new StringBuffer("@import url(jar:cssimportprocessor/style/rainbow/temp.css);\n" +
				".blue { color : #0000FF } ");
		
		String filePath = "/css/folder/subfolder/subfolder/someCSS.css";
		String expectedContent = ".test { align : left; \n" +
						"padding : 0 7px; \n" +
						"background : url('jar:cssimportprocessor/style/img/rainbow.png'); \n"+ 
				"}\n" +
				".blue { color : #0000FF } ";
		
		status = getBundleProcessingStatus(filePath, "jar:cssimportprocessor/style/rainbow/temp.css");
		String result = processor.postProcessBundle(status, data).toString().replaceAll("\r", "");		
		assertEquals("Content was not rewritten properly",expectedContent, result);
	}

	@Test
	public void testBasicRelativeURLWithMediaImport() {
		// basic test
		StringBuffer data = new StringBuffer("@import url(temp.css) screen;\n" +
				".blue { color : #0000FF } ");
		
		String filePath = "/css/folder/subfolder/subfolder/someCSS.css";
		String expectedContent = "@media screen {\n" +
				".test { align : left; \n" +
						"padding : 0 7px; \n" +
						"background : url('../img/rainbow.png'); \n"+ 
				"}\n" +
				"}\n\n"+
				".blue { color : #0000FF } ";
		
		status = getBundleProcessingStatus(filePath, "/css/folder/subfolder/subfolder/temp.css");
		String result = processor.postProcessBundle(status, data).toString();		
		assertEquals("Content was not rewritten properly",expectedContent, result);
	}
	
	private BundleProcessingStatus getBundleProcessingStatus(String filePath, String expectedCssImportPath) {
		//ResourceReaderHandler rsHandler = getResourceReaderHandler(expectedCssImportPath);
		
		try {
			when(rsHandler.getResource(expectedCssImportPath)).thenReturn(new StringReader(".test { align : left; \n" +
							"padding : 0 7px; \n" +
							"background : url('../img/rainbow.png'); \n"+
					"}"));
			when(rsHandler.getResource(bundle, expectedCssImportPath, true)).thenReturn(new StringReader(".test { align : left; \n" +
					"padding : 0 7px; \n" +
					"background : url('../img/rainbow.png'); \n"+
			"}"));
			
		} catch (ResourceNotFoundException e) {
			fail("This should not happen");
		}
		config.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		
		BinaryResourcesHandler imgRsHandler = (BinaryResourcesHandler) config.getContext().getAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE);
		imgRsHandler.getConfig().getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		imgRsHandler.getConfig().setContext(config.getContext());
		BundleProcessingStatus status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE,bundle, rsHandler, config);
		status.setLastPathAdded(filePath);
		return status;
	}
	
}
