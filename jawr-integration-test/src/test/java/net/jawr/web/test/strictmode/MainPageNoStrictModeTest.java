/**
 * 
 */
package net.jawr.web.test.strictmode;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import net.jawr.web.test.JawrTestConfigFiles;
import net.jawr.web.test.utils.Utils;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlScript;

/**
 * Test case for standard page in production mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/standard/config/web.xml", jawrConfig = "net/jawr/web/strictmode/config/jawr-no-strict-mode.properties")
public class MainPageNoStrictModeTest extends MainPageStrictModeTest {

	@Test
	public void checkGeneratedJsLinks() throws FailingHttpStatusCodeException,
			MalformedURLException, IOException {

		// Test generated Script link
		final List<HtmlScript> scripts = getJsScriptTags();
		assertEquals(1, scripts.size());
		final HtmlScript script = scripts.get(0);
		assertEquals(getUrlPrefix() + "/690372103.en_US/js/bundle/msg.js",
				script.getSrcAttribute());

		// Check access to link with wrong hashcode
		WebClient webClient = new WebClient();
		webClient.setThrowExceptionOnFailingStatusCode(false);
		int status = webClient
				.getPage(getServerUrlPrefix() + getUrlPrefix() + "/777777.en_US/js/bundle/msg.js")
				.getWebResponse().getStatusCode();

		assertEquals(200, status);

	}

	@Test
	public void checkGeneratedCssLinks() throws FailingHttpStatusCodeException,
			MalformedURLException, IOException {
		// Test generated Css link
		final List<HtmlLink> styleSheets = getHtmlLinkTags();
		assertEquals(1, styleSheets.size());
		final HtmlLink css = styleSheets.get(0);
		assertEquals(getUrlPrefix() + "/N33754198/fwk/core/component.css",
				css.getHrefAttribute());

		// Check access to link with wrong hashcode
		WebClient webClient = new WebClient();
		webClient.setThrowExceptionOnFailingStatusCode(false);
		int status = webClient
				.getPage(getServerUrlPrefix() + getUrlPrefix() + "/777777/fwk/core/component.css")
				.getWebResponse().getStatusCode();

		assertEquals(200, status);
	}

	@Test
	public void checkGeneratedHtmlImageLinks() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		// Test generated HTML image link
		final List<?> images = getHtmlImageTags();
		assertEquals(1, images.size());
		final HtmlImage img = (HtmlImage) images.get(0);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix()
						+ "/cbfc517da02d6a64a68e5fea9a5de472f1/img/appIcons/application.png",
				img.getSrcAttribute());

		// Check access to link with wrong hashcode
		WebClient webClient = new WebClient();
		webClient.setThrowExceptionOnFailingStatusCode(false);
		int status = webClient
				.getPage(getServerUrlPrefix() + getUrlPrefix()
						+ "/cb7777777/img/appIcons/application.png")
				.getWebResponse().getStatusCode();

		assertEquals(200, status);
	}

}
