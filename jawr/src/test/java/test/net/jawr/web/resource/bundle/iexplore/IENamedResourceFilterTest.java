package test.net.jawr.web.resource.bundle.iexplore;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import net.jawr.web.resource.bundle.iexplore.IENamedResourceFilter;

public class IENamedResourceFilterTest extends TestCase {

	

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testFilterPathSet() {
		Set<String> tst = new HashSet<String>();
		
		tst.add("/js/some.js");
		tst.add("/js/ot_ie_her.js");
		tst.add("/js/some_ie.js");
		tst.add("/js/some_lt_6_ie.js");
		tst.add("/js/some_gte_6.00_ie.js");
		
		Map<String, List<String>> t = new IENamedResourceFilter().filterPathSet(tst);
		
		assertEquals(3,t.keySet().size());
		assertTrue(t.keySet().contains("[if IE]"));
		assertTrue(t.keySet().contains("[if lt IE 6]"));
		assertTrue(t.keySet().contains("[if gte IE 6.00]"));
		
	}

}
