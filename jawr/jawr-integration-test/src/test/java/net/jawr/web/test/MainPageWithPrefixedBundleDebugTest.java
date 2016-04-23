/**
 * 
 */
package net.jawr.web.test;

import com.gargoylesoftware.htmlunit.WebClient;

/**
 * Test case for standard page in production mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/standard/config/web.xml", jawrConfig = "net/jawr/web/debug/config/jawr-with-prefixed-bundle.properties")
public class MainPageWithPrefixedBundleDebugTest extends MainPageDebugTest {

	/**
	 * Creates the web client
	 * 
	 * @return the web client
	 */
	protected WebClient createWebClient() {

		WebClient webClient = super.createWebClient();
		return webClient;
	}

	/**
	 * Returns the page URL to test
	 * 
	 * @return the page URL to test
	 */
	protected String getPageUrl() {
		return getServerUrlPrefix() + getUrlPrefix()
				+ "/index.jsp";
	}
}
