package test.net.jawr.web.resource.bundle.factory.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.factory.PropertiesBundleConstant;
import net.jawr.web.resource.bundle.factory.util.PropertiesConfigHelper;

public class PropertiesConfigHelperTestCase extends TestCase {

	public void testGetJsBundleNames() throws IOException {

		Properties prop = new Properties();
		InputStream is = null;
		try{
			is = PropertiesConfigHelperTestCase.class
				.getResourceAsStream("test1.properties");
			prop.load(is);
		}finally{
			IOUtils.close(is);
		}
		
		PropertiesConfigHelper helper = new PropertiesConfigHelper(prop, "js");
		Set<String> bundleNames = helper.getPropertyBundleNameSet();
		assertEquals(3, bundleNames.size());
		
		Map<String, String> map = helper.getCustomGlobalPostprocessorMap();
		assertTrue(map.size() == 1);
		assertEquals(map.get("sampleGPost"), "net.jawr.resource.postprocessor.TestSampleGlobalPostProcessor");
				
		map = helper.getCustomGlobalPreprocessorMap();
		assertTrue(map.size() == 1);
		assertEquals(map.get("sampleGPre"), "net.jawr.resource.preprocessor.TestSampleGlobalPreProcessor");
		
		map = helper.getCustomPostProcessorMap();
		assertTrue(map.size() == 2);
		assertEquals(map.get("sample"), "net.jawr.resource.postprocessor.TestSamplePostProcessor");
		assertEquals(map.get("sample2"), "net.jawr.resource.postprocessor.TestSamplePostProcessor2");
				
		Set<String> set = helper.getCommonPropertyAsSet(PropertiesBundleConstant.CUSTOM_GENERATORS);
		assertTrue(set.size() == 1);
		assertTrue(set.contains("net.jawr.resource.generator.TestSampleImageGenerator"));
		
	}
	
	public void testGetJsBundleNames2() throws IOException {

		Properties prop = new Properties();
		InputStream is = null;
		try{
			is = PropertiesConfigHelperTestCase.class
				.getResourceAsStream("test2.properties");
			prop.load(is);
		}finally{
			IOUtils.close(is);
		}
		
		PropertiesConfigHelper helper = new PropertiesConfigHelper(prop, "js");
		Set<String> bundleNames = helper.getPropertyBundleNameSet();
		assertEquals(3, bundleNames.size());
		
		Map<String, String> map = helper.getCustomGlobalPostprocessorMap();
		assertEquals(1, map.size());
		assertEquals(map.get("sampleGPost"), "net.jawr.resource.postprocessor.SampleGlobalPostProcessor");
				
		map = helper.getCustomGlobalPreprocessorMap();
		assertEquals(1, map.size());
		assertEquals(map.get("sampleGPre"), "net.jawr.resource.postprocessor.SampleGlobalPreProcessor");
		
		map = helper.getCustomPostProcessorMap();
		assertEquals(2, map.size());
		assertEquals(map.get("sample"), "net.jawr.resource.postprocessor.SamplePostProcessor1");
		assertEquals(map.get("sample2"), "net.jawr.resource.postprocessor.TestSamplePostProcessor2");
				
		Set<String> set = helper.getCommonPropertyAsSet(PropertiesBundleConstant.CUSTOM_GENERATORS);
		assertEquals(1, set.size());
		assertTrue(set.contains("net.jawr.resource.generator.SampleImageGenerator1"));
		
	}

	
	
	public void testGetCssBundleNames() throws IOException {

		Properties prop = new Properties();
		InputStream is = null;
		try{
			is = PropertiesConfigHelperTestCase.class
				.getResourceAsStream("test1.properties");
			prop.load(is);
		}finally{
			IOUtils.close(is);
		}
		
		PropertiesConfigHelper helper = new PropertiesConfigHelper(prop, "css");
		Set<String> bundleNames = helper.getPropertyBundleNameSet();
		assertEquals(2, bundleNames.size());
		
	}

}
