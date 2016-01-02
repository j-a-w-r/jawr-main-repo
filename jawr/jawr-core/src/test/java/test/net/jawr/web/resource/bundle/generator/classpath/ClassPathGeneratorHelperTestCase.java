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
package test.net.jawr.web.resource.bundle.generator.classpath;

import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.*;

import net.jawr.web.resource.bundle.generator.classpath.ClassPathGeneratorHelper;
import test.net.jawr.web.FileUtils;

/**
 * The ClassPathGeneratorHelper Test Case
 * 
 * @author Ibrahim Chaehoi
 */
public class ClassPathGeneratorHelperTestCase {

	private ClassPathGeneratorHelper helper;
	
	@Test
	public void testGetResourcesNameFromClassOnFileSystem(){
		helper = new ClassPathGeneratorHelper();
		Set<String> resources = helper.getResourceNames("generator/classpath/");
		assertEquals(6, resources.size());
		assertTrue(resources.contains("a file with space.css"));
		assertTrue(resources.contains("css/"));
		assertTrue(resources.contains("expected/"));
		assertTrue(resources.contains("img/"));
		assertTrue(resources.contains("temp.css"));
		assertTrue(resources.contains("temp.less"));
	}
	
	@Test
	public void testGetResourcesNameFromJar(){
		helper = new ClassPathGeneratorHelper();
		Set<String> resources = helper.getResourceNames("META-INF/resources/webjars/bootstrap/3.2.0/");
		assertEquals(5, resources.size());
		assertTrue(resources.contains("css/"));
		assertTrue(resources.contains("fonts/"));
		assertTrue(resources.contains("js/"));
		assertTrue(resources.contains("less/"));
		assertTrue(resources.contains("webjars-requirejs.js"));
	}
	
	@Test
	public void testGetFilePathFromFileSystem() throws Exception{
		helper = new ClassPathGeneratorHelper();
		String filePath = helper.getFilePath("generator/classpath/temp.css");
		assertEquals(FileUtils.getClassPathFile("generator/classpath/temp.css").getAbsolutePath(), filePath);
	
		filePath = helper.getFilePath("generator/classpath/a file with space.css");
		assertEquals(FileUtils.getClassPathFile("generator/classpath/a file with space.css").getAbsolutePath(), filePath);
	}
	
	@Test
	public void testGetFilePathFromJar(){
		helper = new ClassPathGeneratorHelper();
		String filePath = helper.getFilePath("META-INF/resources/webjars/bootstrap/3.2.0/webjars-requirejs.js");
		assertTrue(filePath.replace('\\', '/').endsWith("org/webjars/bootstrap/3.2.0/bootstrap-3.2.0.jar"));
	}
}
