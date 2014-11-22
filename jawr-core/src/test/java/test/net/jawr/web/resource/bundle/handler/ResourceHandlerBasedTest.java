package test.net.jawr.web.resource.bundle.handler;

import java.io.BufferedReader;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import junit.framework.TestCase;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.handler.bundle.ResourceBundleHandler;
import net.jawr.web.resource.handler.bundle.ServletContextResourceBundleHandler;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.resource.handler.reader.ServletContextResourceReaderHandler;
import test.net.jawr.web.FileUtils;
import test.net.jawr.web.servlet.mock.MockServletContext;

/**
 *
 * @author jhernandez
 */
public abstract class ResourceHandlerBasedTest  extends  TestCase {
    
    protected static final String TMP_DIR = "tmp/";
	protected static final String WORK_DIR = "work/";
	
	protected JawrConfig config;
	
	protected ResourceReaderHandler createResourceReaderHandler(String rootDir,String resourceType,Charset charset) {

		    GeneratorRegistry generatorRegistry = new GeneratorRegistry(resourceType);
		    JawrConfig config = new JawrConfig(resourceType, new Properties());
		    config.setCharsetName(charset.name());
		    config.setGeneratorRegistry(generatorRegistry);
		    generatorRegistry.setConfig(config);
		    return createResourceReaderHandler(rootDir, resourceType, charset, config);
	}
	
	protected ResourceReaderHandler createResourceReaderHandler(String rootDir,String resourceType,Charset charset, JawrConfig config) {
		try {
		    FileUtils.createDir(rootDir);

		    String work = FileUtils.createDir(rootDir + WORK_DIR).getCanonicalPath().replaceAll("%20", " ");
		    MockServletContext ctx = new MockServletContext(work, rootDir + TMP_DIR);
		    this.config = config; 
		    return new ServletContextResourceReaderHandler(ctx, config, config.getGeneratorRegistry());
		} catch (Exception ex) {
		     ex.printStackTrace();
		   throw new RuntimeException(ex);
		}
	}
	
	protected ResourceBundleHandler createResourceBundleHandler(String rootDir, Charset charset) {
	
		return createResourceBundleHandler(rootDir, charset, "js");
	}
	
	protected ResourceBundleHandler createResourceBundleHandler(String rootDir,Charset charset, String resourceType) {
		try {
		    FileUtils.createDir(rootDir);

		    String work = FileUtils.createDir(rootDir + WORK_DIR).getCanonicalPath().replaceAll("%20", " ");
		    String temp = FileUtils.createDir(rootDir + TMP_DIR).getCanonicalPath().replaceAll("%20", " ");
		    MockServletContext ctx = new MockServletContext(work, temp);
		    
		    GeneratorRegistry generatorRegistry = new GeneratorRegistry();
		    JawrConfig config = new JawrConfig(resourceType, new Properties());
		    config.setCharsetName(charset.name());
			return new ServletContextResourceBundleHandler(ctx, charset, generatorRegistry, resourceType);
		} catch (Exception ex) {
		     ex.printStackTrace();
		   throw new RuntimeException(ex);
		}
	}
//	protected FileSystemResourceHandler createResourceHandler(String rootDir,Charset charset) {
//	try {
//	    FileUtils.createDir(rootDir);
//
//	    File tmp = FileUtils.createDir(rootDir + TMP_DIR);
//
//	    String work = FileUtils.createDir(rootDir + WORK_DIR).getCanonicalPath().replaceAll("%20", " ");
//	    MockServletContext ctx = new MockServletContext(work, rootDir + TMP_DIR);
//	    
//	    return new FileSystemResourceHandler(work, tmp, charset, new GeneratorRegistry(), null);
//	} catch (Exception ex) {
//	     ex.printStackTrace();
//	   throw new RuntimeException(ex);
//	}
//	}
	

	protected String fullyReadReader(Reader rd)  throws Exception {
		BufferedReader brd = new BufferedReader(rd);
		StringBuffer sb = new StringBuffer();
		String t;
		while((t = brd.readLine()) != null)
			sb.append(t);
		return sb.toString();
	}
	

	protected String fullyReadChannel(ReadableByteChannel channel, String charsetName)  throws Exception {
		
		GZIPInputStream gzIn = new GZIPInputStream(Channels.newInputStream(channel));
		ReadableByteChannel chan = Channels.newChannel(gzIn);
		Reader rd = Channels.newReader(chan, charsetName);
		String res =  fullyReadReader(rd);
		rd.close();
		return res;
	}	
    
}
