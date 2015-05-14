package test.net.jawr.web.minification;

import java.io.File;

import junit.framework.TestCase;
import net.jawr.web.minification.CSSMinifier;
import test.net.jawr.web.FileUtils;

public class CSSMinifierTest extends TestCase {
	private String source;
	private String expected;
	private static final String TEST_FOLDER = "/cssminifier";
	
	public void testMinifyCSS() throws Exception {
		
		source = FileUtils.readFile(new File(FileUtils.getClasspathRootDir() + TEST_FOLDER + "/source.css"));
		expected = FileUtils.readFile(new File(FileUtils.getClasspathRootDir() +  TEST_FOLDER +"/expected.css"));
		
		CSSMinifier minifier = new CSSMinifier();
		StringBuffer actual = minifier.minifyCSS(new StringBuffer(source));
		assertEquals("Error in minifier",expected.toString(), actual.toString());
	}
	
	public void test2MinifyCSS() throws Exception {
		
		source = FileUtils.readFile(new File(FileUtils.getClasspathRootDir() + TEST_FOLDER + "/source2.css"));
		expected = FileUtils.readFile(new File(FileUtils.getClasspathRootDir() +  TEST_FOLDER +"/expected2.css"));
		
		CSSMinifier minifier = new CSSMinifier();
		StringBuffer actual = minifier.minifyCSS(new StringBuffer(source));
		assertEquals("Error in minifier",expected.toString(), actual.toString());
	}
	
	public void testMinifyCSSMultiLine() {
		CSSMinifier minifier = new CSSMinifier();
		StringBuffer data = new StringBuffer(".some-class { \n" +
				"  background: transparent\n" +
				"url(image/path);\n" +
				"}");
		StringBuffer actual = minifier.minifyCSS(data);
		StringBuffer result = new StringBuffer(".some-class{background:transparent url(image/path);}");
		
		assertEquals("Error in minifier",result.toString(), actual.toString());
	}
	
}
