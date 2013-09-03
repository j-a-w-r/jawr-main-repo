package net.jawr.web.test.servlet.mapping.standard;

import static org.junit.Assert.assertEquals;
import static net.jawr.web.test.JawrIntegrationServer.SERVER_URL;
import static net.jawr.web.test.JawrIntegrationServer.CONTEXT_PATH;

import java.util.Collections;
import java.util.List;

import net.jawr.web.test.AbstractPageTest;
import net.jawr.web.test.JawrTestConfigFiles;
import net.jawr.web.test.utils.Utils;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.JavaScriptPage;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlImageInput;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlScript;

/**
 * Test case for page using JS and Image servlet mapping in production mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/servlet/mapping/config/web-js-img-servlet-mapping.xml", jawrConfig = "net/jawr/web/servlet/mapping/config/jawr.properties")
public class MainPageJsImgServletMappingTest extends AbstractPageTest {

	/**
	 * Returns the page URL to test
	 * @return the page URL to test
	 */
	protected String getPageUrl() {
		return SERVER_URL + CONTEXT_PATH+"/index.jsp";
	}

	@Test
	public void testPageLoad() throws Exception {

		final List<String> expectedAlerts = Collections
				.singletonList("A little message retrieved from the message bundle : Hello $ world!");
		assertEquals(expectedAlerts, collectedAlerts);
		
		assertContentEquals("/net/jawr/web/servlet/mapping/resources/standard/index-jsp-js-img-servlet-mapping-result-expected.txt", page);
	}

	@Test
	public void checkGeneratedJsLinks() {
		// Test generated Script link
		final List<?> scripts = getJsScriptTags();
		assertEquals(1, scripts.size());
		final HtmlScript script = (HtmlScript) scripts.get(0);
		assertEquals(
				CONTEXT_PATH+"/jsJawr/690372103.en_US/js/bundle/msg.js",
				script.getSrcAttribute());
	}

	@Test
	public void testJsBundleContent() throws Exception {

		final List<?> scripts = getJsScriptTags();
		final HtmlScript script = (HtmlScript) scripts.get(0);
		final JavaScriptPage page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/servlet/mapping/resources/standard/msg-bundle.js", page);
	}

	@Test
	public void checkGeneratedCssLinks() {
		// Test generated Css link
		final List<?> styleSheets = getHtmlLinkTags();
		assertEquals(1, styleSheets.size());
		final HtmlLink css = (HtmlLink) styleSheets.get(0);
		assertEquals(
				CONTEXT_PATH+"/N762152484/fwk/core/component.css",
				css.getHrefAttribute());

	}

	@Test
	public void testCssBundleContent() throws Exception {

		final List<?> styleSheets = getHtmlLinkTags();
		final HtmlLink css = (HtmlLink) styleSheets.get(0);
		final TextPage page = getCssPage(css);
		assertContentEquals("/net/jawr/web/servlet/mapping/resources/standard/component-js-img-servlet-mapping-expected.css", page);
	}

	@Test
	public void checkGeneratedHtmlImageLinks() {
		// Test generated HTML image link
		final List<?> images = getHtmlImageTags();
		assertEquals(1, images.size());
		final HtmlImage img = (HtmlImage) images.get(0);
		Utils.assertGeneratedLinkEquals(CONTEXT_PATH+"/imgJawr/cbfc517da02d6a64a68e5fea9a5de472f1/img/appIcons/application.png",
				img.getSrcAttribute());

	}

	@Test
	public void checkGeneratedHtmlImageInputLinks() {
		// Test generated HTML image link
		final List<?> images = getHtmlImageInputTags();
		assertEquals(1, images.size());
		final HtmlImageInput img = (HtmlImageInput) images.get(0);
		Utils.assertGeneratedLinkEquals(CONTEXT_PATH+"/imgJawr/cb30a18063ef42b090194a7e936086960f/img/cog.png", 
				img.getSrcAttribute());

	}
}
