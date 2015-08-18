/**
 * 
 */
package test.net.jawr.web.resource.bundle;

import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.DebugInclusion;
import net.jawr.web.resource.bundle.InclusionPattern;
import net.jawr.web.resource.bundle.JoinableResourceBundleImpl;
import net.jawr.web.resource.bundle.JoinableResourceBundlePropertySerializer;
import net.jawr.web.resource.bundle.factory.PropertiesBundleConstant;
import net.jawr.web.resource.bundle.factory.util.PropertiesConfigHelper;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Test case for JoinableResourceBundle serializer
 * 
 * @author Ibrahim Chaehoi
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class JoinableResourceBundleSerializerTestCase {

	@Mock
	private ResourceReaderHandler rsHandler;

	@Mock
	private AbstractChainedResourceBundlePostProcessor bundleProcessor;

	@Mock
	private AbstractChainedResourceBundlePostProcessor fileProcessor;

	@Before
	public void setUp() {
		when(rsHandler.getResourceNames(Matchers.anyString())).thenReturn(
				new HashSet<String>(Arrays.asList("script1.js", "script2.js")));
		when(bundleProcessor.getId()).thenReturn(
				"myBundlePostProcessor1,myBundlePostProcessor2");
		when(fileProcessor.getId()).thenReturn(
				"myFilePostProcessor1,myFilePostProcessor2");
	}

	@Test
	public void testGlobalBundleSerialization() {

		String bundleName = "myBundle";
		String resourceType = "js";
		List<String> mappings = Arrays.asList("/bundle/content/**",
				"/bundle/myScript.js");

		GeneratorRegistry generatorRegistry = new GeneratorRegistry();
		InclusionPattern inclusionPattern = new InclusionPattern(true, 0);
		JoinableResourceBundleImpl bundle = new JoinableResourceBundleImpl(
				"/bundle/myBundle.js", bundleName, null, "js",
				inclusionPattern, rsHandler, generatorRegistry);
		bundle.setMappings(mappings);
		bundle.setBundleDataHashCode(null, "123456");

		Properties props = new Properties();
		JoinableResourceBundlePropertySerializer.serializeInProperties(bundle,
				resourceType, props);

		PropertiesConfigHelper helper = new PropertiesConfigHelper(props,
				resourceType);
		assertEquals("/bundle/myBundle.js", helper.getCustomBundleProperty(
				bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ID));

		Set<String> expectedMappings = new HashSet<String>(Arrays.asList(
				"/bundle/content/script1.js", "/bundle/content/script2.js",
				"/bundle/myScript.js"));
		assertEquals(expectedMappings, helper.getCustomBundlePropertyAsSet(
				bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_MAPPINGS));

		assertEquals("true", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_GLOBAL_FLAG));
		assertEquals("false", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGNEVER,
				"false"));
		assertEquals("false", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGONLY,
				"false"));
		assertEquals("123456", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE));

	}

	@Test
	public void testStdBundleSerialization() {

		String bundleName = "myBundle";
		String resourceType = "js";
		List<String> mappings = Arrays.asList("/bundle/content/**",
				"/bundle/myScript.js");

		GeneratorRegistry generatorRegistry = new GeneratorRegistry();

		InclusionPattern inclusionPattern = new InclusionPattern(false, 3,
				DebugInclusion.ALWAYS);
		JoinableResourceBundleImpl bundle = new JoinableResourceBundleImpl(
				"/bundle/myBundle.js", bundleName, null, "js",
				inclusionPattern, rsHandler, generatorRegistry);
		bundle.setMappings(mappings);
		bundle.setAlternateProductionURL("http://hostname/scripts/myBundle.js");
		bundle.setExplorerConditionalExpression("if lt IE 6");

		Map<String, VariantSet> variants = new HashMap<String, VariantSet>();
		variants.put(
				JawrConstant.LOCALE_VARIANT_TYPE,
				new VariantSet(JawrConstant.LOCALE_VARIANT_TYPE, "fr", Arrays
						.asList("fr", "en_US")));
		bundle.setVariants(variants);
		bundle.setBundleDataHashCode(null, "N123456");
		bundle.setBundleDataHashCode("fr", "123456");
		bundle.setBundleDataHashCode("en_US", "789");

		bundle.setBundlePostProcessor(bundleProcessor);
		bundle.setUnitaryPostProcessor(fileProcessor);

		Properties props = new Properties();
		JoinableResourceBundlePropertySerializer.serializeInProperties(bundle,
				resourceType, props);

		PropertiesConfigHelper helper = new PropertiesConfigHelper(props,
				resourceType);
		assertEquals("/bundle/myBundle.js", helper.getCustomBundleProperty(
				bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ID));

		Set<String> expectedMappings = new HashSet<String>(Arrays.asList(
				"/bundle/content/script1.js", "/bundle/content/script2.js",
				"/bundle/myScript.js"));
		assertEquals(expectedMappings, helper.getCustomBundlePropertyAsSet(
				bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_MAPPINGS));

		assertEquals("false", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_GLOBAL_FLAG,
				"false"));
		assertEquals("3", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ORDER));
		assertEquals("false", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGNEVER,
				"false"));
		assertEquals("false", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGONLY,
				"false"));
		assertEquals(
				"http://hostname/scripts/myBundle.js",
				helper.getCustomBundleProperty(
						bundleName,
						PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PRODUCTION_ALT_URL));
		assertEquals(
				"if lt IE 6",
				helper.getCustomBundleProperty(
						bundleName,
						PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_IE_CONDITIONAL_EXPRESSION));
		assertEquals(
				"myBundlePostProcessor1,myBundlePostProcessor2",
				helper.getCustomBundleProperty(
						bundleName,
						PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_POSTPROCESSOR));
		assertEquals(
				"myFilePostProcessor1,myFilePostProcessor2",
				helper.getCustomBundleProperty(
						bundleName,
						PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_FILE_POSTPROCESSOR));
		assertEquals("N123456", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE));

		Map<String, VariantSet> expectedVariants = new HashMap<String, VariantSet>();
		expectedVariants.put(
				JawrConstant.LOCALE_VARIANT_TYPE,
				new VariantSet(JawrConstant.LOCALE_VARIANT_TYPE, "fr", Arrays
						.asList("fr", "en_US")));
		Map<String, VariantSet> variantSets = helper
				.getCustomBundleVariantSets(bundleName);
		assertEquals(expectedVariants, variantSets);

		assertEquals("N123456", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE));
		assertEquals("123456", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE_VARIANT
						+ "fr"));
		assertEquals("789", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE_VARIANT
						+ "en_US"));

	}

	@Test
	public void testStdBundleSerializationDebugOnly() {

		String bundleName = "myBundle";
		String resourceType = "js";
		List<String> mappings = Arrays.asList("/bundle/content/**",
				"/bundle/myScript.js");

		GeneratorRegistry generatorRegistry = new GeneratorRegistry();

		InclusionPattern inclusionPattern = new InclusionPattern(false, 3,
				DebugInclusion.ONLY);
		JoinableResourceBundleImpl bundle = new JoinableResourceBundleImpl(
				"/bundle/myBundle.js", bundleName, null, "js",
				inclusionPattern, rsHandler, generatorRegistry);
		bundle.setMappings(mappings);
		bundle.setAlternateProductionURL("http://hostname/scripts/myBundle.js");
		bundle.setExplorerConditionalExpression("if lt IE 6");

		Map<String, VariantSet> variants = new HashMap<String, VariantSet>();
		variants.put(
				JawrConstant.LOCALE_VARIANT_TYPE,
				new VariantSet(JawrConstant.LOCALE_VARIANT_TYPE, "fr", Arrays
						.asList("fr", "en_US")));
		bundle.setVariants(variants);
		bundle.setBundleDataHashCode(null, "N123456");
		bundle.setBundleDataHashCode("fr", "123456");
		bundle.setBundleDataHashCode("en_US", "789");

		bundle.setBundlePostProcessor(bundleProcessor);
		bundle.setUnitaryPostProcessor(fileProcessor);

		Properties props = new Properties();
		JoinableResourceBundlePropertySerializer.serializeInProperties(bundle,
				resourceType, props);

		PropertiesConfigHelper helper = new PropertiesConfigHelper(props,
				resourceType);
		assertEquals("/bundle/myBundle.js", helper.getCustomBundleProperty(
				bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ID));

		Set<String> expectedMappings = new HashSet<String>(Arrays.asList(
				"/bundle/content/script1.js", "/bundle/content/script2.js",
				"/bundle/myScript.js"));
		assertEquals(expectedMappings, helper.getCustomBundlePropertyAsSet(
				bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_MAPPINGS));

		assertEquals("false", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_GLOBAL_FLAG,
				"false"));
		assertEquals("3", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ORDER));
		assertEquals("false", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGNEVER,
				"false"));
		assertEquals("true", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGONLY,
				"false"));
		assertEquals(
				"http://hostname/scripts/myBundle.js",
				helper.getCustomBundleProperty(
						bundleName,
						PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PRODUCTION_ALT_URL));
		assertEquals(
				"if lt IE 6",
				helper.getCustomBundleProperty(
						bundleName,
						PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_IE_CONDITIONAL_EXPRESSION));
		assertEquals(
				"myBundlePostProcessor1,myBundlePostProcessor2",
				helper.getCustomBundleProperty(
						bundleName,
						PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_POSTPROCESSOR));
		assertEquals(
				"myFilePostProcessor1,myFilePostProcessor2",
				helper.getCustomBundleProperty(
						bundleName,
						PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_FILE_POSTPROCESSOR));
		assertEquals("N123456", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE));

		Map<String, VariantSet> expectedVariants = new HashMap<String, VariantSet>();
		expectedVariants.put(
				JawrConstant.LOCALE_VARIANT_TYPE,
				new VariantSet(JawrConstant.LOCALE_VARIANT_TYPE, "fr", Arrays
						.asList("fr", "en_US")));
		Map<String, VariantSet> variantSets = helper
				.getCustomBundleVariantSets(bundleName);
		assertEquals(expectedVariants, variantSets);

		assertEquals("N123456", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE));
		assertEquals("123456", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE_VARIANT
						+ "fr"));
		assertEquals("789", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE_VARIANT
						+ "en_US"));

	}

	@Test
	public void testStdBundleSerializationDebugNever() {

		String bundleName = "myBundle";
		String resourceType = "js";
		List<String> mappings = Arrays.asList("/bundle/content/**",
				"/bundle/myScript.js");

		GeneratorRegistry generatorRegistry = new GeneratorRegistry();

		InclusionPattern inclusionPattern = new InclusionPattern(false, 3,
				DebugInclusion.NEVER);
		JoinableResourceBundleImpl bundle = new JoinableResourceBundleImpl(
				"/bundle/myBundle.js", bundleName, null, "js",
				inclusionPattern, rsHandler, generatorRegistry);
		bundle.setMappings(mappings);
		bundle.setAlternateProductionURL("http://hostname/scripts/myBundle.js");
		bundle.setExplorerConditionalExpression("if lt IE 6");

		Map<String, VariantSet> variants = new HashMap<String, VariantSet>();
		variants.put(
				JawrConstant.LOCALE_VARIANT_TYPE,
				new VariantSet(JawrConstant.LOCALE_VARIANT_TYPE, "fr", Arrays
						.asList("fr", "en_US")));
		bundle.setVariants(variants);
		bundle.setBundleDataHashCode(null, "N123456");
		bundle.setBundleDataHashCode("fr", "123456");
		bundle.setBundleDataHashCode("en_US", "789");

		bundle.setBundlePostProcessor(bundleProcessor);
		bundle.setUnitaryPostProcessor(fileProcessor);

		Properties props = new Properties();
		JoinableResourceBundlePropertySerializer.serializeInProperties(bundle,
				resourceType, props);

		PropertiesConfigHelper helper = new PropertiesConfigHelper(props,
				resourceType);
		assertEquals("/bundle/myBundle.js", helper.getCustomBundleProperty(
				bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ID));

		Set<String> expectedMappings = new HashSet<String>(Arrays.asList(
				"/bundle/content/script1.js", "/bundle/content/script2.js",
				"/bundle/myScript.js"));
		assertEquals(expectedMappings, helper.getCustomBundlePropertyAsSet(
				bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_MAPPINGS));

		assertEquals("false", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_GLOBAL_FLAG,
				"false"));
		assertEquals("3", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ORDER));
		assertEquals("true", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGNEVER,
				"false"));
		assertEquals("false", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGONLY,
				"false"));
		assertEquals(
				"http://hostname/scripts/myBundle.js",
				helper.getCustomBundleProperty(
						bundleName,
						PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PRODUCTION_ALT_URL));
		assertEquals(
				"if lt IE 6",
				helper.getCustomBundleProperty(
						bundleName,
						PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_IE_CONDITIONAL_EXPRESSION));
		assertEquals(
				"myBundlePostProcessor1,myBundlePostProcessor2",
				helper.getCustomBundleProperty(
						bundleName,
						PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_POSTPROCESSOR));
		assertEquals(
				"myFilePostProcessor1,myFilePostProcessor2",
				helper.getCustomBundleProperty(
						bundleName,
						PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_FILE_POSTPROCESSOR));
		assertEquals("N123456", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE));

		Map<String, VariantSet> expectedVariants = new HashMap<String, VariantSet>();
		expectedVariants.put(
				JawrConstant.LOCALE_VARIANT_TYPE,
				new VariantSet(JawrConstant.LOCALE_VARIANT_TYPE, "fr", Arrays
						.asList("fr", "en_US")));
		Map<String, VariantSet> variantSets = helper
				.getCustomBundleVariantSets(bundleName);
		assertEquals(expectedVariants, variantSets);

		assertEquals("N123456", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE));
		assertEquals("123456", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE_VARIANT
						+ "fr"));
		assertEquals("789", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE_VARIANT
						+ "en_US"));

	}

	@Test
	public void testExternalBundleSerialization() {

		String bundleName = "myBundle";
		String resourceType = "js";
		GeneratorRegistry generatorRegistry = new GeneratorRegistry();

		InclusionPattern inclusionPattern = new InclusionPattern(false, 3,
				DebugInclusion.ALWAYS);
		JoinableResourceBundleImpl bundle = new JoinableResourceBundleImpl(
				"/bundle/myBundle.js", bundleName, null, "js",
				inclusionPattern, rsHandler, generatorRegistry);
		bundle.setAlternateProductionURL("http://hostname/scripts/myBundle.min.js");
		bundle.setDebugURL("http://hostname/scripts/myBundle.js");

		Properties props = new Properties();
		JoinableResourceBundlePropertySerializer.serializeInProperties(bundle,
				resourceType, props);

		PropertiesConfigHelper helper = new PropertiesConfigHelper(props,
				resourceType);

		assertEquals("/bundle/myBundle.js", helper.getCustomBundleProperty(
				bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ID));
		assertEquals(
				"http://hostname/scripts/myBundle.min.js",
				helper.getCustomBundleProperty(
						bundleName,
						PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PRODUCTION_ALT_URL));

		assertEquals(
				"http://hostname/scripts/myBundle.js",
				helper.getCustomBundleProperty(
						bundleName,
						PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUG_URL));
		assertEquals("3", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ORDER));
		assertEquals("false", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGNEVER,
				"false"));
		assertEquals("false", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGONLY,
				"false"));

	}

}
