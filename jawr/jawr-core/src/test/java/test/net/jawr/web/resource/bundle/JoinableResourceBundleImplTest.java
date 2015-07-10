/**
 * 
 */
package test.net.jawr.web.resource.bundle;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.jawr.web.resource.bundle.DebugInclusion;
import net.jawr.web.resource.bundle.InclusionPattern;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.JoinableResourceBundleImpl;
import net.jawr.web.resource.bundle.iterator.BundlePath;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import test.net.jawr.web.resource.bundle.handler.ResourceHandlerBasedTest;


/**
 * 
 * @author jordi
 */
public class JoinableResourceBundleImplTest extends  ResourceHandlerBasedTest  {
	
	private static final String ROOT_TESTDIR = "/joinableresourcebundle/";
	private JoinableResourceBundle fullCollection;
	private JoinableResourceBundle partialCollection;

	/**
	 * 
	 */
	public JoinableResourceBundleImplTest() {
		InclusionPattern pattern = new InclusionPattern(true,0,DebugInclusion.ALWAYS);
		List<String> fullMapping = Collections.singletonList("js/**");
		
		List<String> partialMapping = new ArrayList<String>();
		partialMapping.add("/js/subfolder/");
		partialMapping.add("/outsider.js");
		
		ResourceReaderHandler rsHandler = null;
		try {
			rsHandler = createResourceReaderHandler(ROOT_TESTDIR, "js",Charset.forName("UTF-8"));
		} catch (Exception e) {
			System.out.println("Error in test constructor");
			e.printStackTrace();
		}
		fullCollection = new JoinableResourceBundleImpl("full.js","full", null, "js",pattern,fullMapping,rsHandler, config.getGeneratorRegistry());
		partialCollection = new JoinableResourceBundleImpl("partial.js","partial", null, "js",pattern,partialMapping,rsHandler, config.getGeneratorRegistry());
	}
 
	/**
	 * Test method for {@link net.jawr.web.resource.bundle.JoinableResourceBundleImpl#belongsToBundle(java.lang.String)}.
	 * Test if the bundle recognizzes which items belong to it. 
	 */
	public void testBelongsToBundle() {
		// Full collection
		assertTrue("/js/script1.js should belong to the collection",fullCollection.belongsToBundle("/js/script1.js"));
		assertTrue("/js/script2.js should belong to the collection",fullCollection.belongsToBundle("/js/script2.js"));
		assertTrue("/js/subfolder/subfolderscript.js should belong to the collection",fullCollection.belongsToBundle("/js/subfolder/subfolderscript.js"));
		assertTrue("/js/subfolder2/subfolderscript2.js should belong to the collection",fullCollection.belongsToBundle("/js/subfolder2/subfolderscript2.js"));
		assertFalse("/outsider.js should not belong to the collection",fullCollection.belongsToBundle("/outsider.js"));
		
		// Partial collection
		assertTrue("[partialMapping] /js/subfolder/subfolderscript.js should belong to the collection",partialCollection.belongsToBundle("/js/subfolder/subfolderscript.js"));
		assertTrue("[partialMapping] /outsider.js should belong to the collection",partialCollection.belongsToBundle("/outsider.js"));
		assertFalse("[partialMapping] /js/script1.js should not belong to the collection",partialCollection.belongsToBundle("/js/script1.js"));
		
	}


	/**
	 * Test method for {@link net.jawr.web.resource.bundle.JoinableResourceBundleImpl#getItemPathList()}.
	 */
	public void testGetItemPathList() {
		// Full collection
		List<BundlePath> expectedInFullCol = new ArrayList<BundlePath>();
		expectedInFullCol.add(new BundlePath(null, "/js/script2.js"));
		expectedInFullCol.add(new BundlePath(null, "/js/subfolder/subfolderscript.js"));
		expectedInFullCol.add(new BundlePath(null, "/js/subfolder2/subfolderscript2.js"));
		expectedInFullCol.add(new BundlePath(null, "/js/script1.js"));
		assertEquals("Order of inclusion does not match the expected. ",expectedInFullCol, fullCollection.getItemPathList());

		// Partial collection
		List<BundlePath> expectedInPartCol = new ArrayList<BundlePath>();
		expectedInPartCol.add(new BundlePath(null, "/js/subfolder/subfolderscript.js"));
		expectedInPartCol.add(new BundlePath(null, "/outsider.js"));
		assertEquals("[partialMapping] Order of inclusion does not match the expected. ",expectedInPartCol, partialCollection.getItemPathList());
	}


}
