/**
 * 
 */
package net.jawr.web.test.locale;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.JavaScriptPage;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlImageInput;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlScript;

import static org.junit.Assert.assertEquals;

import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.test.AbstractPageTest;
import net.jawr.web.test.JawrIntegrationServer;
import net.jawr.web.test.JawrTestConfigFiles;
import net.jawr.web.test.utils.Utils;

/**
 * Test case for page using a specific locale in production mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/standard/config/web.xml", jawrConfig = "net/jawr/web/locale/config/jawr-watch.properties")
public class MainPageLocaleFrResourceBundleWatcherTest extends AbstractPageTest {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.test.AbstractPageTest#setup()
	 */
	@Override
	public void setup() throws Exception {

		resetResourcesContent();

		super.setup();
	}

	/**
	 * reset resources content
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected void resetResourcesContent() {
		try {
			// init linked resource
			InputStream is = getClass()
					.getResourceAsStream("/net/jawr/web/locale/resources/messages.properties.backup");
			OutputStream out;
			out = new FileOutputStream(
					JawrIntegrationServer.getInstance().getWebAppRootDir() + "/WEB-INF/classes/messages.properties");
			IOUtils.copy(is, out, true);

			is = getClass().getResourceAsStream("/net/jawr/web/locale/resources/messages_fr.properties.backup");
			out = new FileOutputStream(
					JawrIntegrationServer.getInstance().getWebAppRootDir() + "/WEB-INF/classes/messages_fr.properties");
			IOUtils.copy(is, out, true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.test.AbstractPageTest#teardown()
	 */
	@Override
	public void teardown() {

		resetResourcesContent();

		super.teardown();
	}

	/**
	 * Returns the page URL to test
	 * 
	 * @return the page URL to test
	 */
	protected String getPageUrl() {
		return getServerUrlPrefix() + getUrlPrefix() + "/index.jsp";
	}

	public String getAcceptedLanguage() {
		return "fr";
	}

	protected static String getTempFolder() {
		return "jawr-integration-locale-update-test";
	}

	@Test
	public void testPageLoad() throws Exception {

		final List<String> expectedAlerts = Collections
				.singletonList("A little message retrieved from the message bundle : Bonjour $ le monde!");
		assertEquals(expectedAlerts, collectedAlerts);
		assertContentEquals("/net/jawr/web/locale/resources/index-jsp-result-fr-expected.txt", page);

		checkGeneratedJsLinks();
		checkJsBundleContent();
		checkGeneratedCssLinks();
		checkCssBundleContent();
		checkGeneratedHtmlImageInputLinks();
		checkGeneratedHtmlImageLinks();

		updateResources();

		// Wait a little bit
		Thread.sleep(3000);

		page = webClient.getPage(getPageUrl());
		assertContentEquals("/net/jawr/web/locale/resources/index-jsp-updated-result-fr-expected.txt", page);

		checkUpdatedGeneratedJsLinks();
		checkUpdatedJsBundleContent();
		checkGeneratedCssLinks();
		checkCssBundleContent();
		checkGeneratedHtmlImageInputLinks();
		checkGeneratedHtmlImageLinks();
	}

	/**
	 * Update linked resources
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	protected void updateResources() throws FileNotFoundException, IOException {
		// Update linked resource
		InputStream is = getClass().getResourceAsStream("/net/jawr/web/locale/resources/messages_updated.properties");
		OutputStream out = new FileOutputStream(
				JawrIntegrationServer.getInstance().getWebAppRootDir() + "/WEB-INF/classes/messages.properties");
		IOUtils.copy(is, out, true);

		is = getClass().getResourceAsStream("/net/jawr/web/locale/resources/messages_updated_fr.properties");
		out = new FileOutputStream(
				JawrIntegrationServer.getInstance().getWebAppRootDir() + "/WEB-INF/classes/messages_fr.properties");
		IOUtils.copy(is, out, true);
	}

	public void checkGeneratedJsLinks() {
		// Test generated Script link
		final List<?> scripts = getJsScriptTags();
		assertEquals(1, scripts.size());
		final HtmlScript script = (HtmlScript) scripts.get(0);
		assertEquals(getUrlPrefix() + "/N1694952078.fr/js/bundle/msg.js", script.getSrcAttribute());
	}

	public void checkJsBundleContent() throws Exception {

		final List<?> scripts = getJsScriptTags();
		final HtmlScript script = (HtmlScript) scripts.get(0);
		final JavaScriptPage page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/locale/resources/msg-bundle-fr.js", page);
	}

	public void checkUpdatedGeneratedJsLinks() {
		// Test generated Script link
		final List<?> scripts = getJsScriptTags();
		assertEquals(1, scripts.size());
		final HtmlScript script = (HtmlScript) scripts.get(0);
		assertEquals(getUrlPrefix() + "/N404005114.fr/js/bundle/msg.js", script.getSrcAttribute());
	}

	public void checkUpdatedJsBundleContent() throws Exception {

		final List<?> scripts = getJsScriptTags();
		final HtmlScript script = (HtmlScript) scripts.get(0);
		final JavaScriptPage page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/locale/resources/msg-bundle-fr-updated.js", page);
	}
	
	public void checkGeneratedCssLinks() {
		// Test generated Css link
		final List<HtmlLink> styleSheets = getHtmlLinkTags();
		assertEquals(1, styleSheets.size());
		final HtmlLink css = styleSheets.get(0);
		assertEquals(getUrlPrefix() + "/497434506/fwk/core/component.css", css.getHrefAttribute());

	}

	public void checkCssBundleContent() throws Exception {

		final List<HtmlLink> styleSheets = getHtmlLinkTags();
		final HtmlLink css = styleSheets.get(0);
		final TextPage page = getCssPage(css);
		assertContentEquals("/net/jawr/web/standard/resources/component-expected.css", page);
	}

	public void checkGeneratedHtmlImageLinks() {
		// Test generated HTML image link
		final List<?> images = getHtmlImageTags();
		assertEquals(1, images.size());
		final HtmlImage img = (HtmlImage) images.get(0);
		Utils.assertGeneratedLinkEquals(
				getUrlPrefix() + "/cbfc517da02d6a64a68e5fea9a5de472f1/img/appIcons/application.png",
				img.getSrcAttribute());

	}

	public void checkGeneratedHtmlImageInputLinks() {
		// Test generated HTML image link
		final List<HtmlImageInput> images = getHtmlImageInputTags();
		assertEquals(1, images.size());
		final HtmlImageInput img = images.get(0);
		Utils.assertGeneratedLinkEquals(getUrlPrefix() + "/cb30a18063ef42b090194a7e936086960f/img/cog.png",
				img.getSrcAttribute());

	}

}
