/**
 * 
 */
package test.net.jawr.web.resource.bundle.postprocess.impl;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.when;

import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.impl.CssCharsetFilterPostProcessor;
import test.net.jawr.log.AppenderForTesting;
import test.net.jawr.web.FileUtils;

/**
 * @author Ibrahim Chaehoi
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CssBundleCharsetCleanerPostProcessorTest {

	@Mock
	private JoinableResourceBundle bundle;
	
	@Before
	public void setUp(){
		PropertyConfigurator.configure(CssBundleCharsetCleanerPostProcessorTest.class.getResource("/postprocessor/cssbundlecharset/log4j-test.properties"));
		AppenderForTesting.clear();
		when(bundle.getId()).thenReturn("/bundle1.css");
	}
	
	@Test
	public void testPostProcessBundle() throws Exception {
		CssCharsetFilterPostProcessor processor = new CssCharsetFilterPostProcessor();
		StringBuffer sb = new StringBuffer(FileUtils.readClassPathFile("postprocessor/cssbundlecharset/standard-bundle.css"));
		BundleProcessingStatus status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE, bundle,null,null);
		StringBuffer ret = processor.postProcessBundle(status, sb);
		assertEquals(FileUtils.readClassPathFile("postprocessor/cssbundlecharset/standard-bundle-result.css"), ret.toString());
	}
	
	@Test
	public void testPostProcessBundleWithDifferentCharsetWarning() throws Exception {
		
		CssCharsetFilterPostProcessor processor = new CssCharsetFilterPostProcessor();
		StringBuffer sb = new StringBuffer(FileUtils.readClassPathFile("postprocessor/cssbundlecharset/different-charset-decl-bundle.css"));
		BundleProcessingStatus status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE, bundle,null,null);
		StringBuffer ret = processor.postProcessBundle(status, sb);
		assertEquals(FileUtils.readClassPathFile("postprocessor/cssbundlecharset/different-charset-decl-bundle-result.css"), ret.toString());
		
		String[] messages = AppenderForTesting.getMessages();
		assertEquals(2, messages.length);
		assertEquals("The bundle '/bundle1.css' contains CSS with different charset declaration.", messages[1]);
	}
	
	@Test
	public void testPostProcessBundleWithCharsetNotAtTheBegining() throws Exception {
		
		CssCharsetFilterPostProcessor processor = new CssCharsetFilterPostProcessor();
		StringBuffer sb = new StringBuffer(FileUtils.readClassPathFile("postprocessor/cssbundlecharset/charset-decl-not-at-the-top-bundle.css"));
		BundleProcessingStatus status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE, bundle,null,null);
		StringBuffer ret = processor.postProcessBundle(status, sb);
		assertEquals(FileUtils.readClassPathFile("postprocessor/cssbundlecharset/charset-decl-not-at-the-top-bundle-result.css"), ret.toString());
	}
}
