package test.net.jawr.web.resource.bundle.handler;

import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundleDependencyException;
import net.jawr.web.exception.DuplicateBundlePathException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.iterator.ResourceBundlePathsIterator;
import net.jawr.web.resource.handler.bundle.ResourceBundleHandler;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import test.net.jawr.web.resource.bundle.PredefinedBundlesHandlerUtil;
import test.net.jawr.web.servlet.mock.MockServletContext;

public class ResourceBundlesHandlerImplTest  extends  ResourceHandlerBasedTest {
	
	private static final String ROOT_DEFAULT_FOLDER = "/collectionshandler/default/";
	private static final String ROOT_DEFAULT_DEBUG_FOLDER = "/collectionshandler/debug/";
	private static final String ROOT_SIMPLE_FOLDER = "/collectionshandler/simple/";
	private ResourceBundlesHandler defaultHandler;
	private ResourceBundlesHandler defaultDebugCollection;
	private ResourceBundlesHandler simpleHandler;

	public ResourceBundlesHandlerImplTest() {
		try {
			Charset charsetUtf = Charset.forName("UTF-8"); 
			
			ResourceReaderHandler handler = createResourceReaderHandler(ROOT_DEFAULT_FOLDER,"js",charsetUtf);
			ResourceReaderHandler handlerSimple = createResourceReaderHandler(ROOT_SIMPLE_FOLDER,"js",charsetUtf);
			ResourceReaderHandler handlerDebug = createResourceReaderHandler(ROOT_DEFAULT_DEBUG_FOLDER,"js",charsetUtf);
			
			ResourceBundleHandler bundleHandler = createResourceBundleHandler(ROOT_DEFAULT_FOLDER,charsetUtf);
			ResourceBundleHandler bundleHandlerSimple = createResourceBundleHandler(ROOT_SIMPLE_FOLDER,charsetUtf);
			ResourceBundleHandler bundleHandlerDebug = createResourceBundleHandler(ROOT_DEFAULT_DEBUG_FOLDER,charsetUtf);
			
			JawrConfig config = new JawrConfig("js", new Properties());
			config.setCharsetName("UTF-8");
			config.setDebugModeOn(false);
			config.setGzipResourcesModeOn(false);
			//config.setURLPrefix(RESOURCES_PREFIX);
			GeneratorRegistry generatorRegistry = new GeneratorRegistry();
			config.setGeneratorRegistry(generatorRegistry);
			generatorRegistry.setConfig(config);
			config.setContext(new MockServletContext());
			
			JawrConfig configDebug = new JawrConfig("js",new Properties());
			configDebug.setCharsetName("UTF-8");
			configDebug.setDebugModeOn(true);
			GeneratorRegistry debugGeneratorRegistry = new GeneratorRegistry();
			configDebug.setGeneratorRegistry(debugGeneratorRegistry);
			configDebug.setContext(new MockServletContext());
			debugGeneratorRegistry.setConfig(configDebug);
			//configDebug.setURLPrefix(RESOURCES_PREFIX);
			
			defaultHandler = PredefinedBundlesHandlerUtil.buildSingleBundleHandler(handler, bundleHandler, config);
			simpleHandler = PredefinedBundlesHandlerUtil.buildSimpleBundles(handlerSimple, bundleHandlerSimple,"/js","js", config);
			defaultDebugCollection = PredefinedBundlesHandlerUtil.buildSimpleBundles(handlerDebug, bundleHandlerDebug,"/js","js", configDebug);
			
		} catch (Exception e) {
			System.out.println("Error in test constructor");
			e.printStackTrace();
		}
	}
	
	public void testHashCodeGeneration(){
		JoinableResourceBundle bundle = defaultHandler.resolveBundleForPath("/script.js");
		assertEquals("N1266058766", bundle.getBundleDataHashCode(null));
	}
	
	public void testGetSingleFilePath() {
		
		assertTrue("The collection path was not initialized properly", 
					defaultHandler.getBundlePaths("/script.js",null,null).next().getPath().endsWith("/script.js"));
		
	}
	public void testGetNormalCollectionPaths() {
		
		ResourceBundlePathsIterator globalSimplePaths = simpleHandler.getGlobalResourceBundlePaths(null,null);
		assertTrue("Path ordering does not match expected. ", globalSimplePaths.next().getPath().endsWith("/library.js"));
		assertTrue("Path ordering does not match expected. ", globalSimplePaths.next().getPath().endsWith("/global.js"));
		assertTrue("Path ordering does not match expected. ", globalSimplePaths.next().getPath().endsWith("/debugOff.js"));
		
		ResourceBundlePathsIterator simplePaths = simpleHandler.getBundlePaths("/js/one.js",null,null);
		//assertEquals("Invalid number of paths returned",new Integer(4), new Integer(simplePaths.size()));
		assertTrue("Path ordering does not match expected. ", simplePaths.next().getPath().endsWith("js/one.js"));
		
	}
	public void testGetDebugCollectionPaths() {
		
		ResourceBundlePathsIterator simplePaths = defaultDebugCollection.getGlobalResourceBundlePaths(null,null);
		assertEquals("Path ordering does not match expected. ","/js/lib/prototype/protoype.js", simplePaths.next().getPath());
		assertEquals("Path ordering does not match expected. ","/js/lib/lib2.js", simplePaths.next().getPath());
		assertEquals("Path ordering does not match expected. ","/js/lib/scriptaculous/scriptaculous.js", simplePaths.next().getPath());
		
	}

	public void testWriteCollectionTo() {
		StringWriter writer = new StringWriter();
		try {
			defaultHandler.writeBundleTo("/dummy/script.js", writer);
		} catch (ResourceNotFoundException e) {
			fail("File was not found:" + e.getRequestedPath());
		}
		assertTrue("Nothing was written to the file", writer.getBuffer().length() > 0);
		
		writer = new StringWriter();
		try {
			simpleHandler.writeBundleTo("/dummy/js/one.js", writer);
		} catch (ResourceNotFoundException e) {
			fail("File was not found:" + e.getRequestedPath());
		}
		assertTrue("Nothing was written to the file", writer.getBuffer().length() > 0);
	}

	public void testResolveCollectionForPath() {
		assertEquals("Get script by id failed","/script.js", defaultHandler.resolveBundleForPath("/script.js").getId());
		assertEquals("Get script by script name failed","/script.js", defaultHandler.resolveBundleForPath("/js/script1.js").getId());

		assertEquals("Get script by id failed","/library.js", simpleHandler.resolveBundleForPath("/library.js").getId());
		assertEquals("Get script by script name failed","/global.js", simpleHandler.resolveBundleForPath("/js/global/global.js").getId());
	}

	public void testNotificationOnCompositeBundle() throws DuplicateBundlePathException, BundleDependencyException{
		
		Charset charsetUtf = Charset.forName("UTF-8"); 
		JawrConfig simpleConfig = new JawrConfig("js",new Properties());
		simpleConfig.setCharsetName("UTF-8");
		simpleConfig.setDebugModeOn(true);
		GeneratorRegistry debugGeneratorRegistry = new GeneratorRegistry();
		simpleConfig.setGeneratorRegistry(debugGeneratorRegistry);
		simpleConfig.setContext(new MockServletContext());
		debugGeneratorRegistry.setConfig(simpleConfig);
		
		ResourceReaderHandler handlerSimple = createResourceReaderHandler(ROOT_SIMPLE_FOLDER,"js",charsetUtf);
		ResourceBundleHandler bundleHandlerSimple = createResourceBundleHandler(ROOT_SIMPLE_FOLDER,charsetUtf);
		ResourceBundlesHandler bundlesHandler = PredefinedBundlesHandlerUtil.buildSimpleCompositeBundles(handlerSimple, bundleHandlerSimple, "/js","js", simpleConfig);
		
		List<JoinableResourceBundle> bundles = bundlesHandler.getContextBundles();
		JoinableResourceBundle bundleChild2 = null;
		for(JoinableResourceBundle bundle : bundles){
			if(bundle.getName().equals("libraryChild2")){
				bundleChild2 = bundle;
				break;
			}
		}
		
		bundlesHandler.notifyModification(Arrays.asList(bundleChild2));
		for(JoinableResourceBundle bundle : bundles){
			if(bundle.getName().equals("libraryChild1")){
				assertFalse(bundle.isDirty());
			}
			if(bundle.getName().equals("libraryChild2")){
				assertTrue(bundle.isDirty());
			}
			if(bundle.getName().equals("library")){
				assertTrue(bundle.isDirty());
			}
		}
	}
}
