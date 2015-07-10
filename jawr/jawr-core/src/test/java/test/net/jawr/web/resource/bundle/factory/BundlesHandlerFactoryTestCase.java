/**
 * 
 */
package test.net.jawr.web.resource.bundle.factory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;

import org.mockito.Mockito;

import junit.framework.TestCase;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundleDependencyException;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.DuplicateBundlePathException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.JoinableResourceBundleContent;
import net.jawr.web.resource.bundle.factory.PropertiesBasedBundlesHandlerFactory;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.resource.handler.bundle.ResourceBundleHandler;
import net.jawr.web.resource.handler.reader.ResourceReader;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

/**
 * @author Ibrahim Chaehoi
 *
 */
public class BundlesHandlerFactoryTestCase extends TestCase {

	public void testBundleWithInvalidBundleId1() throws IOException, DuplicateBundlePathException, BundleDependencyException{
		try{
			getBundles("css", "/bundle/factory/bundleshandlerfactory/jawr-invalid-bundleId1.properties");
			fail("No bundle processing exception has been throwned");
		}catch(BundlingProcessException e){
			
		}
		
	}
	
	public void testBundleWithInvalidBundleId2() throws IOException, DuplicateBundlePathException, BundleDependencyException{
		try{
			getBundles("css", "/bundle/factory/bundleshandlerfactory/jawr-invalid-bundleId2.properties");
			fail("No bundle processing exception has been throwned");
		}catch(BundlingProcessException e){
			
		}
	}
	
	public void testBundleWithInvalidBundleId3() throws IOException, DuplicateBundlePathException, BundleDependencyException{
		try{
			getBundles("css", "/bundle/factory/bundleshandlerfactory/jawr-invalid-bundleId3.properties");
			fail("No bundle processing exception has been throwned");
		}catch(BundlingProcessException e){
			
		}
	}
	
	/**
	 * Test the dependency resolution
	 * @throws DuplicateBundlePathException
	 * @throws BundleDependencyException
	 * @throws IOException
	 */
	public void testDependencyResolution() throws DuplicateBundlePathException, BundleDependencyException, IOException{
		
		List<JoinableResourceBundle> bundles = getBundles("css", "/bundle/factory/bundleshandlerfactory/jawr.properties");
		assertEquals(4, bundles.size());
		
		for (Iterator<JoinableResourceBundle> iterator = bundles.iterator(); iterator.hasNext();) {
			JoinableResourceBundle bundle = (JoinableResourceBundle) iterator.next();
			if(bundle.getName().equals("component")){
				assertEquals(Arrays.asList("component3", "component4", "component2"), getBundleNames(bundle.getDependencies()));
			
			}else if(bundle.getName().equals("component2")){
				
				assertEquals(Arrays.asList("component3", "component4"), getBundleNames(bundle.getDependencies()));
			
			}else if(bundle.getName().equals("component3")){
				assertTrue(getBundleNames(bundle.getDependencies()).isEmpty());
			
			}else if(bundle.getName().equals("component4")){
				assertTrue(getBundleNames(bundle.getDependencies()).isEmpty());
			}
		}
	}
	
	/**
	 * Test the dependency resolution
	 * @throws DuplicateBundlePathException
	 * @throws IOException
	 */
	public void testDependencyResolutionWithCircularDependency() throws IOException, DuplicateBundlePathException{
		
		try {
			getBundles("css", "/bundle/factory/bundleshandlerfactory/jawr-circular-dependency.properties");
			fail("No circular dependency exception has been throwned");
		} catch (BundleDependencyException e) {
			
		}
	}
	
	/**
	 * Test the dependency resolution
	 * @throws DuplicateBundlePathException
	 * @throws IOException
	 */
	public void testDependencyResolutionWithDependencyInAGlobalBundle() throws IOException, DuplicateBundlePathException{
		
		try {
			getBundles("css", "/bundle/factory/bundleshandlerfactory/jawr-dependency-in-global-bundle.properties");
			fail("No bundle dependency exception has been throwned");
		} catch (BundleDependencyException e) {
			
		}
	}

	/**
	 * Returns the list of bundles generated from the configuration path
	 * @param configPath the configuration path
	 * @return the list of bundles
	 * @throws IOException
	 * @throws DuplicateBundlePathException
	 * @throws BundleDependencyException
	 */
	private List<JoinableResourceBundle> getBundles(String resourceType, String configPath) throws IOException, DuplicateBundlePathException,
			BundleDependencyException {
		
		Properties props = new Properties();
		props.load(BundlesHandlerFactoryTestCase.class.getResourceAsStream(configPath));
		JawrConfig config = new JawrConfig(resourceType, props);
		ServletContext ctx = Mockito.mock(ServletContext.class);
		config.setContext(ctx);
		GeneratorRegistry generatorRegistry = new GeneratorRegistry(){

			private static final long serialVersionUID = 1L;

			/* (non-Javadoc)
			 * @see net.jawr.web.resource.bundle.generator.GeneratorRegistry#getAvailableVariantMap(java.util.Map, java.util.Map)
			 */
			@Override
			public Map<String, String> getAvailableVariantMap(Map<String, VariantSet> variants, Map<String, String> curVariants){
				return new HashMap<String, String>();
			}

			/* (non-Javadoc)
			 * @see net.jawr.web.resource.bundle.generator.GeneratorRegistry#getAvailableVariants(java.lang.String)
			 */
			@Override
			public Map<String, VariantSet> getAvailableVariants(String bundle) {
				return new HashMap<String, VariantSet>();
			}

			/* (non-Javadoc)
			 * @see net.jawr.web.resource.bundle.generator.GeneratorRegistry#isPathGenerated(java.lang.String)
			 */
			public boolean isPathGenerated(String path) {
				return false;
			}
		};
		generatorRegistry.setConfig(config);
		config.setGeneratorRegistry(generatorRegistry);
		ResourceReaderHandler resourceReaderHandler = getResourceReaderHandler();
		generatorRegistry.setResourceReaderHandler(resourceReaderHandler);
		PropertiesBasedBundlesHandlerFactory propsBundlesHandlerFactory = new PropertiesBasedBundlesHandlerFactory(props, resourceType, resourceReaderHandler, getResourceBundleHandler(resourceType), config);
		ResourceBundlesHandler handler = propsBundlesHandlerFactory.buildResourceBundlesHandler();
		
		List<JoinableResourceBundle> bundles = handler.getContextBundles();
		return bundles;
	}
	
	/**
	 * Returns the list of bundle names
	 * @param bundles the bundles
	 * @return the list of bundle names
	 */
	private static List<String> getBundleNames(List<JoinableResourceBundle> bundles) {
		
		List<String> bundleNames = new ArrayList<String>();
		if(bundles != null){
			for (Iterator<JoinableResourceBundle> iterator = bundles.iterator(); iterator
					.hasNext();) {
				bundleNames.add(((JoinableResourceBundle) iterator.next()).getName());
			}
		}
		return bundleNames;
	}

	
	private ResourceReaderHandler getResourceReaderHandler(){
		return new ResourceReaderHandler() {
			
			public void setWorkingDirectory(String workingDir) {
				
			}
			
			public boolean isDirectory(String resourcePath) {
				return false;
			}
			
			public String getWorkingDirectory() {
				return null;
			}
			
			public Set<String> getResourceNames(String dirPath) {
				return new HashSet<String>();
			}
			
			public InputStream getResourceAsStream(String resourceName,
					boolean processingBundle) throws ResourceNotFoundException {
				throw new ResourceNotFoundException(resourceName);
			}
			
			public InputStream getResourceAsStream(String resourceName)
					throws ResourceNotFoundException {
				throw new ResourceNotFoundException(resourceName);
			}
			
			public Reader getResource(String resourceName, boolean processingBundle)
					throws ResourceNotFoundException {
				throw new ResourceNotFoundException(resourceName);
			}
			
			public Reader getResource(String resourceName)
					throws ResourceNotFoundException {
				throw new ResourceNotFoundException(resourceName);
			}
			
			public void addResourceReaderToStart(ResourceReader rd) {
				
			}
			
			public void addResourceReaderToEnd(ResourceReader rd) {
				
			}

			@Override
			public Reader getResource(String resourceName,
					boolean processingBundle, List<Class<?>> excludedReader)
					throws ResourceNotFoundException {
				return null;
			}
		};
	}
	
	private ResourceBundleHandler getResourceBundleHandler(final String resourceType){
		
		return new ResourceBundleHandler(){

			public Properties getJawrBundleMapping() {
				return null;
			}

			public InputStream getResourceBundleAsStream(String bundleName)
					throws ResourceNotFoundException {
				return null;
			}

			public ReadableByteChannel getResourceBundleChannel(
					String bundleName) throws ResourceNotFoundException {
				return null;
			}

			public Reader getResourceBundleReader(String bundleName)
					throws ResourceNotFoundException {
				return null;
			}

			public String getResourceType() {
				return resourceType;
			}

			public boolean isExistingMappingFile() {
				return false;
			}

			public void storeBundle(String bundleName,
					JoinableResourceBundleContent bundleResourcesContent) {
				
			}

			public void storeJawrBundleMapping(Properties bundleMapping) {
				
			}

			public String getBundleTextDirPath() {
				return null;
			}
		};
	}
}
