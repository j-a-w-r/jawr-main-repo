/**
 * 
 */
package net.jawr.web.test.locale;

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
@JawrTestConfigFiles( webXml="net/jawr/web/standard/config/web.xml", jawrConfig="net/jawr/web/locale/config/jawr-filter.properties")
public class MainPageLocaleFrFilterTest extends MainPageTest {

	/* (non-Javadoc)
	 * @see net.jawr.web.test.MainPageTest#getPageUrl()
	 */
	protected String getPageUrl() {
		return getServerUrlPrefix() + getUrlPrefix()+"/index.jsp";
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.test.AbstractPageTest#getAcceptedLanguage()
	 */
	public String getAcceptedLanguage(){
		return "fr";
	}
	
	@Test
	public void testPageLoad() throws Exception {
	    
		final List<String> expectedAlerts = Collections
				.singletonList("A little message retrieved from the message bundle : Bonjour $ le monde!");
		assertEquals(expectedAlerts, collectedAlerts);

	    assertContentEquals("/net/jawr/web/locale/resources/index-jsp-result-fr-filter-expected.txt", page);
	}
	
	@Test
	public void checkGeneratedJsLinks(){
		// Test generated Script link
	    final List<?> scripts = getJsScriptTags();
	    assertEquals(1, scripts.size());
	    final HtmlScript script = (HtmlScript) scripts.get(0);
	    assertEquals(getUrlPrefix()+"/N1694952078.fr/js/bundle/msg.js", script.getSrcAttribute());
	}
	
	@Test
	public void testJsBundleContent() throws Exception {
		
		final List<?> scripts = getJsScriptTags();
		final HtmlScript script = (HtmlScript) scripts.get(0);
		final JavaScriptPage page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/locale/resources/msg-bundle-fr-filter.js", page);
	}
	
}
