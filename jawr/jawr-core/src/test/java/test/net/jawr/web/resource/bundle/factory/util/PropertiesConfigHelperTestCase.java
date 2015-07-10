package test.net.jawr.web.resource.bundle.factory.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.factory.util.PropertiesConfigHelper;

public class PropertiesConfigHelperTestCase extends TestCase {

	public void testGetJsBundleNames() throws IOException {

		Properties prop = new Properties();
		InputStream is = PropertiesConfigHelperTestCase.class
				.getResourceAsStream("test1.properties");
		prop.load(is);

		PropertiesConfigHelper helper = new PropertiesConfigHelper(prop, "js");
		Set<String> bundleNames = helper.getPropertyBundleNameSet();
		assertEquals(3, bundleNames.size());
		IOUtils.close(is);
	}

	public void testGetCssBundleNames() throws IOException {

		Properties prop = new Properties();
		InputStream is = PropertiesConfigHelperTestCase.class
				.getResourceAsStream("test1.properties");
		prop.load(is);

		PropertiesConfigHelper helper = new PropertiesConfigHelper(prop, "css");
		Set<String> bundleNames = helper.getPropertyBundleNameSet();
		assertEquals(2, bundleNames.size());
		IOUtils.close(is);
	}

}
