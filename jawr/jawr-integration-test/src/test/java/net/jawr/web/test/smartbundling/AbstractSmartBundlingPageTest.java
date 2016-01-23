/**
 * Copyright 2015 Ibrahim Chaehoi
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package net.jawr.web.test.smartbundling;

import static org.junit.Assert.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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

import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.test.JawrIntegrationServer;
import net.jawr.web.test.JawrTestConfigFiles;
import net.jawr.web.test.utils.Utils;

/**
 * 
 * @author Ibrahim Chaehoi
 */
public class AbstractSmartBundlingPageTest {

	/** The logger */
	private static Logger LOGGER = LoggerFactory.getLogger(MainPageTest.class);
	/** The web client */
	protected WebClient webClient;
	/** The list of alerts collected */
	protected List<String> collectedAlerts;
	/** The HTML page */
	protected HtmlPage page;
	/** The temporary directory */
	private static String tmpDir;
	/** The backup of the temp directory */
	private static File tmpDirBackup;

	@BeforeClass
	public static void setInitFlag() throws IOException, InterruptedException {

		LOGGER.debug("****** Start Test case *********");
		JawrIntegrationServer.getInstance().initBeforeTestCase();
		tmpDir = System.getProperty("java.io.tmpdir") + getTempFolder();
		File dir = new File(tmpDir);
		if(dir.exists()){
			if(!net.jawr.web.util.FileUtils.deleteDirectory(tmpDir)){
				Thread.sleep(300);
				if(!net.jawr.web.util.FileUtils.deleteDirectory(tmpDir)){
					fail("Unable to delete temp dir : '"+tmpDir+"'");
				}	
			}	
		}
		
		tmpDirBackup = JawrIntegrationServer.getInstance().getJettyWebAppContext().getTempDirectory();
		JawrIntegrationServer.getInstance().getJettyWebAppContext().setTempDirectory(new File(tmpDir));
	}

	/**
	 * @return
	 */
	protected static String getTempFolder() {
		return "jawr-integration-smartbundling-test-"+Calendar.getInstance().getTimeInMillis();
	}

	/**
	 * Returns the webapp context path
	 * 
	 * @return the webapp context path
	 */
	protected String getUrlPrefix() {
		return JawrIntegrationServer.getInstance().getContextPath();
	}

	/**
	 * Returns the server url prefix
	 * 
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
	 * 
	 * @return the page URL to test
	 */
	protected String getPageUrl() {
		return getServerUrlPrefix() + getUrlPrefix() + "/index.jsp";
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
		net.jawr.web.util.FileUtils.deleteDirectory(tmpDir);
		JawrIntegrationServer.getInstance().getJettyWebAppContext().setTempDirectory(tmpDirBackup);
		LOGGER.debug("****** End Test case *********");
	}

	@Before
	public void setup() throws Exception {

		// Make sure that files are the correct one
		String webappRootDir = JawrIntegrationServer.getInstance().getWebAppRootDir();
		File srcDir = new File(webappRootDir + "/smartbundling/backup/");
		File destDir = new File(webappRootDir, "/smartbundling/");
		net.jawr.web.util.FileUtils.clearDirectory(destDir.getAbsolutePath() + "/css/");
		net.jawr.web.util.FileUtils.clearDirectory(destDir.getAbsolutePath() + "/js/");
		net.jawr.web.util.FileUtils.copyDirectory(srcDir, destDir);

		LOGGER.debug("****** Start Test " + getClass() + " *********");
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
	protected void initConfigFile() {

		JawrTestConfigFiles annotationConfig = (JawrTestConfigFiles) getClass()
				.getAnnotation(JawrTestConfigFiles.class);
		try {
			String currentJawrConfigPath = annotationConfig.jawrConfig();
			String webappRootDir = JawrIntegrationServer.getInstance().getWebAppRootDir();

			OutputStream outFile = new FileOutputStream(new File(webappRootDir, "/WEB-INF/classes/jawr.properties"));
			IOUtils.copy(getClass().getClassLoader().getResourceAsStream(currentJawrConfigPath), outFile);
			IOUtils.close(outFile);

			String currentWebXmlPath = annotationConfig.webXml();
			outFile = new FileOutputStream(new File(webappRootDir, "/WEB-INF/web.xml"));
			IOUtils.copy(getClass().getClassLoader().getResourceAsStream(currentWebXmlPath), outFile);
			IOUtils.close(outFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@After
	public void teardown() {
		LOGGER.debug("****** End Test " + getClass() + " *********");
	}

	/**
	 * 
	 */
	public AbstractSmartBundlingPageTest() {
		super();
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
	protected void assertContentEquals(String fileName, Page page) throws Exception {
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
	protected JavaScriptPage getJavascriptPage(final HtmlScript script) throws IOException, MalformedURLException {
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
	protected TextPage getCssPage(final HtmlLink css) throws IOException, MalformedURLException {
		return webClient.getPage(JawrIntegrationServer.SERVER_URL + css.getHrefAttribute());
	}

}