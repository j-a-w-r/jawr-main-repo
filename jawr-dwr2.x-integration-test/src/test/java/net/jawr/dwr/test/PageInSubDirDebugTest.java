/**
 * 
 */
package net.jawr.dwr.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.List;

import net.jawr.web.test.utils.Utils;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlImageInput;

/**
 * Test case for a page defined in a subdirectory in debug mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrDWRTestConfigFiles(webXml = "net/jawr/web/standard/config/web.xml", jawrConfig = "net/jawr/web/debug/config/jawr.properties", dwrConfig = "net/jawr/web/standard/config/dwr.xml")
public class PageInSubDirDebugTest extends MainPageDebugTest {

	/**
	 * Returns the page URL to test
	 * 
	 * @return the page URL to test
	 */
	protected String getPageUrl() {
		return getServerUrlPrefix() + getUrlPrefix() + "/subdir/index.jsp";
	}

	@Test
	public void testPageLoad() throws Exception {

		final List<String> expectedAlerts = Collections
				.singletonList("A little message retrieved from the message bundle : Hello $ world!");
		assertEquals(expectedAlerts, collectedAlerts);

		assertContentEquals(
				"/net/jawr/web/standard/resources/subdir/index-jsp-result-debug-expected.txt",
				page);

		// Checks that before the call to the server the element is empty
		HtmlElement spanResult = page.getHtmlElementById("demoReply");
		assertNull(spanResult.getFirstChild());

		HtmlElement button = page.getHtmlElementById("sendButton");
		page = button.click();
		webClient.waitForBackgroundJavaScript(5 * 1000);

		// Checks that after the call to the server the element is correctly
		// filled
		assertEquals("Hello, Joe", spanResult.getFirstChild().getNodeValue());
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
		Utils.assertGeneratedLinkEquals(getUrlPrefix()+"/cb30a18063ef42b090194a7e936086960f/subdir/img/cog.png", 
				img.getSrcAttribute());

	}
	
}
