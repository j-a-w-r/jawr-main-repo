package net.jawr.web.test.strictmode;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlImage;

import net.jawr.web.test.JawrTestConfigFiles;
import net.jawr.web.test.utils.Utils;

/**
 * Test case for standard page in debug mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/debug/config/web.xml", jawrConfig = "net/jawr/web/strictmode/config/jawr-debug-no-strict-mode.properties")
public class MainPageDebugNoStrictModeTest extends MainPageDebugStrictModeTest {

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

		// Check access to link with wrong hashcode already referenced in img tag
		WebClient webClient = new WebClient();
		webClient.setThrowExceptionOnFailingStatusCode(false);
		int status = webClient
				.getPage(getServerUrlPrefix() + getUrlPrefix()
						+ "/cb7777777/img/appIcons/application.png")
				.getWebResponse().getStatusCode();

		assertEquals(200, status);
		
		// Check access to resource with wrong hashcode and not referenced by jawr
		status = webClient
				.getPage(getServerUrlPrefix() + getUrlPrefix()
						+ "/cb7777777/img/calendarIcons/clock/clock_add.png")
				.getWebResponse().getStatusCode();

		assertEquals(200, status);
	}
}
