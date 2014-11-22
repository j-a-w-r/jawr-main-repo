package test.net.jawr.web.resource.bundle.postprocess.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Properties;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.impl.JSMinPostProcessor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JSMinPostProcessorTest {

	@Mock
	private JoinableResourceBundle bundle;
	
	@Before
	public void setup(){
		
		when(bundle.getId()).thenReturn("/myJsBundle.js");
	}
	
    /**
     * Test the ability to compress javascript using JSMin. 
     */
	@Test
    public void testPostProcessBundle() {
		String script = "//comment\n        \talert('áéñí')";
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

		// getBundle("/myBundle.js")
		BundleProcessingStatus status = new BundleProcessingStatus(BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle,null,config);
		StringBuffer ret = processor.postProcessBundle(status, new StringBuffer(script));
		
		// Not really testing JSMin, that is supposed to work. 
		assertEquals("alert('áéñí');", ret.toString());
	}
    
	@Test
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
			Assert.fail("UnsupportedEncodingException that will never be thrown");
		}
	    BundleProcessingStatus status = new BundleProcessingStatus(BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle,null,config);
		StringBuffer ret = processor.postProcessBundle(status, new StringBuffer(script));
		
		// Not really testing JSMin, that is supposed to work. 
		assertEquals("!function(){console.log(1)}();", ret.toString());
	}
    
	@Test
    public void testPostProcessMultilineString() {
		String script = "!function() { \tconsole.log(\"my message\\\nin multiline\"); }()";
		Charset charset = Charset.forName("UTF-8");
		JawrConfig config = new JawrConfig("js", new Properties());
		config.setCharsetName("UTF-8");
		JSMinPostProcessor processor = new JSMinPostProcessor();
		StringBuffer sb = new StringBuffer();
		try {
			sb.append(script.getBytes(charset.name()));
		} catch (UnsupportedEncodingException ignore) {
			Assert.fail("UnsupportedEncodingException that will never be thrown");
		}
	    BundleProcessingStatus status = new BundleProcessingStatus(BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle,null,config);
		StringBuffer ret = processor.postProcessBundle(status, new StringBuffer(script));
		
		// Not really testing JSMin, that is supposed to work. 
		assertEquals("!function(){console.log(\"my message\\\nin multiline\");}();", ret.toString());
	}
	
	@Test
    public void testPostProcessStringWithTabCharacter() {
		String script = "!function() {		console.log(\"	my message\\\n	my message in multiline\"); }()";
		Charset charset = Charset.forName("UTF-8");
		JawrConfig config = new JawrConfig("js", new Properties());
		config.setCharsetName("UTF-8");
		JSMinPostProcessor processor = new JSMinPostProcessor();
		StringBuffer sb = new StringBuffer();
		try {
			sb.append(script.getBytes(charset.name()));
		} catch (UnsupportedEncodingException ignore) {
			Assert.fail("UnsupportedEncodingException that will never be thrown");
		}
	    BundleProcessingStatus status = new BundleProcessingStatus(BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle,null,config);
		StringBuffer ret = processor.postProcessBundle(status, new StringBuffer(script));
		
		// Not really testing JSMin, that is supposed to work. 
		assertEquals("!function(){console.log(\"\tmy message\\\n\tmy message in multiline\");}();", ret.toString());
	}
	
	@Test
	public void testPostRegularExpression() throws Exception {
		
		String script ="function test(mStyle) { return /(url\\s*\\(.*?){3}/.test(mStyle.background);}";
		Charset charset = Charset.forName("UTF-8");
		JawrConfig config = new JawrConfig("js", new Properties());
		config.setCharsetName("UTF-8");
		JSMinPostProcessor processor = new JSMinPostProcessor();
		StringBuffer sb = new StringBuffer();
		try {
			sb.append(script.getBytes(charset.name()));
		} catch (UnsupportedEncodingException ignore) {
			Assert.fail("UnsupportedEncodingException that will never be thrown");
		}
	    BundleProcessingStatus status = new BundleProcessingStatus(BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle,null,config);
		StringBuffer ret = processor.postProcessBundle(status, new StringBuffer(script));
		
		// Not really testing JSMin, that is supposed to work. 
		assertEquals("function test(mStyle){return/(url\\s*\\(.*?){3}/.test(mStyle.background);}", ret.toString());
	}
	
}
