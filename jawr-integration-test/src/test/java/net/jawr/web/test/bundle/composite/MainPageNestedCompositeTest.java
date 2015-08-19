/**
 * 
 */
package net.jawr.web.test.bundle.composite;

import static org.junit.Assert.assertEquals;

import java.util.List;

import net.jawr.web.test.JawrTestConfigFiles;
import net.jawr.web.test.MainPageTest;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.JavaScriptPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlScript;

/**
 * Test case for nested composite bundle in production mode.
 * s
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/standard/config/web.xml", jawrConfig = "net/jawr/web/bundle/composite/standard/config/jawr.properties")
public class MainPageNestedCompositeTest extends MainPageTest {

	
	/* (non-Javadoc)
	 * @see net.jawr.web.test.AbstractPageTest#createWebClient()
	 */
	@Override
	protected WebClient createWebClient() {
		WebClient webClient = super.createWebClient();
		webClient.setJavaScriptEnabled(false);
		return webClient;
	}

	/**
	 * Returns the page URL to test
	 * @return the page URL to test
	 */
	protected String getPageUrl() {
		return getServerUrlPrefix() + getUrlPrefix()+"/bundle/composite/index.jsp";
	}

	@Test
	public void testPageLoad() throws Exception {

		assertContentEquals("/net/jawr/web/bundle/composite/standard/resources/index-jsp-result-expected.txt", page);
	}

	@Test
	public void checkGeneratedJsLinks() {
		// Test generated Script link
		final List<HtmlScript> scripts = getJsScriptTags();
		assertEquals(2, scripts.size());
		HtmlScript script = scripts.get(0);
		assertEquals(
				getUrlPrefix()+"/N1877851367/js/bundle/lib.js",
				script.getSrcAttribute());
		script = scripts.get(1);
		assertEquals(
				getUrlPrefix()+"/690372103.en_US/js/bundle/msg.js",
				script.getSrcAttribute());
	}

	@Test
	public void testJsBundleContent() throws Exception {

		final List<HtmlScript> scripts = getJsScriptTags();
		HtmlScript script = scripts.get(0);
		JavaScriptPage page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/bundle/composite/standard/resources/lib.js", page);
		
		script = scripts.get(1);
		page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/standard/resources/msg-bundle.js", page);
	}
	
}
