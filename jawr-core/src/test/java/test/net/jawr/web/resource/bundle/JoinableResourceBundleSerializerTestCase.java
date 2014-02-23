/**
 * 
 */
package test.net.jawr.web.resource.bundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;
import net.jawr.web.JawrConstant;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.DebugInclusion;
import net.jawr.web.resource.bundle.InclusionPattern;
import net.jawr.web.resource.bundle.JoinableResourceBundleImpl;
import net.jawr.web.resource.bundle.JoinableResourceBundlePropertySerializer;
import net.jawr.web.resource.bundle.factory.PropertiesBundleConstant;
import net.jawr.web.resource.bundle.factory.util.PropertiesConfigHelper;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.resource.handler.reader.ResourceReader;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

/**
 * Test case for JoinableResourceBundle  serializer
 * @author Ibrahim Chaehoi
 *
 */
public class JoinableResourceBundleSerializerTestCase extends TestCase {

	public void testGlobalBundleSerialization(){
		
		String bundleName = "myBundle";
		String resourceType = "js";
		List<String> mappings = Arrays.asList("/bundle/content/**", "/bundle/myScript.js");
		
		ResourceReaderHandler handler = new TestResourceHandler();
		GeneratorRegistry generatorRegistry = new GeneratorRegistry();
		InclusionPattern inclusionPattern = new InclusionPattern(true, 0);
		JoinableResourceBundleImpl bundle = new JoinableResourceBundleImpl("/bundle/myBundle.js", bundleName, null, "js", inclusionPattern, handler, generatorRegistry);
		bundle.setMappings(mappings);
		bundle.setBundleDataHashCode(null, "123456");
		
		Properties props = new Properties();
		JoinableResourceBundlePropertySerializer.serializeInProperties(bundle, resourceType, props);
		
		PropertiesConfigHelper helper = new PropertiesConfigHelper(props, resourceType);
		assertEquals("/bundle/myBundle.js", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ID));
		
		Set<String> expectedMappings = new HashSet<String>(Arrays.asList("/bundle/content/script1.js", "/bundle/content/script2.js", "/bundle/myScript.js"));
		assertEquals(expectedMappings, helper.getCustomBundlePropertyAsSet(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_MAPPINGS));
		
		assertEquals("true", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_GLOBAL_FLAG));
		assertEquals("false", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGNEVER, "false"));
		assertEquals("false", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGONLY, "false"));
		assertEquals("123456", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE));
		
	}

	public void testStdBundleSerialization(){
		
		String bundleName = "myBundle";
		String resourceType = "js";
		List<String> mappings = Arrays.asList("/bundle/content/**", "/bundle/myScript.js");
		
		ResourceReaderHandler handler = new TestResourceHandler();
		GeneratorRegistry generatorRegistry = new GeneratorRegistry();
		
		InclusionPattern inclusionPattern = new InclusionPattern(false, 3, DebugInclusion.ALWAYS);
		JoinableResourceBundleImpl bundle = new JoinableResourceBundleImpl("/bundle/myBundle.js", bundleName, null, "js", inclusionPattern, handler, generatorRegistry);
		bundle.setMappings(mappings);
		bundle.setAlternateProductionURL("http://hostname/scripts/myBundle.js");
		bundle.setExplorerConditionalExpression("if lt IE 6");
		
		Map<String, VariantSet> variants = new HashMap<String, VariantSet>();
		variants.put(JawrConstant.LOCALE_VARIANT_TYPE, new VariantSet(JawrConstant.LOCALE_VARIANT_TYPE, "fr", Arrays.asList("fr", "en_US")));
		bundle.setVariants(variants);
		bundle.setBundleDataHashCode(null, "N123456");
		bundle.setBundleDataHashCode("fr", "123456");
		bundle.setBundleDataHashCode("en_US", "789");
		
		ResourceBundlePostProcessor bundlePostProcessor = new AbstractChainedResourceBundlePostProcessor("myBundlePostProcessor1,myBundlePostProcessor2"){

			protected StringBuffer doPostProcessBundle(
					BundleProcessingStatus status, StringBuffer bundleData)
					throws IOException {
				return null;
			}
		};
		bundle.setBundlePostProcessor(bundlePostProcessor);

		ResourceBundlePostProcessor filePostProcessor = new AbstractChainedResourceBundlePostProcessor("myFilePostProcessor1,myFilePostProcessor2"){

			protected StringBuffer doPostProcessBundle(
					BundleProcessingStatus status, StringBuffer bundleData)
					throws IOException {
				return null;
			}
		};
		bundle.setUnitaryPostProcessor(filePostProcessor);
		
		Properties props = new Properties();
		JoinableResourceBundlePropertySerializer.serializeInProperties(bundle, resourceType, props);
		
		PropertiesConfigHelper helper = new PropertiesConfigHelper(props, resourceType);
		assertEquals("/bundle/myBundle.js", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ID));
		
		Set<String> expectedMappings = new HashSet<String>(Arrays.asList("/bundle/content/script1.js", "/bundle/content/script2.js", "/bundle/myScript.js"));
		assertEquals(expectedMappings, helper.getCustomBundlePropertyAsSet(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_MAPPINGS));
		
		assertEquals("false", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_GLOBAL_FLAG, "false"));
		assertEquals("3", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ORDER));
		assertEquals("false", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGNEVER, "false"));
		assertEquals("false", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGONLY, "false"));
		assertEquals("http://hostname/scripts/myBundle.js", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PRODUCTION_ALT_URL));
		assertEquals("if lt IE 6", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_IE_CONDITIONAL_EXPRESSION));
		assertEquals("myBundlePostProcessor1,myBundlePostProcessor2", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_POSTPROCESSOR));
		assertEquals("myFilePostProcessor1,myFilePostProcessor2", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_FILE_POSTPROCESSOR));
		assertEquals("N123456", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE));
		
		Map<String, VariantSet> expectedVariants = new HashMap<String, VariantSet>();
		expectedVariants.put(JawrConstant.LOCALE_VARIANT_TYPE, new VariantSet(JawrConstant.LOCALE_VARIANT_TYPE, "fr", Arrays.asList("fr", "en_US")));
		Map<String, VariantSet> variantSets = helper.getCustomBundleVariantSets(bundleName);
		assertEquals(expectedVariants, variantSets);
		
		assertEquals("N123456", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE));
		assertEquals("123456", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE_VARIANT+"fr"));
		assertEquals("789", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE_VARIANT+"en_US"));
		
	}
	
	public void testStdBundleSerializationDebugOnly(){
		
		String bundleName = "myBundle";
		String resourceType = "js";
		List<String> mappings = Arrays.asList("/bundle/content/**", "/bundle/myScript.js");
		
		ResourceReaderHandler handler = new TestResourceHandler();
		GeneratorRegistry generatorRegistry = new GeneratorRegistry();
		
		InclusionPattern inclusionPattern = new InclusionPattern(false, 3, DebugInclusion.ONLY);
		JoinableResourceBundleImpl bundle = new JoinableResourceBundleImpl("/bundle/myBundle.js", bundleName, null, "js", inclusionPattern, handler, generatorRegistry);
		bundle.setMappings(mappings);
		bundle.setAlternateProductionURL("http://hostname/scripts/myBundle.js");
		bundle.setExplorerConditionalExpression("if lt IE 6");
		
		Map<String, VariantSet> variants = new HashMap<String, VariantSet>();
		variants.put(JawrConstant.LOCALE_VARIANT_TYPE, new VariantSet(JawrConstant.LOCALE_VARIANT_TYPE, "fr", Arrays.asList("fr", "en_US")));
		bundle.setVariants(variants);
		bundle.setBundleDataHashCode(null, "N123456");
		bundle.setBundleDataHashCode("fr", "123456");
		bundle.setBundleDataHashCode("en_US", "789");
		
		ResourceBundlePostProcessor bundlePostProcessor = new AbstractChainedResourceBundlePostProcessor("myBundlePostProcessor1,myBundlePostProcessor2"){

			protected StringBuffer doPostProcessBundle(
					BundleProcessingStatus status, StringBuffer bundleData)
					throws IOException {
				return null;
			}
		};
		bundle.setBundlePostProcessor(bundlePostProcessor);

		ResourceBundlePostProcessor filePostProcessor = new AbstractChainedResourceBundlePostProcessor("myFilePostProcessor1,myFilePostProcessor2"){

			protected StringBuffer doPostProcessBundle(
					BundleProcessingStatus status, StringBuffer bundleData)
					throws IOException {
				return null;
			}
		};
		bundle.setUnitaryPostProcessor(filePostProcessor);
		
		Properties props = new Properties();
		JoinableResourceBundlePropertySerializer.serializeInProperties(bundle, resourceType, props);
		
		PropertiesConfigHelper helper = new PropertiesConfigHelper(props, resourceType);
		assertEquals("/bundle/myBundle.js", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ID));
		
		Set<String> expectedMappings = new HashSet<String>(Arrays.asList("/bundle/content/script1.js", "/bundle/content/script2.js", "/bundle/myScript.js"));
		assertEquals(expectedMappings, helper.getCustomBundlePropertyAsSet(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_MAPPINGS));
		
		assertEquals("false", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_GLOBAL_FLAG, "false"));
		assertEquals("3", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ORDER));
		assertEquals("false", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGNEVER, "false"));
		assertEquals("true", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGONLY, "false"));
		assertEquals("http://hostname/scripts/myBundle.js", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PRODUCTION_ALT_URL));
		assertEquals("if lt IE 6", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_IE_CONDITIONAL_EXPRESSION));
		assertEquals("myBundlePostProcessor1,myBundlePostProcessor2", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_POSTPROCESSOR));
		assertEquals("myFilePostProcessor1,myFilePostProcessor2", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_FILE_POSTPROCESSOR));
		assertEquals("N123456", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE));
		
		Map<String, VariantSet> expectedVariants = new HashMap<String, VariantSet>();
		expectedVariants.put(JawrConstant.LOCALE_VARIANT_TYPE, new VariantSet(JawrConstant.LOCALE_VARIANT_TYPE, "fr", Arrays.asList("fr", "en_US")));
		Map<String, VariantSet> variantSets = helper.getCustomBundleVariantSets(bundleName);
		assertEquals(expectedVariants, variantSets);
		
		assertEquals("N123456", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE));
		assertEquals("123456", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE_VARIANT+"fr"));
		assertEquals("789", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE_VARIANT+"en_US"));
		
	}
	
	public void testStdBundleSerializationDebugNever(){
		
		String bundleName = "myBundle";
		String resourceType = "js";
		List<String> mappings = Arrays.asList("/bundle/content/**", "/bundle/myScript.js");
		
		ResourceReaderHandler handler = new TestResourceHandler();
		GeneratorRegistry generatorRegistry = new GeneratorRegistry();
		
		InclusionPattern inclusionPattern = new InclusionPattern(false, 3, DebugInclusion.NEVER);
		JoinableResourceBundleImpl bundle = new JoinableResourceBundleImpl("/bundle/myBundle.js", bundleName, null, "js", inclusionPattern, handler, generatorRegistry);
		bundle.setMappings(mappings);
		bundle.setAlternateProductionURL("http://hostname/scripts/myBundle.js");
		bundle.setExplorerConditionalExpression("if lt IE 6");
		
		Map<String, VariantSet> variants = new HashMap<String, VariantSet>();
		variants.put(JawrConstant.LOCALE_VARIANT_TYPE, new VariantSet(JawrConstant.LOCALE_VARIANT_TYPE, "fr", Arrays.asList("fr", "en_US")));
		bundle.setVariants(variants);
		bundle.setBundleDataHashCode(null, "N123456");
		bundle.setBundleDataHashCode("fr", "123456");
		bundle.setBundleDataHashCode("en_US", "789");
		
		ResourceBundlePostProcessor bundlePostProcessor = new AbstractChainedResourceBundlePostProcessor("myBundlePostProcessor1,myBundlePostProcessor2"){

			protected StringBuffer doPostProcessBundle(
					BundleProcessingStatus status, StringBuffer bundleData)
					throws IOException {
				return null;
			}
		};
		bundle.setBundlePostProcessor(bundlePostProcessor);

		ResourceBundlePostProcessor filePostProcessor = new AbstractChainedResourceBundlePostProcessor("myFilePostProcessor1,myFilePostProcessor2"){

			protected StringBuffer doPostProcessBundle(
					BundleProcessingStatus status, StringBuffer bundleData)
					throws IOException {
				return null;
			}
		};
		bundle.setUnitaryPostProcessor(filePostProcessor);
		
		Properties props = new Properties();
		JoinableResourceBundlePropertySerializer.serializeInProperties(bundle, resourceType, props);
		
		PropertiesConfigHelper helper = new PropertiesConfigHelper(props, resourceType);
		assertEquals("/bundle/myBundle.js", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ID));
		
		Set<String> expectedMappings = new HashSet<String>(Arrays.asList("/bundle/content/script1.js", "/bundle/content/script2.js", "/bundle/myScript.js"));
		assertEquals(expectedMappings, helper.getCustomBundlePropertyAsSet(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_MAPPINGS));
		
		assertEquals("false", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_GLOBAL_FLAG, "false"));
		assertEquals("3", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ORDER));
		assertEquals("true", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGNEVER, "false"));
		assertEquals("false", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGONLY, "false"));
		assertEquals("http://hostname/scripts/myBundle.js", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PRODUCTION_ALT_URL));
		assertEquals("if lt IE 6", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_IE_CONDITIONAL_EXPRESSION));
		assertEquals("myBundlePostProcessor1,myBundlePostProcessor2", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_POSTPROCESSOR));
		assertEquals("myFilePostProcessor1,myFilePostProcessor2", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_FILE_POSTPROCESSOR));
		assertEquals("N123456", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE));
		
		Map<String, VariantSet> expectedVariants = new HashMap<String, VariantSet>();
		expectedVariants.put(JawrConstant.LOCALE_VARIANT_TYPE, new VariantSet(JawrConstant.LOCALE_VARIANT_TYPE, "fr", Arrays.asList("fr", "en_US")));
		Map<String, VariantSet> variantSets = helper.getCustomBundleVariantSets(bundleName);
		assertEquals(expectedVariants, variantSets);
		
		assertEquals("N123456", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE));
		assertEquals("123456", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE_VARIANT+"fr"));
		assertEquals("789", helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE_VARIANT+"en_US"));
		
	}

	private static class TestResourceHandler implements ResourceReaderHandler{

		public Set<String> getResourceNames(String path) {
			
			List<String> paths = Arrays.asList("script1.js", "script2.js");
			return new HashSet<String>(paths);
		}

		public boolean isDirectory(String path) {
			
			return path.endsWith("/**");
		}

		public void addResourceReaderToEnd(ResourceReader rd) {
			
		}

		public void addResourceReaderToStart(ResourceReader rd) {
			
		}

		public Reader getResource(String resourceName)
				throws ResourceNotFoundException {
			return null;
		}

		public Reader getResource(String resourceName, boolean processingBundle)
				throws ResourceNotFoundException {
			return null;
		}

		public InputStream getResourceAsStream(String resourceName)
				throws ResourceNotFoundException {
			return null;
		}

		public InputStream getResourceAsStream(String resourceName,
				boolean processingBundle) throws ResourceNotFoundException {
			return null;
		}

		public String getWorkingDirectory() {
			return null;
		}

		public void setWorkingDirectory(String workingDir) {
			
		}

		@Override
		public Reader getResource(String resourceName,
				boolean processingBundle, List<Class<?>> excludedReader)
				throws ResourceNotFoundException {
			return null;
		}
		
	}
}
