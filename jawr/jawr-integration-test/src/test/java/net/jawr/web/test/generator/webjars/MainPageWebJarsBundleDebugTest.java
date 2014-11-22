package net.jawr.web.test.generator.webjars;

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
 * Test case for page using image generator feature in production mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/generator/webjars/debug/config/web.xml", jawrConfig = "net/jawr/web/generator/webjars/debug/config/jawr.properties")
public class MainPageWebJarsBundleDebugTest extends MainPageWebJarsBundleTest {

	@Test
	public void testPageLoad() throws Exception {

		final List<String> expectedAlerts = Collections
				.singletonList("A little message retrieved from the message bundle : Hello $ world!");
		assertEquals(expectedAlerts, collectedAlerts);

		assertContentEquals("/net/jawr/web/generator/webjars/debug/resources/index-jsp-result-debug-mode-expected.txt", page);
		
	}

	@Test
	public void checkGeneratedJsLinks() {
		// Test generated Script link
		final List<?> scripts = getJsScriptTags();
		assertEquals(4, scripts.size());
		HtmlScript script = (HtmlScript) scripts.get(0);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.js?d=11111&generationConfigParam=webjars%3A%2Fjquery%2F1.10.2%2Fjquery.js",
				script.getSrcAttribute());
		
		script = (HtmlScript) scripts.get(1);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.js?d=11111&generationConfigParam=webjars%3A%2Fbootstrap%2F3.2.0%2Fjs%2Fbootstrap.js",
				script.getSrcAttribute());
		
		script = (HtmlScript) scripts.get(2);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.js?d=11111&generationConfigParam=messages%3Amessages%40en_US",
				script.getSrcAttribute());
		
		script = (HtmlScript) scripts.get(3);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.js?d=11111&generationConfigParam=testJs%3AgeneratedContent.js",
				script.getSrcAttribute());
	}

	@Test
	public void testJsBundleContent() throws Exception {

		final List<?> scripts = getJsScriptTags();
		HtmlScript script = (HtmlScript) scripts.get(0);
		JavaScriptPage page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/generator/webjars/debug/resources/jquery.js", page);
		
		script = (HtmlScript) scripts.get(1);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/generator/webjars/debug/resources/bootstrap.js", page);
		
		script = (HtmlScript) scripts.get(2);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/generator/webjars/debug/resources/msg-debug.js", page);
		
		script = (HtmlScript) scripts.get(3);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/generator/webjars/debug/resources/generatedContent.js", page);
	}

	@Test
	public void checkGeneratedCssLinks() {
		// Test generated Css link
		final List<?> styleSheets = getHtmlLinkTags();
		assertEquals(3, styleSheets.size());
		HtmlLink css = (HtmlLink) styleSheets.get(0);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.css?d=11111&generationConfigParam=webjars%3A%2Fbootstrap%2F3.2.0%2Fcss%2Fbootstrap.css",
				css.getHrefAttribute());
		
		css = (HtmlLink) styleSheets.get(1);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.css?d=11111&generationConfigParam=jar%3Afwk%2Fcss%2Ftemp.css",
				css.getHrefAttribute());
		
		css = (HtmlLink) styleSheets.get(2);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/css/one.css?d=11111",css.getHrefAttribute());
		
	}

	@Test
	public void testCssBundleContent() throws Exception {

		final List<?> styleSheets = getHtmlLinkTags();
		HtmlLink css = (HtmlLink) styleSheets.get(0);
		TextPage page = getCssPage(css);
		assertContentEquals("/net/jawr/web/generator/webjars/debug/resources/bootstrap.css", page);
		
		css = (HtmlLink) styleSheets.get(1);
		page = getCssPage(css);
		assertContentEquals("/net/jawr/web/generator/webjars/debug/resources/jar_temp.css", page);
		
		
		css = (HtmlLink) styleSheets.get(2);
		page = getCssPage(css);
		assertContentEquals("/net/jawr/web/generator/webjars/debug/resources/one.css", page);
	
	}

}
