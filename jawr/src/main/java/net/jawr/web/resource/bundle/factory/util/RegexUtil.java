/**
 * Copyright 2008 Jordi Hernández Sellés
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
package net.jawr.web.resource.bundle.factory.util;

/**
 * Utils to work with regular expressions and matchers
 * @author Jordi Hernández Sellés
 */
public class RegexUtil {
	
	/**
	 * Fixes a bad problem with regular expression replacement strings. 
	 * Replaces \ and $ for escaped versions for regex replacement. 
	 * This was somehow fixed in java 5 (a similar method was added). Since Jawr supports 1.4, 
	 * this method is used instead.  
	 * 
	 * @param replacement
	 * @return
	 */
	public static String adaptReplacementToMatcher(String replacement) {
		// Double the backslashes, so they are left as they are after replacement. 
		String result = replacement.replaceAll("\\\\", "\\\\\\\\");
		// Add backslashes after dollar signs 
		result = result.replaceAll("\\$", "\\\\\\$");
		return result;
	}

}
