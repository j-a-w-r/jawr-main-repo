/**
 * 
 */
package net.jawr.web.test.smartbundling;

import static org.junit.Assert.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import net.jawr.web.config.jmx.JawrConfigManagerMBean;
import net.jawr.web.test.JawrTestConfigFiles;
import net.jawr.web.test.jmx.JawrJmxClient;

/**
 * Test case for standard page in production mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/smartbundling/config/web.xml", jawrConfig = "net/jawr/web/smartbundling/config/jawr-watch.properties")
public class MainPageTestWatcher extends MainPageTest {

	protected static String getTempFolder() {
		return "jawr-integration-smartbundling-test-watch-1";
	}
	
	
	/* (non-Javadoc)
	 * @see net.jawr.web.test.smartbundling.MainPageTest#updateContent()
	 */
	@Override
	protected void updateContent() throws Exception, InterruptedException, FileNotFoundException, IOException {

		updateResources();
		
		// Wait a little bit
		Thread.sleep(300);
		 
		JawrJmxClient jmxClient = new JawrJmxClient();
		JawrConfigManagerMBean cssMBean = jmxClient.getCssMbean();
		
		List<String> bundles = cssMBean.getDirtyBundleNames();
		assertEquals(1, bundles.size());
		assertEquals("component", bundles.get(0));
		
		JawrConfigManagerMBean jsMBean = jmxClient.getJsMbean();
		bundles = jsMBean.getDirtyBundleNames();
		assertEquals(1, bundles.size());
		assertEquals("msg", bundles.get(0));
		
		jmxClient.getApplicationMBean().rebuildDirtyBundles();
	}
	
}
