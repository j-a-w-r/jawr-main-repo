/**
 * 
 */
package test.net.jawr.web.resource.bundle.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundleDependencyException;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.DuplicateBundlePathException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.factory.PropertiesBasedBundlesHandlerFactory;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.resource.handler.bundle.ResourceBundleHandler;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Ibrahim Chaehoi
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class BundlesHandlerFactoryTestCase {

	@Mock
	private ResourceBundleHandler resourceBundleHandler;
	
	@Mock 
	private ResourceReaderHandler resourceReaderHandler;
	
	@Before
	public void setup() throws ResourceNotFoundException{
		when(resourceBundleHandler.getResourceType()).thenReturn("css");
		when(resourceReaderHandler.getResourceNames(Matchers.anyString())).thenReturn(new HashSet<String>());
		when(resourceReaderHandler.getResource(Matchers.anyString())).thenThrow(new ResourceNotFoundException(""));
		when(resourceReaderHandler.getResource((JoinableResourceBundle) Matchers.any(), Matchers.anyString(), Matchers.anyBoolean())).thenThrow(new ResourceNotFoundException(""));
	}
	
	@Test
	public void testBundleWithInvalidBundleId1() throws IOException, DuplicateBundlePathException, BundleDependencyException{
		try{
			getBundles("css", "/bundle/factory/bundleshandlerfactory/jawr-invalid-bundleId1.properties");
			fail("No bundle processing exception has been throwned");
		}catch(BundlingProcessException e){
			
		}
		
	}
	
	@Test
	public void testBundleWithInvalidBundleId2() throws IOException, DuplicateBundlePathException, BundleDependencyException{
		try{
			getBundles("css", "/bundle/factory/bundleshandlerfactory/jawr-invalid-bundleId2.properties");
			fail("No bundle processing exception has been throwned");
		}catch(BundlingProcessException e){
			
		}
	}
	
	@Test
	public void testBundleWithInvalidBundleId3() throws IOException, DuplicateBundlePathException, BundleDependencyException{
		try{
			getBundles("css", "/bundle/factory/bundleshandlerfactory/jawr-invalid-bundleId3.properties");
			fail("No bundle processing exception has been throwned");
		}catch(BundlingProcessException e){
			
		}
	}
	
	/**
	 * Test the dependency resolution
	 */
	@Test
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
	@Test
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
	@Test
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
		generatorRegistry.setResourceReaderHandler(resourceReaderHandler);
		PropertiesBasedBundlesHandlerFactory propsBundlesHandlerFactory = new PropertiesBasedBundlesHandlerFactory(props, resourceType, resourceReaderHandler, resourceBundleHandler, config);
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
}
