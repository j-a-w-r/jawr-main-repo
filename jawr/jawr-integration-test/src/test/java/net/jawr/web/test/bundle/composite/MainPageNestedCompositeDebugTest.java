package net.jawr.web.test.bundle.composite;

import static net.jawr.web.resource.bundle.factory.util.PathNormalizer.asPath;

import static org.junit.Assert.assertEquals;

import java.util.List;

import net.jawr.web.test.JawrTestConfigFiles;
import net.jawr.web.test.utils.Utils;

import org.junit.Test;
import org.webjars.WebJarAssetLocator;

import com.gargoylesoftware.htmlunit.JavaScriptPage;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlScript;

/**
 * Test case for nested composite bundle page in debug mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/standard/config/web.xml", jawrConfig = "net/jawr/web/bundle/composite/debug/config/jawr.properties")
public class MainPageNestedCompositeDebugTest extends MainPageNestedCompositeTest {

	@Test
	public void testPageLoad() throws Exception {

		assertContentEquals("/net/jawr/web/bundle/composite/debug/resources/index-jsp-result-debug-mode-expected.txt", page);
		
	}

	@Test
	public void checkGeneratedJsLinks() {
		// Test generated Script link
		final List<?> scripts = getJsScriptTags();
		assertEquals(10, scripts.size());
		int idx = 0;
		HtmlScript script = (HtmlScript) scripts.get(idx++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.js?d=11111&generationConfigParam=webjars%3Aangular.js",
				script.getSrcAttribute());
		
		script = (HtmlScript) scripts.get(idx++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.js?d=11111&generationConfigParam=webjars%3Aangular-animate.js",
				script.getSrcAttribute());
		
		script = (HtmlScript) scripts.get(idx++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.js?d=11111&generationConfigParam=webjars%3Aangular-cookies.js",
				script.getSrcAttribute());
		
		script = (HtmlScript) scripts.get(idx++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.js?d=11111&generationConfigParam=webjars%3Aangular-route.js",
				script.getSrcAttribute());
		
		script = (HtmlScript) scripts.get(idx++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.js?d=11111&generationConfigParam=webjars%3Aangular-touch.js",
				script.getSrcAttribute());
		
		script = (HtmlScript) scripts.get(idx++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.js?d=11111&generationConfigParam=webjars%3Aangular-mocks.js",
				script.getSrcAttribute());
		
		script = (HtmlScript) scripts.get(idx++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.js?d=11111&generationConfigParam=webjars%3A%2Fjquery.js",
				script.getSrcAttribute());
		
		script = (HtmlScript) scripts.get(idx++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.js?d=11111&generationConfigParam=webjars%3A%2Fjs%2Fbootstrap.js",
				script.getSrcAttribute());
				
		script = (HtmlScript) scripts.get(idx++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.js?d=11111&generationConfigParam=messages%3Amessages%40en_US",
				script.getSrcAttribute());
		script = (HtmlScript) scripts.get(idx++);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.js?d=11111&generationConfigParam=testJs%3AgeneratedContent.js",
				script.getSrcAttribute());
	}

	@Test
	public void testJsBundleContent() throws Exception {

		final List<?> scripts = getJsScriptTags();
		int idx = 0;
		HtmlScript script = (HtmlScript) scripts.get(idx++);
		JavaScriptPage page = getJavascriptPage(script);
		WebJarAssetLocator locator = new WebJarAssetLocator();
		
		assertContentEquals(asPath(locator.getFullPath("angular.js")), page);
		
		script = (HtmlScript) scripts.get(idx++);
		page = getJavascriptPage(script);
		assertContentEquals(asPath(locator.getFullPath("angular-animate.js")), page);
		
		script = (HtmlScript) scripts.get(idx++);
		page = getJavascriptPage(script);
		assertContentEquals(asPath(locator.getFullPath("angular-cookies.js")), page);
		
		script = (HtmlScript) scripts.get(idx++);
		page = getJavascriptPage(script);
		assertContentEquals(asPath(locator.getFullPath("angular-route.js")), page);
		
		script = (HtmlScript) scripts.get(idx++);
		page = getJavascriptPage(script);
		assertContentEquals(asPath(locator.getFullPath("angular-touch.js")), page);
		
		script = (HtmlScript) scripts.get(idx++);
		page = getJavascriptPage(script);
		assertContentEquals(asPath(locator.getFullPath("angular-mocks.js")), page);
		
		script = (HtmlScript) scripts.get(idx++);
		page = getJavascriptPage(script);
		assertContentEquals(asPath(locator.getFullPath("jquery.js")), page);
		
		script = (HtmlScript) scripts.get(idx++);
		page = getJavascriptPage(script);
		assertContentEquals(asPath(locator.getFullPath("bootstrap.js")), page);
		
		script = (HtmlScript) scripts.get(idx++);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/debug/resources/msg-debug.js", page);
		
		script = (HtmlScript) scripts.get(idx++);
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
				getUrlPrefix()+"/jawr_generator.css?d=11111&generationConfigParam=jar%3Afwk%2Fcss%2Ftemp.css",
				css.getHrefAttribute());
		
		css = (HtmlLink) styleSheets.get(1);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/css/one.css?d=11111",css.getHrefAttribute());
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
