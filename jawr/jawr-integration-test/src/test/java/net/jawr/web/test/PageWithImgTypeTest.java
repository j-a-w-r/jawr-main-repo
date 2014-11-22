/**
 * 
 */
package net.jawr.web.test;

/**
 * Test case for a page defined with a binary servlet using the img type parameter.
 * This class is used to ensure that Jawr still works with the img init-parameter
 * 
 * @author ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/standard/config/web-with-img-type-init-param-servlet.xml", jawrConfig = "net/jawr/web/standard/config/jawr.properties")
public class PageWithImgTypeTest extends MainPageTest {

	/**
	 * Returns the page URL to test
	 * @return the page URL to test
	 */
	protected String getPageUrl() {
		return getServerUrlPrefix() + getUrlPrefix()+"/index.jsp";
	}	
}
