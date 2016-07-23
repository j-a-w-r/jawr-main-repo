package test.net.jawr.web.resource.bundle.postprocess.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.EmptyResourceBundlePostProcessor;

@RunWith(MockitoJUnitRunner.class)
public class EmptyPostProcessorTest {

	@Mock
	private JoinableResourceBundle bundle;
	
	@Before
	public void setup(){
		
		when(bundle.getId()).thenReturn("/myJsBundle.js");
	}
	
    @Test
    public void testBundlePostProcessJSBundleWithAnonymousFunc() {
		
		JawrConfig config = new JawrConfig("js", new Properties());
		EmptyResourceBundlePostProcessor processor = new EmptyResourceBundlePostProcessor();
		StringBuffer sb = new StringBuffer("!function() { console.log(1) }()");
		
		BundleProcessingStatus status = new BundleProcessingStatus(BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle,null,config);
		StringBuffer ret = processor.postProcessBundle(status, sb);
		
		assertEquals("!function() { console.log(1) }()", ret.toString());
	}
    
    @Test
    public void testFilePostProcessJSBundleWithAnonymousFunc() {
		
		JawrConfig config = new JawrConfig("js", new Properties());
		EmptyResourceBundlePostProcessor processor = new EmptyResourceBundlePostProcessor();
		StringBuffer sb = new StringBuffer("!function() { console.log(1) }()");
		
		BundleProcessingStatus status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE, bundle,null,config);
		StringBuffer ret = processor.postProcessBundle(status, sb);
		
		assertEquals("!function() { console.log(1) }();", ret.toString());
	}
    
	@Test
    public void testBundlePostProcesJsBundleWithStandardFunc() {
		String script = "function(){ alert('hello')};";
		JawrConfig config = new JawrConfig("js", new Properties());
		EmptyResourceBundlePostProcessor processor = new EmptyResourceBundlePostProcessor();
		StringBuffer sb = new StringBuffer(script);
		
		BundleProcessingStatus status = new BundleProcessingStatus(BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle,null,config);
		StringBuffer ret = processor.postProcessBundle(status, sb);
		
		assertEquals(script, ret.toString());
	}
    
	@Test
    public void testFileBundlePostProcesJsBundleWithStandardFunc() {
		String script = "function(){ alert('hello')};";
		JawrConfig config = new JawrConfig("js", new Properties());
		EmptyResourceBundlePostProcessor processor = new EmptyResourceBundlePostProcessor();
		StringBuffer sb = new StringBuffer(script);
		
		BundleProcessingStatus status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE, bundle,null,config);
		StringBuffer ret = processor.postProcessBundle(status, sb);
		
		assertEquals(script, ret.toString());
	}
    
	@Test
    public void testBundlePostProcesCssBundle() {
		String css = ".intro{ padding-top : 20px; }";
		JawrConfig config = new JawrConfig("css", new Properties());
		EmptyResourceBundlePostProcessor processor = new EmptyResourceBundlePostProcessor();
		StringBuffer sb = new StringBuffer(css);
		
		BundleProcessingStatus status = new BundleProcessingStatus(BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle,null,config);
		StringBuffer ret = processor.postProcessBundle(status, sb);
		
		assertEquals(css, ret.toString());
	}
	
	@Test
    public void testFileBundlePostProcesCssBundle() {
		String css = ".intro{ padding-top : 20px; }";
		JawrConfig config = new JawrConfig("css", new Properties());
		EmptyResourceBundlePostProcessor processor = new EmptyResourceBundlePostProcessor();
		StringBuffer sb = new StringBuffer(css);
		
		BundleProcessingStatus status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE, bundle,null,config);
		StringBuffer ret = processor.postProcessBundle(status, sb);
		
		assertEquals(css, ret.toString());
	}
}
