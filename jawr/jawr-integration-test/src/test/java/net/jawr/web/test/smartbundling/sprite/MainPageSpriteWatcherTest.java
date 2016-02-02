/**
 * 
 */
package net.jawr.web.test.smartbundling.sprite;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.jawr.web.test.JawrTestConfigFiles;

/**
 * Test case for standard page in production mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/smartbundling/config/web.xml", jawrConfig = "net/jawr/web/smartbundling/config/jawr-sprite-watch.properties")
public class MainPageSpriteWatcherTest extends MainPageSpriteTest {

	protected static String getTempFolder() {
		return "jawr-integration-smartbundling-test-sprite-watcher-1";
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.test.smartbundling.MainPageTest#updateContent()
	 */
	@Override
	protected void updateContent() throws Exception, InterruptedException, FileNotFoundException, IOException {

		updateResources();

		// Wait a little bit
		Thread.sleep(3000);
	}
	
}
