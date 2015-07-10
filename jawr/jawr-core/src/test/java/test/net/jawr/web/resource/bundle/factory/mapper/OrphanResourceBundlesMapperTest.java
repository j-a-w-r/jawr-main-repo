/**
 * 
 */
package test.net.jawr.web.resource.bundle.factory.mapper;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.jawr.web.exception.DuplicateBundlePathException;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.factory.mapper.OrphanResourceBundlesMapper;
import net.jawr.web.resource.bundle.iterator.BundlePath;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import test.net.jawr.web.resource.bundle.MockJoinableResourceBundle;
import test.net.jawr.web.resource.bundle.handler.ResourceHandlerBasedTest;

/**
 * @author jhernandez
 * @author ibrahim Chaehoi
 */
public class OrphanResourceBundlesMapperTest extends  ResourceHandlerBasedTest {
	private static final String ROOT_TESTDIR = "/orphanspathfactory/";
	private OrphanResourceBundlesMapper factory;
	
	public OrphanResourceBundlesMapperTest() {
		try {			
			Charset charsetUtf = Charset.forName("UTF-8"); 
			
			ResourceReaderHandler rsHandler = createResourceReaderHandler(ROOT_TESTDIR,"js",charsetUtf);
			List<JoinableResourceBundle> bundles = new ArrayList<JoinableResourceBundle>();
			
			List<String> globalPaths = Arrays.asList("/js/global/global.js");
			bundles.add(buildMockResourceBundle(globalPaths, Collections.singleton("/js/global/.license")));
			
			List<String> libraryPaths = Arrays.asList("/js/lib/lib.js");
			bundles.add(buildMockResourceBundle(libraryPaths,Collections.singleton("")));
			
			List<String> debugPaths = Arrays.asList("/js/debug/off/debugOff.js", "/js/debug/on/debugOn.js");
			bundles.add(buildMockResourceBundle(debugPaths,Collections.singleton("")));
			
			factory = new OrphanResourceBundlesMapper("", rsHandler, config.getGeneratorRegistry(), bundles, "js");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Test method for {@link net.jawr.web.resource.bundle.factory.mapper.OrphanResourceBundlesMapper#getOrphans()}.
	 */
	public void testGetOrphans() {
		
		List<String> data = null;
		try {
			data = factory.getOrphansList();
		} catch (DuplicateBundlePathException e) {
			fail("DuplicateBundlePathException for bundle path: " + e.getBundlePath());
		}
		
		// Test correct paths were added in proper order
		String path = data.get(0);
		assertEquals("Licenses file not added: /js/one/.license","/js/one/.license",path);
		path = data.get(1);
		assertEquals("Expected path not added at proper position: /js/one/one.js","/js/one/one.js",path);
		path = data.get(2);
		assertEquals("Expected path not added at proper position: /js/three/one.js","/js/three/one.js",path);
		
		
		assertTrue("Expected path not added: /js/debug/off/includeThisOne.js",data.contains("/js/debug/off/includeThisOne.js"));
		
		// Ensure ordering in a subfolder
		int pos = data.indexOf("/js/two/three.js");
		assertTrue("Expected path not added: /js/two/three.js",pos != -1);
		assertEquals("Expected path not added at proper position: /js/two/one.js","/js/two/one.js",(String) data.get(++pos));
		assertEquals("Expected path not added at proper position: /js/two/two.js","/js/two/two.js",(String) data.get(++pos));
		
		
		assertTrue("Expected path not added: /js/three/one.js",data.contains("/js/three/one.js"));
		assertTrue("Expected path not added: /js/one.js",data.contains("/js/one.js"));
		assertTrue("Expected path not added: /js/two.js",data.contains("/js/two.js"));
		
		// Test no invalid paths were added
		assertFalse("Unexpected license added: /js/global/.license", data.contains("/js/global/.license"));
		assertFalse("Unexpected path added: /js/global/global.js", data.contains("/js/global/global.js"));
		assertFalse("Unexpected path added: /js/lib/lib.js", data.contains("/js/lib/lib.js"));
		assertFalse("Unexpected path added: /js/debug/off/debugOff.js", data.contains("/js/debug/off/debugOff.js"));
		assertFalse("Unexpected path added: /js/debug/on/debugOn.js", data.contains("/js/debug/on/debugOn.js"));
		assertFalse("Unexpected path added: /js/one/shouldNotBeAdded.ext", data.contains("/js/one/shouldNotBeAdded.ext"));
		
	}
	
	private JoinableResourceBundle buildMockResourceBundle(final List<String> avoidedPaths, final Set<String> licenses) {
		
		return new MockJoinableResourceBundle() {

			public boolean belongsToBundle(String itemPath) {
				return avoidedPaths.contains(itemPath);
			}

			public List<BundlePath> getItemPathList() {
				List<BundlePath> bundlePaths = new ArrayList<BundlePath>();
				for(String path : avoidedPaths){
					bundlePaths.add(new BundlePath(null, path));
				}
				return bundlePaths;
			}

			public Set<String> getLicensesPathList() {
				return licenses;
			}

		};		
	}
}
