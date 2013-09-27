/**
 * Copyright 2007-2012 Jordi Hernández Sellés, ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.sorting;

import java.io.Serializable;
import java.util.Comparator;

import net.jawr.web.resource.bundle.JoinableResourceBundle;

/**
 * Implementation of Comparator interface used to determine the inclusion order
 * of global bundles. Compares the inclusion order attribute of each bundle's 
 * InclusionPattern attribute.  
 * 
 * @author Jordi Hernández Sellés
 * @author ibrahim Chaehoi
 *
 */
public class GlobalResourceBundleComparator implements Comparator<JoinableResourceBundle>, Serializable {

	/** The serial version UID */
	private static final long serialVersionUID = -277897413409167116L;

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(T, T)
	 */
	public int compare(JoinableResourceBundle bundleA, JoinableResourceBundle bundleB) {
		Integer a = new Integer(bundleA.getInclusionPattern().getInclusionOrder());
		Integer b = new Integer(bundleB.getInclusionPattern().getInclusionOrder());
		return a.compareTo(b);
	}

}
