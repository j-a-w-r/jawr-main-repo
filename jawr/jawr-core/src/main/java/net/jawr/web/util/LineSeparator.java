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
package net.jawr.web.util;

/**
 * This class defines the lineseparator enumeration
 * 
 * @author Ibrahim Chaehoi
 */
public enum LineSeparator {

	CRLF ("\r\n"),
	LF ("\r\n"),
	AUTO (System.getProperty("line.separator"));
	
	private String value;
	
	/**Constructor
	 * @param value the line separator value
	 */
	LineSeparator(String value){
		this.value = value;
	}
	
	/**
	 * Returns the line separator
	 * @return the line separator
	 */
	public String getLineSeparator(){
		return value;
	}

	@Override
	public String toString() {
		return getLineSeparator();
	}
	
	
}
