package test.net.jawr.web.resource.bundle.postprocess.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
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
import net.jawr.web.resource.bundle.postprocess.impl.CSSURLPathRewriterPostProcessor;
import test.net.jawr.web.resource.bundle.MockJoinableResourceBundle;
import test.net.jawr.web.resource.bundle.handler.MockResourceReaderHandler;
import test.net.jawr.web.servlet.mock.MockServletContext;

public class CSSURLRewriterPostProcessorTest extends TestCase {
	
	
	JoinableResourceBundle bundle;
	JawrConfig config;
	BundleProcessingStatus status;
	CSSURLPathRewriterPostProcessor processor;
	
	
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
		status = new BundleProcessingStatus(BundleProcessingStatus.BUNDLE_PROCESSING_TYPE,bundle,null,config);
		addGeneratorRegistryToConfig(config, "js");
		status.setLastPathAdded("/css/someCSS.css");
		processor = new CSSURLPathRewriterPostProcessor();
	}

	private GeneratorRegistry addGeneratorRegistryToConfig(JawrConfig config, String type) {
		GeneratorRegistry generatorRegistry = new GeneratorRegistry(type){

			public boolean isHandlingCssImage(String cssResourcePath) {
				
				boolean result = false;
				if(cssResourcePath.startsWith("jar:")){
					result = true;
				}
				return result;
			}
		};
		generatorRegistry.setConfig(config);
		config.setGeneratorRegistry(generatorRegistry);
		return generatorRegistry;
	}
	
	public void testBasicURLRewriting() {
		// basic test
		StringBuffer data = new StringBuffer("background-image:url(../../../../../images/someImage.gif);");
		//StringBuffer data = new StringBuffer("background-image:url(../../../../images/someImage.gif);");
		// the image is at /images
		String filePath = "/css/folder/subfolder/subfolder/someCSS.css";
		// Expected: goes 1 back for servlet mapping, 1 back for prefix, 1 back for the id having a subdir path. 
		String expectedURL = "background-image:url(../../../images/someImage.gif);";
		status.setLastPathAdded(filePath);		
		String result = processor.postProcessBundle(status, data).toString();		
		assertEquals("URL was not rewritten properly",expectedURL, result);
		
	}
	
	public void testBasicURLWithAbsolutePathRewriting() {
		// basic test
		StringBuffer data = new StringBuffer("background-image:url(/images/someImage.gif);");
		//StringBuffer data = new StringBuffer("background-image:url(../../../../images/someImage.gif);");
		// the image is at /images
		String filePath = "/css/folder/subfolder/subfolder/someCSS.css";
		// Expected: goes 1 back for servlet mapping, 1 back for prefix, 1 back for the id having a subdir path. 
		String expectedURL = "background-image:url(/images/someImage.gif);";
		status.setLastPathAdded(filePath);		
		String result = processor.postProcessBundle(status, data).toString();		
		assertEquals("URL was not rewritten properly",expectedURL, result);
		
	}
	
	public void testBasicURLWithAbsolutePathInContextPathRewriting() {
		// basic test
		StringBuffer data = new StringBuffer("background-image:url(/myApp/images/someImage.gif);");
		config.getConfigProperties().put("jawr.css.url.rewriter.context.path", "/myApp/");
		//StringBuffer data = new StringBuffer("background-image:url(../../../../images/someImage.gif);");
		// the image is at /images
		String filePath = "/css/folder/subfolder/subfolder/someCSS.css";
		// Expected: goes 1 back for servlet mapping, 1 back for prefix, 1 back for the id having a subdir path. 
		String expectedURL = "background-image:url(../../../images/someImage.gif);";
		status.setLastPathAdded(filePath);		
		String result = processor.postProcessBundle(status, data).toString();		
		assertEquals("URL was not rewritten properly",expectedURL, result);
	}
	
	public void testBackReferenceAndSpaces() {
		// Now a back reference must be created, and there are quotes and spaces
		StringBuffer data = new StringBuffer("background-image:url( \n 'images/someImage.gif' );");
		status.setLastPathAdded("/someCSS.css");
		// Expected: goes 1 back for servlet mapping, 1 back for prefix , 1 back for the id having a subdir path. 
		String expectedURL = "background-image:url('../../../images/someImage.gif');";
		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly : " +expectedURL + "    \n:  " + result,expectedURL, result);
	}
	
	public void testBackReferenceNoUrlMapping() {
		StringBuffer data = new StringBuffer("background-image:url(  'images/someImage.gif' );");
		// Remove the url mapping from config, one back reference less expected
		config.setServletMapping("");
		String expectedURL = "background-image:url('../../css/images/someImage.gif');";
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly",expectedURL, result);
		
	}
	
	public void testSameLevelUrl() {
		
		// An image at the same path as the css
		status.setLastPathAdded("/css/someCSS.css");
		StringBuffer data = new StringBuffer("background-image:url(  'folder/subfolder/subfolder/someImage.gif' );");
		// Expected: goes 1 back for prefix , 1 back for the id having a subdir path. 
		String expectedURL = "background-image:url('../../../css/folder/subfolder/subfolder/someImage.gif');";
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly",expectedURL, result);
		
	}

	
	public void testSameLineURLs() {
		// Now a back reference must be created, and there are quotes and spaces
		
		StringBuffer data = new StringBuffer(".rule1{background:url(some\\(Image\\).gif);background:url(hue_bg.png) no-repeat;top:4px;}");
		status.setLastPathAdded("/css/someCSS.css");
		// Expected: goes 1 back for servlet mapping, 1 back for prefix , 1 back for the id having a subdir path. 
		String expectedURL = ".rule1{background:url(../../../css/some\\(Image\\).gif);background:url(../../../css/hue_bg.png) no-repeat;top:4px;}";		
		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly : " +expectedURL + "    \n:  " + result,expectedURL, result);
	}
	
	
	public void testSameLevelUrlWithPartialBackreference() {
		
		// An image at the same path as the css
		status.setLastPathAdded("/css/subpath/someCSS.css");
		StringBuffer data = new StringBuffer("background-image:url(  '../folder/subfolder/subfolder/someImage.gif' );");
		// Test several URLs
		data.append("background-image:url(  '../folder/subfolder/subfolder/someOtherImage.gif' );");
		
		// Expected: goes 1 back for prefix , 1 back for the id having a subdir path. 		
		String expectedURL = "background-image:url('../../../css/folder/subfolder/subfolder/someImage.gif');";
		expectedURL += "background-image:url('../../../css/folder/subfolder/subfolder/someOtherImage.gif');";
		String result = processor.postProcessBundle(status, data).toString();	

		assertEquals("URL was not rewritten properly",expectedURL, result);
		
	}
	public void testSameLevelResource() {
		
		// An image at the same path as the css
		status.setLastPathAdded("/css/subpath/someCSS.css");
		StringBuffer data = new StringBuffer("background-image:url(  'someImage.gif' );");
		// Expected: goes 1 back for prefix , 1 back for the id having a subdir path. 
		String expectedURL = "background-image:url('../../../css/subpath/someImage.gif');";
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly",expectedURL, result);
		
	}

	public void testSameLevelExtraPathMapping() {
		// Set a path with several contexts to test if backtracking is done right. 
		status.getJawrConfig().setServletMapping("/foo/bar/baz/");
		// An image at the same path as the css
		status.setLastPathAdded("/css/subpath/someCSS.css");
		StringBuffer data = new StringBuffer("background-image:url(  'someImage.gif' );");
		// Expected: goes 3 back for prefix , 1 back for the id having a subdir path. 
		String expectedURL = "background-image:url('../../../../../css/subpath/someImage.gif');";
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly",expectedURL, result);
		
	}

	public void testSameLevelResourceLeadingDotSlash() {
		
		// An image at the same path as the css
		status.setLastPathAdded("/css/subpath/someCSS.css");
		StringBuffer data = new StringBuffer("background-image:url(  './someImage.gif' );");
		// Expected: goes 1 back for prefix , 1 back for the id having a subdir path. 
		String expectedURL = "background-image:url('../../../css/subpath/someImage.gif');";
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly",expectedURL, result);
		
	}
	public void testSameLevelUrlWithComplexBackreference() {
		
		// An image at the same path as the css
		status.setLastPathAdded("/css/subpath/anotherPath/someCSS.css");
		StringBuffer data = new StringBuffer("background-image:url(  '../folder/subfolder/subfolder/someImage.gif' );");
		// Expected: goes 1 back for prefix , 1 back for the id having a subdir path. 
		String expectedURL = "background-image:url('../../../css/subpath/folder/subfolder/subfolder/someImage.gif');";
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly",expectedURL, result);
		
	}
	public void testSameUrlWithDollarSymbol() {
		
		// An image at the same path as the css
		status.setLastPathAdded("/css/someCSS.css");
		StringBuffer data = new StringBuffer("background-image:url(  'folder/subfolder/subfolder$/someImage.gif' );");
		// Expected: goes 1 back for prefix , 1 back for the id having a subdir path. 
		String expectedURL = "background-image:url('../../../css/folder/subfolder/subfolder$/someImage.gif');";
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly",expectedURL, result);
		
	}

	public void testUpperCaseUrl() {
		
		// An image at the same path as the css
		status.setLastPathAdded("/css/someCSS.css");
		StringBuffer data = new StringBuffer("background-image:URL(  'folder/subfolder/subfolder/someImage.gif' );");
		// Expected: goes 1 back for prefix , 1 back for the id having a subdir path. 
		String expectedURL = "background-image:url('../../../css/folder/subfolder/subfolder/someImage.gif');";
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly",expectedURL, result);
		
	}

	public void testSameUrlWithParens() {
		StringBuffer data = new StringBuffer("background-image:url(  'images/some\\(Image\\).gif' );");
		// Remove the url mapping from config, one back reference less expected
		config.setServletMapping("");
		String expectedURL = "background-image:url('../../css/images/some\\(Image\\).gif');";
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly:" + result,expectedURL, result);
		
	}

	public void testSameUrlWithQuotes() {
		StringBuffer data = new StringBuffer("background-image:url(  'images/some\\'Image\\\".gif' );");
		// Remove the url mapping from config, one back reference less expected
		config.setServletMapping("");
		String expectedURL = "background-image:url('../../css/images/some\\'Image\\\".gif');";
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly:" + result,expectedURL, result);
		
	}
	

	public void testDomainRelativeUrl() {
		StringBuffer data = new StringBuffer("background-image:url('/someImage.gif');");
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly:" + result,data.toString(), result);
		
	}
	
	public void testDblSlashDomainRelativeUrl() {
		StringBuffer data = new StringBuffer("background-image:url('//someImage.gif');");
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly:" + result,data.toString(), result);
		
	}
	
	public void testStaticUrlWithProtocol() {
		StringBuffer data = new StringBuffer("background-image:url('http://www.someSite.org/someImage.gif');");
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly:" + result,data.toString(), result);
		
	}
	public void testStaticUrlWithProtocolAndParens() {
		StringBuffer data = new StringBuffer("background-image:url(http://www.someSite.org/some\\(Image.gif\\));");
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly:" + result,data.toString(), result);
		
	}
	
	public void testImgURLFromClasspathCssRewriting() {

		// Set the properties
		Properties props = new Properties();
		props.setProperty(JawrConfig.JAWR_CSS_CLASSPATH_HANDLE_IMAGE, "true");
		config = new JawrConfig("css", props);
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		addGeneratorRegistryToConfig(config, "css");
		
		// Set up the Image servlet Jawr config
		props = new Properties();
		JawrConfig imgServletJawrConfig = new JawrConfig("img", props);
		imgServletJawrConfig.setServletMapping("/cssImg/");
		addGeneratorRegistryToConfig(imgServletJawrConfig, "img");
		FakeResourceReaderHandler rsHandler = new FakeResourceReaderHandler();
		config.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		ImageResourcesHandler imgRsHandler = new ImageResourcesHandler(imgServletJawrConfig, rsHandler, null);
		imgServletJawrConfig.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		servletContext.setAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE, imgRsHandler);
		
		status = new BundleProcessingStatus(BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle, null, config);

		// Css data
		StringBuffer data = new StringBuffer(
				"background-image:url(../../images/logo.png);");
		
		// Css path
		String filePath = "jar:style/default/assets/someCSS.css";
		
		// Expected: goes 3 back to the context path, then add the CSS image servlet mapping,
		// then go to the image path
		// the image is at classPath:/style/images/someImage.gif
		String expectedURL = "background-image:url(../../../cssImg/jar_cb3015770054/style/images/logo.png);";
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly", expectedURL, result);

	}
	
	
	
	public void testURLImgClasspathCssRewriting() {

		// Set the properties
		Properties props = new Properties();
		props.setProperty(JawrConfig.JAWR_CSS_CLASSPATH_HANDLE_IMAGE, "true");
		config = new JawrConfig("css", props);
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		addGeneratorRegistryToConfig(config, "css");
		
		// Set up the Image servlet Jawr config
		props = new Properties();
		JawrConfig imgServletJawrConfig = new JawrConfig("img", props);
		GeneratorRegistry generatorRegistry = addGeneratorRegistryToConfig(imgServletJawrConfig, "img");
		FakeResourceReaderHandler rsHandler = new FakeResourceReaderHandler();
		generatorRegistry.setResourceReaderHandler(rsHandler);
		imgServletJawrConfig.setServletMapping("/cssImg/");
		ImageResourcesHandler imgRsHandler = new ImageResourcesHandler(imgServletJawrConfig, rsHandler, null);
		servletContext.setAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE, imgRsHandler);
		
		status = new BundleProcessingStatus(BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle, null, config);

		// Css data
		StringBuffer data = new StringBuffer(
				"background-image:url(jar:style/images/logo.png);");
		
		// Css path
		String filePath = "style/default/assets/someCSS.css";
		
		// Expected: goes 3 back to the context path, then add the CSS image servlet mapping,
		// then go to the image path
		// the image is at classPath:/style/images/someImage.gif
		String expectedURL = "background-image:url(../../../cssImg/jar_cb3015770054/style/images/logo.png);";
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly", expectedURL, result);

	}
	
	public void testImgURLRewritingForDataScheme() {

		// Set the properties
		Properties props = new Properties();
		props.setProperty(JawrConfig.JAWR_CSS_CLASSPATH_HANDLE_IMAGE, "true");
		config = new JawrConfig("css", props);
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		
		// Set up the Image servlet Jawr config
		props = new Properties();
		JawrConfig imgServletJawrConfig = new JawrConfig("img", props);
		imgServletJawrConfig.setServletMapping("/cssImg/");
		ImageResourcesHandler imgRsHandler = new ImageResourcesHandler(imgServletJawrConfig, null, null);
		servletContext.setAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE, imgRsHandler);
		
		status = new BundleProcessingStatus(BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle, null, config);

		// Css data
		StringBuffer data = new StringBuffer(
				"background-image: url(data:image/gif;base64,AAAA);");
		
		// Css path
		String filePath = "style/default/assets/someCSS.css";
		
		// Expected: goes 3 back to the context path, then add the CSS image servlet mapping,
		// then go to the image path
		// the image is at classPath:/style/images/someImage.gif
		String expectedURL = "background-image: url(data:image/gif;base64,AAAA);";
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly", expectedURL, result);

	}
	
	public void testBasicURLWithCachedImageRewriting() {
		
		// Set the properties
		Properties props = new Properties();
		config = new JawrConfig("css", props);
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		addGeneratorRegistryToConfig(config, "css");
		status = new BundleProcessingStatus(BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle, null, config);

		// Set up the Image servlet Jawr config
		props = new Properties();
		JawrConfig imgServletJawrConfig = new JawrConfig("img", props);
		ImageResourcesHandler imgRsHandler = new ImageResourcesHandler(imgServletJawrConfig, null, null);
		addGeneratorRegistryToConfig(imgServletJawrConfig, "img");
		servletContext.setAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE, imgRsHandler);
		imgRsHandler.addMapping("/images/someImage.gif", "/cp653321354/images/someImage.gif");
		// basic test
		StringBuffer data = new StringBuffer("background-image:url(../../../../images/someImage.gif);");
		// the image is at /images
		String filePath = "/css/folder/subfolder/subfolder/someCSS.css";
		// Expected: goes 1 back for servlet mapping, 1 back for prefix, 1 back for the id having a subdir path. 
		String expectedURL = "background-image:url(../../../cp653321354/images/someImage.gif);";
		status.setLastPathAdded(filePath);		
		
		
		String result = processor.postProcessBundle(status, data).toString();		
		assertEquals("URL was not rewritten properly",expectedURL, result);
		
	}
	
	
	public void testBasicURLWithNonExistingImageRewriting() {
		
		// Set the properties
		Properties props = new Properties();
		config = new JawrConfig("css", props);
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		addGeneratorRegistryToConfig(config, "css");
		FakeResourceReaderHandler rsHandler = new FakeResourceReaderHandler();
		status = new BundleProcessingStatus(BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle, rsHandler, config);

		// Set up the Image servlet Jawr config
		props = new Properties();
		JawrConfig imgServletJawrConfig = new JawrConfig("img", props);
		addGeneratorRegistryToConfig(imgServletJawrConfig, "img");
		
		ImageResourcesHandler imgRsHandler = new ImageResourcesHandler(imgServletJawrConfig, rsHandler, null);
		servletContext.setAttribute(JawrConstant.IMG_CONTEXT_ATTRIBUTE, imgRsHandler);
		// basic test
		StringBuffer data = new StringBuffer("background-image:url(../../../../images/someImage.gif);");
		// the image is at /images
		String filePath = "/css/folder/subfolder/subfolder/someCSS.css";
		// Expected: goes 1 back for servlet mapping, 1 back for prefix, 1 back for the id having a subdir path. 
		String expectedURL = "background-image:url(../../../images/someImage.gif);";
		status.setLastPathAdded(filePath);		
		
		
		String result = processor.postProcessBundle(status, data).toString();		
		assertEquals("URL was not rewritten properly",expectedURL, result);
		
	}
	
	
	public void testMultiLine() {
		StringBuffer data = new StringBuffer("\nsomeRule {");
		data.append("\n");
		data.append("\tfont-size:12pt;");
		data.append("\n");
		data.append("\tbackground: #00ff00 url('folder/subfolder/subfolder/someImage.gif') no-repeat fixed center; ");
		data.append("\n");
		data.append("}");
		data.append("\n");
		data.append("anotherRule");
		data.append("\n");
		data.append("{");
		data.append("\n");
		//data.append("\tbackground-image:url( ../../../../../images/someImage.gif );");
		data.append("\tbackground-image:url( ../images/someImage.gif );");
		data.append("\n");
		data.append("}");
		data.append("\n");
		data.append("otherRule");
		data.append("\n");
		data.append("{");
		data.append("\n");
		data.append("\tbackground-image:url( 'http://www.someSite.org/someImage.gif' );");
		data.append("\n");
		data.append("}\n");
		
		StringBuffer expected = new StringBuffer("\nsomeRule {");
		expected.append("\n");
		expected.append("\tfont-size:12pt;");
		expected.append("\n");
		expected.append("\tbackground: #00ff00 url('../../../css/folder/subfolder/subfolder/someImage.gif') no-repeat fixed center; ");
		expected.append("\n");
		expected.append("}");
		expected.append("\n");
		expected.append("anotherRule");
		expected.append("\n");
		expected.append("{");
		expected.append("\n");
		expected.append("\tbackground-image:url(../../../images/someImage.gif);");
		expected.append("\n");
		expected.append("}");
		expected.append("\n");
		expected.append("otherRule");
		expected.append("\n");
		expected.append("{");
		expected.append("\n");
		expected.append("\tbackground-image:url('http://www.someSite.org/someImage.gif');");
		expected.append("\n");
		expected.append("}\n");
		
		String result = processor.postProcessBundle(status, data).toString();	
		assertEquals("URL was not rewritten properly:",expected.toString(), result);
		
	}
	private JoinableResourceBundle buildFakeBundle(final String id, final String urlPrefix) {
		
		return new MockJoinableResourceBundle(){
			
			/* (non-Javadoc)
			 * @see test.net.jawr.web.resource.bundle.MockJoinableResourceBundle#getId()
			 */
			public String getId() {
				return id;
			}

			public String getURLPrefix(Map<String, String> variants) {
				return urlPrefix;
			}
		};
			
	}
	
	private static class FakeResourceReaderHandler extends MockResourceReaderHandler {

		public Reader getResource(String resourceName)
				throws ResourceNotFoundException {
			
			throw new ResourceNotFoundException(resourceName);
		}

		public Reader getResource(String resourceName, boolean processingBundle)
				throws ResourceNotFoundException {
			
			throw new ResourceNotFoundException(resourceName);
			
		}

		public InputStream getResourceAsStream(String resourceName,
				boolean processingBundle) throws ResourceNotFoundException {
			throw new ResourceNotFoundException(resourceName);
		}

		public InputStream getResourceAsStream(String resourceName)
				throws ResourceNotFoundException {
			
			if(resourceName.equals("jar:style/images/logo.png")){
				return new ByteArrayInputStream("Fake value".getBytes());
			}
			
			throw new ResourceNotFoundException(resourceName);
		}
	}
}
