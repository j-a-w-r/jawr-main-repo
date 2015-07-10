package net.jawr.web.test.bundle.dependency;

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
@JawrTestConfigFiles(webXml = "net/jawr/web/standard/config/web.xml", jawrConfig = "net/jawr/web/bundle/dependency/debug/config/jawr.properties")
public class MainPageDependencyDebugTest extends MainPageDependencyTest {

	@Test
	public void testPageLoad() throws Exception {

		final List<String> expectedAlerts = Collections
				.singletonList("A little message retrieved from the message bundle : Hello $ world!");
		assertEquals(expectedAlerts, collectedAlerts);

		assertContentEquals("/net/jawr/web/bundle/dependency/debug/resources/index-jsp-result-debug-mode-expected.txt", page);
		
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
	public void testJsBundleContent() throws Exception {

		final List<?> scripts = getJsScriptTags();
		HtmlScript script = (HtmlScript) scripts.get(0);
		JavaScriptPage page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/debug/resources/msg-debug.js", page);
		
		script = (HtmlScript) scripts.get(1);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/debug/resources/generatedContent.js", page);
	}

	@Test
	public void checkGeneratedCssLinks() {
		// Test generated Css link
		final List<?> styleSheets = getHtmlLinkTags();
		assertEquals(8, styleSheets.size());
		HtmlLink css = (HtmlLink) styleSheets.get(0);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/css/inclusion/global/global.css?d=11111",
				css.getHrefAttribute());
		
		css = (HtmlLink) styleSheets.get(1);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/css/inclusion/global/global_1.css?d=11111",css.getHrefAttribute());
		
		css = (HtmlLink) styleSheets.get(2);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/css/one.css?d=11111",css.getHrefAttribute());
		
		css = (HtmlLink) styleSheets.get(3);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/css/two.css?d=11111",css.getHrefAttribute());
		
		css = (HtmlLink) styleSheets.get(4);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/css/one.css?d=11111",css.getHrefAttribute());
		
		css = (HtmlLink) styleSheets.get(5);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/css/two.css?d=11111",css.getHrefAttribute());
		
		css = (HtmlLink) styleSheets.get(6);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/css/two.css?d=11111",css.getHrefAttribute());
		
		css = (HtmlLink) styleSheets.get(7);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/css/one.css?d=11111",css.getHrefAttribute());
	}

	@Test
	public void testCssBundleContent() throws Exception {

		final List<?> styleSheets = getHtmlLinkTags();
		HtmlLink css = (HtmlLink) styleSheets.get(0);
		TextPage page = getCssPage(css);
		assertContentEquals("/net/jawr/web/bundle/dependency/debug/resources/global.css", page);
		
		css = (HtmlLink) styleSheets.get(1);
		page = getCssPage(css);
		assertContentEquals("/net/jawr/web/bundle/dependency/debug/resources/global_1.css", page);
		
		css = (HtmlLink) styleSheets.get(2);
		page = getCssPage(css);
		assertContentEquals("/net/jawr/web/bundle/dependency/debug/resources/one.css", page);
		
		css = (HtmlLink) styleSheets.get(3);
		page = getCssPage(css);
		assertContentEquals("/net/jawr/web/bundle/dependency/debug/resources/two.css", page);
		
		css = (HtmlLink) styleSheets.get(4);
		page = getCssPage(css);
		assertContentEquals("/net/jawr/web/bundle/dependency/debug/resources/one.css", page);
		
		css = (HtmlLink) styleSheets.get(5);
		page = getCssPage(css);
		assertContentEquals("/net/jawr/web/bundle/dependency/debug/resources/two.css", page);
		
		css = (HtmlLink) styleSheets.get(6);
		page = getCssPage(css);
		assertContentEquals("/net/jawr/web/bundle/dependency/debug/resources/two.css", page);
		
		css = (HtmlLink) styleSheets.get(7);
		page = getCssPage(css);
		assertContentEquals("/net/jawr/web/bundle/dependency/debug/resources/one.css", page);
	}

}
