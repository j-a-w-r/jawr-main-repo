package test.net.jawr.web.resource.bundle;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.CompositeResourceBundle;
import net.jawr.web.resource.bundle.DebugInclusion;
import net.jawr.web.resource.bundle.InclusionPattern;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.JoinableResourceBundleImpl;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.iterator.BundlePath;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import test.net.jawr.web.ResourceHandlerBasedUtils;

@RunWith(MockitoJUnitRunner.class)
public class CompositeResourceBundleTest {
	private static final String ROOT_TESTDIR = "/compositeresourcebundle/";
	private JoinableResourceBundle compositeCollectionNoDebug;
	private JoinableResourceBundle compositeCollectionDebugOnly;
	private JoinableResourceBundle compositeCollectionDebugAlways;
	
	private JawrConfig config = null;
	
	public CompositeResourceBundleTest() {
	
		ResourceHandlerBasedUtils rhb = new ResourceHandlerBasedUtils();
		ResourceReaderHandler rsHandler = null;
		try {
			rsHandler = rhb.createResourceReaderHandler(ROOT_TESTDIR, "js",Charset.forName("UTF-8"));
		} catch (Exception e) {
			System.out.println("Error in test constructor");
			e.printStackTrace();
		}
	
		config = rhb.getConfig();
		List<String> mappingA = new ArrayList<String>();
		mappingA.add("/js/subfolder/");
		mappingA.add("/outsider.js");
		
		List<String> mappingB = Collections.singletonList("/js/subfolder2/"); 
		
		InclusionPattern onDebug = new InclusionPattern(false,0,DebugInclusion.ONLY);
		InclusionPattern excludedOnDebug = new InclusionPattern(false,0,DebugInclusion.NEVER);
		
		JoinableResourceBundleImpl bundleA = new JoinableResourceBundleImpl("/bundles/compositeChildA.js","compositeChildA", null, "js", onDebug,mappingA,rsHandler,  config.getGeneratorRegistry());
		JoinableResourceBundleImpl bundleB = new JoinableResourceBundleImpl("/bundles/compositeChildB.js","compositeChildB", null, "js", excludedOnDebug,mappingB,rsHandler, config.getGeneratorRegistry());
		List<JoinableResourceBundle> bundles = new ArrayList<JoinableResourceBundle>();
		bundles.add(bundleA);
		bundles.add(bundleB);
		
		Properties props = new Properties();
		JawrConfig config = new JawrConfig("js", props);
		config.setDebugModeOn(false);
		GeneratorRegistry generatorRegistry = new GeneratorRegistry();
		config.setGeneratorRegistry(generatorRegistry);
		compositeCollectionNoDebug = new CompositeResourceBundle("/bundles/compositeNoDebug.js","compositeNoDebug",bundles,new InclusionPattern(false, 0, DebugInclusion.NEVER),rsHandler, null, "js",generatorRegistry);
		config.setDebugModeOn(true);
		compositeCollectionDebugOnly = new CompositeResourceBundle("/bundles/compositeDebugOnly.js","compositeDebugOnly",bundles,new InclusionPattern(false, 0, DebugInclusion.ONLY),rsHandler, null, "js",generatorRegistry);

		compositeCollectionDebugAlways = new CompositeResourceBundle("/bundles/compositeDebugAlways.js","compositeDebugAlways",bundles,new InclusionPattern(false, 0, DebugInclusion.ALWAYS),rsHandler, null, "js",generatorRegistry);

	}
	
	@Test
	public void testDebugModeInclusion_debug() {
		
		assertTrue("/outsider.js should be added in debug mode",
				belongsToItemDebugPathList(compositeCollectionDebugOnly,"/outsider.js"));
		
		assertTrue("/js/subfolder/subfolderscript.js should be added in debug mode",
				belongsToItemDebugPathList(compositeCollectionDebugOnly, "/js/subfolder/subfolderscript.js"));
		
		assertFalse("/js/subfolder2/subfolderscript2.js should not be added in debug mode",
				belongsToItemDebugPathList(compositeCollectionDebugOnly, "/js/subfolder2/subfolderscript2.js"));
		
	}

	public boolean belongsToItemPathList(JoinableResourceBundle bundle, String path){
		
		return belongsToPathList(bundle.getItemPathList(), path);
	}
	
	public boolean belongsToItemDebugPathList(JoinableResourceBundle bundle, String path){
		
		return belongsToPathList(bundle.getItemDebugPathList(), path);
	}
	
	public boolean belongsToPathList(List<BundlePath> bundlePaths, String path){
		
		boolean result = false;
		for(BundlePath bundlePath : bundlePaths){
			if(bundlePath.getPath().equals(path)){
				result = true;
				break;
			}
		}
		
		return result;
	}
	
	@Test
	public void testDebugModeInclusion_nodebug() {
		
		assertFalse("/outsider.js should not be added in production mode",
				belongsToItemPathList(compositeCollectionNoDebug, "/outsider.js"));
		
		assertFalse("/js/subfolder/subfolderscript.js should not be added in production mode",
				belongsToItemPathList(compositeCollectionNoDebug, "/js/subfolder/subfolderscript.js"));
		
		assertTrue("/js/subfolder2/subfolderscript2.js should be added in production mode",
				belongsToItemPathList(compositeCollectionNoDebug, "/js/subfolder2/subfolderscript2.js"));
	}
	
	@Test
	public void testDebugModeInclusion_always() {
		
		assertTrue("/outsider.js should be added in debug mode",
				belongsToItemDebugPathList(compositeCollectionDebugAlways, "/outsider.js"));
		
		assertTrue("/js/subfolder/subfolderscript.js should be added in debug mode",
				belongsToItemDebugPathList(compositeCollectionDebugAlways, "/js/subfolder/subfolderscript.js"));
		
		assertFalse("/js/subfolder2/subfolderscript2.js should not be added in debug mode",
				belongsToItemDebugPathList(compositeCollectionDebugAlways, "/js/subfolder2/subfolderscript2.js"));
	}
	
	// Test: debugonly is not added on prod. mode, and is added in debug mode. 
	// Test: debug never is not added in debug mode, and is not added in prod. mode. 
	// Test: different postprocessors execute right. 
}
