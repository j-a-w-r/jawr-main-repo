/**
 * 
 */
package net.jawr.web.test.locale;


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
 * Test case for page using a specific locale in production mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles( webXml="net/jawr/web/standard/config/web.xml", jawrConfig="net/jawr/web/debug/config/jawr.properties")
public class MainPageLocaleFrDebugTest extends MainPageLocaleFrTest {

	public String getAcceptedLanguage(){
		return "fr";
	}
	
	@Test
	public void testPageLoad() throws Exception {
	    
	    final List<String> expectedAlerts = Collections.singletonList("A little message retrieved from the message bundle : Bonjour $ le monde!");
	    assertEquals(expectedAlerts, collectedAlerts);
	    assertContentEquals("/net/jawr/web/locale/resources/index-jsp-result-fr-debug-expected.txt", page);
	}
	
	@Test
	public void checkGeneratedJsLinks(){
		// Test generated Script link
		final List<?> scripts = getJsScriptTags();
		assertEquals(2, scripts.size());
		HtmlScript script = (HtmlScript) scripts.get(0);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()+"/jawr_generator.js?d=11111&generationConfigParam=messages%3Amessages%40fr",
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
		assertContentEquals("/net/jawr/web/locale/resources/msg-debug-fr.js", page);
		
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
