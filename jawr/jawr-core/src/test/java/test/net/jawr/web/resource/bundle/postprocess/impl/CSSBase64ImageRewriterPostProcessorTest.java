package test.net.jawr.web.resource.bundle.postprocess.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.when;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.BinaryResourcesHandler;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.impl.css.base64.Base64ImageEncoderPostProcessor;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import test.net.jawr.web.FileUtils;
import test.net.jawr.web.servlet.mock.MockServletContext;

@RunWith(MockitoJUnitRunner.class)
public class CSSBase64ImageRewriterPostProcessorTest {

	@Mock
	private JoinableResourceBundle bundle;
	
	@Mock
	private ResourceReaderHandler rsHandler;
	
	private JawrConfig config;
	private BundleProcessingStatus status;
	private Base64ImageEncoderPostProcessor processor;
	private String workingDirectory;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		
		// Bundle path (full url would be: /servletMapping/prefix/css/bundle.css
		final String bundlePath = "/css/bundle.css";
		// Bundle url prefix
		final String urlPrefix = "/v00";

		workingDirectory = FileUtils.getClasspathRootDir() + "/base64Postprocessor/work";
		clearWorkDirectory(workingDirectory);
		
		when(rsHandler.getWorkingDirectory()).thenReturn(workingDirectory);
		when(rsHandler.getResourceAsStream(Matchers.anyString())).thenAnswer(new Answer<InputStream>() {
			
			@Override
			public InputStream answer(InvocationOnMock invocation) throws Throwable {
				String resourceName = (String) invocation.getArguments()[0];
				InputStream is = null;
				if (resourceName.startsWith("sprite:")) {
					is = new ByteArrayInputStream("Fake value".getBytes());
				}

				if (resourceName.indexOf("bigImage") != -1) {

					int length = 400000;
					byte[] data = new byte[length];
					for (int i = 0; i < length; i++) {
						data[i] = (byte) ((int) i % 2);
					}
					is = new ByteArrayInputStream(data);
				} else {
					is = new ByteArrayInputStream("Fake value".getBytes());
				}
				return is;
			}
		});
		
		when(bundle.getId()).thenReturn(bundlePath);
		when(bundle.getURLPrefix(Matchers.anyMap())).thenReturn(urlPrefix);
		config = new JawrConfig("css", new Properties());
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");		
		status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE, bundle,null,config);
		addGeneratorRegistryToConfig(config, "css");
		status.setLastPathAdded("/css/someCSS.css");
		processor = new Base64ImageEncoderPostProcessor();
	}

	private void clearWorkDirectory(String workingDirectory){
		File workCssSpriteDir = new File(workingDirectory, "cssSprites/src");
		FileUtils.clearDirectory(workCssSpriteDir.getAbsolutePath());
	}
	
	private GeneratorRegistry addGeneratorRegistryToConfig(JawrConfig config, String type) {
		GeneratorRegistry generatorRegistry = new GeneratorRegistry(type) {
			private static final long serialVersionUID = 1L;

			public boolean isHandlingCssImage(String cssResourcePath) {

				boolean result = false;
				if (cssResourcePath.startsWith("jar:")) {
					result = true;
				}
				return result;
			}
		};
		generatorRegistry.setConfig(config);
		config.setGeneratorRegistry(generatorRegistry);
		return generatorRegistry;
	}

	@Test
	public void testBasicImgCssRewriting() {

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
		JawrConfig imgServletJawrConfig = new JawrConfig(JawrConstant.BINARY_TYPE, props);
		imgServletJawrConfig.setServletMapping("/cssImg/");
		addGeneratorRegistryToConfig(imgServletJawrConfig, JawrConstant.BINARY_TYPE);
		config.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		BinaryResourcesHandler binaryRsHandler = new BinaryResourcesHandler(imgServletJawrConfig, rsHandler, null);
		imgServletJawrConfig.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		servletContext.setAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE, binaryRsHandler);

		status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE, bundle, null, config);

		// Css data
		StringBuffer data = new StringBuffer("background-image:url(../../images/logo.png);");

		// Css path
		String filePath = "style/default/assets/someCSS.css";

		// Expected: goes 3 back to the context path, then add the CSS image
		// servlet mapping,
		// then go to the image path
		// the image is at classPath:/style/images/someImage.gif
		String expectedURL = "background-image:url(data:image/png;base64,RmFrZSB2YWx1ZQ==);";
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly", expectedURL, result);
	}

	@Test
	public void testBasicImgAsolutURLCssRewriting() {

		// Set the properties
		Properties props = new Properties();
		props.setProperty(JawrConfig.JAWR_CSS_CLASSPATH_HANDLE_IMAGE, "true");
		props.setProperty(JawrConstant.JAWR_CSS_URL_REWRITER_CONTEXT_PATH, "/myApp");
		config = new JawrConfig("css", props);
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		addGeneratorRegistryToConfig(config, "css");

		// Set up the Image servlet Jawr config
		props = new Properties();
		JawrConfig imgServletJawrConfig = new JawrConfig(JawrConstant.BINARY_TYPE, props);
		imgServletJawrConfig.setServletMapping("/cssImg/");
		addGeneratorRegistryToConfig(imgServletJawrConfig, JawrConstant.BINARY_TYPE);
		config.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		BinaryResourcesHandler imgRsHandler = new BinaryResourcesHandler(imgServletJawrConfig, rsHandler, null);
		imgServletJawrConfig.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		servletContext.setAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE, imgRsHandler);

		status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE, bundle, null, config);

		// Css data
		StringBuffer data = new StringBuffer("background-image:url(/myApp/images/logo.png);");

		// Css path
		String filePath = "style/default/assets/someCSS.css";

		// Expected: goes 3 back to the context path, then add the CSS image
		// servlet mapping,
		// then go to the image path
		// the image is at classPath:/style/images/someImage.gif
		String expectedURL = "background-image:url(data:image/png;base64,RmFrZSB2YWx1ZQ==);";
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly", expectedURL, result);
	}

	@Test
	public void testEncodeTooBigImgCssRewriting() {

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
		JawrConfig imgServletJawrConfig = new JawrConfig(JawrConstant.BINARY_TYPE, props);
		imgServletJawrConfig.setServletMapping("/cssImg/");
		addGeneratorRegistryToConfig(imgServletJawrConfig, JawrConstant.BINARY_TYPE);
		config.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		BinaryResourcesHandler imgRsHandler = new BinaryResourcesHandler(imgServletJawrConfig, rsHandler, null);
		imgServletJawrConfig.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		servletContext.setAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE, imgRsHandler);

		status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE, bundle, null, config);

		// Css data
		StringBuffer data = new StringBuffer("background-image:url(../../images/logo-bigImage.png);");

		// Css path
		String filePath = "style/default/assets/someCSS.css";

		// Expected: goes 3 back to the context path, then add the CSS image
		// servlet mapping,
		// then go to the image path
		// the image is at classPath:/style/images/someImage.gif
		String expectedURL = "background-image:url(style/images/logo-bigImage.png);";
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly", expectedURL, result);
	}

	@Test
	public void testBasicImgCssRewritingEncodeByDefault() {

		// Set the properties
		Properties props = new Properties();
		props.setProperty(JawrConfig.JAWR_CSS_CLASSPATH_HANDLE_IMAGE, "true");
		props.setProperty(JawrConstant.BASE64_ENCODE_BY_DEFAULT, "true");
		config = new JawrConfig("css", props);
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		addGeneratorRegistryToConfig(config, "css");

		// Set up the Image servlet Jawr config
		props = new Properties();
		JawrConfig imgServletJawrConfig = new JawrConfig(JawrConstant.BINARY_TYPE, props);
		imgServletJawrConfig.setServletMapping("/cssImg/");
		addGeneratorRegistryToConfig(imgServletJawrConfig, JawrConstant.BINARY_TYPE);
		config.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		BinaryResourcesHandler imgRsHandler = new BinaryResourcesHandler(imgServletJawrConfig, rsHandler, null);
		imgServletJawrConfig.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		servletContext.setAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE, imgRsHandler);

		status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE, bundle, null, config);

		// Css data
		StringBuffer data = new StringBuffer("background-image:url(../../images/logo.png);");

		// Css path
		String filePath = "style/default/assets/someCSS.css";

		// Expected: goes 3 back to the context path, then add the CSS image
		// servlet mapping,
		// then go to the image path
		// the image is at classPath:/style/images/someImage.gif
		String expectedURL = "background-image:url(data:image/png;base64,RmFrZSB2YWx1ZQ==);";
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly", expectedURL, result);
	}

	@Test
	public void testBasicImgCssRewritingDontEncodeByDefault() {

		// Set the properties
		Properties props = new Properties();
		props.setProperty(JawrConfig.JAWR_CSS_CLASSPATH_HANDLE_IMAGE, "true");
		props.setProperty(JawrConstant.BASE64_ENCODE_BY_DEFAULT, "false");
		config = new JawrConfig("css", props);
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		addGeneratorRegistryToConfig(config, "css");

		// Set up the Image servlet Jawr config
		props = new Properties();
		JawrConfig imgServletJawrConfig = new JawrConfig(JawrConstant.BINARY_TYPE, props);
		imgServletJawrConfig.setServletMapping("/cssImg/");
		addGeneratorRegistryToConfig(imgServletJawrConfig, JawrConstant.BINARY_TYPE);
		config.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		BinaryResourcesHandler imgRsHandler = new BinaryResourcesHandler(imgServletJawrConfig, rsHandler, null);
		imgServletJawrConfig.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		servletContext.setAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE, imgRsHandler);

		status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE, bundle, null, config);

		// Css data
		StringBuffer data = new StringBuffer("background-image:url(../../images/logo.png);");

		// Css path
		String filePath = "style/default/assets/someCSS.css";

		// Expected: goes 3 back to the context path, then add the CSS image
		// servlet mapping,
		// then go to the image path
		// the image is at classPath:/style/images/someImage.gif
		String expectedURL = "background-image:url(../../../cssImg/cb3015770054/style/images/logo.png);";
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly", expectedURL, result);
	}

	@Test
	public void testBasicImgCssSkipRewritingEncodeByDefault() {

		// Set the properties
		Properties props = new Properties();
		props.setProperty(JawrConfig.JAWR_CSS_CLASSPATH_HANDLE_IMAGE, "true");
		props.setProperty(JawrConstant.BASE64_ENCODE_BY_DEFAULT, "true");
		config = new JawrConfig("css", props);
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		addGeneratorRegistryToConfig(config, "css");

		// Set up the Image servlet Jawr config
		props = new Properties();
		JawrConfig imgServletJawrConfig = new JawrConfig(JawrConstant.BINARY_TYPE, props);
		imgServletJawrConfig.setServletMapping("/cssImg/");
		addGeneratorRegistryToConfig(imgServletJawrConfig, JawrConstant.BINARY_TYPE);
		config.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		BinaryResourcesHandler imgRsHandler = new BinaryResourcesHandler(imgServletJawrConfig, rsHandler, null);
		imgServletJawrConfig.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		servletContext.setAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE, imgRsHandler);

		status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE, bundle, null, config);

		// Css data
		StringBuffer data = new StringBuffer("background-image:url(../../images/logo.png); /** jawr:base64-skip */");

		// Css path
		String filePath = "style/default/assets/someCSS.css";

		// Expected: goes 3 back to the context path, then add the CSS image
		// servlet mapping,
		// then go to the image path
		// the image is at classPath:/style/images/someImage.gif
		String expectedURL = "background-image:url(../../../cssImg/cb3015770054/style/images/logo.png); /** jawr:base64-skip */";
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly", expectedURL, result);
	}

	@Test
	public void testBasicImgCssSkipRewritingDontEncodeByDefault() {

		// Set the properties
		Properties props = new Properties();
		props.setProperty(JawrConfig.JAWR_CSS_CLASSPATH_HANDLE_IMAGE, "true");
		props.setProperty(JawrConstant.BASE64_ENCODE_BY_DEFAULT, "false");
		config = new JawrConfig("css", props);
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		addGeneratorRegistryToConfig(config, "css");

		// Set up the Image servlet Jawr config
		props = new Properties();
		JawrConfig imgServletJawrConfig = new JawrConfig(JawrConstant.BINARY_TYPE, props);
		imgServletJawrConfig.setServletMapping("/cssImg/");
		addGeneratorRegistryToConfig(imgServletJawrConfig, JawrConstant.BINARY_TYPE);
		config.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		BinaryResourcesHandler imgRsHandler = new BinaryResourcesHandler(imgServletJawrConfig, rsHandler, null);
		imgServletJawrConfig.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		servletContext.setAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE, imgRsHandler);

		status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE, bundle, null, config);

		// Css data
		StringBuffer data = new StringBuffer("background-image:url(../../images/logo.png); /** jawr:base64-skip */");

		// Css path
		String filePath = "style/default/assets/someCSS.css";

		// Expected: goes 3 back to the context path, then add the CSS image
		// servlet mapping,
		// then go to the image path
		// the image is at classPath:/style/images/someImage.gif
		String expectedURL = "background-image:url(../../../cssImg/cb3015770054/style/images/logo.png); /** jawr:base64-skip */";
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly", expectedURL, result);
	}

	@Test
	public void testBasicImgCssForceBase64RewritingEncodeByDefault() {

		// Set the properties
		Properties props = new Properties();
		props.setProperty(JawrConfig.JAWR_CSS_CLASSPATH_HANDLE_IMAGE, "true");
		props.setProperty(JawrConstant.BASE64_ENCODE_BY_DEFAULT, "true");
		config = new JawrConfig("css", props);
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		addGeneratorRegistryToConfig(config, "css");

		// Set up the Image servlet Jawr config
		props = new Properties();
		JawrConfig imgServletJawrConfig = new JawrConfig(JawrConstant.BINARY_TYPE, props);
		imgServletJawrConfig.setServletMapping("/cssImg/");
		addGeneratorRegistryToConfig(imgServletJawrConfig, JawrConstant.BINARY_TYPE);
		config.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		BinaryResourcesHandler imgRsHandler = new BinaryResourcesHandler(imgServletJawrConfig, rsHandler, null);
		imgServletJawrConfig.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		servletContext.setAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE, imgRsHandler);

		status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE, bundle, null, config);

		// Css data
		StringBuffer data = new StringBuffer("background-image:url(../../images/logo.png); /** jawr:base64 */");

		// Css path
		String filePath = "style/default/assets/someCSS.css";

		// Expected: goes 3 back to the context path, then add the CSS image
		// servlet mapping,
		// then go to the image path
		// the image is at classPath:/style/images/someImage.gif
		String expectedURL = "background-image:url(data:image/png;base64,RmFrZSB2YWx1ZQ==); /** jawr:base64 */";
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly", expectedURL, result);
	}

	@Test
	public void testBasicImgCssForceBase64RewritingDontEncodeByDefault() {

		// Set the properties
		Properties props = new Properties();
		props.setProperty(JawrConfig.JAWR_CSS_CLASSPATH_HANDLE_IMAGE, "true");
		props.setProperty(JawrConstant.BASE64_ENCODE_BY_DEFAULT, "false");
		config = new JawrConfig("css", props);
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		addGeneratorRegistryToConfig(config, "css");

		// Set up the Image servlet Jawr config
		props = new Properties();
		JawrConfig imgServletJawrConfig = new JawrConfig(JawrConstant.BINARY_TYPE, props);
		imgServletJawrConfig.setServletMapping("/cssImg/");
		addGeneratorRegistryToConfig(imgServletJawrConfig, JawrConstant.BINARY_TYPE);
		config.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		BinaryResourcesHandler imgRsHandler = new BinaryResourcesHandler(imgServletJawrConfig, rsHandler, null);
		imgServletJawrConfig.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		servletContext.setAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE, imgRsHandler);

		status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE, bundle, null, config);

		// Css data
		StringBuffer data = new StringBuffer("background-image:url(../../images/logo.png); /** jawr:base64 */");

		// Css path
		String filePath = "style/default/assets/someCSS.css";

		// Expected: goes 3 back to the context path, then add the CSS image
		// servlet mapping,
		// then go to the image path
		// the image is at classPath:/style/images/someImage.gif
		String expectedURL = "background-image:url(data:image/png;base64,RmFrZSB2YWx1ZQ==); /** jawr:base64 */";
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly", expectedURL, result);
	}

	@Test
	public void testMultipleImgCssSkipRewritingEncodeByDefault() throws Exception {

		// Set the properties
		Properties props = new Properties();
		props.setProperty(JawrConfig.JAWR_CSS_CLASSPATH_HANDLE_IMAGE, "true");
		props.setProperty(JawrConstant.BASE64_ENCODE_BY_DEFAULT, "true");
		props.setProperty("jawr.css.bundle.factory.global.preprocessors", "smartsprites");
		config = new JawrConfig("css", props);
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		addGeneratorRegistryToConfig(config, "css");

		// Set up the Image servlet Jawr config
		props = new Properties();
		props.setProperty("jawr.css.bundle.factory.global.preprocessors", "smartsprites");
		JawrConfig imgServletJawrConfig = new JawrConfig(JawrConstant.BINARY_TYPE, props);
		imgServletJawrConfig.setServletMapping("/cssImg/");
		addGeneratorRegistryToConfig(imgServletJawrConfig, JawrConstant.BINARY_TYPE);
		config.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		BinaryResourcesHandler imgRsHandler = new BinaryResourcesHandler(imgServletJawrConfig, rsHandler, null);
		imgServletJawrConfig.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		servletContext.setAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE, imgRsHandler);

		status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE, bundle, null, config);

		// Css data
		StringBuffer data = new StringBuffer(FileUtils.readClassPathFile("base64Postprocessor/temp.css"));

		// Css path
		String filePath = "style/default/assets/someCSS.css";

		// Expected: goes 3 back to the context path, then add the CSS image
		// servlet mapping,
		// then go to the image path
		// the image is at classPath:/style/images/someImage.gif
		String expectedURL = FileUtils.readClassPathFile("base64Postprocessor/temp-result-encode-by-default.css"); // "background-image:url(../../../cssImg/cb3015770054/style/images/logo.png);
																													// /**
																													// jawr:base64-skip
																													// */";
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly", expectedURL, result);
	}

	@Test
	public void testMultipleImgCssSkipRewritingDontEncodeByDefault() throws Exception {

		// Set the properties
		Properties props = new Properties();
		props.setProperty(JawrConfig.JAWR_CSS_CLASSPATH_HANDLE_IMAGE, "true");
		props.setProperty(JawrConstant.BASE64_ENCODE_BY_DEFAULT, "false");
		props.setProperty("jawr.css.bundle.factory.global.preprocessors", "smartsprites");
		config = new JawrConfig("css", props);
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		addGeneratorRegistryToConfig(config, "css");

		// Set up the Image servlet Jawr config
		props = new Properties();
		props.setProperty("jawr.css.bundle.factory.global.preprocessors", "smartsprites");
		JawrConfig imgServletJawrConfig = new JawrConfig(JawrConstant.BINARY_TYPE, props);
		imgServletJawrConfig.setServletMapping("/cssImg/");
		addGeneratorRegistryToConfig(imgServletJawrConfig, JawrConstant.BINARY_TYPE);
		config.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		BinaryResourcesHandler imgRsHandler = new BinaryResourcesHandler(imgServletJawrConfig, rsHandler, null);
		imgServletJawrConfig.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		servletContext.setAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE, imgRsHandler);

		status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE, bundle, null, config);

		// Css data
		StringBuffer data = new StringBuffer(FileUtils.readClassPathFile("base64Postprocessor/temp.css"));

		// Css path
		String filePath = "style/default/assets/someCSS.css";

		// Expected: goes 3 back to the context path, then add the CSS image
		// servlet mapping,
		// then go to the image path
		// the image is at classPath:/style/images/someImage.gif
		String expectedURL = FileUtils.readClassPathFile("base64Postprocessor/temp-result-dont-encode-by-default.css"); // "background-image:url(../../../cssImg/cb3015770054/style/images/logo.png);
																														// /**
																														// jawr:base64-skip
																														// */";
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("URL was not rewritten properly", expectedURL, result);
	}

	@Test
	public void testBase64ForIE6() throws Exception {

		// Set the properties
		Properties props = new Properties();
		props.setProperty(JawrConfig.JAWR_CSS_CLASSPATH_HANDLE_IMAGE, "true");
		props.setProperty(JawrConstant.BASE64_ENCODE_BY_DEFAULT, "true");
		props.setProperty("jawr.css.bundle.factory.global.preprocessors", "smartsprites");
		config = new JawrConfig("css", props);
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		addGeneratorRegistryToConfig(config, "css");

		// Set up the Image servlet Jawr config
		props = new Properties();
		props.setProperty("jawr.css.bundle.factory.global.preprocessors", "smartsprites");
		JawrConfig imgServletJawrConfig = new JawrConfig(JawrConstant.BINARY_TYPE, props);
		imgServletJawrConfig.setServletMapping("/cssImg/");
		addGeneratorRegistryToConfig(imgServletJawrConfig, JawrConstant.BINARY_TYPE);
		config.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		BinaryResourcesHandler imgRsHandler = new BinaryResourcesHandler(imgServletJawrConfig, rsHandler, null);
		imgServletJawrConfig.getGeneratorRegistry().setResourceReaderHandler(rsHandler);
		servletContext.setAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE, imgRsHandler);

		status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE, bundle, null, config);
		Map<String, String> bundleVariants = new HashMap<String, String>();
		bundleVariants.put(JawrConstant.BROWSER_VARIANT_TYPE, JawrConstant.BROWSER_IE6);
		status.setBundleVariants(bundleVariants);
		// Css data
		StringBuffer data = new StringBuffer(FileUtils.readClassPathFile("base64Postprocessor/temp.css"));

		// Css path
		String filePath = "style/default/assets/someCSS.css";

		// Expected: goes 3 back to the context path, then add the CSS image
		// servlet mapping,
		// then go to the image path
		// the image is at classPath:/style/images/someImage.gif
		String expectedURL = FileUtils.readClassPathFile("base64Postprocessor/temp-result-ie6_7-file-processing.css");
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();

		assertEquals("URL was not rewritten properly", expectedURL, result);
		status.setProcessingType(BundleProcessingStatus.BUNDLE_PROCESSING_TYPE);
		status.setSearchingPostProcessorVariants(false);
		result = processor.postProcessBundle(status, new StringBuffer(result)).toString();
		expectedURL = FileUtils.readClassPathFile("base64Postprocessor/temp-result-ie6_7-bundle-processing.css");
		assertEquals("URL was not rewritten properly", expectedURL, FileUtils.removeCarriageReturn(result));
	}

}
