/**
 * Copyright 2012 Ibrahim CHAEHOI
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
package net.jawr.web.resource.bundle;

/**
 * The enumeration defining the bundle inclusion types for debug
 * 
 * @author Ibrahim Chaehoi
 */
public enum DebugInclusion {
	
	ONLY,
	NEVER,
	ALWAYS;
	
	/**
	 * Factory method for inclusion
	 * @param isDebugOnly
	 * @param isDebugNever
	 * @return
	 */
	public static DebugInclusion get( boolean isDebugOnly, boolean isDebugNever){
		
		DebugInclusion inclusion = DebugInclusion.ALWAYS; 
		if(isDebugOnly){
			inclusion = DebugInclusion.ONLY;
		}
		if(isDebugNever){
			inclusion = DebugInclusion.NEVER;
		}
		
		return inclusion;
	}

	
	
}