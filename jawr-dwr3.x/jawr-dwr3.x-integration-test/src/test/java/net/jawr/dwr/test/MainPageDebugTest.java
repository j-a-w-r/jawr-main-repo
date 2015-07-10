package net.jawr.dwr.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.List;

import net.jawr.web.test.utils.Utils;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.JavaScriptPage;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlScript;

/**
 * Test case for standard page in debug mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrDWRTestConfigFiles(webXml = "net/jawr/web/standard/config/web.xml", jawrConfig = "net/jawr/web/debug/config/jawr.properties", dwrConfig = "net/jawr/web/standard/config/dwr.xml")
public class MainPageDebugTest extends MainPageTest {

	@Test
	public void testPageLoad() throws Exception {

		final List<String> expectedAlerts = Collections
				.singletonList("A little message retrieved from the message bundle : Hello $ world!");
		assertEquals(expectedAlerts, collectedAlerts);

		assertContentEquals(
				"/net/jawr/web/debug/resources/index-jsp-result-debug-mode-expected.txt",
				page);

		// Checks that before the call to the server the element is empty
		HtmlElement spanResult = page.getHtmlElementById("demoReply");
		assertNull(spanResult.getFirstChild());

		HtmlElement button = page.getHtmlElementById("sendButton");
		page = button.click();
		webClient.waitForBackgroundJavaScript(5 * 1000);

		// Checks that after the call to the server the element is correctly
		// filled
		assertEquals("Hello, Joe", spanResult.getFirstChild().getNodeValue());
	}

	@Test
	public void checkGeneratedJsLinks() {
		
		@SuppressWarnings("unchecked")
		List<HtmlScript> scripts = (List<HtmlScript>) page.getByXPath("html/head/script");
		HtmlScript script = (HtmlScript) scripts.get(5);
		assertEquals("if(!JAWR){var JAWR = {};};;JAWR.jawr_dwr_path='/jawr-dwr3.x-integration-test/dwr';JAWR.app_context_path='/jawr-dwr3.x-integration-test';",
				 Utils.removeGeneratedRandomReferences(script.getFirstChild().getNodeValue()));
		
		// Test generated Script link
		scripts = getJsScriptTags();
		assertEquals(9, scripts.size());
		int i = 0;
		script = (HtmlScript) scripts.get(i++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()
						+ "/jawr/js/global/global.js?d=11111",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(i++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()
						+ "/jawr/js/global/global_1.js?d=11111",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(i++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()
						+ "/jawr/js/global/jawr.js?d=11111",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(i++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()
						+ "/jawr/jawr_generator.js?d=11111&generationConfigParam=dwr%3A_engine",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(i++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()
						+ "/jawr/jawr_generator.js?d=11111&generationConfigParam=dwr%3A_util",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(i++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()
						+ "/jawr/jawr_generator.js?d=11111&generationConfigParam=dwr%3A_**",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(i++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()
						+ "/jawr/jawr_generator.js?d=11111&generationConfigParam=messages%3Amessages%40en_US",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(i++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()
						+ "/jawr/jawr_generator.js?d=11111&generationConfigParam=testJs%3AgeneratedContent.js",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(i++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()
						+ "/jawr/js/index/index.js?d=11111",
				script.getSrcAttribute());
	}

	@Test
	public void testJsBundleContent() throws Exception {

		final List<?> scripts = getJsScriptTags();
		int i = 0;
		HtmlScript script = (HtmlScript) scripts.get(i++);
		JavaScriptPage page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/debug/resources/global.js", page);

		script = (HtmlScript) scripts.get(i++);
		page = getJavascriptPage(script);
		assertContentEquals(
				"/net/jawr/web/debug/resources/global_1.js", page);
		
		script = (HtmlScript) scripts.get(i++);
		page = getJavascriptPage(script);
		assertContentEquals(
				"/net/jawr/web/debug/resources/jawr.js", page);
		
		script = (HtmlScript) scripts.get(i++);
		page = getJavascriptPage(script);
		assertContentEquals(
				"/net/jawr/web/debug/resources/dwrEngine.js", page);
		
		script = (HtmlScript) scripts.get(i++);
		page = getJavascriptPage(script);
		assertContentEquals(
				"/net/jawr/web/debug/resources/dwrUtils.js", page);
		
		script = (HtmlScript) scripts.get(i++);
		page = getJavascriptPage(script);
		assertContentEquals(
				"/net/jawr/web/debug/resources/dwrAll.js", page);
	}

	@Test
	public void checkGeneratedCssLinks() {
		// Test generated Css link
		final List<?> styleSheets = getHtmlLinkTags();
		assertEquals(2, styleSheets.size());
		HtmlLink css = (HtmlLink) styleSheets.get(0);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()
						+ "/css/jawr_generator.css?d=11111&generationConfigParam=jar%3Afwk%2Fcss%2Ftemp.css",
				css.getHrefAttribute());

		css = (HtmlLink) styleSheets.get(1);
		Utils.assertGeneratedLinkEquals(getUrlPrefix()
				+ "/css/css/one.css?d=11111", css.getHrefAttribute());
	}

	@Test
	public void testCssBundleContent() throws Exception {

		final List<?> styleSheets = getHtmlLinkTags();
		HtmlLink css = (HtmlLink) styleSheets.get(0);
		TextPage page = getCssPage(css);
		assertContentEquals("/net/jawr/web/debug/resources/jar_temp.css", page);

		css = (HtmlLink) styleSheets.get(1);
		page = getCssPage(css);
		assertContentEquals("/net/jawr/web/debug/resources/one.css", page);
	}

}
