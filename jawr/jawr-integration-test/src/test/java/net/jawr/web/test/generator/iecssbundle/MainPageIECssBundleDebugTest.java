package net.jawr.web.test.generator.iecssbundle;


import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import net.jawr.web.test.JawrTestConfigFiles;
import net.jawr.web.test.utils.Utils;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.JavaScriptPage;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlScript;

/**
 * Test case for IE css bundle generator in debug mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/generator/iecssbundle/debug/config/web.xml", jawrConfig = "net/jawr/web/generator/iecssbundle/debug/config/jawr.properties")
public class MainPageIECssBundleDebugTest extends MainPageIECssBundleTest {

	@Override
	protected WebClient createWebClient() {
		
		WebClient webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER_6);
		// Defines the accepted language for the web client.
		webClient.addRequestHeader("Accept-Language", getAcceptedLanguage());
		return webClient;
	}

	@Test
	public void testPageLoad() throws Exception {

		final List<String> expectedAlerts = Collections
				.singletonList("A little message retrieved from the message bundle : Hello $ world!");
		assertEquals(expectedAlerts, collectedAlerts);

		assertContentEquals("/net/jawr/web/generator/iecssbundle/debug/resources/ie-index-jsp-result-debug-mode-expected.txt", page);
		
	}

	@Test
	public void checkGeneratedJsLinks() {
		// Test generated Script link
		final List<?> scripts = getJsScriptTags();
		assertEquals(2, scripts.size());
		HtmlScript script = (HtmlScript) scripts.get(0);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.js?d=11111&generationConfigParam=messages%3Amessages%40en_US",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(1);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.js?d=11111&generationConfigParam=testJs%3AgeneratedContent.js",
				script.getSrcAttribute());
	}

	@Test
	public void checkJsBundleContent() throws Exception {

		final List<?> scripts = getJsScriptTags();
		HtmlScript script = (HtmlScript) scripts.get(0);
		JavaScriptPage page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/generator/iecssbundle/debug/resources/msg-debug.js", page);
		
		script = (HtmlScript) scripts.get(1);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/generator/iecssbundle/debug/resources/generatedContent.js", page);
	}

	@Test
	public void checkGeneratedCssLinks() {
		// Test generated Css link
		final List<?> styleSheets = getHtmlLinkTags();
		assertEquals(2, styleSheets.size());
		HtmlLink css = (HtmlLink) styleSheets.get(0);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.css?d=11111&generationConfigParam=ieCssGen%3A%2FN1545881524%2Fbundles%2FglobalStyleBundle.css",
				css.getHrefAttribute());
		
		css = (HtmlLink) styleSheets.get(1);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.css?d=11111&generationConfigParam=ieCssGen%3A%2F497434506%2Ffwk%2Fcore%2Fcomponent.css",
				css.getHrefAttribute());
	}

	@Test
	public void testCssBundleContent() throws Exception {

		final List<?> styleSheets = getHtmlLinkTags();
		HtmlLink css = (HtmlLink) styleSheets.get(0);
		TextPage page = getCssPage(css);
		assertContentEquals("/net/jawr/web/generator/iecssbundle/debug/resources/ieGlobalCssBundle.css", page);
		
		css = (HtmlLink) styleSheets.get(1);
		page = getCssPage(css);
		assertContentEquals("/net/jawr/web/generator/iecssbundle/debug/resources/ieCssBundle.css", page);
	}

}
