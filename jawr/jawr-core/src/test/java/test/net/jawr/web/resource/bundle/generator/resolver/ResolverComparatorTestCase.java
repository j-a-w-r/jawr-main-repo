/**
 * Copyright 2016 Ibrahim Chaehoi
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package test.net.jawr.web.resource.bundle.generator.resolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;

import net.jawr.web.resource.bundle.generator.resolver.PrefixedPathResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResolverComparator;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.SuffixedPathResolver;

/**
 * Unit tests for ResolverComparator
 * 
 * @author Ibrahim Chaehoi
 */
public class ResolverComparatorTestCase {

	/**
	 * Test method for {@link net.jawr.web.resource.bundle.generator.resolver.ResolverComparator#compare(net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver, net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver)}.
	 */
	@Test
	public void testCompare() {
		ResourceGeneratorResolver resolver1 = new PrefixedPathResolver("test1");
		ResourceGeneratorResolver resolver2 = new SuffixedPathResolver("test2");
		
		List<ResourceGeneratorResolver> resolvers = new ArrayList<>();
		resolvers.add(resolver1);
		resolvers.add(resolver2);
		
		ResolverComparator comparator = new  ResolverComparator();
		Collections.sort(resolvers, comparator);
		
		assertEquals(resolver2, resolvers.get(0));
		assertEquals(resolver1, resolvers.get(1));
		
	}

}
