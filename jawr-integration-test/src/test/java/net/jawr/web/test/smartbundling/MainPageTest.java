/**
 * 
 */
package net.jawr.web.test.smartbundling;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.JavaScriptPage;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlImageInput;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlScript;

import static org.junit.Assert.assertEquals;

import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.test.JawrIntegrationServer;
import net.jawr.web.test.JawrTestConfigFiles;
import net.jawr.web.test.utils.Utils;

/**
 * Test case for standard page in production mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/smartbundling/config/web.xml", jawrConfig = "net/jawr/web/smartbundling/config/jawr.properties")
public class MainPageTest {

	/** The logger */
	private static Logger LOGGER = LoggerFactory.getLogger(MainPageTest.class);

	/** The web client */
	protected WebClient webClient;

	/** The list of alerts collected */
	protected List<String> collectedAlerts;

	/** The HTML page */
	protected HtmlPage page;
	
	/**
	 * Returns the webapp context path
	 * @return the webapp context path
	 */
	protected String getUrlPrefix(){
		return JawrIntegrationServer.getInstance().getContextPath();
	}
	
	/**
	 * Returns the server url prefix
	 * @return the server url prefix
	 */
	protected String getServerUrlPrefix() {
		return JawrIntegrationServer.getInstance().getServerUrlPrefix();
	}
	
	/**
	 * Creates the web client
	 * 
	 * @return the web client
	 */
	protected WebClient createWebClient() {
		
		WebClient webClient = new WebClient(BrowserVersion.FIREFOX_3);
		// Defines the accepted language for the web client.
		webClient.addRequestHeader("Accept-Language", getAcceptedLanguage());
		return webClient;
	}
	
	/**
	 * Returns the page URL to test
	 * @return the page URL to test
	 */
	protected String getPageUrl() {
		return getServerUrlPrefix() + getUrlPrefix()+"/index.jsp";
	}
	
	@BeforeClass
	public static void setInitFlag() throws IOException {
		
		LOGGER.debug("****** Start Test case *********");
		JawrIntegrationServer.getInstance().initBeforeTestCase();
	}

	@Before
	public void setup() throws Exception {

		// Make sure that files are the correct one
		String webappRootDir = JawrIntegrationServer.getInstance().getWebAppRootDir();
		File srcDir = new File(webappRootDir+"/smartbundling/backup/"); 
		File destDir = new File(webappRootDir,"/smartbundling/");
		net.jawr.web.util.FileUtils.clearDirectory(destDir.getAbsolutePath()+"/css/");
		net.jawr.web.util.FileUtils.clearDirectory(destDir.getAbsolutePath()+"/js/");
		net.jawr.web.util.FileUtils.copyDirectory(srcDir, destDir);
		
		LOGGER.debug("****** Start Test "+getClass()+" *********");
		initConfigFile();
		JawrIntegrationServer.getInstance().setup();

		webClient = createWebClient();
		collectedAlerts = new ArrayList<String>();
		webClient.setAlertHandler(new CollectingAlertHandler(collectedAlerts));
		page = webClient.getPage(getPageUrl());
	}
	
	/**
	 * Initialize the web app config files
	 */
	protected void initConfigFile(){
	
		JawrTestConfigFiles annotationConfig = (JawrTestConfigFiles) getClass()
				.getAnnotation(JawrTestConfigFiles.class);
		try{
			String currentJawrConfigPath = annotationConfig.jawrConfig();
			String webappRootDir = JawrIntegrationServer.getInstance().getWebAppRootDir();
			
			OutputStream outFile = new FileOutputStream(new File(webappRootDir, "/WEB-INF/classes/jawr.properties"));
			IOUtils.copy(getClass().getClassLoader().getResourceAsStream(
					currentJawrConfigPath), outFile);
			IOUtils.close(outFile);
			
			String currentWebXmlPath = annotationConfig.webXml();
			outFile = new FileOutputStream(new File(webappRootDir, "/WEB-INF/web.xml"));
			IOUtils.copy(getClass().getClassLoader().getResourceAsStream(
					currentWebXmlPath), outFile);
			IOUtils.close(outFile);	
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}

	@After
	public void teardown(){
		LOGGER.debug("****** End Test "+getClass()+" *********");
	}
	
	/**
	 * Resets the test configuration.
	 * 
	 * @throws Exception
	 *             if an exception occurs
	 */
	@AfterClass
	public static void resetTestConfiguration() throws Exception {

		JawrIntegrationServer.getInstance().resetTestConfiguration();
		LOGGER.debug("****** End Test case *********");
		
	}
	
	/**
	 * Returns the locale used for the test
	 * 
	 * @return the locale used for the test
	 */
	public String getAcceptedLanguage() {
		return "en-us";
	}

	/**
	 * Assert that the content of the file name equals to the response of the
	 * page.
	 * 
	 * @param fileName
	 *            the file name
	 * @param page
	 *            the web page
	 * @throws Exception
	 *             if an exception occurs.
	 */
	protected void assertContentEquals(String fileName, Page page)
			throws Exception {
		Utils.assertContentEquals(getClass(), fileName, page);
	}

	/**
	 * Returns the list of HTML script tags which have an src attribute.
	 * 
	 * @return the list of HTML script tag.
	 */
	@SuppressWarnings("unchecked")
	protected List<HtmlScript> getJsScriptTags() {
		return (List<HtmlScript>) page.getByXPath("html/head/script[@src]");
	}

	/**
	 * Returns the list of HTML link tags.
	 * 
	 * @return the list of HTML script tags.
	 */
	@SuppressWarnings("unchecked")
	protected List<HtmlLink> getHtmlLinkTags() {
		return (List<HtmlLink>) page.getByXPath("html/head/link");
	}

	/**
	 * Returns the list of HTML link tags.
	 * 
	 * @return the list of HTML script tags.
	 */
	@SuppressWarnings("unchecked")
	protected List<HtmlImage> getHtmlImageTags() {
		return (List<HtmlImage>) page.getByXPath("//img");
	}

	/**
	 * Returns the list of HTML link tags.
	 * 
	 * @return the list of HTML script tags.
	 */
	@SuppressWarnings("unchecked")
	protected List<HtmlImageInput> getHtmlImageInputTags() {
		return (List<HtmlImageInput>) page.getByXPath("//input[@type='image']");
	}

	/**
	 * Call the webserver to retrieve the javascript page associated to the Html
	 * script object.
	 * 
	 * @param script
	 *            the Html script
	 * @return the javascript page
	 * @throws IOException
	 *             if an IOException occurs
	 * @throws MalformedURLException
	 *             if a MalformedURLException occurs
	 */
	protected JavaScriptPage getJavascriptPage(final HtmlScript script)
			throws IOException, MalformedURLException {
		return webClient.getPage(JawrIntegrationServer.SERVER_URL + script.getSrcAttribute());
	}

	/**
	 * Call the webserver to retrieve the css page associated to the Html link
	 * object.
	 * 
	 * @param css
	 *            the Html link
	 * @return the css page
	 * @throws IOException
	 *             if an IOException occurs
	 * @throws MalformedURLException
	 *             if a MalformedURLException occurs
	 */
	protected TextPage getCssPage(final HtmlLink css) throws IOException,
			MalformedURLException {
		return webClient.getPage(JawrIntegrationServer.SERVER_URL + css.getHrefAttribute());
	}
	
	@Test
	public void testPageLoad() throws Exception {

		final List<String> expectedAlerts = Collections
				.singletonList("A little message retrieved from the message bundle : Hello $ world!");
		assertEquals(expectedAlerts, collectedAlerts);
		
		assertContentEquals("/net/jawr/web/smartbundling/resources/index-jsp-result-1-expected.txt", page);

		checkStandardGeneratedCssLinks();
		checkStandardGeneratedJsLinks();
		checkStandardJsBundleContent();
		checkStandardCssBundleContent();
		checkStandardGeneratedHtmlImageLinks();
		checkStandardGeneratedHtmlImageInputLinks();
		
		JawrIntegrationServer.getInstance().getJettyWebAppContext().stop();
		
		// Sleep
		Thread.sleep(3000);
		
		// Update CSS and JS file
		InputStream is = getClass().getResourceAsStream("/net/jawr/web/smartbundling/resources/css/three.css");
		OutputStream out = new FileOutputStream(JawrIntegrationServer.getInstance().getWebAppRootDir()+"/smartbundling/css/three.css");
		IOUtils.copy(is, out, true);
		
		is = getClass().getResourceAsStream("/net/jawr/web/smartbundling/resources/js/script.js");
		out = new FileOutputStream(JawrIntegrationServer.getInstance().getWebAppRootDir()+"/smartbundling/js/script.js");
		IOUtils.copy(is, out, true);
		
		JawrIntegrationServer.getInstance().getJettyWebAppContext().start();
		page = webClient.getPage(getPageUrl());
		assertContentEquals("/net/jawr/web/smartbundling/resources/index-jsp-result-2-expected.txt", page);

		checkUpdatedGeneratedCssLinks();
		checkUpdatedGeneratedJsLinks();
		checkUpdatedJsBundleContent();
		checkUpdatedCssBundleContent();
		checkUpdatedGeneratedHtmlImageLinks();
		checkUpdatedGeneratedHtmlImageInputLinks();
		
	}

	public void checkStandardGeneratedJsLinks() {
		// Test generated Script link
		final List<HtmlScript> scripts = getJsScriptTags();
		assertEquals(1, scripts.size());
		final HtmlScript script = scripts.get(0);
		assertEquals(
				getUrlPrefix()+"/839854056.en_US/js/bundle/msg.js",
				script.getSrcAttribute());
	}

	public void checkStandardJsBundleContent() throws Exception {

		final List<HtmlScript> scripts = getJsScriptTags();
		final HtmlScript script = scripts.get(0);
		final JavaScriptPage page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/smartbundling/resources/msg-bundle.js", page);
	}

	public void checkStandardGeneratedCssLinks() {
		// Test generated Css link
		final List<HtmlLink> styleSheets = getHtmlLinkTags();
		assertEquals(1, styleSheets.size());
		final HtmlLink css = styleSheets.get(0);
		assertEquals(
				getUrlPrefix()+"/N2053553881/fwk/core/component.css",
				css.getHrefAttribute());

	}

	public void checkStandardCssBundleContent() throws Exception {

		final List<HtmlLink> styleSheets = getHtmlLinkTags();
		final HtmlLink css = styleSheets.get(0);
		final TextPage page = getCssPage(css);
		assertContentEquals("/net/jawr/web/smartbundling/resources/component-expected.css", page);
	}

	public void checkStandardGeneratedHtmlImageLinks() {
		// Test generated HTML image link
		final List<?> images = getHtmlImageTags();
		assertEquals(1, images.size());
		final HtmlImage img = (HtmlImage) images.get(0);
		Utils.assertGeneratedLinkEquals(getUrlPrefix()+"/cbfc517da02d6a64a68e5fea9a5de472f1/img/appIcons/application.png",
				img.getSrcAttribute());

	}

	public void checkStandardGeneratedHtmlImageInputLinks() {
		// Test generated HTML image link
		final List<HtmlImageInput> images = getHtmlImageInputTags();
		assertEquals(1, images.size());
		final HtmlImageInput img = images.get(0);
		Utils.assertGeneratedLinkEquals(getUrlPrefix()+"/cb30a18063ef42b090194a7e936086960f/img/cog.png", 
				img.getSrcAttribute());

	}
	
	public void checkUpdatedGeneratedJsLinks() {
		// Test generated Script link
		final List<HtmlScript> scripts = getJsScriptTags();
		assertEquals(1, scripts.size());
		final HtmlScript script = scripts.get(0);
		assertEquals(
				getUrlPrefix()+"/995562889.en_US/js/bundle/msg.js",
				script.getSrcAttribute());
	}

	public void checkUpdatedJsBundleContent() throws Exception {

		final List<HtmlScript> scripts = getJsScriptTags();
		final HtmlScript script = scripts.get(0);
		final JavaScriptPage page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/smartbundling/resources/msg-bundle-2.js", page);
	}

	public void checkUpdatedGeneratedCssLinks() {
		// Test generated Css link
		final List<HtmlLink> styleSheets = getHtmlLinkTags();
		assertEquals(1, styleSheets.size());
		final HtmlLink css = styleSheets.get(0);
		assertEquals(
				getUrlPrefix()+"/1701415561/fwk/core/component.css",
				css.getHrefAttribute());

	}

	public void checkUpdatedCssBundleContent() throws Exception {

		final List<HtmlLink> styleSheets = getHtmlLinkTags();
		final HtmlLink css = styleSheets.get(0);
		final TextPage page = getCssPage(css);
		assertContentEquals("/net/jawr/web/smartbundling/resources/component-expected-2.css", page);
	}

	public void checkUpdatedGeneratedHtmlImageLinks() {
		// Test generated HTML image link
		final List<?> images = getHtmlImageTags();
		assertEquals(1, images.size());
		final HtmlImage img = (HtmlImage) images.get(0);
		Utils.assertGeneratedLinkEquals(getUrlPrefix()+"/cbfc517da02d6a64a68e5fea9a5de472f1/img/appIcons/application.png",
				img.getSrcAttribute());

	}

	public void checkUpdatedGeneratedHtmlImageInputLinks() {
		// Test generated HTML image link
		final List<HtmlImageInput> images = getHtmlImageInputTags();
		assertEquals(1, images.size());
		final HtmlImageInput img = images.get(0);
		Utils.assertGeneratedLinkEquals(getUrlPrefix()+"/cb30a18063ef42b090194a7e936086960f/img/cog.png", 
				img.getSrcAttribute());

	}
	
}
