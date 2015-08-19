/**
 * Copyright 2015 Ibrahim Chaehoi
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
 
import static org.junit.Assert.*;

import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.resolver.WebJarsLocatorPathResolver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Ibrahim Chaehoi
 */
@RunWith(MockitoJUnitRunner.class)
public class WebJarsLocatorPathResolverTest {

	private WebJarsLocatorPathResolver resolver;
	
	@Before
	public void setUp(){
		resolver = new WebJarsLocatorPathResolver(GeneratorRegistry.WEBJARS_GENERATOR_PREFIX+GeneratorRegistry.PREFIX_SEPARATOR);
	}

	@Test
	public void testResolverWithFullPath(){
		String result = resolver.getResourcePath("webjars:/bootstrap/3.2.0/css/bootstrap.css");
		assertEquals(result, "/bootstrap/3.2.0/css/bootstrap.css");
	}
	
	@Test
	public void testResolverWithShortPath(){
		String result = resolver.getResourcePath("webjars:/css/bootstrap.css");
		assertEquals(result, "/bootstrap/3.2.0/css/bootstrap.css");
	}
	
	@Test
	public void testResolverWithFileName(){
		String result = resolver.getResourcePath("webjars:/bootstrap.css");
		assertEquals(result, "/bootstrap/3.2.0/css/bootstrap.css");
	}
	
	@Test
	public void testResolverWithSpecifiedWebJars(){
		String result = resolver.getResourcePath("webjars:/css/bootstrap.css[bootstrap]");
		assertEquals(result, "/bootstrap/3.2.0/css/bootstrap.css");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testResolverWithIncorrectSpecifiedWebJars(){
		String result = resolver.getResourcePath("webjars:/css/bootstrap.css[jquery]");
		assertEquals(result, "/bootstrap/3.2.0/css/bootstrap.css");
	}
	
}
