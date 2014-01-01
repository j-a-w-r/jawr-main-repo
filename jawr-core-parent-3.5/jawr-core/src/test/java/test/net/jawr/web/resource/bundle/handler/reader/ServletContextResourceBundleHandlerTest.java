package test.net.jawr.web.resource.bundle.handler.reader;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Properties;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.resource.handler.reader.ServletContextResourceReaderHandler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import test.net.jawr.web.FileUtils;
import test.net.jawr.web.servlet.mock.MockServletContext;

public class ServletContextResourceBundleHandlerTest {

	protected static final String TMP_DIR = "tmp/";
	protected static final String WORK_DIR = "work/";
	private ResourceReaderHandler rsHandler;
	private String workDir;
	private String tmpDir;
	private String externalBaseContextDir;
	
	@Before
	public void setup() throws Exception{
		workDir = FileUtils.getClasspathRootDir()+"/resourcehandler/work";
	    tmpDir = FileUtils.getClasspathRootDir()+"/resourcehandler/temp";
	    externalBaseContextDir = FileUtils.getClasspathRootDir()+"/resourcehandler/otherDir";
	}
	
	@Test
	public void testStandardConfig() throws Exception{
		
	    initRsReader(new Properties());
	    
	    checkReadResource("temp.js", "/resourcehandler/work/");
	    checkReadResource("/folder/temp.js", "/resourcehandler/work/");
	}
	
	@Test
	public void testBaseDirectoryContext() throws Exception{
		
		Properties prop = new Properties();
		prop.setProperty(JawrConstant.JAWR_BASECONTEXT_DIRECTORY, externalBaseContextDir);
	    initRsReader(prop);
	    
	    checkReadResource("temp.js", "/resourcehandler/work/");
	    checkReadResource("/folder/temp.js", "/resourcehandler/work/");
	    // Test a resource not present in the war content
	    checkReadResource("/folder/temp1.js", "/resourcehandler/otherDir/");
	}
	
	@Test
	public void testBaseDirectoryContextWithPriority() throws Exception{
		
		Properties prop = new Properties();
		prop.setProperty(JawrConstant.JAWR_BASECONTEXT_DIRECTORY, externalBaseContextDir);
		// High priority false => War content takes precedence on external dir base context
		prop.setProperty(JawrConstant.JAWR_BASECONTEXT_DIRECTORY_HIGH_PRIORITY, "false");
	    initRsReader(prop);
	    
	    checkReadResource("temp.js", "/resourcehandler/work/");
	    checkReadResource("/folder/temp.js", "/resourcehandler/work/");
	    // Test a resource not present in the war content
	    checkReadResource("/folder/temp1.js", "/resourcehandler/otherDir/");
	    
	    // High priority true => External dir base context takes precedence on War content 
		prop.setProperty(JawrConstant.JAWR_BASECONTEXT_DIRECTORY_HIGH_PRIORITY, "true");
	    initRsReader(prop);
	    
	    checkReadResource("temp.js", "/resourcehandler/otherDir/");
	    checkReadResource("/folder/temp.js", "/resourcehandler/otherDir/");
	    // Test a resource not present in the war content
	    checkReadResource("/folder/temp1.js", "/resourcehandler/otherDir/");
	}

	private void initRsReader(Properties properties) throws IOException {
		MockServletContext ctx = new MockServletContext(workDir, tmpDir);
	    GeneratorRegistry generatorRegistry = new GeneratorRegistry();
	    JawrConfig config = new JawrConfig("js", properties);
	    config.setGeneratorRegistry(generatorRegistry);
	    generatorRegistry.setConfig(config);
	    rsHandler = new ServletContextResourceReaderHandler(ctx, config, generatorRegistry);
	}
	
	private void checkReadResource(String resourcePath, String expectedResultBaseDir) throws Exception{
		
		String baseDir = expectedResultBaseDir;
		if(baseDir.charAt(0) == '/'){
			baseDir = baseDir.substring(1);
		}
		Reader rd = rsHandler.getResource(resourcePath);
	    StringWriter swr = new StringWriter();
	    IOUtils.copy(rd, swr);
	    Assert.assertEquals(FileUtils.readClassPathFile(baseDir+resourcePath), FileUtils.removeCarriageReturn(swr.getBuffer().toString()));
	}
}
