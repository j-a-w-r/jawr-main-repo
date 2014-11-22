package net.jawr.web.test.strictmode;

import net.jawr.web.test.JawrTestConfigFiles;

/**
 * Test case for standard page in debug mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/debug/config/web.xml", jawrConfig = "net/jawr/web/strictmode/config/jawr-debug-no-strict-mode.properties")
public class MainPageDebugNoStrictModeTest extends MainPageDebugStrictModeTest {

}
