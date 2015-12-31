/**
 * 
 */
package net.jawr.web.test;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import net.jawr.web.test.utils.Utils;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.JavaScriptPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlScript;

/**
 * Test case for standard page in production mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/standard/config/web.xml", jawrConfig = "net/jawr/web/standard/config/jawr-with-external-bundle-debug.properties")
public class MainPageWithExternalBundleDebugTest extends MainPageDebugTest {

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
				+ "/index-with-external-bundle.jsp";
	}

	@Test
	public void testPageLoad() throws Exception {

		final List<String> expectedAlerts = Collections
				.singletonList("A little message retrieved from the message bundle : Hello $ world!");
		assertEquals(expectedAlerts, collectedAlerts);

		assertContentEquals(
				"/net/jawr/web/standard/resources/index-with-external-bundle-jsp-result-expected-debug.txt",
				page);
	}

	@Test
	public void checkGeneratedJsLinks() {

		// Test generated Script link
		final List<?> scripts = getJsScriptTags();
		assertEquals(3, scripts.size());
		HtmlScript script = (HtmlScript) scripts.get(0);
		assertEquals("http://mycomp/js/foo.js", script.getSrcAttribute());
		script = (HtmlScript) scripts.get(1);

		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()
						+ "/jawr_generator.js?d=11111&generationConfigParam=messages%3Amessages%40en_US",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(2);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()
						+ "/jawr_generator.js?d=11111&generationConfigParam=testJs%3AgeneratedContent.js",
				script.getSrcAttribute());
	}

	@Test
	public void testJsBundleContent() throws Exception {

		final List<?> scripts = getJsScriptTags();
		HtmlScript script = (HtmlScript) scripts.get(1);
		JavaScriptPage page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/debug/resources/msg-debug.js", page);
		
		script = (HtmlScript) scripts.get(2);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/debug/resources/generatedContent.js", page);
	}

}
