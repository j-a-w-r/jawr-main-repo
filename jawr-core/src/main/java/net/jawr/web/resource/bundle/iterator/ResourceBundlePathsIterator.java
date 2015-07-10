/**
 * Copyright 2008-2014 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.iterator;

import java.util.Iterator;

/**
 * An iterator specialized in doing recursion over the 
 * list of paths that should be included in pages whenever
 * a bundle is invoked on a page.  
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public interface ResourceBundlePathsIterator extends Iterator<BundlePath> {

	/**
	 * @return Next path belonging to the bundle. 
	 */
	public BundlePath nextPath();
}
