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
package net.jawr.web.minification;

/**
 * This class defines the result of a compression process
 * 
 * @author Ibrahim Chaehoi
 */
public class CompressionResult {

	/** The compressed code */
	private final String code;
	
	/** The code source map */
	private final String sourceMap;
	
	/**
	 * Constructor
	 * @param code the compressed resource
	 * @param sourceMap the source map
	 */
	public CompressionResult(String code, String sourceMap) {
		super();
		this.code = code;
		this.sourceMap = sourceMap;
	}
	
	/**
	 * Returns the compressed resource
	 * @return the compressed resource
	 */
	public String getCode() {
		return code;
	}
	
	/**
	 * Returns the source map
	 * @return the source map
	 */
	public String getSourceMap() {
		return sourceMap;
	}
	
}
