/**
 * 
 */
package test.net.jawr.web.resource.bundle.factory.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import net.jawr.web.resource.bundle.factory.util.JsonPropertiesSource;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author ibrahim chaehoi
 */
public class JsonPropertiesSourceTest {

	private JsonPropertiesSource jsonPropSrc;
	
	@Test
	public void testLoad() throws FileNotFoundException, IOException{
		
		jsonPropSrc = new JsonPropertiesSource();
		jsonPropSrc.setConfigLocation("bundle/factory/util/json/jawr.json");
		Properties props = jsonPropSrc.getConfigProperties();
		Properties expectedProps = new Properties();
		expectedProps.load(getClass().getResourceAsStream("/bundle/factory/util/json/expected_jawr.properties"));
		Assert.assertEquals(expectedProps, props);
	}
}
