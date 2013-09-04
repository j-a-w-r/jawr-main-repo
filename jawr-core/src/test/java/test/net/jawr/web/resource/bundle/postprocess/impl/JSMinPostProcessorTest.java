package test.net.jawr.web.resource.bundle.postprocess.impl;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Properties;

import junit.framework.TestCase;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.impl.JSMinPostProcessor;
import test.net.jawr.web.resource.bundle.MockJoinableResourceBundle;

public class JSMinPostProcessorTest extends TestCase {

    /**
     * Test the ability to compress javascript using JSMin. 
     */
    public void testPostProcessBundle() {
		String script = "//comment\n        \talert('αιρν')";
		Charset charset = Charset.forName("UTF-8");
		JawrConfig config = new JawrConfig("js", new Properties());
		config.setCharsetName("UTF-8");
		JSMinPostProcessor processor = new JSMinPostProcessor();
		StringBuffer sb = new StringBuffer();
		try {
			sb.append(script.getBytes(charset.name()));
		} catch (UnsupportedEncodingException ignore) {
			fail("UnsupportedEncodingException that will never be thrown");
		}
	    BundleProcessingStatus status = new BundleProcessingStatus(BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, getBundle("/myBundle.js"),null,config);
		StringBuffer ret = processor.postProcessBundle(status, new StringBuffer(script));
		
		// Not really testing JSMin, that is supposed to work. 
		assertEquals("\nalert('αιρν');", ret.toString());
	}
    
    public void testPostProcessAnonymousFunc() {
		String script = "!function() { console.log(1) }()";
		Charset charset = Charset.forName("UTF-8");
		JawrConfig config = new JawrConfig("js", new Properties());
		config.setCharsetName("UTF-8");
		JSMinPostProcessor processor = new JSMinPostProcessor();
		StringBuffer sb = new StringBuffer();
		try {
			sb.append(script.getBytes(charset.name()));
		} catch (UnsupportedEncodingException ignore) {
			fail("UnsupportedEncodingException that will never be thrown");
		}
	    BundleProcessingStatus status = new BundleProcessingStatus(BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, getBundle("/myBundle.js"),null,config);
		StringBuffer ret = processor.postProcessBundle(status, new StringBuffer(script));
		
		// Not really testing JSMin, that is supposed to work. 
		assertEquals("!function(){console.log(1)}();", ret.toString());
	}
    
    public JoinableResourceBundle getBundle(final String id){
    	return new MockJoinableResourceBundle() {
			
			public String getId() {
				return id;
			}
		};
    }

}
