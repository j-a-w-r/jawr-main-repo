/**
 * Copyright 2014 Ibrahim Chaehoi
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

package test.net.jawr.web;

import net.jawr.web.util.StringUtils;

import org.junit.Assert;

/**
 * @author Ibrahim Chaehoi
 */
public class TestUtils {

	/**
	 * Assert that two contents are equals without taking in account the line feed character.
	 * @param msg the message
	 * @param expected the expected String to check
	 * @param actual the actual String to check
	 */
	public static void assertContentEquals(String msg, String expected, String actual){
		Assert.assertEquals(msg, StringUtils.normalizeLineFeed(expected), StringUtils.normalizeLineFeed(actual));
	}
	
	
	/**
	 * Gets the java version as a float value (1.4, 1.5, 1.6, ....)
	 * 
	 * @return the java version
	 */
	public static float getJavaVersion () {
	    String version = System.getProperty("java.version");
	    int pos = version.indexOf('.');
	    pos = version.indexOf('.', pos+1);
	    return Float.parseFloat(version.substring (0, pos));
	}
}
