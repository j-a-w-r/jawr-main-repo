/**
 * 
 */
package net.jawr.web.test;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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
