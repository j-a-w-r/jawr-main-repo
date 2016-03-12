/**
 * 
 */
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
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlImageInput;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlScript;

/**
 * Test case for standard page in production mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrDWRTestConfigFiles(webXml = "net/jawr/web/standard/config/web.xml", jawrConfig = "net/jawr/web/standard/config/jawr.properties", 
dwrConfig = "net/jawr/web/standard/config/dwr.xml")
public class MainPageTest extends AbstractDwrPageTest {

	/**
	 * Returns the page URL to test
	 * @return the page URL to test
	 */
	protected String getPageUrl() {
		
		return getServerUrlPrefix() + getUrlPrefix()+"/index.jsp";
	}

	@Test
	public void testPageLoad() throws Exception {

		final List<String> expectedAlerts = Collections
				.singletonList("A little message retrieved from the message bundle : Hello $ world!");
		assertEquals(expectedAlerts, collectedAlerts);
		
		assertContentEquals("/net/jawr/web/standard/resources/index-jsp-result-expected.txt", page);
		
		// Checks that before the call to the server the element is empty 
		HtmlElement spanResult = page.getHtmlElementById( "demoReply" ) ;
		assertNull(spanResult.getFirstChild());
		
		HtmlElement button = page.getHtmlElementById( "sendButton" ) ;
		page = button.click() ;
		webClient.waitForBackgroundJavaScript(5 * 1000);
		
		// Checks that after the call to the server the element is correctly filled 
		assertEquals("Hello, Joe", spanResult.getFirstChild().getNodeValue());
		
	}

	@Test
	public void checkGeneratedJsLinks() {
		// Test generated Script link
		final List<HtmlScript> scripts = getJsScriptTags();
		assertEquals(2, scripts.size());
		HtmlScript script = scripts.get(0);
		assertEquals(
				getUrlPrefix()+"/jawr/826075590/bundles/global.js",
				script.getSrcAttribute());
		script = scripts.get(1);
		assertEquals(
				getUrlPrefix()+"/jawr/N341689529.en_US/js/bundle/msg.js",
				script.getSrcAttribute());
	}

	@Test
	public void testJsBundleContent() throws Exception {

		final List<HtmlScript> scripts = getJsScriptTags();
		HtmlScript script = scripts.get(0);
		JavaScriptPage page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/standard/resources/global.js", page);
		
		script = scripts.get(1);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/standard/resources/msg-bundle.js", page);
	}

	@Test
	public void checkGeneratedCssLinks() {
		// Test generated Css link
		final List<HtmlLink> styleSheets = getHtmlLinkTags();
		assertEquals(1, styleSheets.size());
		final HtmlLink css = styleSheets.get(0);
		assertEquals(
				getUrlPrefix()+"/css/2049149885/fwk/core/component.css",
				css.getHrefAttribute());

	}

	@Test
	public void testCssBundleContent() throws Exception {

		final List<HtmlLink> styleSheets = getHtmlLinkTags();
		final HtmlLink css = styleSheets.get(0);
		final TextPage page = getCssPage(css);
		assertContentEquals("/net/jawr/web/standard/resources/component-expected.css", page);
	}

	@Test
	public void checkGeneratedHtmlImageLinks() {
		// Test generated HTML image link
		final List<?> images = getHtmlImageTags();
		assertEquals(1, images.size());
		final HtmlImage img = (HtmlImage) images.get(0);
		Utils.assertGeneratedLinkEquals(getUrlPrefix()+"/cbfc517da02d6a64a68e5fea9a5de472f1/img/appIcons/application.png",
				img.getSrcAttribute());

	}

	@Test
	public void checkGeneratedHtmlImageInputLinks() {
		// Test generated HTML image link
		final List<HtmlImageInput> images = getHtmlImageInputTags();
		assertEquals(1, images.size());
		final HtmlImageInput img = images.get(0);
		Utils.assertGeneratedLinkEquals(getUrlPrefix()+"/cb30a18063ef42b090194a7e936086960f/img/cog.png", 
				img.getSrcAttribute());

	}
	
}
