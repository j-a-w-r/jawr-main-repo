package net.jawr.web.test.generator.skin;


import static org.junit.Assert.assertEquals;

import java.util.List;

import net.jawr.web.test.JawrTestConfigFiles;
import net.jawr.web.test.utils.Utils;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlLink;

/**
 * Test case for page using image generator feature in production mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/generator/skin/debug/config/web.xml", jawrConfig = "net/jawr/web/generator/skin/debug/config/jawr.properties")
public class MainPageGeneratedResourceDisplayAlternateSkinDebugIECssBundleTest extends MainPageGeneratedResourceDisplayAlternateSkinDebugTest {


	/* (non-Javadoc)
	 * @see net.jawr.web.AbstractPageTest#createWebClient()
	 */
	@Override
	protected WebClient createWebClient() {
		
		WebClient webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER_6);
		// Defines the accepted language for the web client.
		webClient.addRequestHeader("Accept-Language", getAcceptedLanguage());
		return webClient;
	}
	
	@Test
	public void testPageLoad() throws Exception {
		
		assertContentEquals("/net/jawr/web/generator/skin/debug/resources/index-jsp-result-with-alternate-skin-ie-debug-mode-expected.txt", page);
	}

	
	@Test
	public void checkGeneratedCssLinks() {
		// Test generated Css link
		final List<?> styleSheets = getHtmlLinkTags();
		assertEquals(2, styleSheets.size());
		HtmlLink css = (HtmlLink) styleSheets.get(0);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.css?d=11111&generationConfigParam=ieCssGen%3A%2FN101424371.en_US%40summer%2Ffwk%2Fcore%2Fcomponent.css",
				css.getHrefAttribute());
		
		css = (HtmlLink) styleSheets.get(1);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.css?d=11111&generationConfigParam=ieCssGen%3A%2FN1715014092.en_US%40winter%2Ffwk%2Fcore%2Fcomponent.css",css.getHrefAttribute());
		checkAlternateStyle(css, "winter");
	}

	@Test
	public void testCssBundleContent() throws Exception {

		final List<?> styleSheets = getHtmlLinkTags();
		HtmlLink css = (HtmlLink) styleSheets.get(0);
		TextPage page = getCssPage(css);
		assertContentEquals("/net/jawr/web/generator/skin/debug/resources/component-ie-debug.css", page);
		
		css = (HtmlLink) styleSheets.get(1);
		page = getCssPage(css);
		assertContentEquals("/net/jawr/web/generator/skin/debug/resources/component-ie-skin-winter-debug.css", page);
		
	}

}
