/**
 * 
 */
package net.jawr.web.test.strictmode;

import net.jawr.web.test.JawrTestConfigFiles;

/**
 * Test case for standard page in production mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/strictmode/config/web-with-servlet-mapping.xml", jawrConfig = "net/jawr/web/strictmode/config/jawr-debug-no-strict-mode.properties")
public class MainPageDebugNoStrictModeWithServletMappingTest extends
		MainPageDebugStrictModeWithServletMappingTest {

}
