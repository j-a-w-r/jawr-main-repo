package net.jawr.maven;

import java.io.File;

import junit.framework.TestCase;

import org.junit.Assert;


/**
 * Bundle processor test case
 * 
 * @author Ibrahim Chaehoi
 */
public class JawrMojoTestCase extends TestCase {

	public void testBundleProcessing() throws Exception{
		
		
		JawrMojo test = new JawrMojo();
		String rootDir = getClass().getResource("/").getFile();
		test.setRootPath(rootDir+"/baseDir");
		test.setTempDirPath(rootDir+"/tmpDir");
		test.setDestDirPath(rootDir+"/destDir");
		
		test.execute();
		
		String bundlePath = rootDir+"/destDir/jawrTmp/text/cssJawrPath/fwk/core/component.css";
		Assert.assertTrue("Bundle has not been created", new File(bundlePath).exists());
	}
	
}
