/**
 * 
 */
package test.net.jawr.web.resource.bundle.locale;

import java.util.List;

import org.junit.Assert;
import junit.framework.TestCase;
import net.jawr.web.resource.bundle.locale.LocaleUtils;

/**
 * Test case class for Local utils
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class LocalUtilsTestCase extends TestCase {

	public void testGetLocaleAvailablePrefixes(){
		
		List<String> result = LocaleUtils.getAvailableLocaleSuffixesForBundle("bundleLocale.messages");
		Assert.assertEquals(3, result.size());
		Assert.assertTrue(result.contains(""));
		Assert.assertTrue(result.contains("es"));
		Assert.assertTrue(result.contains("fr"));
	}
	
	public void testGetLocaleAvailablePrefixesWithNamespace(){
		
		List<String> result = LocaleUtils.getAvailableLocaleSuffixesForBundle("bundleLocale.messages(mynamespace)");
		Assert.assertEquals(3, result.size());
		Assert.assertTrue(result.contains(""));
		Assert.assertTrue(result.contains("es"));
		Assert.assertTrue(result.contains("fr"));
	}
	
	public void testGetLocaleAvailablePrefixesWithFilter(){
		
		List<String> result = LocaleUtils.getAvailableLocaleSuffixesForBundle("bundleLocale.messages[ui.msg]");
		Assert.assertEquals(3, result.size());
		Assert.assertTrue(result.contains(""));
		Assert.assertTrue(result.contains("es"));
		Assert.assertTrue(result.contains("fr"));
	}
	
	public void testGetLocaleAvailablePrefixesWithFilterAndNamespace(){
		
		List<String> result = LocaleUtils.getAvailableLocaleSuffixesForBundle("bundleLocale.messages(mynamespace)[ui.msg]");
		Assert.assertEquals(3, result.size());
		Assert.assertTrue(result.contains(""));
		Assert.assertTrue(result.contains("es"));
		Assert.assertTrue(result.contains("fr"));
	}
	
	// TODO test for Grails with servlet context
}
