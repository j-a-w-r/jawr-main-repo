/**
 * 
 */
package test.net.jawr.web.resource.bundle.sorting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;
import net.jawr.web.resource.bundle.InclusionPattern;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.sorting.GlobalResourceBundleComparator;
import test.net.jawr.web.resource.bundle.MockJoinableResourceBundle;

/**
 * @author jhernandez
 *
 */
public class GlobalResourceBundleComparatorTest extends TestCase {
	
	public void testSortGlobalResourceBundles() {
		
		List<JoinableResourceBundle> unsorted = new ArrayList<JoinableResourceBundle>();
		unsorted.add(createMockBundle("four",4));
		unsorted.add(createMockBundle("two",2));
		unsorted.add(createMockBundle("one",1));
		unsorted.add(createMockBundle("three",3));
		Collections.sort(unsorted, new GlobalResourceBundleComparator());
		
		String name = ((JoinableResourceBundle) unsorted.get(0)).getId();
		assertTrue("Sorted list at position 0 does not match expected",name.equals("one"));
		name = ((JoinableResourceBundle) unsorted.get(1)).getId();
		assertTrue("Sorted list at position 1 does not match expected",name.equals("two"));
		name = ((JoinableResourceBundle) unsorted.get(2)).getId();
		assertTrue("Sorted list at position 2 does not match expected",name.equals("three"));
		name = ((JoinableResourceBundle) unsorted.get(3)).getId();
		assertTrue("Sorted list at position 3 does not match expected",name.equals("four"));
		
	}
	
	private JoinableResourceBundle createMockBundle(final String name, final int index) {
		
		return new MockJoinableResourceBundle(){

			public InclusionPattern getInclusionPattern() {
				return new InclusionPattern(true,index);
			}

			public String getId() {
				return name;
			}
		};
	}

}
