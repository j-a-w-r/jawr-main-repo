package net.jawr.web.test.inclusion;

import static net.jawr.web.test.utils.Utils.assertGeneratedLinkEquals;
import static org.junit.Assert.assertEquals;

import java.util.List;

import net.jawr.web.test.AbstractPageTest;
import net.jawr.web.test.JawrTestConfigFiles;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.JavaScriptPage;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlScript;

/**
 * Test case for page using inclusion features in debug mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/inclusion/debug/config/web.xml", jawrConfig = "net/jawr/web/inclusion/debug/config/jawr.properties")
public class MainPageInclusionDebugTest extends AbstractPageTest {

	/* (non-Javadoc)
	 * @see net.jawr.web.AbstractPageTest#createWebClient()
	 */
	@Override
	protected WebClient createWebClient() {
		WebClient webClient = super.createWebClient();
		
		// Update the webClient so it will not throw an exception when it will try to load the external JS file which doesn't exists
		webClient.setThrowExceptionOnFailingStatusCode(false);
		return webClient;
	}

	@Override
	protected String getPageUrl() {
		return getServerUrlPrefix() + getUrlPrefix()+"/inclusion/inclusionTest.jsp";
	}

	@Test
	public void testPageLoad() throws Exception {
		
		assertContentEquals("/net/jawr/web/inclusion/debug/resources/inclusionTest-result-expected.txt", page);
	}

	@Test
	public void checkGeneratedJsLinks() {
		// Test generated Script link
		final List<?> scripts = getJsScriptTags();
		assertEquals(8, scripts.size());
		HtmlScript script = (HtmlScript) scripts.get(0);
		assertGeneratedLinkEquals(
				getUrlPrefix()+"/js/global/jawr.js?d=11111",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(1);
		assertGeneratedLinkEquals(
				getUrlPrefix()+"/js/inclusion/global/global.js?d=11111",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(2);
		assertGeneratedLinkEquals(
				getUrlPrefix()+"/js/inclusion/global/global_1.js?d=11111",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(3);
		assertGeneratedLinkEquals(
				getUrlPrefix()+"/js/index/index.js?d=11111",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(4);
		assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.js?d=11111&generationConfigParam=testJs%3AgeneratedContent.js",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(5);
		assertGeneratedLinkEquals(
				getUrlPrefix()+"/js/inclusion/debugOnly.js?d=11111",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(6);
		assertGeneratedLinkEquals(
				getUrlPrefix()+"/js/index/index.js?d=11111",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(7);
		assertGeneratedLinkEquals(
				getUrlPrefix()+"/js/inclusion/externalProduction.js?d=11111",
				script.getSrcAttribute());
	}

	@Test
	public void testJsBundleContent() throws Exception {

		final List<?> scripts = getJsScriptTags();
		HtmlScript script = (HtmlScript) scripts.get(0);
		JavaScriptPage page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/inclusion/debug/resources/js/jawr.js", page);
		
		script = (HtmlScript) scripts.get(1);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/inclusion/debug/resources/js/global.js", page);
		
		script = (HtmlScript) scripts.get(2);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/inclusion/debug/resources/js/global_1.js", page);
		
		script = (HtmlScript) scripts.get(3);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/inclusion/debug/resources/js/index.js", page);
		
		script = (HtmlScript) scripts.get(4);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/inclusion/debug/resources/js/generatedContent.js", page);
		
		script = (HtmlScript) scripts.get(5);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/inclusion/debug/resources/js/debugOnly.js", page);
		
		script = (HtmlScript) scripts.get(6);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/inclusion/debug/resources/js/index.js", page);
		
		script = (HtmlScript) scripts.get(7);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/inclusion/debug/resources/js/externalProduction.js", page);
		
	}

	@Test
	public void checkGeneratedCssLinks() {
		// Test generated Css link
		final List<?> styleSheets = getHtmlLinkTags();
		assertEquals(6, styleSheets.size());
		HtmlLink css = (HtmlLink) styleSheets.get(0);
					
		assertGeneratedLinkEquals(
				getUrlPrefix()+"/css/inclusion/global/global.css?d=11111",
				css.getHrefAttribute());
		css = (HtmlLink) styleSheets.get(1);
		assertGeneratedLinkEquals(
				getUrlPrefix()+"/css/inclusion/global/global_1.css?d=11111",
				css.getHrefAttribute());
		css = (HtmlLink) styleSheets.get(2);
		assertGeneratedLinkEquals(
				getUrlPrefix()+"/css/one.css?d=11111",
				css.getHrefAttribute());
		css = (HtmlLink) styleSheets.get(3);
		assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.css?d=11111&generationConfigParam=testCss%3AgeneratedContent.css",
				css.getHrefAttribute());
		css = (HtmlLink) styleSheets.get(4);
		assertGeneratedLinkEquals(
				getUrlPrefix()+"/css/inclusion/debugOnly.css?d=11111",
				css.getHrefAttribute());
		css = (HtmlLink) styleSheets.get(5);
		assertGeneratedLinkEquals(
				getUrlPrefix()+"/css/inclusion/externalProduction.css?d=11111",
				css.getHrefAttribute());

	}

	@Test
	public void testCssBundleContent() throws Exception {

		final List<?> styleSheets = getHtmlLinkTags();
		HtmlLink css = (HtmlLink) styleSheets.get(0);
		TextPage page = getCssPage(css);
		assertContentEquals("/net/jawr/web/inclusion/debug/resources/css/global.css", page);
		
		css = (HtmlLink) styleSheets.get(1);
		page = getCssPage(css);
		assertContentEquals("/net/jawr/web/inclusion/debug/resources/css/global_1.css", page);
		
		css = (HtmlLink) styleSheets.get(2);
		page = getCssPage(css);
		assertContentEquals("/net/jawr/web/inclusion/debug/resources/css/one.css", page);
		
		css = (HtmlLink) styleSheets.get(3);
		page = getCssPage(css);
		assertContentEquals("/net/jawr/web/inclusion/debug/resources/css/generatedContent.css", page);
		
		css = (HtmlLink) styleSheets.get(4);
		page = getCssPage(css);
		assertContentEquals("/net/jawr/web/inclusion/debug/resources/css/debugOnly.css", page);
		
		css = (HtmlLink) styleSheets.get(5);
		page = getCssPage(css);
		assertContentEquals("/net/jawr/web/inclusion/debug/resources/css/externalProduction.css", page);
		
	}
}
