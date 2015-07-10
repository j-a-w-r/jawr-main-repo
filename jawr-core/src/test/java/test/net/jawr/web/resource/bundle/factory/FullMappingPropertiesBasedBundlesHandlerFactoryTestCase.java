package test.net.jawr.web.resource.bundle.factory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import junit.framework.TestCase;
import net.jawr.web.JawrConstant;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.DebugInclusion;
import net.jawr.web.resource.bundle.InclusionPattern;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.JoinableResourceBundleImpl;
import net.jawr.web.resource.bundle.JoinableResourceBundlePropertySerializer;
import net.jawr.web.resource.bundle.factory.FullMappingPropertiesBasedBundlesHandlerFactory;
import net.jawr.web.resource.bundle.factory.postprocessor.PostProcessorChainFactory;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.iterator.BundlePath;
import net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.resource.handler.reader.ResourceReader;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

/**
 * Test case for FullMappingPropertiesBasedBundlesHandlerFactory
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class FullMappingPropertiesBasedBundlesHandlerFactoryTestCase extends
		TestCase {

	public void testGetGlobalResourceBundles() {
		
		ResourceReaderHandler rsHandler = new TestResourceReaderHandler();
		PostProcessorChainFactory chainFactory = new TestPostProcessorChainFactory();
		
		GeneratorRegistry generatorRegistry = new GeneratorRegistry();
		FullMappingPropertiesBasedBundlesHandlerFactory factory = new FullMappingPropertiesBasedBundlesHandlerFactory("js", rsHandler, generatorRegistry, chainFactory);
		
		Properties props = new Properties();
		
		JoinableResourceBundle globalBundle = getGlobalBundle();
		JoinableResourceBundlePropertySerializer.serializeInProperties(globalBundle, "js", props);
		
		List<JoinableResourceBundle> resourcesBundles = factory.getResourceBundles(props);
		assertEquals(1, resourcesBundles.size());
		JoinableResourceBundle bundle = (JoinableResourceBundle) resourcesBundles.get(0);
		
		assertEquals("/bundle/myGlobalBundle.js", bundle.getId());
		Set<BundlePath> expectedMappings = new HashSet<BundlePath>(asBundlePathList("/bundle/content/script1.js", "/bundle/content/script2.js", "/bundle/myScript.js"));
		assertEquals(expectedMappings, new HashSet<BundlePath>(bundle.getItemPathList()));
		
		assertEquals(true, bundle.getInclusionPattern().isGlobal());
		assertEquals(false, bundle.getInclusionPattern().isExcludeOnDebug());
		assertEquals(false, bundle.getInclusionPattern().isIncludeOnlyOnDebug());
		assertEquals("123456", bundle.getBundleDataHashCode(null));
	}

	public void testGetStdResourceBundles() {
		
		testGetStdResourceBundle(DebugInclusion.ALWAYS);
		testGetStdResourceBundle(DebugInclusion.ONLY);
		testGetStdResourceBundle(DebugInclusion.NEVER);
	}

	protected void testGetStdResourceBundle(DebugInclusion debugInclusion) {
	
		ResourceReaderHandler rsHandler = new TestResourceReaderHandler();
		PostProcessorChainFactory chainFactory = new TestPostProcessorChainFactory();
		
		GeneratorRegistry generatorRegistry = new GeneratorRegistry();
		FullMappingPropertiesBasedBundlesHandlerFactory factory = new FullMappingPropertiesBasedBundlesHandlerFactory("js", rsHandler, generatorRegistry, chainFactory);
		
		Properties props = new Properties();
		
		JoinableResourceBundle stdBundle = getStdBundle("myBundle", debugInclusion);
		JoinableResourceBundlePropertySerializer.serializeInProperties(stdBundle, "js", props);
		
		List<JoinableResourceBundle> resourcesBundles = factory.getResourceBundles(props);
		assertEquals(1, resourcesBundles.size());
		
		JoinableResourceBundle bundle = (JoinableResourceBundle) resourcesBundles.get(0);
			
		assertEquals("/bundle/myBundle.js", bundle.getId());
				
		assertEquals(debugInclusion, bundle.getInclusionPattern().getDebugInclusion());
		Set<BundlePath> expectedMappings = new HashSet<BundlePath>(asBundlePathList("/bundle/content/script1.js", "/bundle/content/script2.js", "/bundle/myScript.js"));
		if(debugInclusion.equals(DebugInclusion.ONLY)){
			assertEquals(expectedMappings, new HashSet<BundlePath>(bundle.getItemDebugPathList()));
		}else{
			assertEquals(expectedMappings, new HashSet<BundlePath>(bundle.getItemPathList()));
		}
		
		assertEquals(true, bundle.getInclusionPattern().isGlobal());
		assertEquals(3, bundle.getInclusionPattern().getInclusionOrder());
		assertEquals("http://hostname/scripts/myBundle.js", bundle.getAlternateProductionURL());
		assertEquals("if lt IE 6", bundle.getExplorerConditionalExpression());
		assertEquals("myBundlePostProcessor1,myBundlePostProcessor2", ((AbstractChainedResourceBundlePostProcessor) bundle.getBundlePostProcessor()).getId());
		assertEquals("myFilePostProcessor1,myFilePostProcessor2", ((AbstractChainedResourceBundlePostProcessor) bundle.getUnitaryPostProcessor()).getId());
		
		Set<String> expectedLocales = new HashSet<String>(Arrays.asList("", "fr", "en_US"));
		assertEquals(expectedLocales, new HashSet<String>(bundle.getVariantKeys()));
		
		assertEquals("N123456", bundle.getBundleDataHashCode(null));
		assertEquals("N123456", bundle.getBundleDataHashCode(""));
		assertEquals("123456", bundle.getBundleDataHashCode("fr"));
		assertEquals("789", bundle.getBundleDataHashCode("en_US"));
	}

	public void testGetResourceBundlesWithDependencies() {
		
		testGetResourceBundlesWithDependencies(DebugInclusion.ALWAYS);
		testGetResourceBundlesWithDependencies(DebugInclusion.ONLY);
		testGetResourceBundlesWithDependencies(DebugInclusion.NEVER);
	}

	protected void testGetResourceBundlesWithDependencies(DebugInclusion inclusion) {
		ResourceReaderHandler rsHandler = new TestResourceReaderHandler();
		PostProcessorChainFactory chainFactory = new TestPostProcessorChainFactory();
		
		GeneratorRegistry generatorRegistry = new GeneratorRegistry();
		FullMappingPropertiesBasedBundlesHandlerFactory factory = new FullMappingPropertiesBasedBundlesHandlerFactory("js", rsHandler, generatorRegistry, chainFactory);
		
		Properties props = new Properties();
		
		List<JoinableResourceBundle> bundleWithDependencies = getBundleWithDependencies(inclusion);
		for (Iterator<JoinableResourceBundle> iterator = bundleWithDependencies.iterator(); iterator
				.hasNext();) {
			JoinableResourceBundle  aBundle = iterator.next();
			JoinableResourceBundlePropertySerializer.serializeInProperties(aBundle, "js", props);
		}
		
		List<JoinableResourceBundle> resourcesBundles = factory.getResourceBundles(props);
		assertEquals(3, resourcesBundles.size());
		
		for (Iterator<JoinableResourceBundle> iterator = resourcesBundles.iterator(); iterator
				.hasNext();) {
			JoinableResourceBundle bundle = iterator.next();
			
			assertEquals(inclusion, bundle.getInclusionPattern().getDebugInclusion());
			
			Set<BundlePath> expectedMappings = new HashSet<BundlePath>(asBundlePathList("/bundle/content/script1.js", "/bundle/content/script2.js", "/bundle/myScript.js"));
			if(inclusion.equals(DebugInclusion.ONLY)){
				assertEquals(expectedMappings, new HashSet<BundlePath>(bundle.getItemDebugPathList()));
			}else{
				assertEquals(expectedMappings, new HashSet<BundlePath>(bundle.getItemPathList()));
			}
			
			assertEquals(true, bundle.getInclusionPattern().isGlobal());
			assertEquals(3, bundle.getInclusionPattern().getInclusionOrder());
			assertEquals("if lt IE 6", bundle.getExplorerConditionalExpression());
			assertEquals("myBundlePostProcessor1,myBundlePostProcessor2", ((AbstractChainedResourceBundlePostProcessor) bundle.getBundlePostProcessor()).getId());
			assertEquals("myFilePostProcessor1,myFilePostProcessor2", ((AbstractChainedResourceBundlePostProcessor) bundle.getUnitaryPostProcessor()).getId());
			
			if(bundle.getName().equals("myBundle")){
				assertEquals(2, bundle.getDependencies().size());
				assertEquals("myBundle1", ((JoinableResourceBundle)bundle.getDependencies().get(0)).getName());
				assertEquals("myBundle2", ((JoinableResourceBundle)bundle.getDependencies().get(1)).getName());
			}else{
				assertNull(bundle.getDependencies());
			}
			
			Set<String> expectedLocales = new HashSet<String>(Arrays.asList("", "fr", "en_US"));
			assertEquals(expectedLocales, new HashSet<String>(bundle.getVariantKeys()));
			
			assertEquals("N123456", bundle.getBundleDataHashCode(null));
			assertEquals("123456", bundle.getBundleDataHashCode("fr"));
			assertEquals("789", bundle.getBundleDataHashCode("en_US"));
		}
	}

	public void testGetVariantResourceBundles() {
		
		ResourceReaderHandler rsHandler = new TestResourceReaderHandler();
		PostProcessorChainFactory chainFactory = new TestPostProcessorChainFactory();
		
		GeneratorRegistry generatorRegistry = new GeneratorRegistry();
		FullMappingPropertiesBasedBundlesHandlerFactory factory = new FullMappingPropertiesBasedBundlesHandlerFactory("js", rsHandler, generatorRegistry, chainFactory);
		
		Properties props = new Properties();
		
		JoinableResourceBundle stdBundle = getBundleWithVariants(DebugInclusion.ALWAYS);
		JoinableResourceBundlePropertySerializer.serializeInProperties(stdBundle, "js", props);
		
		List<JoinableResourceBundle> resourcesBundles = factory.getResourceBundles(props);
		assertEquals(1, resourcesBundles.size());
		
		JoinableResourceBundle bundle = (JoinableResourceBundle) resourcesBundles.get(0);
			
		assertEquals("/bundle/myBundle.js", bundle.getId());
				
		Set<BundlePath> expectedMappings = new HashSet<BundlePath>(asBundlePathList("/bundle/content/script1.js", "/bundle/content/script2.js", "/bundle/myScript.js"));
		assertEquals(expectedMappings, new HashSet<BundlePath>(bundle.getItemPathList()));
		
		assertEquals(true, bundle.getInclusionPattern().isGlobal());
		assertEquals(3, bundle.getInclusionPattern().getInclusionOrder());
		assertEquals(DebugInclusion.ALWAYS, bundle.getInclusionPattern().getDebugInclusion());
		assertEquals("http://hostname/scripts/myBundle.js", bundle.getAlternateProductionURL());
		assertEquals("if lt IE 6", bundle.getExplorerConditionalExpression());
		assertEquals("myBundlePostProcessor1,myBundlePostProcessor2", ((AbstractChainedResourceBundlePostProcessor) bundle.getBundlePostProcessor()).getId());
		assertEquals("myFilePostProcessor1,myFilePostProcessor2", ((AbstractChainedResourceBundlePostProcessor) bundle.getUnitaryPostProcessor()).getId());
		
		Set<String> expectedVariants = new HashSet<String>(Arrays.asList("@summer","@winter","fr@summer", "fr@winter","en_US@summer","en_US@winter"));
		assertEquals(expectedVariants, new HashSet<String>(bundle.getVariantKeys()));
		
		assertEquals("N123456", bundle.getBundleDataHashCode(null));
		assertEquals("178456", bundle.getBundleDataHashCode("@summer"));
		assertEquals("418451", bundle.getBundleDataHashCode("@winter"));
		assertEquals("123456", bundle.getBundleDataHashCode("fr@summer"));
		assertEquals("789", bundle.getBundleDataHashCode("en_US@summer"));
		assertEquals("123456", bundle.getBundleDataHashCode("fr@winter"));
		assertEquals("789", bundle.getBundleDataHashCode("en_US@winter"));
	}

	private JoinableResourceBundle getGlobalBundle(){
		String bundleName = "myGlobalBundle";
		List<String> mappings = Arrays.asList("/bundle/content/**", "/bundle/myScript.js");
		
		ResourceReaderHandler handler = new TestResourceReaderHandler();
		InclusionPattern inclusionPattern = new InclusionPattern(true, 0);
		GeneratorRegistry generatorRegistry = new GeneratorRegistry();
		JoinableResourceBundle bundle = new JoinableResourceBundleImpl("/bundle/myGlobalBundle.js", bundleName, null, "js", inclusionPattern, handler, generatorRegistry);
		bundle.setMappings(mappings);
		bundle.setBundleDataHashCode(null, "123456");
		
		return bundle;
	}
	
	private JoinableResourceBundleImpl getStdBundle(String bundleName, DebugInclusion inclusion){		
		List<String> mappings = Arrays.asList("/bundle/content/**", "/bundle/myScript.js");
		
		ResourceReaderHandler handler = new TestResourceReaderHandler();
		InclusionPattern inclusionPattern = new InclusionPattern(true, 3, inclusion);
				
		GeneratorRegistry generatorRegistry = new GeneratorRegistry();
		JoinableResourceBundleImpl bundle = new JoinableResourceBundleImpl("/bundle/"+bundleName+".js", bundleName, null, "js", inclusionPattern, handler, generatorRegistry);
		bundle.setMappings(mappings);
		bundle.setAlternateProductionURL("http://hostname/scripts/"+bundleName+".js");
		bundle.setExplorerConditionalExpression("if lt IE 6");
		
		Map<String, VariantSet> variants = new HashMap<String, VariantSet>();
		variants.put(JawrConstant.LOCALE_VARIANT_TYPE, new VariantSet(JawrConstant.LOCALE_VARIANT_TYPE, "", Arrays.asList("", "fr", "en_US")));
		bundle.setVariants(variants);
		bundle.setBundleDataHashCode(null, "N123456");
		bundle.setBundleDataHashCode("", "N123456");
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
	
		return bundle;
	}
	
	private List<BundlePath> asBundlePathList(String... paths){
		List<BundlePath> result = new ArrayList<BundlePath>();
		for(String path : paths){
			result.add(new BundlePath(null, path));
		}
		
		return result;
	}
	
	private List<JoinableResourceBundle> getBundleWithDependencies(DebugInclusion inclusion){
		
		List<JoinableResourceBundle> bundles = new ArrayList<JoinableResourceBundle>();
		JoinableResourceBundleImpl bundle = getStdBundle("myBundle", inclusion);
		JoinableResourceBundleImpl bundle1 = getStdBundle("myBundle1", inclusion);
		JoinableResourceBundleImpl bundle2 = getStdBundle("myBundle2", inclusion);
		
		bundle.setDependencies(Arrays.asList(new JoinableResourceBundle[]{bundle1, bundle2}));
		bundles.add(bundle);
		bundles.add(bundle1);
		bundles.add(bundle2);
		return bundles;
	}
	
	private JoinableResourceBundleImpl getBundleWithVariants(DebugInclusion inclusion){
		
		String bundleName = "myBundle";
		List<String> mappings = Arrays.asList("/bundle/content/**", "/bundle/myScript.js");
		
		ResourceReaderHandler handler = new TestResourceReaderHandler();
		InclusionPattern inclusionPattern = new InclusionPattern(true, 3, inclusion);
		GeneratorRegistry generatorRegistry = new GeneratorRegistry();
		JoinableResourceBundleImpl bundle = new JoinableResourceBundleImpl("/bundle/myBundle.js", bundleName, null, "js", inclusionPattern, handler, generatorRegistry);
		bundle.setMappings(mappings);
		bundle.setAlternateProductionURL("http://hostname/scripts/myBundle.js");
		bundle.setExplorerConditionalExpression("if lt IE 6");
		
		Map<String, VariantSet> variants = new HashMap<String, VariantSet>();
		variants.put(JawrConstant.SKIN_VARIANT_TYPE, new VariantSet(JawrConstant.SKIN_VARIANT_TYPE, "summer", Arrays.asList("summer", "winter")));
		variants.put(JawrConstant.LOCALE_VARIANT_TYPE, new VariantSet(JawrConstant.LOCALE_VARIANT_TYPE, "", Arrays.asList("","fr", "en_US")));
		bundle.setVariants(variants);
		bundle.setBundleDataHashCode(null, "N123456");
		bundle.setBundleDataHashCode("@summer", "178456");
		bundle.setBundleDataHashCode("@winter", "418451");
		bundle.setBundleDataHashCode("fr@summer", "123456");
		bundle.setBundleDataHashCode("en_US@summer", "789");
		bundle.setBundleDataHashCode("fr@winter", "123456");
		bundle.setBundleDataHashCode("en_US@winter", "789");
		
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
	
		return bundle;
	}

	private static class TestPostProcessorChainFactory implements PostProcessorChainFactory{

		public ResourceBundlePostProcessor buildDefaultProcessorChain() {
			return null;
		}

		public ResourceBundlePostProcessor buildDefaultUnitProcessorChain() {
			return null;
		}

		public ResourceBundlePostProcessor buildPostProcessorChain(
				String processorKeys) {
			
			return new TestChainedResourceBundlePostProcessor(processorKeys);
		}

		public void setCustomPostprocessors(Map<String, String> keysClassNames) {
			
		}

		public ResourceBundlePostProcessor buildDefaultCompositeProcessorChain() {
			return null;
		}

		public ResourceBundlePostProcessor buildDefaultUnitCompositeProcessorChain() {
			return null;
		}
	}
	
	private static class TestChainedResourceBundlePostProcessor extends AbstractChainedResourceBundlePostProcessor{
		
		public TestChainedResourceBundlePostProcessor(String id) {
			super(id);
		}
		
		protected StringBuffer doPostProcessBundle(
				BundleProcessingStatus status,
				StringBuffer bundleData) throws IOException {
			return null;
		}
	}
	
	private static class TestResourceReaderHandler implements ResourceReaderHandler{

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
