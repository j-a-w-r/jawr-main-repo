/**
 * 
 */
package test.net.jawr.web.resource.bundle.sorting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.when;

import net.jawr.web.resource.bundle.InclusionPattern;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.sorting.GlobalResourceBundleComparator;

/**
 * @author jhernandez
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class GlobalResourceBundleComparatorTest {

	@Test
	public void testSortGlobalResourceBundles() {

		List<JoinableResourceBundle> unsorted = new ArrayList<JoinableResourceBundle>();
		unsorted.add(createMockBundle("four", 4));
		unsorted.add(createMockBundle("two", 2));
		unsorted.add(createMockBundle("one", 1));
		unsorted.add(createMockBundle("three", 3));
		Collections.sort(unsorted, new GlobalResourceBundleComparator());

		String name = ((JoinableResourceBundle) unsorted.get(0)).getId();
		assertTrue("Sorted list at position 0 does not match expected", name.equals("one"));
		name = ((JoinableResourceBundle) unsorted.get(1)).getId();
		assertTrue("Sorted list at position 1 does not match expected", name.equals("two"));
		name = ((JoinableResourceBundle) unsorted.get(2)).getId();
		assertTrue("Sorted list at position 2 does not match expected", name.equals("three"));
		name = ((JoinableResourceBundle) unsorted.get(3)).getId();
		assertTrue("Sorted list at position 3 does not match expected", name.equals("four"));

	}

	private JoinableResourceBundle createMockBundle(final String name, final int index) {

		JoinableResourceBundle bundle = Mockito.mock(JoinableResourceBundle.class);
		when(bundle.getId()).thenReturn(name);
		when(bundle.getInclusionPattern()).thenReturn(new InclusionPattern(true, index));
		return bundle;
	}

}
