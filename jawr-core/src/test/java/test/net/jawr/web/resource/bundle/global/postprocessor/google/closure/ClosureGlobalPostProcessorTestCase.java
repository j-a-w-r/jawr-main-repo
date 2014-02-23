package test.net.jawr.web.resource.bundle.global.postprocessor.google.closure;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import net.jawr.web.DebugMode;
import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.InclusionPattern;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.factory.global.postprocessor.GlobalPostProcessingContext;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.global.postprocessor.google.closure.ClosureGlobalPostProcessor;
import net.jawr.web.resource.bundle.handler.ClientSideHandlerGenerator;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.iterator.ConditionalCommentCallbackHandler;
import net.jawr.web.resource.bundle.iterator.ResourceBundlePathsIterator;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

import org.junit.Before;
import org.junit.Test;

import test.net.jawr.web.FileUtils;
import test.net.jawr.web.servlet.mock.MockServletContext;

public class ClosureGlobalPostProcessorTestCase {

	private JoinableResourceBundle bundle01;
	private JoinableResourceBundle bundle02;
	private JoinableResourceBundle bundle03;
	private JoinableResourceBundle msgBundle;
	private JoinableResourceBundle variantBundle;
	private JawrConfig config;
	private GlobalPostProcessingContext ctx;
	private ClosureGlobalPostProcessor processor;
	private String srcDir;
	private String tempDir;
	private String destDir;
	
	@Before
	public void setUp() throws Exception {
		InclusionPattern inclusionPattern = new InclusionPattern(false, 0);
		
		// Bundle path (full url would be: /servletMapping/prefix/css/bundle.css
		bundle01 = mock(JoinableResourceBundle.class);
		when(bundle01.getVariants()).thenReturn(new HashMap<String, VariantSet>());
		when(bundle01.getId()).thenReturn("/myBundle/bundle01.js");
		when(bundle01.getName()).thenReturn("bundle01");
		when(bundle01.getInclusionPattern()).thenReturn(inclusionPattern);
		
		bundle02 = mock(JoinableResourceBundle.class);
		when(bundle02.getVariants()).thenReturn(new HashMap<String, VariantSet>());
		when(bundle02.getId()).thenReturn("/myBundle/bundle02.js");
		when(bundle02.getName()).thenReturn("bundle02");
		when(bundle02.getInclusionPattern()).thenReturn(inclusionPattern);
			
		bundle03 = mock(JoinableResourceBundle.class);
		when(bundle03.getVariants()).thenReturn(new HashMap<String, VariantSet>());
		when(bundle03.getId()).thenReturn("/myBundle/bundle03.js");
		when(bundle03.getName()).thenReturn("bundle03");
		when(bundle03.getInclusionPattern()).thenReturn(inclusionPattern);
		
		msgBundle = mock(JoinableResourceBundle.class);
		Map<String, VariantSet> variantMap = new HashMap<String, VariantSet>();
		VariantSet variantSet = new VariantSet("locale", "", Arrays.asList("", "fr", "en"));
		variantMap.put("locale", variantSet);
		when(msgBundle.getVariants()).thenReturn(variantMap);
		when(msgBundle.getId()).thenReturn("/myBundle/msgBundle.js");
		when(msgBundle.getName()).thenReturn("msgBundle");
		when(msgBundle.getInclusionPattern()).thenReturn(inclusionPattern);
		
		variantBundle = mock(JoinableResourceBundle.class);
		variantMap = new HashMap<String, VariantSet>();
		variantSet = new VariantSet("locale", "", Arrays.asList("", "fr"));
		variantMap.put("locale", variantSet);
		variantSet = new VariantSet("connectionType", "", Arrays.asList("", "ssl"));
		variantMap.put("connectionType", variantSet);
		when(variantBundle.getVariants()).thenReturn(variantMap);
		when(variantBundle.getId()).thenReturn("/myBundle/variantBundle.js");
		when(variantBundle.getName()).thenReturn("variantBundle");
		when(variantBundle.getInclusionPattern()).thenReturn(inclusionPattern);
		
		srcDir = FileUtils.getClasspathRootDir()+"/global/postprocessor/google/closure/bundle/";
		tempDir = FileUtils.getClasspathRootDir()+"/global/postprocessor/google/closure/temp/";
		destDir = FileUtils.getClasspathRootDir()+"/global/postprocessor/google/closure/result/";
		processor = new ClosureGlobalPostProcessor(srcDir, tempDir, destDir);
	}

	private void initProcessingContext(Properties props)
			throws Exception, ResourceNotFoundException {
		
		String bundleDirPath = FileUtils.getClasspathRootDir()+"/global/postprocessor/google/closure/bundle/";
		ResourceBundlesHandler rsBundlesHandler = getResourceBundlesHandler(bundleDirPath);
		
		config = new JawrConfig("js",props);
		ServletContext servletContext = new MockServletContext();
		servletContext.setAttribute(JawrConstant.JS_CONTEXT_ATTRIBUTE, rsBundlesHandler);
		config.setContext(servletContext);
		config.setCharsetName("UTF-8");		
		addGeneratorRegistryToConfig(config, "js");
		
		ResourceReaderHandler rsHandler = mock(ResourceReaderHandler.class);
		when(rsHandler.getWorkingDirectory()).thenReturn(FileUtils.getClasspathRootDir()+"/global/postprocessor/google/closure/");
		when(rsHandler.getResource("extern.js")).thenReturn(new StringReader(FileUtils.readClassPathFile("global/postprocessor/google/closure/externs/extern.js")));
		
		ctx = new GlobalPostProcessingContext(config, rsBundlesHandler, rsHandler, true);
	}

	private GeneratorRegistry addGeneratorRegistryToConfig(JawrConfig config, String type) {
		GeneratorRegistry generatorRegistry = new GeneratorRegistry(type);
		generatorRegistry.setConfig(config);
		config.setGeneratorRegistry(generatorRegistry);
		return generatorRegistry;
	}
	
	@Test
	public void testPostProcessingBasic() throws Exception {
		
		Properties props = new Properties();
		//props.put("jawr.js.closure.modules", "bundle01:bundle02");
		props.put("jawr.js.closure.externs", "extern.js");
		initProcessingContext(props);
		List<JoinableResourceBundle> bundles = new ArrayList<JoinableResourceBundle>();
		bundles.add(bundle01);
		bundles.add(bundle02);
		processor.processBundles(ctx, bundles);
		
		// Check bundles
		compareResult("bundle01");
		compareResult("bundle02");
		
	}

	@Test
	public void testPostProcessingBundleWithJawrAndClosureDependencies() throws ResourceNotFoundException, Exception{
		
		Properties props = new Properties();
		props.put("jawr.js.closure.modules", "bundle01:bundle02");
		props.put("jawr.js.closure.externs", "extern.js");
		initProcessingContext(props);
		
		when(bundle01.getDependencies()).thenReturn(Arrays.asList(bundle03));
		
		List<JoinableResourceBundle> bundles = new ArrayList<JoinableResourceBundle>();
		bundles.add(bundle01);
		bundles.add(bundle02);
		bundles.add(bundle03);
		processor.processBundles(ctx, bundles);
		
		// Check bundles
		compareResult("bundle01");
		compareResult("bundle02");
		compareResult("bundle03");
		
	}
	
	@Test
	public void testPostProcessingBundleWithVariants1() throws ResourceNotFoundException, Exception{
		
		Properties props = new Properties();
		props.put("jawr.js.closure.modules", "bundle01:bundle02,msgBundle");
		props.put("jawr.js.closure.externs", "extern.js");
		initProcessingContext(props);
		
		List<JoinableResourceBundle> bundles = new ArrayList<JoinableResourceBundle>();
		bundles.add(bundle01);
		bundles.add(bundle02);
		bundles.add(bundle03);
		bundles.add(msgBundle);
		processor.processBundles(ctx, bundles);
		
		// Check bundles
		compareResult("bundle01");
		compareResult("bundle02");
		compareResult("bundle03");
		compareResult("msgBundle");
		compareResult("msgBundle@en");
		compareResult("msgBundle@fr");
	}
	
	@Test
	public void testPostProcessingBundleWithVariants2() throws ResourceNotFoundException, Exception{
		
		Properties props = new Properties();
		props.put("jawr.js.closure.modules", "msgBundle:variantBundle");
		props.put("jawr.js.closure.externs", "extern.js");
		initProcessingContext(props);
		
		List<JoinableResourceBundle> bundles = new ArrayList<JoinableResourceBundle>();
		bundles.add(bundle01);
		bundles.add(bundle02);
		bundles.add(bundle03);
		bundles.add(msgBundle);
		bundles.add(variantBundle);
		processor.processBundles(ctx, bundles);
		
		// Check bundles
		compareResult("variantBundle@@");
		compareResult("variantBundle@@fr");
		compareResult("variantBundle@ssl@");
		compareResult("variantBundle@ssl@fr");
				
	}
	
	@Test
	public void testPostProcessingBundleWithAdvancedCompression() throws ResourceNotFoundException, Exception{
		
		Properties props = new Properties();
		props.put("jawr.js.closure.modules", "bundle01:bundle02,msgBundle");
		props.put("jawr.js.closure.externs", "extern.js");
		initProcessingContext(props);
		
		List<JoinableResourceBundle> bundles = new ArrayList<JoinableResourceBundle>();
		bundles.add(bundle01);
		bundles.add(bundle02);
		bundles.add(bundle03);
		bundles.add(msgBundle);
		processor.processBundles(ctx, bundles);
		
		// Check bundles
		compareResult("bundle01", false);
		compareResult("bundle02", false);
		compareResult("bundle03", false);
		compareResult("msgBundle", false);
		compareResult("msgBundle@en", false);
		compareResult("msgBundle@fr", false);
	}

	private void compareResult(String bundle) throws Exception {
		compareResult(bundle, true);
	}

	private void compareResult(String bundle, boolean whitespaceCompression) throws Exception {
		String expected = FileUtils.readClassPathFile("global/postprocessor/google/closure/expectedResult/"+bundle+"_"+(whitespaceCompression? "whitespace" :"advanced") + "_compression.js");
		String result = FileUtils.readFile(destDir+"myBundle/"+bundle+".js");
		assertEquals(expected, result);
	}
	
	private ResourceBundlesHandler getResourceBundlesHandler(final String bundleDirPath) {
		
		ResourceBundlesHandler bundlesHandler = new ResourceBundlesHandler() {
			
			//final Map<String, String> mapContent = mapData;
			public void writeBundleTo(String bundlePath, Writer writer)
					throws ResourceNotFoundException {
				
				String path = PathNormalizer.removeVariantPrefixFromPath(bundlePath);
				String content;
				try {
					content = FileUtils.readFile(bundleDirPath+path);
				} catch (Exception e1) {
					throw new ResourceNotFoundException(path);
				}
				
				try {
					writer.append(content);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
			public void streamBundleTo(String bundlePath, OutputStream out)
					throws ResourceNotFoundException {
				
			}
			
			public JoinableResourceBundle resolveBundleForPath(String path) {
				
				return null;
			}
			
			public boolean isGlobalResourceBundle(String resourceBundleId) {
				return false;
			}
			
			public void initAllBundles() {
				
			}
			
			public List<JoinableResourceBundle> getContextBundles() {
				return null;
			}
			
			public JawrConfig getConfig() {
				return config;
			}
			
			public ClientSideHandlerGenerator getClientSideHandler() {
				return null;
			}

			public ResourceBundlePathsIterator getGlobalResourceBundlePaths(
					ConditionalCommentCallbackHandler commentCallbackHandler,
					Map<String, String> variants) {
				return null;
			}

			public ResourceBundlePathsIterator getGlobalResourceBundlePaths(
					DebugMode debugMode,
					ConditionalCommentCallbackHandler commentCallbackHandler,
					Map<String, String> variants) {
				return null;
			}

			public ResourceBundlePathsIterator getGlobalResourceBundlePaths(
					String bundlePath,
					ConditionalCommentCallbackHandler commentCallbackHandler,
					Map<String, String> variants) {
				return null;
			}

			public ResourceBundlePathsIterator getBundlePaths(String bundleId,
					ConditionalCommentCallbackHandler commentCallbackHandler,
					Map<String, String> variants) {
				return null;
			}

			public ResourceBundlePathsIterator getBundlePaths(
					DebugMode debugMode, String bundleId,
					ConditionalCommentCallbackHandler commentCallbackHandler,
					Map<String, String> variants) {
				return null;
			}

			public boolean containsValidBundleHashcode(String requestedPath) {
				return false;
			}

			public String getBundleTextDirPath() {
				return bundleDirPath;
			}

			public List<JoinableResourceBundle> getGlobalBundles() {
				return new ArrayList<JoinableResourceBundle>();
			}

			public String getResourceType() {
				return null;
			}
			
		};
		return bundlesHandler;
	}

}
