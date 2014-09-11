/**
 * 
 */
package net.jawr.web.test;


/**
 * Test case for a page defined in a subdirectory in debug mode.
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/standard/config/web-with-img-type-init-param-servlet.xml", jawrConfig = "net/jawr/web/debug/config/jawr.properties")
public class PageWithImgTypeDebugTest extends MainPageDebugTest {

	/**
	 * Returns the page URL to test
	 * @return the page URL to test
	 */
	protected String getPageUrl() {
		return getServerUrlPrefix() + getUrlPrefix()+"/index.jsp";
	}

}
