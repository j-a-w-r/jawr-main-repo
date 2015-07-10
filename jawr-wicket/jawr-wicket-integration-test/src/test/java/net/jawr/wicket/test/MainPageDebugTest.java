package net.jawr.wicket.test;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import net.jawr.web.test.JawrTestConfigFiles;
import net.jawr.web.test.utils.Utils;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.JavaScriptPage;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlScript;

/**
 * Test case for standard page in debug mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/standard/config/web.xml", jawrConfig = "net/jawr/web/debug/config/jawr.properties")
public class MainPageDebugTest extends MainPageTest {

	@Test
	public void testPageLoad() throws Exception {

		final List<String> expectedAlerts = Collections
				.singletonList("A little message retrieved from the message bundle : Hello $ world!");
		assertEquals(expectedAlerts, collectedAlerts);

		assertContentEquals(
				"/net/jawr/web/debug/resources/index-jsp-result-debug-mode-expected.txt",
				page);
	}

	@Test
	public void checkGeneratedJsLinks() {
		
		// Test generated Script link
		List<HtmlScript> scripts = getJsScriptTags();
		assertEquals(6, scripts.size());
		int i = 0;
		HtmlScript script = (HtmlScript) scripts.get(i++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()
						+ "/jawrJs/js/global/global.js?d=11111",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(i++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()
						+ "/jawrJs/js/global/global_1.js?d=11111",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(i++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()
						+ "/jawrJs/js/global/jawr.js?d=11111",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(i++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()
						+ "/jawrJs/js/index/index.js?d=11111",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(i++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()
						+ "/jawrJs/jawr_generator.js?d=11111&generationConfigParam=messages%3Amessages%40en_US",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(i++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()
						+ "/jawrJs/jawr_generator.js?d=11111&generationConfigParam=testJs%3AgeneratedContent.js",
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
				"/net/jawr/web/debug/resources/index.js", page);
		
		script = (HtmlScript) scripts.get(i++);
		page = getJavascriptPage(script);
		assertContentEquals(
				"/net/jawr/web/debug/resources/msg-debug.js", page);
		
		script = (HtmlScript) scripts.get(i++);
		page = getJavascriptPage(script);
		assertContentEquals(
				"/net/jawr/web/debug/resources/generatedContent.js", page);
		

		
	}

	@Test
	public void checkGeneratedCssLinks() {
		// Test generated Css link
		final List<?> styleSheets = getHtmlLinkTags();
		assertEquals(2, styleSheets.size());
		HtmlLink css = (HtmlLink) styleSheets.get(0);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()
						+ "/jawrCss/jawr_generator.css?d=11111&generationConfigParam=jar%3Afwk%2Fcss%2Ftemp.css",
				css.getHrefAttribute());

		css = (HtmlLink) styleSheets.get(1);
		Utils.assertGeneratedLinkEquals(getUrlPrefix()
				+ "/jawrCss/css/one.css?d=11111", css.getHrefAttribute());
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
