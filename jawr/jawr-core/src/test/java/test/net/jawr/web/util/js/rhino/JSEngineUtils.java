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

package test.net.jawr.web.util.js.rhino;

import javax.script.ScriptEngineManager;

import org.slf4j.Logger;

/**
 * An utility class for JS engine
 * 
 * @author Ibrahim Chaehoi
 */
public class JSEngineUtils {

	/**
	 * Checks if a JS engine is available
	 * @param engineName the JS engine name
	 * @param logger the logger 
	 * @return true if the JS engine is available
	 */
	public static boolean isJsEngineAvailable(String engineName, Logger logger) {
		
		boolean engineExists = new ScriptEngineManager().getEngineByName(engineName) != null;
		if (!engineExists) {
			logger.warn("The JS engine '" + engineName
					+ "' is not available for tests.");
		}
		return engineExists;
	}

}
