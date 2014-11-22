package net.jawr.web.bundle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;
import net.jawr.web.JawrConstant;
import net.jawr.web.bundle.processor.BundleProcessor;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.resource.handler.reader.ServletContextResourceReaderHandler;
import net.jawr.web.servlet.mock.MockServletContext;

import org.junit.Assert;
import org.junit.Test;

/**
 * Bundle processor test case
 * 
 * @author Ibrahim Chaehoi
 */
public class BundleProcessorTestCase extends TestCase {

	private String servletAPIVersion = "2.5";

	private BundleProcessor bundleProcessor = new BundleProcessor();

	@Test
	public void testFinalGenerationJSBundlePath() throws IOException {

		JawrConfig jawrConfig = new JawrConfig(JawrConstant.JS_TYPE,
				new Properties());
		GeneratorRegistry generatorRegistry = new GeneratorRegistry();
		jawrConfig.setGeneratorRegistry(generatorRegistry);
		MockServletContext servletContext = new MockServletContext(
				servletAPIVersion);
		File tmpDir = new File("temp");
		servletContext.setAttribute(JawrConstant.SERVLET_CONTEXT_TEMPDIR,
				tmpDir);
		jawrConfig.setContext(servletContext);
		ResourceReaderHandler handler = new ServletContextResourceReaderHandler(
				servletContext, jawrConfig, generatorRegistry);
		generatorRegistry.setResourceReaderHandler(handler);
		Map<String, String> variantMap = new HashMap<String, String>();

		// JS without servlet mapping
		// File path for production mode
		jawrConfig.setDebugModeOn(false);
		assertEquals("js/bundle/msg.js", bundleProcessor.getFinalBundlePath(
				"/N1785986402/js/bundle/msg.js", jawrConfig, variantMap));
		variantMap.put(JawrConstant.LOCALE_VARIANT_TYPE, "fr");
		assertEquals("js/bundle/msg@fr.js", bundleProcessor.getFinalBundlePath(
				"/N1785986402.fr/js/bundle/msg.js", jawrConfig, variantMap));
		variantMap.put(JawrConstant.LOCALE_VARIANT_TYPE, "en_US");
		assertEquals("js/bundle/msg@en_US.js",
				bundleProcessor.getFinalBundlePath(
						"/N1388754583.en_US/js/bundle/msg.js", jawrConfig,
						variantMap));

		// File path for debug mode
		jawrConfig.setDebugModeOn(false);

		variantMap.clear();
		assertEquals(
				"/jawr_generator/js/messages/messages.js",
				bundleProcessor
						.getFinalBundlePath(
								"/jawr_generator.js?generationConfigParam=messages%3Amessages",
								jawrConfig, variantMap));
		variantMap.put(JawrConstant.LOCALE_VARIANT_TYPE, "fr");
		assertEquals(
				"/jawr_generator/js/messages/messages_fr.js",
				bundleProcessor
						.getFinalBundlePath(
								"/jawr_generator.js?generationConfigParam=messages%3Amessages%40fr",
								jawrConfig, variantMap));

		// JS With servlet mapping
		jawrConfig.setServletMapping("jsJawrPath");
		// File path for production mode
		jawrConfig.setDebugModeOn(false);
		variantMap.clear();
		assertEquals("js/bundle/msg.js", bundleProcessor.getFinalBundlePath(
				"/jsJawrPath/N1785986402/js/bundle/msg.js", jawrConfig,
				variantMap));
		variantMap.put(JawrConstant.LOCALE_VARIANT_TYPE, "fr");
		assertEquals("js/bundle/msg@fr.js", bundleProcessor.getFinalBundlePath(
				"/jsJawrPath/N1785986402.fr/js/bundle/msg.js", jawrConfig,
				variantMap));
		variantMap.put(JawrConstant.LOCALE_VARIANT_TYPE, "en_US");
		assertEquals("js/bundle/msg@en_US.js",
				bundleProcessor.getFinalBundlePath(
						"/jsJawrPath/N1388754583.en_US/js/bundle/msg.js",
						jawrConfig, variantMap));

		// File path for debug mode
		jawrConfig.setDebugModeOn(false);
		variantMap.clear();
		assertEquals(
				"/jawr_generator/js/messages/messages.js",
				bundleProcessor
						.getFinalBundlePath(
								"/jsJawrPath/jawr_generator.js?generationConfigParam=messages%3Amessages",
								jawrConfig, variantMap));
		variantMap.put(JawrConstant.LOCALE_VARIANT_TYPE, "fr");
		assertEquals(
				"/jawr_generator/js/messages/messages_fr.js",
				bundleProcessor
						.getFinalBundlePath(
								"/jsJawrPath/jawr_generator.js?generationConfigParam=messages%3Amessages%40fr",
								jawrConfig, variantMap));

	}

	@Test
	public void testFinalGenerationCSSBundlePath()
			throws IOException {

		JawrConfig jawrConfig = new JawrConfig(JawrConstant.CSS_TYPE,
				new Properties());
		GeneratorRegistry generatorRegistry = new GeneratorRegistry();
		jawrConfig.setGeneratorRegistry(generatorRegistry);
		MockServletContext servletContext = new MockServletContext(
				servletAPIVersion);
		File tmpDir = new File("temp");
		servletContext.setAttribute(JawrConstant.SERVLET_CONTEXT_TEMPDIR,
				tmpDir);
		jawrConfig.setContext(servletContext);

		ResourceReaderHandler handler = new ServletContextResourceReaderHandler(
				servletContext, jawrConfig, generatorRegistry);
		Map<String, String> variantMap = new HashMap<String, String>();

		// CSS Without servlet mapping
		// File path for production mode
		jawrConfig.setContext(servletContext);
		jawrConfig.setDebugModeOn(false);
		jawrConfig.setServletMapping("");
		generatorRegistry = new GeneratorRegistry(JawrConstant.CSS_TYPE);
		generatorRegistry.setResourceReaderHandler(handler);
		jawrConfig.setGeneratorRegistry(generatorRegistry);

		variantMap.clear();
		assertEquals("folder/core/component.css",
				bundleProcessor.getFinalBundlePath(
						"/1414653084/folder/core/component.css", jawrConfig,
						variantMap));
		assertEquals("css/two.css", bundleProcessor.getFinalBundlePath(
				"/N87509158/css/two.css", jawrConfig, variantMap));

		// File path for debug mode
		jawrConfig.setDebugModeOn(true);
		assertEquals("css/one.css", bundleProcessor.getFinalBundlePath(
				"/css/one.css", jawrConfig, variantMap));
		assertEquals(
				"/jawr_generator/css/jar/net/jawr/css/cpStyle.css",
				bundleProcessor
						.getFinalBundlePath(
								"/jawr_generator.css?generationConfigParam=jar%3Anet%2Fjawr%2Fcss%2FcpStyle.css",
								jawrConfig, variantMap));

		// CSS With servlet mapping
		// File path for production mode
		jawrConfig.setDebugModeOn(false);
		jawrConfig.setServletMapping("cssJawrPath");
		generatorRegistry = new GeneratorRegistry(JawrConstant.CSS_TYPE);
		jawrConfig.setGeneratorRegistry(generatorRegistry);
		generatorRegistry.setResourceReaderHandler(handler);

		assertEquals("folder/core/component.css",
				bundleProcessor.getFinalBundlePath(
						"/cssJawrPath/1414653084/folder/core/component.css",
						jawrConfig, variantMap));
		assertEquals("css/two.css", bundleProcessor.getFinalBundlePath(
				"/cssJawrPath/N87509158/css/two.css", jawrConfig, variantMap));

		// File path for debug mode
		jawrConfig.setDebugModeOn(true);
		assertEquals("css/one.css", bundleProcessor.getFinalBundlePath(
				"/cssJawrPath/css/one.css", jawrConfig, variantMap));
		assertEquals(
				"/jawr_generator/css/jar/net/jawr/css/cpStyle.css",
				bundleProcessor
						.getFinalBundlePath(
								"/cssJawrPath/jawr_generator.css?generationConfigParam=jar%3Anet%2Fjawr%2Fcss%2FcpStyle.css",
								jawrConfig, variantMap));

	}

	public void testGetFinalImageName() {

		JawrConfig jawrConfig = new JawrConfig(JawrConstant.JS_TYPE,
				new Properties());

		// Without servlet mapping
		assertEquals(
				"classpathResources/img/iconInformation.gif",
				bundleProcessor
						.getImageFinalPath(
								"/cpCb3df496cbae960efd97933bdd50e5d454/classpathResources/img/iconInformation.gif",
								jawrConfig));
		assertEquals(
				"img/appIcons/application_add.png",
				bundleProcessor
						.getImageFinalPath(
								"/cb31d7ab9cdff1b9eafdf728250d5ea78a/img/appIcons/application_add.png",
								jawrConfig));

		// With servlet mapping
		jawrConfig.setServletMapping("jawrImg");
		assertEquals(
				"classpathResources/img/iconInformation.gif",
				bundleProcessor
						.getImageFinalPath(
								"/jawrImg/cpCb3df496cbae960efd97933bdd50e5d454/classpathResources/img/iconInformation.gif",
								jawrConfig));
		assertEquals(
				"img/appIcons/application_add.png",
				bundleProcessor
						.getImageFinalPath(
								"/jawrImg/cb31d7ab9cdff1b9eafdf728250d5ea78a/img/appIcons/application_add.png",
								jawrConfig));

	}

	public void testBundleProcessing() throws Exception {

		String baseDirPath = FileUtils.getClasspathRootDir()
				+ "/bundleProcessor/wrkDir";
		String tmpDirPath = FileUtils.getClasspathRootDir()
				+ "/bundleProcessor/tmpDir";
		String destDirPath = FileUtils.getClasspathRootDir()
				+ "/bundleProcessor/destDir";

		FileUtils.clearDirectory(tmpDirPath);
		FileUtils.clearDirectory(destDirPath);

		bundleProcessor.process(baseDirPath, tmpDirPath, destDirPath, true);
		checkGeneratedContent(destDirPath);
	}

	public void testBundleProcessingVariant() throws Exception {

		String baseDirPath = FileUtils.getClasspathRootDir()
				+ "/variant/wrkDir";
		String tmpDirPath = FileUtils.getClasspathRootDir() + "/variant/tmpDir";
		String destDirPath = FileUtils.getClasspathRootDir()
				+ "/variant/destDir";

		FileUtils.clearDirectory(tmpDirPath);
		FileUtils.clearDirectory(destDirPath);

		bundleProcessor.process(baseDirPath, tmpDirPath, destDirPath, true);

		checkContentCreated(destDirPath + "/CDN/js/bundle/main.js");
		checkContentCreated(destDirPath + "/CDN/js/bundle/msg.js");
		checkContentCreated(destDirPath + "/CDN/js/bundle/msg@en.js");
		checkContentCreated(destDirPath + "/CDN/js/bundle/msg@en_US.js");
		checkContentCreated(destDirPath + "/CDN/js/bundle/msg@fr.js");
		checkContentCreated(destDirPath + "/CDN/bundles/css/component@@.css");
		checkContentCreated(destDirPath + "/CDN/bundles/css/component@@ssl.css");
		checkContentCreated(destDirPath + "/CDN/bundles/css/component@ie6@.css");
		checkContentCreated(destDirPath
				+ "/CDN/bundles/css/component@ie6@ssl.css");
		checkContentCreated(destDirPath + "/CDN/bundles/css/component@ie7@.css");
		checkContentCreated(destDirPath
				+ "/CDN/bundles/css/component@ie7@ssl.css");
		checkContentCreated(destDirPath + "/CDN/css/one@@.css");
		checkContentCreated(destDirPath + "/CDN/img/calendarIcons/calendar.png");
		checkContentCreated(destDirPath + "/CDN/js/global/jawr.js");
		checkContentCreated(destDirPath + "/CDN/js/global/module.js");
		checkContentCreated(destDirPath + "/CDN/js/index/index.js");
	}

	@Test
	public void testBundleProcessingWithNoFileRemapping() throws Exception {

		String baseDirPath = FileUtils.getClasspathRootDir()
				+ "/bundleProcessor/wrkDir";
		String tmpDirPath = FileUtils.getClasspathRootDir()
				+ "/bundleProcessor/tmpDir";
		String destDirPath = FileUtils.getClasspathRootDir()
				+ "/bundleProcessor/destDir";

		FileUtils.clearDirectory(tmpDirPath);
		FileUtils.clearDirectory(destDirPath);

		bundleProcessor.process(baseDirPath, tmpDirPath, destDirPath, null,
				new ArrayList<String>(), true, true,
				servletAPIVersion);
		checkGeneratedContentWithNoFileRemapping(destDirPath);

	}

	@Test
	public void testSpringBundleProcessing() throws Exception {

		String baseDirPath = FileUtils.getClasspathRootDir()
				+ "/bundleProcessor/spring/wrkDir";
		String tmpDirPath = FileUtils.getClasspathRootDir()
				+ "/bundleProcessor/spring/tmpDir";
		String destDirPath = FileUtils.getClasspathRootDir()
				+ "/bundleProcessor/spring/destDir";

		FileUtils.clearDirectory(tmpDirPath);
		FileUtils.clearDirectory(destDirPath);

		bundleProcessor
				.process(
						baseDirPath,
						tmpDirPath,
						destDirPath,
						"classpath:/spring-JawrConfig.xml,/WEB-INF/dispatcher-servlet.xml",
						new ArrayList<String>(), true, false, servletAPIVersion);
		checkGeneratedContent(destDirPath);
	}

	@Test
	public void testSpringBundleProcessingWithPlaceHolders() throws Exception {

		String baseDirPath = FileUtils.getClasspathRootDir()
				+ "/bundleProcessor/spring/with-placeholders/wrkDir";
		String tmpDirPath = FileUtils.getClasspathRootDir()
				+ "/bundleProcessor/spring/with-placeholders/tmpDir";
		String destDirPath = FileUtils.getClasspathRootDir()
				+ "/bundleProcessor/spring/with-placeholders/destDir";

		FileUtils.clearDirectory(tmpDirPath);
		FileUtils.clearDirectory(destDirPath);

		bundleProcessor
				.process(
						baseDirPath,
						tmpDirPath,
						destDirPath,
						"classpath:/spring-JawrConfig.xml,/WEB-INF/dispatcher-servlet.xml",
						new ArrayList<String>(), true, false, servletAPIVersion);
		checkGeneratedContent(destDirPath);
	}

	@Test
	public void testSpringBundleProcessingWithNoSpringConfigSet()
			throws Exception {

		String baseDirPath = FileUtils.getClasspathRootDir()
				+ "/bundleProcessor/spring/wrkDir";
		String tmpDirPath = FileUtils.getClasspathRootDir()
				+ "/bundleProcessor/spring/tmpDir";
		String destDirPath = FileUtils.getClasspathRootDir()
				+ "/bundleProcessor/spring/destDir";

		FileUtils.clearDirectory(tmpDirPath);
		FileUtils.clearDirectory(destDirPath);

		bundleProcessor.process(baseDirPath, tmpDirPath, destDirPath, true);
		checkGeneratedContent(destDirPath);
	}

	@Test
	public void testSpringProcessingWithoutMappingBundle() throws Exception {

		String baseDirPath = FileUtils.getClasspathRootDir()
				+ "/bundleProcessor/spring/without-mapping/wrkDir";
		String tmpDirPath = FileUtils.getClasspathRootDir()
				+ "/bundleProcessor/spring/without-mapping/tmpDir";
		String destDirPath = FileUtils.getClasspathRootDir()
				+ "/bundleProcessor/spring/without-mapping/destDir";

		FileUtils.clearDirectory(tmpDirPath);
		FileUtils.clearDirectory(destDirPath);

		bundleProcessor.process(baseDirPath, tmpDirPath, destDirPath,
				"/WEB-INF/dispatcher-servlet.xml",
				new ArrayList<String>(), true, false, servletAPIVersion);
		checkGeneratedContent(destDirPath);
	}

	private void checkGeneratedContent(String destDirPath) {

		checkContentCreated(destDirPath + "/CDN/bundle/js/global.js");
		checkContentCreated(destDirPath + "/CDN/bundle/css/component.css");
		checkContentCreated(destDirPath
				+ "/CDN/classpathResources/img/clock.png");
		checkContentCreated(destDirPath
				+ "/CDN/classpathResources/img/iconInformation.gif");
		checkContentCreated(destDirPath + "/CDN/css/one.css");
		checkContentCreated(destDirPath + "/CDN/img/mysprite.png");
		checkContentCreated(destDirPath + "/CDN/img/appIcons/application.png");
		checkContentCreated(destDirPath
				+ "/CDN/img/appIcons/application_add.png");
		checkContentCreated(destDirPath
				+ "/CDN/img/appIcons/application_cascade.png");
		checkContentCreated(destDirPath
				+ "/CDN/img/appIcons/application_delete.png");
		checkContentCreated(destDirPath
				+ "/CDN/img/appIcons/application_double.png");
		checkContentCreated(destDirPath
				+ "/CDN/img/appIcons/application_edit.png");
		checkContentCreated(destDirPath + "/CDN/img/calendarIcons/calendar.png");
		checkContentCreated(destDirPath
				+ "/CDN/jawr_generator/css/jar/classpathResources/css/temp.css");
		checkContentCreated(destDirPath + "/CDN/js/global/jawr.js");
		checkContentCreated(destDirPath + "/CDN/js/global/module.js");
		checkContentCreated(destDirPath + "/CDN/js/index/index.js");
	}

	private void checkGeneratedContentWithNoFileRemapping(String destDirPath) {

		checkContentCreated(destDirPath + "/CDN/N1870965055/bundle/js/global.js");
		checkContentCreated(destDirPath
				+ "/CDN/cssJawrPath/629518941/bundle/css/component.css");
		checkContentCreated(destDirPath + "/CDN/cssJawrPath/css/one.css");
		checkContentCreated(destDirPath
				+ "/CDN/sprite_cb75b5346ffc8f85281cb643aeac356405/img/mysprite.png");
		checkContentCreated(destDirPath
				+ "/CDN/cbfc517da02d6a64a68e5fea9a5de472f1/img/appIcons/application.png");
		checkContentCreated(destDirPath
				+ "/CDN/cb31d7ab9cdff1b9eafdf728250d5ea78a/img/appIcons/application_add.png");
		checkContentCreated(destDirPath
				+ "/CDN/cbd7d8883463526d79104d1eed9ec97c5e/img/appIcons/application_cascade.png");
		checkContentCreated(destDirPath
				+ "/CDN/cbcca9aa625c6632ad7e4770b8964a7951/img/appIcons/application_delete.png");
		checkContentCreated(destDirPath
				+ "/CDN/cbcca39b61ca862a518871f6f7a26ba7af/img/appIcons/application_double.png");
		checkContentCreated(destDirPath
				+ "/CDN/cb3a06e8c3eb5b3712b7aa9d989255f04d/img/appIcons/application_edit.png");
		checkContentCreated(destDirPath
				+ "/CDN/cb5307b72fafd649c3039882c5d106b5d6/img/calendarIcons/calendar.png");
		checkContentCreated(destDirPath
				+ "/CDN/jar_cb3df496cbae960efd97933bdd50e5d454/classpathResources/img/iconInformation.gif");
		checkContentCreated(destDirPath
				+ "/CDN/jar_cbf1cabcee8d06aecc2dd8a9dd5b43549e/classpathResources/img/clock.png");

		checkContentCreated(destDirPath + "/CDN/js/global/jawr.js");
		checkContentCreated(destDirPath + "/CDN/js/global/module.js");
		checkContentCreated(destDirPath + "/CDN/js/index/index.js");
	}

	private void checkContentCreated(String filePath) {
		File file = new File(filePath);
		Assert.assertTrue("File '" + filePath + "' has not been created",
				file.exists());
		Assert.assertTrue("File '" + filePath + "'is empty", file.length() > 0);
	}
}
