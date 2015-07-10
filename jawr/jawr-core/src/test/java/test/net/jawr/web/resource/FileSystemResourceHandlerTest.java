package test.net.jawr.web.resource;

import java.io.Reader;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.Set;

import net.jawr.web.resource.bundle.JoinableResourceBundleContent;
import net.jawr.web.resource.handler.bundle.ResourceBundleHandler;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import test.net.jawr.web.resource.bundle.handler.ResourceHandlerBasedTest;



/**
 * Test for a FileSystemResourceHandler, also tests abstract methods in AbstractResourceHandler. 
 * @author jhernandez
 */
public class FileSystemResourceHandlerTest extends ResourceHandlerBasedTest {
	
	private static final String ROOT_TESTDIR = "/resourcehandler/";
	
	private static String TEST_FILENAME = "/first.js"; 
	
	private static Charset charsetUtf = Charset.forName("UTF-8"); 
	
	private final String testStr;
	
	ResourceReaderHandler rsReaderHandler;
	
	ResourceBundleHandler rsBundleHandler;
	
	public FileSystemResourceHandlerTest(){
   		String temp = null;
		try {			
			// bytes for "�����" in utf-8. Best way to avoid garbled class from wrongly encoded SVN commits
			byte[] data = {-61,-95,-61,-87,	-61,-83,-61,-77,-61,-70,-30,-126,-84};
			temp = new String(data,"UTF-8");
			
			rsReaderHandler = createResourceReaderHandler(ROOT_TESTDIR,"js",charsetUtf);
			rsBundleHandler = createResourceBundleHandler(ROOT_TESTDIR,charsetUtf);
		} catch (Exception e) {
			fail("Error in test constructor " + e.getClass().getName() + ":"+ e.getMessage());
		}
		testStr = temp;
		
	}

        /**
         * Test if the handler can retrieve a file and return a properly encoded reader.  
         */
	public void testGetResource() {
		try {
			Reader rd = rsReaderHandler.getResource(TEST_FILENAME);
			assertNotNull(TEST_FILENAME + " was not found by the handler. ",rd);
			String readData = fullyReadReader(rd);
			
			assertEquals("Reader returned wrong or badly encoded text. ",testStr, readData);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Retrieving file threw exception: " + e.getClass().getName() + ":"+ e.getMessage());
		}
		
	} 

	/**
	 * Tests the ability to retrieve a list of resources from a directory. 
	 */
	public void testGetResourceNames() {
		try {
			// Get all files in root location. 
			Set<String> files = rsReaderHandler.getResourceNames("");
			assertNotNull("No resource names found ",files);
			
			// File found
			assertTrue("Expected resource 'first.js' was not found",files.contains("first.js"));
			
			// Directory found. 
			assertTrue("Expected resource 'folder' was not found",files.contains("folder") || files.contains("folder/"));
		} catch (RuntimeException e) {
			e.printStackTrace();
			fail("Retrieving Resource names from 'folder' threw exception: " + e.getClass().getName() + ":"+ e.getMessage());
		}
	}

	/**
	 * Test if a directory is properly identified as such. 
	 */
	public void testIsDirectory() {
		assertTrue("Subfolder '/folder/subfolder' was not considered a directory",rsReaderHandler.isDirectory("/folder/subfolder"));
		assertFalse("File '/folder/second.js' was considered to be a directory", rsReaderHandler.isDirectory("/folder/second.js"));		
	}

	/**
	 * Test the ability to return a bundle by id, and the correct encoding of the returned data. 
	 */
	public void testGetResourceBundleReader() {
		try {
			// Retrieve a bundle
			Reader rd = rsBundleHandler.getResourceBundleReader("collected.js");
			assertNotNull("'collected.js' was not found by the handler. ",rd);
			
			// Read its data and check the validity
			String readData = fullyReadReader(rd);
			assertEquals("Reader returned wrong or badly encoded text. ","áéíóú", readData);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Retrieving file threw exception: " + e.getClass().getName() + ":"+ e.getMessage());
		}
	}
	
	/**
	 * Test the ability to return a bundle by id as a gzipped resource. 
	 */
	public void testGetResourceBundleChannel() {
		try {
			// Retrieve a bundle
			ReadableByteChannel chan = rsBundleHandler.getResourceBundleChannel("collected.js");
			assertNotNull("'collected.js' was not found by the handler. ",chan);
			
			// Read its data and check the validity
			String readData = fullyReadChannel(chan,charsetUtf.name());
			assertEquals("Reader returned wrong or badly encoded text. ","áéíóú", readData);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Retrieving file threw exception: " + e.getClass().getName() + ":"+ e.getMessage());
		}
	}

	/**
	 * Test if data is properly stored in a file. 
	 */
	public void testStoreBundle() {
		
		StringBuffer sb = new StringBuffer(testStr);
		try {
			// Store data
			rsBundleHandler.storeBundle("/somepath/somesubpath/store/testCollection.js", new JoinableResourceBundleContent(sb));
			
			// Retrieve it back and check its content. 
			Reader rd = rsBundleHandler.getResourceBundleReader("/somepath/somesubpath/store/testCollection.js");
			assertNotNull("'testCollection.js' was not found by the handler. ",rd);
			String readData = fullyReadReader(rd);
			assertEquals("Reader returned invalid data. ",testStr, readData);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Retrieving file threw exception: " + e.getClass().getName() + ":"+ e.getMessage());
		}
	}

}
