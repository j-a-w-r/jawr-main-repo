package test.net.jawr.web.resource.bundle.sorting;

import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import net.jawr.web.resource.bundle.sorting.SortFileParser;

public class SortFileParserTest extends TestCase {

	public void testGetSortedResources() {
		String sortFile = "global\nlib/\nsomeFile.js";
		
		Set<String> res = new HashSet<String>();
		res.add("/someFile.js");
		res.add("/lib/");
		res.add("/someOtherFile.js");
		res.add("/global/");
		res.add("/someDifferentFile.xls");
		res.add("/somedir/");
		StringReader rd = new StringReader(sortFile);
		String baseDir = "/js";
		
		SortFileParser parser = new SortFileParser(rd,res,baseDir);
		List<String> data = parser.getSortedResources();
		
		makeAssertions(res, data);

		assertFalse("Sorted file was not removed from available resources /someFile.js", 
				res.contains("/someFile.js"));
		assertFalse("Sorted dir was not removed from available resources /global/", 
				res.contains("/global/"));
		assertFalse("Sorted dir was not removed from available resources /lib/", 
				res.contains("/lib/"));
		
		// Test different format in available resources
		res = new HashSet<String>();
		res.add("/someFile.js");
		res.add("/lib");
		res.add("/someOtherFile.js");
		res.add("/global");
		res.add("/someDifferentFile.xls");
		res.add("/somedir");
		rd = new StringReader(sortFile);
		
		
		parser = new SortFileParser(rd,res,baseDir);
		data = parser.getSortedResources();
		
		makeAssertions(res, data);

		assertFalse("Sorted file was not removed from available resources /someFile.js", 
				res.contains("/someFile.js"));
		assertFalse("Sorted dir was not removed from available resources /global", 
				res.contains("/global"));
		assertFalse("Sorted dir was not removed from available resources /lib", 
				res.contains("/lib"));
		
	}

	private void makeAssertions(Set<String> res, List<String> data) {
		assertTrue("Expected mapping /js/global not added at the expected order",
					data.get(0).equals("/js/global"));
		assertTrue("Expected mapping /js/lib not added at the expected order",
				data.get(1).equals("/js/lib"));
		assertTrue("Expected mapping /js/global not added at the expected order",
				data.get(2).equals("/js/someFile.js"));

		assertFalse("Unexpected file mapped /js/someOtherFile.js", 
						data.contains("/js/someOtherFile.js"));
		assertFalse("Unexpected file mapped /js/someDifferentFile.xls", 
						data.contains("/js/someDifferentFile.xls"));
		assertFalse("Unexpected dir mapped /js/somedir", 
				data.contains("/js/somedir"));
		
	}

}
