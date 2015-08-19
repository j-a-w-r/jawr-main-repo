package net.jawr.web.test.generator.browser;

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
@JawrTestConfigFiles(webXml = "net/jawr/web/generator/browser/debug/config/web.xml", jawrConfig = "net/jawr/web/generator/browser/debug/config/jawr.properties")
public class MainPageGeneratedResourceBrowserDebugTest extends AbstractPageTest {

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
		return getServerUrlPrefix() + getUrlPrefix()+"/generator/browser/index.jsp";
	}

	@Test
	public void testPageLoad() throws Exception {
		
		assertContentEquals("/net/jawr/web/generator/browser/debug/resources/index-jsp-result-debug-mode-expected.txt", page);
	}

	@Test
	public void checkGeneratedJsLinks() {
		// Test generated Script link
		final List<HtmlScript> scripts = getJsScriptTags();
		assertEquals(6, scripts.size());
		HtmlScript script = scripts.get(0);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.js?d=11111&generationConfigParam=browse%3A%2Fjs%2FtabView%2FtabView1.js",
				script.getSrcAttribute());
		script = scripts.get(1);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.js?d=11111&generationConfigParam=browse%3A%2Fjs%2FtabView%2FtabView2.js",
				script.getSrcAttribute());
		script = scripts.get(2);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.js?d=11111&generationConfigParam=browse%3A%2Fjs%2FtabView%2FsubTabView%2FsubTabView1.js",
				script.getSrcAttribute());
		script = scripts.get(3);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.js?d=11111&generationConfigParam=browse%3A%2Fjs%2FtabView%2FsubTabView%2FsubTabView2.js",
				script.getSrcAttribute());
		script = scripts.get(4);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.js?d=11111&generationConfigParam=browse%3A%2Fjs%2FtreeView%2FtreeView1.js",
				script.getSrcAttribute());
		script = scripts.get(5);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.js?d=11111&generationConfigParam=browse%3A%2Fjs%2FtreeView%2FtreeView2.js",
				script.getSrcAttribute());
		
	}

	@Test
	public void testJsBundleContent() throws Exception {

		final List<HtmlScript> scripts = getJsScriptTags();
		HtmlScript script = scripts.get(0);
		JavaScriptPage page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/generator/browser/debug/resources/tabView1.js", page);
		
		script = scripts.get(1);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/generator/browser/debug/resources/tabView2.js", page);
		
		script = scripts.get(2);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/generator/browser/debug/resources/subTabView1.js", page);
		
		script = scripts.get(3);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/generator/browser/debug/resources/subTabView2.js", page);
		
		script = scripts.get(4);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/generator/browser/debug/resources/treeView1.js", page);
		
		script = scripts.get(5);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/generator/browser/debug/resources/treeView2.js", page);
	}

	@Test
	public void checkGeneratedCssLinks() {
		// Test generated Css link
		final List<?> styleSheets = getHtmlLinkTags();
		assertEquals(2, styleSheets.size());
		HtmlLink css = (HtmlLink) styleSheets.get(0);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.css?d=11111&generationConfigParam=jar%3Afwk%2Fcss%2Ftemp.css",
				css.getHrefAttribute());
		
		css = (HtmlLink) styleSheets.get(1);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/css/generator/one.css?d=11111",css.getHrefAttribute());
	}

	@Test
	public void testCssBundleContent() throws Exception {

		final List<?> styleSheets = getHtmlLinkTags();
		HtmlLink css = (HtmlLink) styleSheets.get(0);
		TextPage page = getCssPage(css);
		assertContentEquals("/net/jawr/web/generator/img/debug/resources/jar_temp.css", page);
		
		css = (HtmlLink) styleSheets.get(1);
		page = getCssPage(css);
		assertContentEquals("/net/jawr/web/generator/img/debug/resources/one.css", page);
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
