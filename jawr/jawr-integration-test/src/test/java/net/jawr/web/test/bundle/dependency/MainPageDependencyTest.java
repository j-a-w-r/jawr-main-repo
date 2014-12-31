/**
 * 
 */
package net.jawr.web.test.bundle.dependency;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import net.jawr.web.test.AbstractPageTest;
import net.jawr.web.test.JawrTestConfigFiles;
import net.jawr.web.test.utils.Utils;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.JavaScriptPage;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlImageInput;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlScript;

/**
 * Test case for standard page in production mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/standard/config/web.xml", jawrConfig = "net/jawr/web/bundle/dependency/standard/config/jawr.properties")
public class MainPageDependencyTest extends AbstractPageTest {

	/**
	 * Returns the page URL to test
	 * @return the page URL to test
	 */
	protected String getPageUrl() {
		return getServerUrlPrefix() + getUrlPrefix()+"/dependency/dependencyTest.jsp";
	}

	@Test
	public void testPageLoad() throws Exception {

		final List<String> expectedAlerts = Collections
				.singletonList("A little message retrieved from the message bundle : Hello $ world!");
		assertEquals(expectedAlerts, collectedAlerts);
		
		assertContentEquals("/net/jawr/web/bundle/dependency/standard/resources/index-jsp-result-expected.txt", page);
	}

	@Test
	public void checkGeneratedJsLinks() {
		// Test generated Script link
		final List<HtmlScript> scripts = getJsScriptTags();
		assertEquals(1, scripts.size());
		final HtmlScript script = scripts.get(0);
		assertEquals(
				getUrlPrefix()+"/690372103.en_US/js/bundle/msg.js",
				script.getSrcAttribute());
	}

	@Test
	public void testJsBundleContent() throws Exception {

		final List<HtmlScript> scripts = getJsScriptTags();
		final HtmlScript script = scripts.get(0);
		final JavaScriptPage page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/standard/resources/msg-bundle.js", page);
	}

	@Test
	public void checkGeneratedCssLinks() {
		// Test generated Css link
		final List<HtmlLink> styleSheets = getHtmlLinkTags();
		assertEquals(5, styleSheets.size());
		HtmlLink css = styleSheets.get(0);
		assertEquals(
				getUrlPrefix()+"/N1545881524/bundles/globalStyleBundle.css",
				css.getHrefAttribute());
		css = styleSheets.get(1);
		assertEquals(
				getUrlPrefix()+"/N214611420/fwk/core/component3.css",
				css.getHrefAttribute());
		css = styleSheets.get(2);
		assertEquals(
				getUrlPrefix()+"/N214611420/fwk/core/component4.css",
				css.getHrefAttribute());
		css = styleSheets.get(3);
		assertEquals(
				getUrlPrefix()+"/1570160091/fwk/core/component2.css",
				css.getHrefAttribute());
		css = styleSheets.get(4);
		assertEquals(
				getUrlPrefix()+"/N1041692057/fwk/core/component.css",
				css.getHrefAttribute());

	}

	@Test
	public void testCssBundleContent() throws Exception {

		final List<HtmlLink> styleSheets = getHtmlLinkTags();
		HtmlLink css = styleSheets.get(0);
		TextPage page = getCssPage(css);
		assertContentEquals("/net/jawr/web/bundle/dependency/standard/resources/globalStyleBundle.css", page);
		
		page = getCssPage(styleSheets.get(1));
		assertContentEquals("/net/jawr/web/bundle/dependency/standard/resources/component3.css", page);
		
		page = getCssPage(styleSheets.get(2));
		assertContentEquals("/net/jawr/web/bundle/dependency/standard/resources/component4.css", page);
		
		page = getCssPage(styleSheets.get(3));
		assertContentEquals("/net/jawr/web/bundle/dependency/standard/resources/component2.css", page);
		
		page = getCssPage(styleSheets.get(4));
		assertContentEquals("/net/jawr/web/bundle/dependency/standard/resources/component.css", page);
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
