/**
 * 
 */
package net.jawr.web.test.strictmode;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;

import net.jawr.web.test.JawrTestConfigFiles;
import net.jawr.web.test.utils.Utils;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.JavaScriptPage;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlScript;

/**
 * Test case for standard page in production mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/strictmode/config/web-with-servlet-mapping.xml", jawrConfig = "net/jawr/web/strictmode/config/jawr-debug-strict-mode.properties")
public class MainPageDebugStrictModeWithServletMappingTest extends MainPageStrictModeWithServletMappingTest {

	@Test
	public void testPageLoad() throws Exception {

		final List<String> expectedAlerts = Collections
				.singletonList("A little message retrieved from the message bundle : Hello $ world!");
		assertEquals(expectedAlerts, collectedAlerts);

		assertContentEquals("/net/jawr/web/strictmode/resources/index-jsp-result-debug-with-mapping-expected.txt", page);
		
	}

	@Test
	public void checkGeneratedJsLinks() {
		// Test generated Script link
		final List<?> scripts = getJsScriptTags();
		assertEquals(2, scripts.size());
		HtmlScript script = (HtmlScript) scripts.get(0);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawrJS/jawr_generator.js?d=11111&generationConfigParam=messages%3Amessages%40en_US",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(1);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawrJS/jawr_generator.js?d=11111&generationConfigParam=testJs%3AgeneratedContent.js",
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
		assertEquals(2, styleSheets.size());
		HtmlLink css = (HtmlLink) styleSheets.get(0);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawrCSS/jawr_generator.css?d=11111&generationConfigParam=jar%3Afwk%2Fcss%2Ftemp.css",
				css.getHrefAttribute());
		
		css = (HtmlLink) styleSheets.get(1);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawrCSS/css/one.css?d=11111",css.getHrefAttribute());
	}

	@Test
	public void testCssBundleContent() throws Exception {

		final List<?> styleSheets = getHtmlLinkTags();
		HtmlLink css = (HtmlLink) styleSheets.get(0);
		TextPage page = getCssPage(css);
		assertContentEquals("/net/jawr/web/strictmode/resources/jar_temp.css", page);
		
		css = (HtmlLink) styleSheets.get(1);
		page = getCssPage(css);
		assertContentEquals("/net/jawr/web/strictmode/resources/one.css", page);
	}
	
	@Test
	public void checkGeneratedHtmlImageLinks() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		// Test generated HTML image link
		final List<?> images = getHtmlImageTags();
		assertEquals(1, images.size());
		final HtmlImage img = (HtmlImage) images.get(0);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()
						+ "/jawrBin/cbfc517da02d6a64a68e5fea9a5de472f1/img/appIcons/application.png",
				img.getSrcAttribute());

		// Check access to link with wrong hashcode already referenced in img tag
		WebClient webClient = new WebClient();
		webClient.setThrowExceptionOnFailingStatusCode(false);
		int status = webClient
				.getPage(getServerUrlPrefix() + getUrlPrefix()
						+ "/jawrBin/cb7777777/img/appIcons/application.png")
				.getWebResponse().getStatusCode();

		assertEquals(200, status);
		
		// Check access to resource with wrong hashcode and not referenced by jawr
		status = webClient
				.getPage(getServerUrlPrefix() + getUrlPrefix()
						+ "/jawrBin/cb7777777/img/calendarIcons/clock/clock_add.png")
				.getWebResponse().getStatusCode();

		assertEquals(200, status);
	}
}
