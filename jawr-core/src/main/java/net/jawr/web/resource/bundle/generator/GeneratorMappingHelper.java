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

package net.jawr.web.resource.bundle.generator;

/**
 * This class defines a helper class to handle generator mapping
 * 
 * @author Ibrahim Chaehoi
 */
public class GeneratorMappingHelper {

	/** The parentheses regexp finder */
	private static final String PARENFINDER_REGEXP = ".*(\\(.*\\)).*";

	/** The brackets regexp finder */
	private static final String BRACKFINDER_REGEXP = ".*(\\[.*\\]).*";

	/** The path requested */
	private String path;

	/** The values in parentheses */
	private String parenthesesParam;

	/** The values in brackets */
	private String bracketsParam;

	/**
	 * Constructor
	 * @param mapping the resource mapping
	 */
	public GeneratorMappingHelper(String resourceMapping) {
		this.path = resourceMapping;
		// init parameters, if any
		if (path.matches(PARENFINDER_REGEXP)) {
			parenthesesParam = path.substring(path.indexOf('(') + 1,
					path.indexOf(')'));

			path = path.substring(0, path.indexOf('('))
					+ path.substring(path.indexOf(')') + 1);
		}
		if (path.matches(BRACKFINDER_REGEXP)) {
			bracketsParam = path.substring(path.indexOf('[') + 1,
					path.indexOf(']'));

			path = path.substring(0, path.indexOf('['))
					+ path.substring(path.indexOf(']') + 1);
		}
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the parenthesesParam
	 */
	public String getParenthesesParam() {
		return parenthesesParam;
	}

	/**
	 * @param parenthesesParam the parenthesesParam to set
	 */
	public void setParenthesesParam(String parenthesesParam) {
		this.parenthesesParam = parenthesesParam;
	}

	/**
	 * @return the bracketsParam
	 */
	public String getBracketsParam() {
		return bracketsParam;
	}

	/**
	 * @param bracketsParam the bracketsParam to set
	 */
	public void setBracketsParam(String bracketsParam) {
		this.bracketsParam = bracketsParam;
	}
}
