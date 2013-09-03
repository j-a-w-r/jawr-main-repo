/**
 * 
 */
package net.jawr.web.test.locale;

import static net.jawr.web.test.JawrIntegrationServer.CONTEXT_PATH;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import net.jawr.web.test.JawrTestConfigFiles;
import net.jawr.web.test.MainPageTest;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.JavaScriptPage;
import com.gargoylesoftware.htmlunit.html.HtmlScript;


/**
 * Test case for page using a specific locale in production mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles( webXml="net/jawr/web/standard/config/web.xml", jawrConfig="net/jawr/web/standard/config/jawr.properties")
public class MainPageLocaleFrTest extends MainPageTest {

	public String getAcceptedLanguage(){
		return "fr";
	}
	
	@Test
	public void testPageLoad() throws Exception {
	    
	    final List<String> expectedAlerts = Collections.singletonList("A little message retrieved from the message bundle : Bonjour $ le monde!");
	    assertEquals(expectedAlerts, collectedAlerts);
	    assertContentEquals("/net/jawr/web/locale/resources/index-jsp-result-fr-expected.txt", page);
	}
	
	@Test
	public void checkGeneratedJsLinks(){
		// Test generated Script link
	    final List<?> scripts = getJsScriptTags();
	    assertEquals(1, scripts.size());
	    final HtmlScript script = (HtmlScript) scripts.get(0);
	    assertEquals(CONTEXT_PATH+"/N1694952078.fr/js/bundle/msg.js", script.getSrcAttribute());
	}
	
	@Test
	public void testJsBundleContent() throws Exception {
		
		final List<?> scripts = getJsScriptTags();
		final HtmlScript script = (HtmlScript) scripts.get(0);
		final JavaScriptPage page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/locale/resources/msg-bundle-fr.js", page);
	}
	
}
