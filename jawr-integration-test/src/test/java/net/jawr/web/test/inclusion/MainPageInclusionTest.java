package net.jawr.web.test.inclusion;

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
 * Test case for page using inclusion features in production mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/inclusion/standard/config/web.xml", jawrConfig = "net/jawr/web/inclusion/standard/config/jawr.properties")
public class MainPageInclusionTest extends AbstractPageTest {

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
		
		assertContentEquals("/net/jawr/web/inclusion/standard/resources/inclusionTest-result-expected.txt", page);
	}

	@Test
	public void checkGeneratedJsLinks() {
		// Test generated Script link
		final List<?> scripts = getJsScriptTags();
		assertEquals(5, scripts.size());
		HtmlScript script = (HtmlScript) scripts.get(0);
		
		assertEquals(
				getUrlPrefix()+"/N578619511/bundles/globalBundle.js",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(1);
		assertEquals(
				getUrlPrefix()+"/N473643075/bundles/compositeBundle.js",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(2);
		assertEquals(
				getUrlPrefix()+"/411147102/bundles/stdBundle.js",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(3);
		assertEquals(
				getUrlPrefix()+"/1894355696/bundles/productionBundle.js",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(4);
		assertEquals(
				"http://mycompany.com/js/production.js",
				script.getSrcAttribute());
	}

	@Test
	public void testJsBundleContent() throws Exception {

		final List<?> scripts = getJsScriptTags();
		HtmlScript script = (HtmlScript) scripts.get(0);
		JavaScriptPage page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/inclusion/standard/resources/js/globalBundle.js", page);
		
		script = (HtmlScript) scripts.get(1);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/inclusion/standard/resources/js/compositeBundle.js", page);
		
		script = (HtmlScript) scripts.get(2);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/inclusion/standard/resources/js/stdBundle.js", page);
		
		script = (HtmlScript) scripts.get(3);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/inclusion/standard/resources/js/productionBundle.js", page);
		
	}

	@Test
	public void checkGeneratedCssLinks() {
		// Test generated Css link
		final List<?> styleSheets = getHtmlLinkTags();
		assertEquals(4, styleSheets.size());
		HtmlLink css = (HtmlLink) styleSheets.get(0);
		assertEquals(
				getUrlPrefix()+"/N1822449606/bundles/globalStyleBundle.css",
				css.getHrefAttribute());
		css = (HtmlLink) styleSheets.get(1);
		assertEquals(
				getUrlPrefix()+"/N39807316/bundles/compositeStyleBundle.css",
				css.getHrefAttribute());
		css = (HtmlLink) styleSheets.get(2);
		assertEquals(
				getUrlPrefix()+"/N220599642/bundles/productionStyleBundle.css",
				css.getHrefAttribute());
		css = (HtmlLink) styleSheets.get(3);
		assertEquals(
				"http://mycompany.com/css/production.css",
				css.getHrefAttribute());

	}

	@Test
	public void testCssBundleContent() throws Exception {

		final List<?> styleSheets = getHtmlLinkTags();
		HtmlLink css = (HtmlLink) styleSheets.get(0);
		TextPage page = getCssPage(css);
		assertContentEquals("/net/jawr/web/inclusion/standard/resources/css/globalBundle.css", page);
		
		css = (HtmlLink) styleSheets.get(1);
		page = getCssPage(css);
		assertContentEquals("/net/jawr/web/inclusion/standard/resources/css/compositeBundle.css", page);
		
		css = (HtmlLink) styleSheets.get(2);
		page = getCssPage(css);
		assertContentEquals("/net/jawr/web/inclusion/standard/resources/css/productionBundle.css", page);
		
	}
}
