/**
 * 
 */
package net.jawr.web.test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.jawr.web.test.utils.Utils;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
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
@JawrTestConfigFiles(webXml = "net/jawr/web/standard/config/web.xml", jawrConfig = "net/jawr/web/standard/config/jawr.properties")
@Ignore
public class MainPageTestWithEmptyContextPath extends  MainPageTest {

	@Before
	public void setup() throws Exception {

		JawrIntegrationServer.getInstance().getJettyWebAppContext().setContextPath("");
		super.setup();
	}
	
	@After
	public void teardown(){
		JawrIntegrationServer.getInstance().getJettyWebAppContext().setContextPath("/jawr-integration-test");
		super.teardown();
	}
	
	@Test
	public void testPageLoad() throws Exception {

		final List<String> expectedAlerts = Collections
				.singletonList("A little message retrieved from the message bundle : Hello $ world!");
		assertEquals(expectedAlerts, collectedAlerts);
		
		assertContentEquals("/net/jawr/web/standard/resources/index-jsp-result-expected-with-empty-context-path.txt", page);
	}
	
}
