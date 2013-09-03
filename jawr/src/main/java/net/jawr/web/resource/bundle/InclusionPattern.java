/**
 * Copyright 2007-2012 Jordi Hernández Sellés, Ibrahim CHAEHOI
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
 * Inclusion pattern for bundles. Indicates wether a bundle should be
 * considered global (a library), the order of inclusion if it is global, and
 * the behavior for debugging. 
 * 
 * @author Jordi Hernández Sellés
 * @author ibrahim Chaehoi
 */
public class InclusionPattern {
	
	private DebugInclusion inclusion;
	
	private boolean isGlobal;
	private int inclusionOrder;

	/**
	 * Create a new inclusion pattern for a bundle
	 * @param isGlobal If true, the bundle will be included before every other bundle. 
	 * @param inclusionOrder When isGlobal is true, this will set the order of inclusion of this bundle with respect to other global bundles. 
	 * @param includeOnDebug If true, this bundle will only be included in debug mode. 
	 * @param excludeOnDebug If true, this bundle will not be included in debug mode. 
	 */
	public InclusionPattern(boolean isGlobal, int inclusionOrder,
			DebugInclusion inclusion) {
		super();
		this.inclusion = inclusion;
		this.isGlobal = isGlobal;
		this.inclusionOrder = inclusionOrder;
	}
        
        /**
	 * Create a new inclusion pattern for a bundle to include in debug and non-debug mode. .
	 * @param isGlobal If true, the bundle will be included before every other bundle. 
	 * @param inclusionOrder When isGlobal is true, this will set the order of inclusion of this bundle with respect to other global bundles. 
	 */
	public InclusionPattern(boolean isGlobal, int inclusionOrder) {
		
		this(isGlobal, inclusionOrder, DebugInclusion.ALWAYS);
	}
    
	/**
	 * Create a new inclusion pattern for a non-global bundle.
	 */
	public InclusionPattern() {
		
		this(false, 0, DebugInclusion.ALWAYS);
	}
	
	/**
	 * Indicates whether a bundle will only be included when debug mode is on. 
	 * @return
	 */
	public boolean isIncludeOnDebug() {
		return inclusion.equals(DebugInclusion.ALWAYS) || inclusion.equals(DebugInclusion.ONLY);
	}
	
	/**
	 * Returns the debug inclusion type
	 * @return the debug inclusion type
	 */
	public DebugInclusion getDebugInclusion() {
		return inclusion;
	}

	/**
	 * Indicates whether a bundle will only be included when debug mode is on. 
	 * @return
	 */
	public boolean isIncludeOnlyOnDebug() {
		return inclusion.equals(DebugInclusion.ONLY);
	}
	
	/**
	 * Indicates whether a bundle will not be included when debug mode is on. 
	 * @return
	 */
	public boolean isExcludeOnDebug() {
		return inclusion.equals(DebugInclusion.NEVER);
	}
	
	/**
	 * Wether a bundle is global, if it is it will always be included on every page. 
	 * @return
	 */
	public boolean isGlobal() {
		return isGlobal;
	}
	
	/**
	 * For global bundles, states the order of inclusion. 
	 * @return
	 */
	public int getInclusionOrder() {
		return inclusionOrder;
	}
	
	
}
