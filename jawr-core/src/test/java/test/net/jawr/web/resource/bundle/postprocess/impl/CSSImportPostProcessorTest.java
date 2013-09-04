/**
 * 
 */
package test.net.jawr.web.resource.bundle.postprocess.impl;

import java.io.Reader;
import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import junit.framework.TestCase;
import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.ImageResourcesHandler;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.impl.CSSImportPostProcessor;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import test.net.jawr.web.resource.bundle.MockJoinableResourceBundle;
import test.net.jawr.web.resource.bundle.handler.MockResourceReaderHandler;
import test.net.jawr.web.servlet.mock.MockServletContext;

/**
 * @author Ibrahim Chaehoi
 * 
 */
public class CSSImportPostProcessorTest extends TestCase {

	JoinableResourceBundle bundle;
	JawrConfig config;
	BundleProcessingStatus status;
	CSSImportPostProcessor processor;

	protected void setUp() throws Exception {
		super.setUp();
		// Bundle path (full url would be: /servletMapping/prefix/css/bundle.css
		final String bundlePath = "/css/bundle.css";
		// Bundle url prefix
		final String urlPrefix = "/v00";

		bundle = buildFakeBundle(bundlePath, urlPrefix);
		config = new JawrConfig("css", new Properties());
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/js");
		config.setCharsetName("UTF-8");
		config.setCssClasspathImageHandledByClasspathCss(true);
		GeneratorRegistry generatorRegistry = new GeneratorRegistry(JawrConstant.CSS_TYPE);
		generatorRegistry.setConfig(config);
		config.setGeneratorRegistry(generatorRegistry);
		
		JawrConfig imgConfig = new JawrConfig("img", new Properties());
		GeneratorRegistry imgGeneratorRegistry = new GeneratorRegistry(JawrConstant.IMG_TYPE);
		generatorRegistry.setConfig(imgConfig);
		imgConfig.setGeneratorRegistry(imgGeneratorRegistry);
		ImageResourcesHandler imgRsHandler = new ImageResourcesHandler(imgConfig, null, null);
		servletContext.setAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE, imgRsHandler);
		
		processor = new CSSImportPostProcessor();
	}

	

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
		ResourceReaderHandler rsHandler = getResourceReaderHandler(expectedCssImportPath);
		config.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		
		ImageResourcesHandler imgRsHandler = (ImageResourcesHandler) config.getContext().getAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE);
		imgRsHandler.getJawrConfig().getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		imgRsHandler.getJawrConfig().setContext(config.getContext());
		BundleProcessingStatus status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE,bundle, rsHandler, config);
		status.setLastPathAdded(filePath);
		return status;
	}

	private JoinableResourceBundle buildFakeBundle(final String id,
			final String urlPrefix) {

		return new MockJoinableResourceBundle() {
			
			public String getId() {
				return id;
			}

			public String getURLPrefix(Map<String, String> variants) {
				return urlPrefix;
			}
		};

	}
	
	private ResourceReaderHandler getResourceReaderHandler(final String expectedResourcePath) {
		
		return new MockResourceReaderHandler() {
			
			public Reader getResource(String resourceName)
					throws ResourceNotFoundException {
				
				if(!resourceName.equals(expectedResourcePath)){
					fail("The expected resource path was : '"+expectedResourcePath+"'; but we get : '"+resourceName);
				}
				return new StringReader(".test { align : left; \n" +
						"padding : 0 7px; \n" +
						"background : url('../img/rainbow.png'); \n"+
				"}");
			}
		
			public Reader getResource(String resourceName,
					boolean processingBundle) throws ResourceNotFoundException {
				return new StringReader(".test { align : left; \n" +
						"padding : 0 7px; \n" +
						"background : url('../img/rainbow.png'); \n"+
				"}");
			}
		};
	}
}
