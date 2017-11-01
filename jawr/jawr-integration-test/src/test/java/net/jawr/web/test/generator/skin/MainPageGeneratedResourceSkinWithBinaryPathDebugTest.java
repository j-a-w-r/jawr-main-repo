package net.jawr.web.test.generator.skin;


import static org.junit.Assert.assertEquals;

import java.util.List;

import net.jawr.web.test.AbstractPageTest;
import net.jawr.web.test.JawrTestConfigFiles;
import net.jawr.web.test.utils.Utils;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.JavaScriptPage;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlImageInput;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlScript;

/**
 * Test case for page using image generator feature in production mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/generator/skin/debug/config/web-with-binarypath.xml", jawrConfig = "net/jawr/web/generator/skin/debug/config/jawr.properties")
public class MainPageGeneratedResourceSkinWithBinaryPathDebugTest extends AbstractPageTest {

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
		return getServerUrlPrefix() + getUrlPrefix()+"/index.jsp";
	}

	@Test
	public void testPageLoad() throws Exception {
		
		assertContentEquals("/net/jawr/web/generator/skin/debug/resources/index-jsp-result-with-binarypath-debug-mode-expected.txt", page);
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
		assertContentEquals("/net/jawr/web/generator/skin/debug/resources/msg-debug.js", page);
		
		script = (HtmlScript) scripts.get(1);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/debug/resources/generatedContent.js", page);
	}

	@Test
	public void checkGeneratedCssLinks() {
		// Test generated Css link
		final List<?> styleSheets = getHtmlLinkTags();
		assertEquals(4, styleSheets.size());
		HtmlLink css = (HtmlLink) styleSheets.get(0);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.css?d=11111&generationConfigParam=jar%3Afwk%2Fcss%2Ftemp.css",
				css.getHrefAttribute());
		
		css = (HtmlLink) styleSheets.get(1);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/css/one.css?d=11111",css.getHrefAttribute());
		
		css = (HtmlLink) styleSheets.get(2);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.css?d=11111&generationConfigParam=skin%3A%2Fcss%2Fgenerator%2Fskin%2Fsummer%2Fen_US%2Ftheme.css%40en_US%40summer",css.getHrefAttribute());
		
		css = (HtmlLink) styleSheets.get(3);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.css?d=11111&generationConfigParam=skin%3A%2Fcss%2Fgenerator%2Fskin%2Fsummer%2Fen_US%2Ftheme1.css%40en_US%40summer",css.getHrefAttribute());
		
		
	}

	@Test
	public void testCssBundleContent() throws Exception {

		final List<?> styleSheets = getHtmlLinkTags();
		HtmlLink css = (HtmlLink) styleSheets.get(0);
		TextPage page = getCssPage(css);
		assertContentEquals("/net/jawr/web/generator/skin/debug/resources/jar_temp-with-binarypath.css", page);
		
		css = (HtmlLink) styleSheets.get(1);
		page = getCssPage(css);
		assertContentEquals("/net/jawr/web/generator/skin/debug/resources/one-with-binarypath.css", page);
		
		css = (HtmlLink) styleSheets.get(2);
		page = getCssPage(css);
		assertContentEquals("/net/jawr/web/generator/skin/debug/resources/theme_summer_us-with-binarypath.css", page);
		
		css = (HtmlLink) styleSheets.get(3);
		page = getCssPage(css);
		assertContentEquals("/net/jawr/web/generator/skin/debug/resources/theme1_summer_us-with-binarypath.css", page);
		
	}

	@Test
	public void checkGeneratedHtmlImageLinks() {
		// Test generated HTML image link
		final List<?> images = getHtmlImageTags();
		assertEquals(1, images.size());
		final HtmlImage img = (HtmlImage) images.get(0);
		Utils.assertGeneratedLinkEquals(getUrlPrefix()+"/jawrBinary/cbfc517da02d6a64a68e5fea9a5de472f1/img/appIcons/application.png",
				img.getSrcAttribute());

	}

	@Test
	public void checkGeneratedHtmlImageInputLinks() {
		// Test generated HTML image link
		final List<HtmlImageInput> images = getHtmlImageInputTags();
		assertEquals(1, images.size());
		final HtmlImageInput img = images.get(0);
		Utils.assertGeneratedLinkEquals(getUrlPrefix()+"/jawrBinary/cb30a18063ef42b090194a7e936086960f/img/cog.png", 
				img.getSrcAttribute());

	}
}
