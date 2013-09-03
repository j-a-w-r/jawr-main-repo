/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package test.net.jawr.web.resource.bundle.postprocess.impl;

import java.io.ByteArrayOutputStream;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.InclusionPattern;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.JoinableResourceBundleImpl;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandlerImpl;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.impl.LicensesIncluderPostProcessor;
import net.jawr.web.resource.handler.bundle.ResourceBundleHandler;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import test.net.jawr.web.resource.bundle.handler.ResourceHandlerBasedTest;
/**
 *
 * @author jhernandez
 */
public class LicensesIncluderPostProcessorTest  extends  ResourceHandlerBasedTest{

	private static final String ROOT_TESTDIR = "/licenseprocessor/";
	private LicensesIncluderPostProcessor processor;
	private JoinableResourceBundle resourcebundle;
	private ResourceReaderHandler rsHandler;
	private ResourceBundleHandler rsBundleHandler;
	private JawrConfig jeesConfig;
	
	public LicensesIncluderPostProcessorTest() {
	    try {			
		Charset charsetUtf = Charset.forName("UTF-8"); 
		rsHandler = createResourceReaderHandler(ROOT_TESTDIR,"js",charsetUtf);
		rsBundleHandler = createResourceBundleHandler(ROOT_TESTDIR,charsetUtf);
		jeesConfig = new JawrConfig("js", new Properties());
		GeneratorRegistry generatorRegistry = new GeneratorRegistry(JawrConstant.JS_TYPE);
		generatorRegistry.setConfig(jeesConfig);
		jeesConfig.setGeneratorRegistry(generatorRegistry);
		jeesConfig.setCharsetName("UTF-8");
		
		List<String> c = Collections.singletonList("js/**");
		resourcebundle = new JoinableResourceBundleImpl("script.js","script",
										"js",
										new InclusionPattern(true,0),
										c,
										rsHandler, generatorRegistry);
		
		processor = new LicensesIncluderPostProcessor();


	    } catch (Exception e) {
		    System.out.println("Error in test constructor");
		    e.printStackTrace();
	    }
	}
	
	/**
	 * Test the ability to include license files in a bundle. 
	 * @throws Exception 
	 */
	public void testDoPostProcessBundle() throws Exception {
	    List<JoinableResourceBundle> cols = new ArrayList<JoinableResourceBundle>();
	    cols.add(resourcebundle);
	    
	    ResourceBundlesHandler collector = new ResourceBundlesHandlerImpl(cols, rsHandler, rsBundleHandler, jeesConfig);
	    collector.initAllBundles();
	    ByteArrayOutputStream baOs = new ByteArrayOutputStream();
	    WritableByteChannel wrChannel = Channels.newChannel(baOs);
	    Writer writer = Channels.newWriter(wrChannel, jeesConfig.getResourceCharset().name());
	    collector.writeBundleTo("/js/script.js", writer);
	    BundleProcessingStatus status = new BundleProcessingStatus(BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, resourcebundle,rsHandler,jeesConfig);
	    StringBuffer sb = processor.postProcessBundle(status, new StringBuffer(baOs.toString(jeesConfig.getResourceCharset().name())));
	    String contents = sb.toString();
	    assertTrue("License in root folder not included",(contents.indexOf("/** License in folder **/")!=-1));
	    assertTrue("License in subfolder not included",(contents.indexOf("/** License in subfolder **/")!=-1));
	
	}
}
