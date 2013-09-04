/**
 * 
 */
package test.net.jawr.web.resource.bundle.postprocess.impl;

import junit.framework.TestCase;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.impl.CssCharsetFilterPostProcessor;

import org.apache.log4j.PropertyConfigurator;

import test.net.jawr.log.AppenderForTesting;
import test.net.jawr.web.FileUtils;
import test.net.jawr.web.resource.bundle.MockJoinableResourceBundle;

/**
 * @author Ibrahim Chaehoi
 *
 */
public class CssBundleCharsetCleanerPostProcessorTest extends TestCase {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	public void setUp(){
		PropertyConfigurator.configure(CssBundleCharsetCleanerPostProcessorTest.class.getResource("/postprocessor/cssbundlecharset/log4j-test.properties"));
		AppenderForTesting.clear();
	}
	
	public void testPostProcessBundle() throws Exception {
		CssCharsetFilterPostProcessor processor = new CssCharsetFilterPostProcessor();
		StringBuffer sb = new StringBuffer(FileUtils.readClassPathFile("postprocessor/cssbundlecharset/standard-bundle.css"));
		BundleProcessingStatus status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE, getJoinableResourceBundle("/bundle1.css"),null,null);
		StringBuffer ret = processor.postProcessBundle(status, sb);
		assertEquals(FileUtils.readClassPathFile("postprocessor/cssbundlecharset/standard-bundle-result.css"), ret.toString());
	}
	
	public void testPostProcessBundleWithDifferentCharsetWarning() throws Exception {
		
		CssCharsetFilterPostProcessor processor = new CssCharsetFilterPostProcessor();
		StringBuffer sb = new StringBuffer(FileUtils.readClassPathFile("postprocessor/cssbundlecharset/different-charset-decl-bundle.css"));
		BundleProcessingStatus status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE, getJoinableResourceBundle("/bundle1.css"),null,null);
		StringBuffer ret = processor.postProcessBundle(status, sb);
		assertEquals(FileUtils.readClassPathFile("postprocessor/cssbundlecharset/different-charset-decl-bundle-result.css"), ret.toString());
		
		String[] messages = AppenderForTesting.getMessages();
		assertEquals(2, messages.length);
		assertEquals("The bundle '/bundle1.css' contains CSS with different charset declaration.", messages[1]);
	}
	
	public void testPostProcessBundleWithCharsetNotAtTheBegining() throws Exception {
		
		CssCharsetFilterPostProcessor processor = new CssCharsetFilterPostProcessor();
		StringBuffer sb = new StringBuffer(FileUtils.readClassPathFile("postprocessor/cssbundlecharset/charset-decl-not-at-the-top-bundle.css"));
		BundleProcessingStatus status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE, getJoinableResourceBundle("/bundle1.css"),null,null);
		StringBuffer ret = processor.postProcessBundle(status, sb);
		assertEquals(FileUtils.readClassPathFile("postprocessor/cssbundlecharset/charset-decl-not-at-the-top-bundle-result.css"), ret.toString());
	}

	private JoinableResourceBundle getJoinableResourceBundle(final String id){
		
		JoinableResourceBundle bundle = new MockJoinableResourceBundle() {
			
			public String getId() {
				return id;
			}
		};
		return bundle;
		
	}
}
