package test.net.jawr.web.resource.bundle.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.CompositeResourceBundle;
import net.jawr.web.resource.bundle.DebugInclusion;
import net.jawr.web.resource.bundle.InclusionPattern;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.JoinableResourceBundleImpl;
import net.jawr.web.resource.bundle.JoinableResourceBundlePropertySerializer;
import net.jawr.web.resource.bundle.factory.FullMappingPropertiesBasedBundlesHandlerFactory;
import net.jawr.web.resource.bundle.factory.PropertiesBundleConstant;
import net.jawr.web.resource.bundle.factory.postprocessor.PostProcessorChainFactory;
import net.jawr.web.resource.bundle.factory.util.PropertiesConfigHelper;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.iterator.BundlePath;
import net.jawr.web.resource.bundle.mappings.FilePathMapping;
import net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

/**
 * Test case for FullMappingPropertiesBasedBundlesHandlerFactory
 * 
 * @author Ibrahim Chaehoi
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class FullMappingPropertiesBasedBundlesHandlerFactoryTestCase {

	@Mock
	private ResourceReaderHandler rsHandler;

	@Mock
	private PostProcessorChainFactory chainFactory;

	@Mock
	private AbstractChainedResourceBundlePostProcessor bundleProcessor;

	@Mock
	private AbstractChainedResourceBundlePostProcessor fileProcessor;

	@Mock
	private GeneratorRegistry generatorRegistry;

	String filePathScript1 = "/FS/bundle/content/script1.js";
	String filePathScript2 = "/FS/bundle/content/script2.js";
	String filePathScript3 = "/FS/bundle/content/script3.js";
	String filePathScript = "/FS/bundle/myScript.js";
	long script1LastModified = 8768667L;
	long script2LastModified = 8768668L;
	long script3LastModified = 8768669L;
	long scriptLastModified = 8768670L;
	long updatedLastModified = 90987989L;

	@Before
	public void setUp() {
		when(rsHandler.getFilePath("/bundle/content/script1.js")).thenReturn(filePathScript1);
		when(rsHandler.getFilePath("/bundle/content/script2.js")).thenReturn(filePathScript2);
		when(rsHandler.getFilePath("/bundle/myScript.js")).thenReturn("/FS/bundle/myScript.js");

		when(rsHandler.getLastModified(filePathScript1)).thenReturn(script1LastModified);
		when(rsHandler.getLastModified(filePathScript2)).thenReturn(script2LastModified);
		when(rsHandler.getLastModified(filePathScript)).thenReturn(scriptLastModified);

		when(rsHandler.getResourceNames(Matchers.anyString()))
				.thenReturn(new HashSet<>(Arrays.asList("script1.js", "script2.js")));
		when(chainFactory.buildPostProcessorChain("myBundlePostProcessor1,myBundlePostProcessor2"))
				.thenReturn(bundleProcessor);
		when(bundleProcessor.getId()).thenReturn("myBundlePostProcessor1,myBundlePostProcessor2");
		when(chainFactory.buildPostProcessorChain("myFilePostProcessor1,myFilePostProcessor2"))
				.thenReturn(fileProcessor);
		when(fileProcessor.getId()).thenReturn("myFilePostProcessor1,myFilePostProcessor2");
	}

	@Test
	public void testGetGlobalResourceBundles() {

		FullMappingPropertiesBasedBundlesHandlerFactory factory = new FullMappingPropertiesBasedBundlesHandlerFactory(
				"js", rsHandler, generatorRegistry, chainFactory);

		Properties props = new Properties();

		JoinableResourceBundle globalBundle = getGlobalBundle();
		JoinableResourceBundlePropertySerializer.serializeInProperties(globalBundle, "js", props);

		List<JoinableResourceBundle> resourcesBundles = factory.getResourceBundles(props);
		assertEquals(1, resourcesBundles.size());
		JoinableResourceBundle bundle = (JoinableResourceBundle) resourcesBundles.get(0);

		assertEquals("/bundle/myGlobalBundle.js", bundle.getId());
		assertFalse(bundle.isComposite());

		Set<BundlePath> expectedMappings = new HashSet<>(
				asBundlePathList("/bundle/content/script1.js", "/bundle/content/script2.js", "/bundle/myScript.js"));
		assertEquals(expectedMappings, new HashSet<>(bundle.getItemPathList()));

		// Check bundle file mapping
		Set<FilePathMapping> filePathMappings = new HashSet<>(bundle.getFilePathMappings());
		Set<FilePathMapping> expectedFilePathMappings = new HashSet<>();
		expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript1, script1LastModified));
		expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript2, script2LastModified));
		expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript, scriptLastModified));
		assertEquals(expectedFilePathMappings, filePathMappings);

		// Checks if the bundle is considered as modified
		assertFalse(bundle.isDirty());

		assertEquals(true, bundle.getInclusionPattern().isGlobal());
		assertEquals(false, bundle.getInclusionPattern().isExcludeOnDebug());
		assertEquals(false, bundle.getInclusionPattern().isIncludeOnlyOnDebug());
		assertEquals("123456", bundle.getBundleDataHashCode(null));
	}

	/**
	 * Checks the file mapping
	 * 
	 * @param fMapping
	 *            the file path mapping
	 * @param filePath
	 *            the file path
	 * @param lastModified
	 *            the last modified timestamp
	 */
	protected void checkFileMapping(FilePathMapping fMapping, String filePath, long lastModified) {
		assertEquals(filePath, fMapping.getPath());
		assertEquals(lastModified, fMapping.getLastModified());
	}

	@Test
	public void testGetStdResourceBundles() {

		testGetStdResourceBundle(DebugInclusion.ALWAYS);
		testGetStdResourceBundle(DebugInclusion.ONLY);
		testGetStdResourceBundle(DebugInclusion.NEVER);
	}

	protected void testGetStdResourceBundle(DebugInclusion debugInclusion) {

		Set<String> expectedLocales = new HashSet<>(Arrays.asList("", "fr", "en_US"));
		Map<String, VariantSet> variants = new HashMap<>();
		variants.put("locale", new VariantSet("locale", "", expectedLocales));
		when(generatorRegistry.getAvailableVariants(Matchers.anyString())).thenReturn(variants);
		FullMappingPropertiesBasedBundlesHandlerFactory factory = new FullMappingPropertiesBasedBundlesHandlerFactory(
				"js", rsHandler, generatorRegistry, chainFactory);

		Properties props = new Properties();

		JoinableResourceBundle stdBundle = getStdBundle("myBundle", debugInclusion);
		JoinableResourceBundlePropertySerializer.serializeInProperties(stdBundle, "js", props);

		List<JoinableResourceBundle> resourcesBundles = factory.getResourceBundles(props);
		assertEquals(1, resourcesBundles.size());

		JoinableResourceBundle bundle = (JoinableResourceBundle) resourcesBundles.get(0);

		assertEquals("/bundle/myBundle.js", bundle.getId());
		assertFalse(bundle.isComposite());

		assertEquals(debugInclusion, bundle.getInclusionPattern().getDebugInclusion());
		Set<BundlePath> expectedMappings = new HashSet<>(
				asBundlePathList("/bundle/content/script1.js", "/bundle/content/script2.js", "/bundle/myScript.js"));
		if (debugInclusion.equals(DebugInclusion.ONLY)) {
			assertEquals(expectedMappings, new HashSet<>(bundle.getItemDebugPathList()));
		} else {
			assertEquals(expectedMappings, new HashSet<>(bundle.getItemPathList()));
		}

		// Check bundle file mapping
		Set<FilePathMapping> filePathMappings = new HashSet<>(bundle.getFilePathMappings());
		Set<FilePathMapping> expectedFilePathMappings = new HashSet<>();
		expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript1, script1LastModified));
		expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript2, script2LastModified));
		expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript, scriptLastModified));
		assertEquals(expectedFilePathMappings, filePathMappings);

		// Checks if the bundle is considered as modified
		assertFalse(bundle.isDirty());

		assertEquals(true, bundle.getInclusionPattern().isGlobal());
		assertEquals(3, bundle.getInclusionPattern().getInclusionOrder());
		assertEquals("http://hostname/scripts/myBundle.js", bundle.getAlternateProductionURL());
		assertEquals("if lt IE 6", bundle.getExplorerConditionalExpression());
		assertEquals("myBundlePostProcessor1,myBundlePostProcessor2",
				((AbstractChainedResourceBundlePostProcessor) bundle.getBundlePostProcessor()).getId());
		assertEquals("myFilePostProcessor1,myFilePostProcessor2",
				((AbstractChainedResourceBundlePostProcessor) bundle.getUnitaryPostProcessor()).getId());

		assertEquals(expectedLocales, new HashSet<>(bundle.getVariantKeys()));

		assertEquals("N123456", bundle.getBundleDataHashCode(null));
		assertEquals("N123456", bundle.getBundleDataHashCode(""));
		assertEquals("123456", bundle.getBundleDataHashCode("fr"));
		assertEquals("789", bundle.getBundleDataHashCode("en_US"));
	}

	@Test
	public void testGetUpdatedLastModifiedStdResourceBundle() {

		FullMappingPropertiesBasedBundlesHandlerFactory factory = new FullMappingPropertiesBasedBundlesHandlerFactory(
				"js", rsHandler, generatorRegistry, chainFactory);

		Properties props = new Properties();

		DebugInclusion debugInclusion = DebugInclusion.ALWAYS;
		JoinableResourceBundle stdBundle = getStdBundle("myBundle", debugInclusion);
		when(generatorRegistry.getAvailableVariants(Matchers.anyString())).thenReturn(stdBundle.getVariants());
		JoinableResourceBundlePropertySerializer.serializeInProperties(stdBundle, "js", props);

		// Simulate modified resources
		when(rsHandler.getLastModified(filePathScript1)).thenReturn(updatedLastModified);
		when(rsHandler.getLastModified(filePathScript2)).thenReturn(updatedLastModified);
		when(rsHandler.getLastModified(filePathScript)).thenReturn(updatedLastModified);

		List<JoinableResourceBundle> resourcesBundles = factory.getResourceBundles(props);
		assertEquals(1, resourcesBundles.size());

		JoinableResourceBundle bundle = (JoinableResourceBundle) resourcesBundles.get(0);

		assertEquals("/bundle/myBundle.js", bundle.getId());
		assertFalse(bundle.isComposite());

		assertEquals(debugInclusion, bundle.getInclusionPattern().getDebugInclusion());
		Set<BundlePath> expectedMappings = new HashSet<>(
				asBundlePathList("/bundle/content/script1.js", "/bundle/content/script2.js", "/bundle/myScript.js"));
		if (debugInclusion.equals(DebugInclusion.ONLY)) {
			assertEquals(expectedMappings, new HashSet<>(bundle.getItemDebugPathList()));
		} else {
			assertEquals(expectedMappings, new HashSet<>(bundle.getItemPathList()));
		}

		// Check bundle file mapping
		Set<FilePathMapping> filePathMappings = new HashSet<>(bundle.getFilePathMappings());
		Set<FilePathMapping> expectedFilePathMappings = new HashSet<>();
		expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript1, updatedLastModified));
		expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript2, updatedLastModified));
		expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript, updatedLastModified));
		assertEquals(expectedFilePathMappings, filePathMappings);

		// Checks if the bundle is considered as modified
		assertTrue(bundle.isDirty());

		assertEquals(true, bundle.getInclusionPattern().isGlobal());
		assertEquals(3, bundle.getInclusionPattern().getInclusionOrder());
		assertEquals("http://hostname/scripts/myBundle.js", bundle.getAlternateProductionURL());
		assertEquals("if lt IE 6", bundle.getExplorerConditionalExpression());
		assertEquals("myBundlePostProcessor1,myBundlePostProcessor2",
				((AbstractChainedResourceBundlePostProcessor) bundle.getBundlePostProcessor()).getId());
		assertEquals("myFilePostProcessor1,myFilePostProcessor2",
				((AbstractChainedResourceBundlePostProcessor) bundle.getUnitaryPostProcessor()).getId());

		Set<String> expectedLocales = new HashSet<>(Arrays.asList("", "fr", "en_US"));
		assertEquals(expectedLocales, new HashSet<>(bundle.getVariantKeys()));

		assertEquals("N123456", bundle.getBundleDataHashCode(null));
		assertEquals("N123456", bundle.getBundleDataHashCode(""));
		assertEquals("123456", bundle.getBundleDataHashCode("fr"));
		assertEquals("789", bundle.getBundleDataHashCode("en_US"));
	}

	@Test
	public void testGetAddedFilePathStdResourceBundle() {

		FullMappingPropertiesBasedBundlesHandlerFactory factory = new FullMappingPropertiesBasedBundlesHandlerFactory(
				"js", rsHandler, generatorRegistry, chainFactory);

		Properties props = new Properties();

		DebugInclusion debugInclusion = DebugInclusion.ALWAYS;
		JoinableResourceBundle stdBundle = getStdBundle("myBundle", debugInclusion);
		when(generatorRegistry.getAvailableVariants(Matchers.anyString())).thenReturn(stdBundle.getVariants());
		JoinableResourceBundlePropertySerializer.serializeInProperties(stdBundle, "js", props);

		// Simulate modified path mapping
		when(rsHandler.getFilePath("/bundle/content/script3.js")).thenReturn(filePathScript3);
		when(rsHandler.getLastModified(filePathScript3)).thenReturn(script3LastModified);
		when(rsHandler.getResourceNames(Matchers.anyString()))
				.thenReturn(new HashSet<>(Arrays.asList("script1.js", "script2.js", "script3.js")));

		List<JoinableResourceBundle> resourcesBundles = factory.getResourceBundles(props);
		assertEquals(1, resourcesBundles.size());

		JoinableResourceBundle bundle = (JoinableResourceBundle) resourcesBundles.get(0);

		assertEquals("/bundle/myBundle.js", bundle.getId());
		assertFalse(bundle.isComposite());

		assertEquals(debugInclusion, bundle.getInclusionPattern().getDebugInclusion());
		Set<BundlePath> expectedMappings = new HashSet<>(asBundlePathList("/bundle/content/script1.js",
				"/bundle/content/script2.js", "/bundle/content/script3.js", "/bundle/myScript.js"));
		if (debugInclusion.equals(DebugInclusion.ONLY)) {
			assertEquals(expectedMappings, new HashSet<>(bundle.getItemDebugPathList()));
		} else {
			assertEquals(expectedMappings, new HashSet<>(bundle.getItemPathList()));
		}

		// Check bundle file mapping
		Set<FilePathMapping> filePathMappings = new HashSet<>(bundle.getFilePathMappings());
		Set<FilePathMapping> expectedFilePathMappings = new HashSet<>();
		expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript1, script1LastModified));
		expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript2, script2LastModified));
		expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript3, script3LastModified));
		expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript, scriptLastModified));
		assertEquals(expectedFilePathMappings, filePathMappings);

		// Checks if the bundle is considered as modified
		assertTrue(bundle.isDirty());

		assertEquals(true, bundle.getInclusionPattern().isGlobal());
		assertEquals(3, bundle.getInclusionPattern().getInclusionOrder());
		assertEquals("http://hostname/scripts/myBundle.js", bundle.getAlternateProductionURL());
		assertEquals("if lt IE 6", bundle.getExplorerConditionalExpression());
		assertEquals("myBundlePostProcessor1,myBundlePostProcessor2",
				((AbstractChainedResourceBundlePostProcessor) bundle.getBundlePostProcessor()).getId());
		assertEquals("myFilePostProcessor1,myFilePostProcessor2",
				((AbstractChainedResourceBundlePostProcessor) bundle.getUnitaryPostProcessor()).getId());

		Set<String> expectedLocales = new HashSet<>(Arrays.asList("", "fr", "en_US"));
		assertEquals(expectedLocales, new HashSet<>(bundle.getVariantKeys()));

		assertEquals("N123456", bundle.getBundleDataHashCode(null));
		assertEquals("N123456", bundle.getBundleDataHashCode(""));
		assertEquals("123456", bundle.getBundleDataHashCode("fr"));
		assertEquals("789", bundle.getBundleDataHashCode("en_US"));
	}

	@Test
	public void testGetRemovedFilePathStdResourceBundle() {

		FullMappingPropertiesBasedBundlesHandlerFactory factory = new FullMappingPropertiesBasedBundlesHandlerFactory(
				"js", rsHandler, generatorRegistry, chainFactory);

		Properties props = new Properties();

		DebugInclusion debugInclusion = DebugInclusion.ALWAYS;
		JoinableResourceBundle stdBundle = getStdBundle("myBundle", debugInclusion);
		when(generatorRegistry.getAvailableVariants(Matchers.anyString())).thenReturn(stdBundle.getVariants());
		JoinableResourceBundlePropertySerializer.serializeInProperties(stdBundle, "js", props);

		// Simulate modified path mapping
		when(rsHandler.getResourceNames(Matchers.anyString())).thenReturn(new HashSet<>(Arrays.asList("script1.js")));

		List<JoinableResourceBundle> resourcesBundles = factory.getResourceBundles(props);
		assertEquals(1, resourcesBundles.size());

		JoinableResourceBundle bundle = (JoinableResourceBundle) resourcesBundles.get(0);

		assertEquals("/bundle/myBundle.js", bundle.getId());
		assertFalse(bundle.isComposite());

		assertEquals(debugInclusion, bundle.getInclusionPattern().getDebugInclusion());
		Set<BundlePath> expectedMappings = new HashSet<>(
				asBundlePathList("/bundle/content/script1.js", "/bundle/myScript.js"));
		if (debugInclusion.equals(DebugInclusion.ONLY)) {
			assertEquals(expectedMappings, new HashSet<>(bundle.getItemDebugPathList()));
		} else {
			assertEquals(expectedMappings, new HashSet<>(bundle.getItemPathList()));
		}

		// Check bundle file mapping
		Set<FilePathMapping> filePathMappings = new HashSet<>(bundle.getFilePathMappings());
		Set<FilePathMapping> expectedFilePathMappings = new HashSet<>();
		expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript1, script1LastModified));
		expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript, scriptLastModified));
		assertEquals(expectedFilePathMappings, filePathMappings);

		// Checks if the bundle is considered as modified
		assertTrue(bundle.isDirty());

		assertEquals(true, bundle.getInclusionPattern().isGlobal());
		assertEquals(3, bundle.getInclusionPattern().getInclusionOrder());
		assertEquals("http://hostname/scripts/myBundle.js", bundle.getAlternateProductionURL());
		assertEquals("if lt IE 6", bundle.getExplorerConditionalExpression());
		assertEquals("myBundlePostProcessor1,myBundlePostProcessor2",
				((AbstractChainedResourceBundlePostProcessor) bundle.getBundlePostProcessor()).getId());
		assertEquals("myFilePostProcessor1,myFilePostProcessor2",
				((AbstractChainedResourceBundlePostProcessor) bundle.getUnitaryPostProcessor()).getId());

		Set<String> expectedLocales = new HashSet<>(Arrays.asList("", "fr", "en_US"));
		assertEquals(expectedLocales, new HashSet<>(bundle.getVariantKeys()));

		assertEquals("N123456", bundle.getBundleDataHashCode(null));
		assertEquals("N123456", bundle.getBundleDataHashCode(""));
		assertEquals("123456", bundle.getBundleDataHashCode("fr"));
		assertEquals("789", bundle.getBundleDataHashCode("en_US"));
	}

	@Test
	public void testGetUpdatedFilePathStdResourceBundle() {

		FullMappingPropertiesBasedBundlesHandlerFactory factory = new FullMappingPropertiesBasedBundlesHandlerFactory(
				"js", rsHandler, generatorRegistry, chainFactory);

		Properties props = new Properties();

		DebugInclusion debugInclusion = DebugInclusion.ALWAYS;
		JoinableResourceBundle stdBundle = getStdBundle("myBundle", debugInclusion);
		when(generatorRegistry.getAvailableVariants(Matchers.anyString())).thenReturn(stdBundle.getVariants());
		JoinableResourceBundlePropertySerializer.serializeInProperties(stdBundle, "js", props);

		// Simulate modified path mapping
		when(rsHandler.getResourceNames(Matchers.anyString()))
				.thenReturn(new HashSet<>(Arrays.asList("script1.js", "script3.js")));
		when(rsHandler.getFilePath("/bundle/content/script3.js")).thenReturn(filePathScript3);
		when(rsHandler.getLastModified(filePathScript3)).thenReturn(script3LastModified);

		List<JoinableResourceBundle> resourcesBundles = factory.getResourceBundles(props);
		assertEquals(1, resourcesBundles.size());

		JoinableResourceBundle bundle = (JoinableResourceBundle) resourcesBundles.get(0);

		assertEquals("/bundle/myBundle.js", bundle.getId());
		assertFalse(bundle.isComposite());

		assertEquals(debugInclusion, bundle.getInclusionPattern().getDebugInclusion());
		Set<BundlePath> expectedMappings = new HashSet<>(
				asBundlePathList("/bundle/content/script1.js", "/bundle/content/script3.js", "/bundle/myScript.js"));
		if (debugInclusion.equals(DebugInclusion.ONLY)) {
			assertEquals(expectedMappings, new HashSet<>(bundle.getItemDebugPathList()));
		} else {
			assertEquals(expectedMappings, new HashSet<>(bundle.getItemPathList()));
		}

		// Check bundle file mapping
		Set<FilePathMapping> filePathMappings = new HashSet<>(bundle.getFilePathMappings());
		Set<FilePathMapping> expectedFilePathMappings = new HashSet<>();
		expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript1, script1LastModified));
		expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript3, script3LastModified));
		expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript, scriptLastModified));
		assertEquals(expectedFilePathMappings, filePathMappings);

		// Checks if the bundle is considered as modified
		assertTrue(bundle.isDirty());

		assertEquals(true, bundle.getInclusionPattern().isGlobal());
		assertEquals(3, bundle.getInclusionPattern().getInclusionOrder());
		assertEquals("http://hostname/scripts/myBundle.js", bundle.getAlternateProductionURL());
		assertEquals("if lt IE 6", bundle.getExplorerConditionalExpression());
		assertEquals("myBundlePostProcessor1,myBundlePostProcessor2",
				((AbstractChainedResourceBundlePostProcessor) bundle.getBundlePostProcessor()).getId());
		assertEquals("myFilePostProcessor1,myFilePostProcessor2",
				((AbstractChainedResourceBundlePostProcessor) bundle.getUnitaryPostProcessor()).getId());

		Set<String> expectedLocales = new HashSet<>(Arrays.asList("", "fr", "en_US"));
		assertEquals(expectedLocales, new HashSet<>(bundle.getVariantKeys()));

		assertEquals("N123456", bundle.getBundleDataHashCode(null));
		assertEquals("N123456", bundle.getBundleDataHashCode(""));
		assertEquals("123456", bundle.getBundleDataHashCode("fr"));
		assertEquals("789", bundle.getBundleDataHashCode("en_US"));
	}

	@Test
	public void testGetUpdatedVariantStdResourceBundle() {

		FullMappingPropertiesBasedBundlesHandlerFactory factory = new FullMappingPropertiesBasedBundlesHandlerFactory(
				"js", rsHandler, generatorRegistry, chainFactory);

		Properties props = new Properties();

		DebugInclusion debugInclusion = DebugInclusion.ALWAYS;
		JoinableResourceBundle stdBundle = getStdBundle("myBundle", debugInclusion);
		Map<String, VariantSet> variants = new HashMap<>();
		variants.put(JawrConstant.LOCALE_VARIANT_TYPE,
				new VariantSet(JawrConstant.LOCALE_VARIANT_TYPE, "", Arrays.asList("", "fr", "en_US", "es_ES")));

		when(generatorRegistry.getAvailableVariants(Matchers.anyString())).thenReturn(variants);
		JoinableResourceBundlePropertySerializer.serializeInProperties(stdBundle, "js", props);

		// Simulate modified path mapping
		when(rsHandler.getResourceNames(Matchers.anyString()))
				.thenReturn(new HashSet<>(Arrays.asList("script1.js", "script3.js")));
		when(rsHandler.getFilePath("/bundle/content/script3.js")).thenReturn(filePathScript3);
		when(rsHandler.getLastModified(filePathScript3)).thenReturn(script3LastModified);

		List<JoinableResourceBundle> resourcesBundles = factory.getResourceBundles(props);
		assertEquals(1, resourcesBundles.size());

		JoinableResourceBundle bundle = (JoinableResourceBundle) resourcesBundles.get(0);

		assertEquals("/bundle/myBundle.js", bundle.getId());
		assertFalse(bundle.isComposite());

		assertEquals(debugInclusion, bundle.getInclusionPattern().getDebugInclusion());
		Set<BundlePath> expectedMappings = new HashSet<>(
				asBundlePathList("/bundle/content/script1.js", "/bundle/content/script3.js", "/bundle/myScript.js"));
		if (debugInclusion.equals(DebugInclusion.ONLY)) {
			assertEquals(expectedMappings, new HashSet<>(bundle.getItemDebugPathList()));
		} else {
			assertEquals(expectedMappings, new HashSet<>(bundle.getItemPathList()));
		}

		// Check bundle file mapping
		Set<FilePathMapping> filePathMappings = new HashSet<>(bundle.getFilePathMappings());
		Set<FilePathMapping> expectedFilePathMappings = new HashSet<>();
		expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript1, script1LastModified));
		expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript3, script3LastModified));
		expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript, scriptLastModified));
		assertEquals(expectedFilePathMappings, filePathMappings);

		// Checks if the bundle is considered as modified
		assertTrue(bundle.isDirty());

		assertEquals(true, bundle.getInclusionPattern().isGlobal());
		assertEquals(3, bundle.getInclusionPattern().getInclusionOrder());
		assertEquals("http://hostname/scripts/myBundle.js", bundle.getAlternateProductionURL());
		assertEquals("if lt IE 6", bundle.getExplorerConditionalExpression());
		assertEquals("myBundlePostProcessor1,myBundlePostProcessor2",
				((AbstractChainedResourceBundlePostProcessor) bundle.getBundlePostProcessor()).getId());
		assertEquals("myFilePostProcessor1,myFilePostProcessor2",
				((AbstractChainedResourceBundlePostProcessor) bundle.getUnitaryPostProcessor()).getId());

		Set<String> expectedLocales = new HashSet<>(Arrays.asList("", "fr", "en_US", "es_ES"));
		assertEquals(expectedLocales, new HashSet<>(bundle.getVariantKeys()));

		assertNull("N123456", bundle.getBundleDataHashCode(null));
		assertNull("N123456", bundle.getBundleDataHashCode(""));
		assertNull("123456", bundle.getBundleDataHashCode("fr"));
		assertNull("789", bundle.getBundleDataHashCode("en_US"));
	}

	@Test
	public void testGetCompositeResourceBundle() {

		FullMappingPropertiesBasedBundlesHandlerFactory factory = new FullMappingPropertiesBasedBundlesHandlerFactory(
				"js", rsHandler, generatorRegistry, chainFactory);

		Properties props = new Properties();

		DebugInclusion debugInclusion = DebugInclusion.ALWAYS;
		JoinableResourceBundle compositeBundle = getCompositeBundle("myBundle");
		when(generatorRegistry.getAvailableVariants(Matchers.anyString())).thenReturn(compositeBundle.getVariants());
		JoinableResourceBundlePropertySerializer.serializeInProperties(compositeBundle, "js", props);

		List<JoinableResourceBundle> resourcesBundles = factory.getResourceBundles(props);
		assertEquals(3, resourcesBundles.size());

		for (JoinableResourceBundle bundle : resourcesBundles) {

			if (bundle.getName().equals("myBundle")) {
				assertEquals("/bundle/myBundle.js", bundle.getId());
				assertTrue(bundle.isComposite());
				assertEquals(debugInclusion, bundle.getInclusionPattern().getDebugInclusion());
				Set<BundlePath> expectedMappings = new HashSet<>(asBundlePathList("/bundle/content/script1.js",
						"/bundle/content/script2.js", "/bundle/myScript.js"));
				assertEquals(expectedMappings, new HashSet<>(bundle.getItemPathList()));

				// Check bundle file mapping
				Set<FilePathMapping> filePathMappings = new HashSet<>(bundle.getFilePathMappings());
				Set<FilePathMapping> expectedFilePathMappings = new HashSet<>();
				expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript1, script1LastModified));
				expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript2, script2LastModified));
				expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript, scriptLastModified));
				assertEquals(expectedFilePathMappings, filePathMappings);

				// Checks if the bundle is considered as modified
				assertFalse(bundle.isDirty());

				assertEquals(false, bundle.getInclusionPattern().isGlobal());
				assertEquals(0, bundle.getInclusionPattern().getInclusionOrder());
				assertEquals("myBundlePostProcessor1,myBundlePostProcessor2",
						((AbstractChainedResourceBundlePostProcessor) bundle.getBundlePostProcessor()).getId());
				assertEquals("myFilePostProcessor1,myFilePostProcessor2",
						((AbstractChainedResourceBundlePostProcessor) bundle.getUnitaryPostProcessor()).getId());

				assertEquals("123456", bundle.getBundleDataHashCode(null));
			} else if (bundle.getName().equals("child1")) {

				assertEquals("/bundle/child1.js", bundle.getId());
				assertFalse(bundle.isComposite());
				assertEquals(debugInclusion, bundle.getInclusionPattern().getDebugInclusion());
				Set<BundlePath> expectedMappings = new HashSet<>(
						asBundlePathList("/bundle/content/script1.js", "/bundle/content/script2.js"));
				assertEquals(expectedMappings, new HashSet<>(bundle.getItemPathList()));

				// Check bundle file mapping
				Set<FilePathMapping> filePathMappings = new HashSet<>(bundle.getFilePathMappings());
				Set<FilePathMapping> expectedFilePathMappings = new HashSet<>();
				expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript1, script1LastModified));
				expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript2, script2LastModified));
				assertEquals(expectedFilePathMappings, filePathMappings);

				// Checks if the bundle is not considered as modified
				assertFalse(bundle.isDirty());

				assertEquals(false, bundle.getInclusionPattern().isGlobal());
				assertEquals(0, bundle.getInclusionPattern().getInclusionOrder());
				assertNull(bundle.getBundlePostProcessor());
				assertNull(bundle.getUnitaryPostProcessor());
				assertNull(bundle.getBundleDataHashCode(null));

			} else if (bundle.getName().equals("child2")) {

				assertEquals("/bundle/child2.js", bundle.getId());
				assertFalse(bundle.isComposite());
				assertEquals(debugInclusion, bundle.getInclusionPattern().getDebugInclusion());
				Set<BundlePath> expectedMappings = new HashSet<>(asBundlePathList("/bundle/myScript.js"));
				assertEquals(expectedMappings, new HashSet<>(bundle.getItemPathList()));

				// Check bundle file mapping
				Set<FilePathMapping> filePathMappings = new HashSet<>(bundle.getFilePathMappings());
				Set<FilePathMapping> expectedFilePathMappings = new HashSet<>();
				expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript, scriptLastModified));
				assertEquals(expectedFilePathMappings, filePathMappings);

				// Checks if the bundle is not considered as modified
				assertFalse(bundle.isDirty());

				assertEquals(false, bundle.getInclusionPattern().isGlobal());
				assertEquals(0, bundle.getInclusionPattern().getInclusionOrder());
				assertNull(bundle.getBundlePostProcessor());
				assertNull(bundle.getUnitaryPostProcessor());
				assertNull(bundle.getBundleDataHashCode(null));
			}
		}
	}

	@Test
	public void testGetModifiedCompositeResourceBundle() {

		FullMappingPropertiesBasedBundlesHandlerFactory factory = new FullMappingPropertiesBasedBundlesHandlerFactory(
				"js", rsHandler, generatorRegistry, chainFactory);

		Properties props = new Properties();

		DebugInclusion debugInclusion = DebugInclusion.ALWAYS;
		JoinableResourceBundle compositeBundle = getCompositeBundle("myBundle");
		when(generatorRegistry.getAvailableVariants(Matchers.anyString())).thenReturn(compositeBundle.getVariants());
		JoinableResourceBundlePropertySerializer.serializeInProperties(compositeBundle, "js", props);

		// Simulate modified resources
		when(rsHandler.getLastModified(filePathScript1)).thenReturn(updatedLastModified);
		when(rsHandler.getLastModified(filePathScript2)).thenReturn(updatedLastModified);
		when(rsHandler.getLastModified(filePathScript)).thenReturn(updatedLastModified);

		List<JoinableResourceBundle> resourcesBundles = factory.getResourceBundles(props);
		assertEquals(3, resourcesBundles.size());

		for (JoinableResourceBundle bundle : resourcesBundles) {

			if (bundle.getName().equals("myBundle")) {
				assertEquals("/bundle/myBundle.js", bundle.getId());
				assertTrue(bundle.isComposite());
				assertEquals(debugInclusion, bundle.getInclusionPattern().getDebugInclusion());
				Set<BundlePath> expectedMappings = new HashSet<>(asBundlePathList("/bundle/content/script1.js",
						"/bundle/content/script2.js", "/bundle/myScript.js"));
				assertEquals(expectedMappings, new HashSet<>(bundle.getItemPathList()));

				// Check bundle file mapping
				Set<FilePathMapping> filePathMappings = new HashSet<>(bundle.getFilePathMappings());
				Set<FilePathMapping> expectedFilePathMappings = new HashSet<>();
				expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript1, updatedLastModified));
				expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript2, updatedLastModified));
				expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript, updatedLastModified));
				assertEquals(expectedFilePathMappings, filePathMappings);

				// Checks if the bundle is considered as modified
				assertTrue(bundle.isDirty());

				assertEquals(false, bundle.getInclusionPattern().isGlobal());
				assertEquals(0, bundle.getInclusionPattern().getInclusionOrder());
				assertEquals("myBundlePostProcessor1,myBundlePostProcessor2",
						((AbstractChainedResourceBundlePostProcessor) bundle.getBundlePostProcessor()).getId());
				assertEquals("myFilePostProcessor1,myFilePostProcessor2",
						((AbstractChainedResourceBundlePostProcessor) bundle.getUnitaryPostProcessor()).getId());

				assertEquals("123456", bundle.getBundleDataHashCode(null));
			} else if (bundle.getName().equals("child1")) {

				assertEquals("/bundle/child1.js", bundle.getId());
				assertFalse(bundle.isComposite());
				assertEquals(debugInclusion, bundle.getInclusionPattern().getDebugInclusion());
				Set<BundlePath> expectedMappings = new HashSet<>(
						asBundlePathList("/bundle/content/script1.js", "/bundle/content/script2.js"));
				assertEquals(expectedMappings, new HashSet<>(bundle.getItemPathList()));

				// Check bundle file mapping
				Set<FilePathMapping> filePathMappings = new HashSet<>(bundle.getFilePathMappings());
				Set<FilePathMapping> expectedFilePathMappings = new HashSet<>();
				expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript1, updatedLastModified));
				expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript2, updatedLastModified));
				assertEquals(expectedFilePathMappings, filePathMappings);

				// Checks if the bundle is not considered as modified
				assertTrue(bundle.isDirty());

				assertEquals(false, bundle.getInclusionPattern().isGlobal());
				assertEquals(0, bundle.getInclusionPattern().getInclusionOrder());
				assertNull(bundle.getBundlePostProcessor());
				assertNull(bundle.getUnitaryPostProcessor());
				assertNull(bundle.getBundleDataHashCode(null));

			} else if (bundle.getName().equals("child2")) {

				assertEquals("/bundle/child2.js", bundle.getId());
				assertFalse(bundle.isComposite());
				assertEquals(debugInclusion, bundle.getInclusionPattern().getDebugInclusion());
				Set<BundlePath> expectedMappings = new HashSet<>(asBundlePathList("/bundle/myScript.js"));
				assertEquals(expectedMappings, new HashSet<>(bundle.getItemPathList()));

				// Check bundle file mapping
				Set<FilePathMapping> filePathMappings = new HashSet<>(bundle.getFilePathMappings());
				Set<FilePathMapping> expectedFilePathMappings = new HashSet<>();
				expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript, updatedLastModified));
				assertEquals(expectedFilePathMappings, filePathMappings);

				// Checks if the bundle is not considered as modified
				assertTrue(bundle.isDirty());

				assertEquals(false, bundle.getInclusionPattern().isGlobal());
				assertEquals(0, bundle.getInclusionPattern().getInclusionOrder());
				assertNull(bundle.getBundlePostProcessor());
				assertNull(bundle.getUnitaryPostProcessor());
				assertNull(bundle.getBundleDataHashCode(null));
			}
		}
	}

	@Test
	public void testGetResourceBundlesWithDependencies() {

		testGetResourceBundlesWithDependencies(DebugInclusion.ALWAYS);
		testGetResourceBundlesWithDependencies(DebugInclusion.ONLY);
		testGetResourceBundlesWithDependencies(DebugInclusion.NEVER);
	}

	protected void testGetResourceBundlesWithDependencies(DebugInclusion inclusion) {

		FullMappingPropertiesBasedBundlesHandlerFactory factory = new FullMappingPropertiesBasedBundlesHandlerFactory(
				"js", rsHandler, generatorRegistry, chainFactory);

		Properties props = new Properties();

		List<JoinableResourceBundle> bundleWithDependencies = getBundleWithDependencies(inclusion);
		for (JoinableResourceBundle aBundle : bundleWithDependencies) {
			JoinableResourceBundlePropertySerializer.serializeInProperties(aBundle, "js", props);
			when(generatorRegistry.getAvailableVariants(Matchers.anyString())).thenReturn(aBundle.getVariants());
		}

		List<JoinableResourceBundle> resourcesBundles = factory.getResourceBundles(props);
		assertEquals(3, resourcesBundles.size());

		for (JoinableResourceBundle bundle : resourcesBundles) {
			assertEquals(inclusion, bundle.getInclusionPattern().getDebugInclusion());

			Set<BundlePath> expectedMappings = new HashSet<>(asBundlePathList("/bundle/content/script1.js",
					"/bundle/content/script2.js", "/bundle/myScript.js"));
			if (inclusion.equals(DebugInclusion.ONLY)) {
				assertEquals(expectedMappings, new HashSet<>(bundle.getItemDebugPathList()));
			} else {
				assertEquals(expectedMappings, new HashSet<>(bundle.getItemPathList()));
			}

			// Check bundle file mapping
			Set<FilePathMapping> filePathMappings = new HashSet<>(bundle.getFilePathMappings());
			Set<FilePathMapping> expectedFilePathMappings = new HashSet<>();
			expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript1, script1LastModified));
			expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript2, script2LastModified));
			expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript, scriptLastModified));
			assertEquals(expectedFilePathMappings, filePathMappings);

			// Checks if the bundle is considered as modified
			assertFalse(bundle.isDirty());

			assertEquals(true, bundle.getInclusionPattern().isGlobal());
			assertEquals(3, bundle.getInclusionPattern().getInclusionOrder());
			assertEquals("if lt IE 6", bundle.getExplorerConditionalExpression());
			assertEquals("myBundlePostProcessor1,myBundlePostProcessor2",
					((AbstractChainedResourceBundlePostProcessor) bundle.getBundlePostProcessor()).getId());
			assertEquals("myFilePostProcessor1,myFilePostProcessor2",
					((AbstractChainedResourceBundlePostProcessor) bundle.getUnitaryPostProcessor()).getId());

			if (bundle.getName().equals("myBundle")) {
				assertEquals(2, bundle.getDependencies().size());
				assertEquals("myBundle1", ((JoinableResourceBundle) bundle.getDependencies().get(0)).getName());
				assertEquals("myBundle2", ((JoinableResourceBundle) bundle.getDependencies().get(1)).getName());
			} else {
				assertNull(bundle.getDependencies());
			}

			Set<String> expectedLocales = new HashSet<>(Arrays.asList("", "fr", "en_US"));
			assertEquals(expectedLocales, new HashSet<>(bundle.getVariantKeys()));

			assertEquals("N123456", bundle.getBundleDataHashCode(null));
			assertEquals("123456", bundle.getBundleDataHashCode("fr"));
			assertEquals("789", bundle.getBundleDataHashCode("en_US"));
		}
	}

	@Test
	public void testGetVariantResourceBundles() {

		FullMappingPropertiesBasedBundlesHandlerFactory factory = new FullMappingPropertiesBasedBundlesHandlerFactory(
				"js", rsHandler, generatorRegistry, chainFactory);

		Properties props = new Properties();

		JoinableResourceBundle stdBundle = getBundleWithVariants(DebugInclusion.ALWAYS);
		when(generatorRegistry.getAvailableVariants(Matchers.anyString())).thenReturn(stdBundle.getVariants());
		JoinableResourceBundlePropertySerializer.serializeInProperties(stdBundle, "js", props);

		List<JoinableResourceBundle> resourcesBundles = factory.getResourceBundles(props);
		assertEquals(1, resourcesBundles.size());

		JoinableResourceBundle bundle = (JoinableResourceBundle) resourcesBundles.get(0);

		assertEquals("/bundle/myBundle.js", bundle.getId());

		Set<BundlePath> expectedMappings = new HashSet<>(
				asBundlePathList("/bundle/content/script1.js", "/bundle/content/script2.js", "/bundle/myScript.js"));
		assertEquals(expectedMappings, new HashSet<>(bundle.getItemPathList()));

		// Check bundle file mapping
		Set<FilePathMapping> filePathMappings = new HashSet<>(bundle.getFilePathMappings());
		Set<FilePathMapping> expectedFilePathMappings = new HashSet<>();
		expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript1, script1LastModified));
		expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript2, script2LastModified));
		expectedFilePathMappings.add(new FilePathMapping(bundle, filePathScript, scriptLastModified));
		assertEquals(expectedFilePathMappings, filePathMappings);

		// Checks if the bundle is considered as modified
		assertFalse(bundle.isDirty());

		assertEquals(true, bundle.getInclusionPattern().isGlobal());
		assertEquals(3, bundle.getInclusionPattern().getInclusionOrder());
		assertEquals(DebugInclusion.ALWAYS, bundle.getInclusionPattern().getDebugInclusion());
		assertEquals("http://hostname/scripts/myBundle.js", bundle.getAlternateProductionURL());
		assertEquals("if lt IE 6", bundle.getExplorerConditionalExpression());
		assertEquals("myBundlePostProcessor1,myBundlePostProcessor2",
				((AbstractChainedResourceBundlePostProcessor) bundle.getBundlePostProcessor()).getId());
		assertEquals("myFilePostProcessor1,myFilePostProcessor2",
				((AbstractChainedResourceBundlePostProcessor) bundle.getUnitaryPostProcessor()).getId());

		Set<String> expectedVariants = new HashSet<>(
				Arrays.asList("@summer", "@winter", "fr@summer", "fr@winter", "en_US@summer", "en_US@winter"));
		assertEquals(expectedVariants, new HashSet<>(bundle.getVariantKeys()));

		assertEquals("N123456", bundle.getBundleDataHashCode(null));
		assertEquals("178456", bundle.getBundleDataHashCode("@summer"));
		assertEquals("418451", bundle.getBundleDataHashCode("@winter"));
		assertEquals("123456", bundle.getBundleDataHashCode("fr@summer"));
		assertEquals("789", bundle.getBundleDataHashCode("en_US@summer"));
		assertEquals("123456", bundle.getBundleDataHashCode("fr@winter"));
		assertEquals("789", bundle.getBundleDataHashCode("en_US@winter"));
	}

	private JoinableResourceBundle getGlobalBundle() {
		String bundleName = "myGlobalBundle";
		List<String> mappings = Arrays.asList("/bundle/content/**", "/bundle/myScript.js");

		InclusionPattern inclusionPattern = new InclusionPattern(true, 0);
		GeneratorRegistry generatorRegistry = new GeneratorRegistry();
		JoinableResourceBundle bundle = new JoinableResourceBundleImpl("/bundle/myGlobalBundle.js", bundleName, null,
				"js", inclusionPattern, rsHandler, generatorRegistry);
		bundle.setMappings(mappings);
		bundle.setBundleDataHashCode(null, "123456");

		return bundle;
	}

	private JoinableResourceBundleImpl getStdBundle(String bundleName, DebugInclusion inclusion) {
		List<String> mappings = Arrays.asList("/bundle/content/**", "/bundle/myScript.js");

		InclusionPattern inclusionPattern = new InclusionPattern(true, 3, inclusion);

		GeneratorRegistry generatorRegistry = Mockito.mock(GeneratorRegistry.class);
		JoinableResourceBundleImpl bundle = new JoinableResourceBundleImpl("/bundle/" + bundleName + ".js", bundleName,
				null, "js", inclusionPattern, rsHandler, generatorRegistry);
		bundle.setMappings(mappings);
		bundle.setAlternateProductionURL("http://hostname/scripts/" + bundleName + ".js");
		bundle.setExplorerConditionalExpression("if lt IE 6");

		Map<String, VariantSet> variants = new HashMap<>();
		variants.put(JawrConstant.LOCALE_VARIANT_TYPE,
				new VariantSet(JawrConstant.LOCALE_VARIANT_TYPE, "", Arrays.asList("", "fr", "en_US")));
		when(generatorRegistry.getAvailableVariants(Matchers.anyString())).thenReturn(variants);
		bundle.setVariants(variants);
		bundle.setBundleDataHashCode(null, "N123456");
		bundle.setBundleDataHashCode("", "N123456");
		bundle.setBundleDataHashCode("fr", "123456");
		bundle.setBundleDataHashCode("en_US", "789");

		bundle.setBundlePostProcessor(bundleProcessor);
		bundle.setUnitaryPostProcessor(fileProcessor);

		return bundle;
	}

	private JoinableResourceBundle getCompositeBundle(String bundleName) {

		GeneratorRegistry generatorRegistry = Mockito.mock(GeneratorRegistry.class);
		InclusionPattern inclusionPattern = new InclusionPattern(false, 3, DebugInclusion.ALWAYS);

		List<JoinableResourceBundle> nestedBundles = new ArrayList<>();
		JoinableResourceBundle b1 = new JoinableResourceBundleImpl("/bundle/child1.js", "child1", null, "js",
				inclusionPattern, rsHandler, generatorRegistry);
		b1.setMappings(Arrays.asList("/bundle/content/**"));
		nestedBundles.add(b1);

		JoinableResourceBundle b2 = new JoinableResourceBundleImpl("/bundle/child2.js", "child2", null, "js",
				inclusionPattern, rsHandler, generatorRegistry);
		b2.setMappings(Arrays.asList("/bundle/myScript.js"));
		nestedBundles.add(b2);

		JoinableResourceBundle bundle = new CompositeResourceBundle("/bundle/myBundle.js", bundleName, nestedBundles,
				inclusionPattern, rsHandler, null, "js", generatorRegistry);

		bundle.setBundleDataHashCode(null, "123456");

		bundle.setBundlePostProcessor(bundleProcessor);
		bundle.setUnitaryPostProcessor(fileProcessor);

		return bundle;
	}

	@Test
	public void testGetExternalResourceBundles() {

		GeneratorRegistry generatorRegistry = new GeneratorRegistry();
		FullMappingPropertiesBasedBundlesHandlerFactory factory = new FullMappingPropertiesBasedBundlesHandlerFactory(
				"js", rsHandler, generatorRegistry, chainFactory);

		Properties props = new Properties();

		String bundleName = "myBundle";
		String resourceType = "js";

		InclusionPattern inclusionPattern = new InclusionPattern(false, 3, DebugInclusion.ALWAYS);
		JoinableResourceBundleImpl bundle = new JoinableResourceBundleImpl("/bundle/myBundle.js", bundleName, null,
				"js", inclusionPattern, rsHandler, generatorRegistry);
		bundle.setAlternateProductionURL("http://hostname/scripts/myBundle.min.js");
		bundle.setDebugURL("http://hostname/scripts/myBundle.js");

		JoinableResourceBundlePropertySerializer.serializeInProperties(bundle, "js", props);
		List<JoinableResourceBundle> resourcesBundles = factory.getResourceBundles(props);
		assertEquals(1, resourcesBundles.size());

		bundle = (JoinableResourceBundleImpl) resourcesBundles.get(0);

		PropertiesConfigHelper helper = new PropertiesConfigHelper(props, resourceType);

		assertEquals("/bundle/myBundle.js",
				helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ID));
		assertEquals("http://hostname/scripts/myBundle.min.js", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PRODUCTION_ALT_URL));

		assertEquals("http://hostname/scripts/myBundle.js",
				helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUG_URL));
		assertEquals("3",
				helper.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ORDER));
		assertEquals("false", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGNEVER, "false"));
		assertEquals("false", helper.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGONLY, "false"));

	}

	private List<BundlePath> asBundlePathList(String... paths) {
		List<BundlePath> result = new ArrayList<>();
		for (String path : paths) {
			result.add(new BundlePath(null, path));
		}

		return result;
	}

	private List<JoinableResourceBundle> getBundleWithDependencies(DebugInclusion inclusion) {

		List<JoinableResourceBundle> bundles = new ArrayList<>();
		JoinableResourceBundleImpl bundle = getStdBundle("myBundle", inclusion);
		JoinableResourceBundleImpl bundle1 = getStdBundle("myBundle1", inclusion);
		JoinableResourceBundleImpl bundle2 = getStdBundle("myBundle2", inclusion);

		bundle.setDependencies(Arrays.asList(new JoinableResourceBundle[] { bundle1, bundle2 }));
		bundles.add(bundle);
		bundles.add(bundle1);
		bundles.add(bundle2);
		return bundles;
	}

	private JoinableResourceBundleImpl getBundleWithVariants(DebugInclusion inclusion) {

		GeneratorRegistry generatorRegistry = Mockito.mock(GeneratorRegistry.class);
		String bundleName = "myBundle";
		List<String> mappings = Arrays.asList("/bundle/content/**", "/bundle/myScript.js");

		InclusionPattern inclusionPattern = new InclusionPattern(true, 3, inclusion);
		JoinableResourceBundleImpl bundle = new JoinableResourceBundleImpl("/bundle/myBundle.js", bundleName, null,
				"js", inclusionPattern, rsHandler, generatorRegistry);
		bundle.setMappings(mappings);
		bundle.setAlternateProductionURL("http://hostname/scripts/myBundle.js");
		bundle.setExplorerConditionalExpression("if lt IE 6");

		Map<String, VariantSet> variants = new HashMap<>();
		variants.put(JawrConstant.SKIN_VARIANT_TYPE,
				new VariantSet(JawrConstant.SKIN_VARIANT_TYPE, "summer", Arrays.asList("summer", "winter")));
		variants.put(JawrConstant.LOCALE_VARIANT_TYPE,
				new VariantSet(JawrConstant.LOCALE_VARIANT_TYPE, "", Arrays.asList("", "fr", "en_US")));

		when(generatorRegistry.getAvailableVariants(Matchers.anyString())).thenReturn(variants);
		bundle.setVariants(variants);
		bundle.setBundleDataHashCode(null, "N123456");
		bundle.setBundleDataHashCode("@summer", "178456");
		bundle.setBundleDataHashCode("@winter", "418451");
		bundle.setBundleDataHashCode("fr@summer", "123456");
		bundle.setBundleDataHashCode("en_US@summer", "789");
		bundle.setBundleDataHashCode("fr@winter", "123456");
		bundle.setBundleDataHashCode("en_US@winter", "789");

		bundle.setBundlePostProcessor(bundleProcessor);
		bundle.setUnitaryPostProcessor(fileProcessor);

		return bundle;
	}

}
