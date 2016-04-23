/**
 * 
 */
package net.jawr.web.test;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.JavaScriptPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlScript;

/**
 * Test case for standard page in production mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/standard/config/web.xml", jawrConfig = "net/jawr/web/standard/config/jawr-with-prefixed-bundle.properties")
public class MainPageWithPrefixedBundleTest extends MainPageTest {

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

	@Test
	public void testPageLoad() throws Exception {

		final List<String> expectedAlerts = Collections
				.singletonList("A little message retrieved from the message bundle : Hello $ world!");
		assertEquals(expectedAlerts, collectedAlerts);

		assertContentEquals(
				"/net/jawr/web/standard/resources/index-with-prefixed-bundle-jsp-result-expected.txt",
				page);
	}

	@Test
	public void checkGeneratedJsLinks() {
		// Test generated Script link
		final List<HtmlScript> scripts = getJsScriptTags();
		assertEquals(1, scripts.size());
		HtmlScript script = scripts.get(0);
		assertEquals(getUrlPrefix() + "/jawrJs/690372103.en_US/js/bundle/msg.js",
				script.getSrcAttribute());
	}

	@Test
	public void testJsBundleContent() throws Exception {

		final List<HtmlScript> scripts = getJsScriptTags();
		final HtmlScript script = scripts.get(0);
		final JavaScriptPage page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/standard/resources/msg-bundle.js",
				page);
	}

}
