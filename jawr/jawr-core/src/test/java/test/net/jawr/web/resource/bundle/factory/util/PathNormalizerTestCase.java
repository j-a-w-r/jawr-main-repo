package test.net.jawr.web.resource.bundle.factory.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.renderer.BundleRenderer;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test case class for PathNormalizer utility class
 * @author Ibrahim Chaehoi
 */
public class PathNormalizerTestCase {

	private static final String SEP = "/";

	@Test
	public void testJoinDomainToPath(){
		
		assertEquals("https://mydomain.com/myContent/css/folder/myStyle.css",PathNormalizer.joinDomainToPath("https://mydomain.com/myContent","/css/folder/myStyle.css"));
		assertEquals("https://mydomain.com/myContent/css/folder/myStyle.css",PathNormalizer.joinPaths("https://mydomain.com/myContent","/css/folder/myStyle.css"));
		
	}
	
	@Test
	public void testJoinPaths(){
		assertEquals("/myContent/css/folder/myStyle.css",PathNormalizer.joinPaths("/myContent","/css/folder/myStyle.css"));
	}
	
	@Test
	public void testGetParentPath() {

		assertEquals("", PathNormalizer.getParentPath(null));
		assertEquals("/usr/", PathNormalizer.getParentPath("/usr/local/"));
		assertEquals("/usr/local/bin/", PathNormalizer.getParentPath("/usr/local/bin/java.sh"));
		assertEquals("/", PathNormalizer.getParentPath("/"));

	}

	@Test
	public void testGetRelativePath() throws Exception {
		assertEquals(PathNormalizer.getRelativePath(null, null), "");
		assertEquals(PathNormalizer.getRelativePath(null, "/usr/local/java/bin"), "");
		assertEquals(PathNormalizer.getRelativePath("/usr/local/", null), "");
		assertEquals(PathNormalizer.getRelativePath("/usr/local/", "/usr/local/java/bin"), "..");
		assertEquals(PathNormalizer.getRelativePath("/usr/local/", "/usr/local/java/bin/java.sh"), "../..");
		assertEquals(PathNormalizer.getRelativePath("/usr/local/java/bin/java.sh", "/usr/local/"), "");
	}

	// -----------------------------------------------------------------------
	@Test
	public void testConcat() {
		assertEquals(null, PathNormalizer.concatWebPath("", null));
		assertEquals(null, PathNormalizer.concatWebPath(null, null));
		assertEquals(null, PathNormalizer.concatWebPath(null, ""));
		assertEquals(null, PathNormalizer.concatWebPath(null, "a"));
		assertEquals(SEP + "a", PathNormalizer.concatWebPath(null, "/a"));

		assertEquals("/css/folder/subfolder/icons/img.png", PathNormalizer.concatWebPath("/css/folder/subfolder/", "icons/img.png"));
		assertEquals("/css/folder/subfolder/icons/img.png", PathNormalizer.concatWebPath("/css/folder/subfolder/style.css", "icons/img.png"));
		assertEquals("/css/icons/img.png", PathNormalizer.concatWebPath("/css/folder/", "../icons/img.png"));
		assertEquals("/css/icons/img.png", PathNormalizer.concatWebPath("/css/folder/style.css", "../icons/img.png"));

		assertEquals("f" + SEP, PathNormalizer.concatWebPath("", "f/"));
		assertEquals("f", PathNormalizer.concatWebPath("", "f"));
		assertEquals("a" + SEP + "f" + SEP, PathNormalizer.concatWebPath("a/", "f/"));
		assertEquals("a" + SEP + "f", PathNormalizer.concatWebPath("a", "f"));
		assertEquals("a" + SEP + "b" + SEP + "f" + SEP, PathNormalizer.concatWebPath("a/b/", "f/"));

		assertEquals("a" + SEP + "f" + SEP, PathNormalizer.concatWebPath("a/b/", "../f/"));
		assertEquals("f", PathNormalizer.concatWebPath("a/b", "../f"));
		assertEquals("a" + SEP + "c" + SEP + "g" + SEP, PathNormalizer.concatWebPath("a/b/../c/", "f/../g/"));
		assertEquals("a" + SEP + "g", PathNormalizer.concatWebPath("a/b/../c", "f/../g"));

		assertEquals("a" + SEP + "f", PathNormalizer.concatWebPath("a/c.txt", "f"));

		assertEquals(SEP + "f" + SEP, PathNormalizer.concatWebPath("", "/f/"));
		assertEquals(SEP + "f", PathNormalizer.concatWebPath("", "/f"));
		assertEquals("a" + SEP + "f" + SEP, PathNormalizer.concatWebPath("a/", "/f/"));
		assertEquals("a" + SEP + "f", PathNormalizer.concatWebPath("a", "/f"));

		assertEquals("a" + SEP + "b" + SEP + "c" + SEP + "d", PathNormalizer.concatWebPath("a/b/", "/c/d"));
	}

	@Test
	public void testRemoveVariantPrefixFromPath(){
		
		assertEquals("/js/bundle/msg@en_US.js", PathNormalizer.removeVariantPrefixFromPath("/1542603560.en_US/js/bundle/msg.js"));
		assertEquals("/fwk/core/component@en_US@summer.css", PathNormalizer.removeVariantPrefixFromPath("/1576054120.en_US@summer/fwk/core/component.css"));
		assertEquals("/js/bundle/msg.js", PathNormalizer.removeVariantPrefixFromPath("/1542603560/js/bundle/msg.js"));
	}
	
	@Test
	public void testExtractBundleInfoFromPath(){
		
		List<String> bundlePrefixes = new ArrayList<>();
		String[] pathInfos = PathNormalizer.extractBundleInfoFromPath("/1542603560.en_US/js/bundle/msg.js", bundlePrefixes);
		assertEquals(Arrays.asList(null, "/js/bundle/msg.js","en_US", "1542603560"), Arrays.asList(pathInfos));
		pathInfos = PathNormalizer.extractBundleInfoFromPath("/1576054120.en_US@summer/fwk/core/component.css",bundlePrefixes);
		assertEquals(Arrays.asList(null, "/fwk/core/component.css","en_US@summer", "1576054120"), Arrays.asList(pathInfos));
		pathInfos = PathNormalizer.extractBundleInfoFromPath("/1542603560/js/bundle/msg.js",bundlePrefixes);
		assertEquals(Arrays.asList(null, "/js/bundle/msg.js",null, "1542603560"), Arrays.asList(pathInfos));
		
		pathInfos = PathNormalizer.extractBundleInfoFromPath(BundleRenderer.GZIP_PATH_PREFIX+"1542603560/js/bundle/msg.js",bundlePrefixes);
		assertEquals(Arrays.asList(null, "/js/bundle/msg.js",null, "1542603560"), Arrays.asList(pathInfos));
		
		pathInfos = PathNormalizer.extractBundleInfoFromPath(BundleRenderer.GZIP_PATH_PREFIX+"1576054120.en_US@summer/fwk/core/component.css",bundlePrefixes);
		assertEquals(Arrays.asList(null, "/fwk/core/component.css","en_US@summer", "1576054120"), Arrays.asList(pathInfos));
		
		bundlePrefixes.add("/jawr/");
		pathInfos = PathNormalizer.extractBundleInfoFromPath("/jawr/1576054120.en_US@summer/fwk/core/component.css",bundlePrefixes);
		assertEquals(Arrays.asList("/jawr/", "/fwk/core/component.css","en_US@summer", "1576054120"), Arrays.asList(pathInfos));
		
		pathInfos = PathNormalizer.extractBundleInfoFromPath("/jawr/"+BundleRenderer.GZIP_PATH_PREFIX+"1576054120.en_US@summer/fwk/core/component.css",bundlePrefixes);
		assertEquals(Arrays.asList("/jawr/", "/fwk/core/component.css","en_US@summer", "1576054120"), Arrays.asList(pathInfos));
		
	}
	
	@Test
	public void testExtractBinaryResourceInfoFromPath(){
		String[] resourceInfo = PathNormalizer.extractBinaryResourceInfo("/cb33421345/img/myLogo.png");
		assertEquals(Arrays.asList("/img/myLogo.png", "cb33421345"), Arrays.asList(resourceInfo));
		
		resourceInfo = PathNormalizer.extractBinaryResourceInfo("/jar_cb33421345/img/myLogo.png");
		assertEquals(Arrays.asList("jar:/img/myLogo.png", "jar_cb33421345"), Arrays.asList(resourceInfo));
	}
	
	@Test
	public void testConcatWebPath(){
		
		assertEquals("/img/logo.png", PathNormalizer.concatWebPath("/css/generator/one.css", "../../img/logo.png"));
	}
	
	@Test
	public void checkNormalizePath(){
		
		Assert.assertTrue(PathNormalizer.isNormalized("/webapp/js/gzip_8762387/bundle/commonBundle.js"));
		Assert.assertFalse(PathNormalizer.isNormalized("/webapp/js/gzip_8762387/bundle/commonBundle.js/../../temp"));
		Assert.assertFalse(PathNormalizer.isNormalized("/webapp/js/gzip_8762387/bundle/commonBundle.js/./temp"));
	}
}
