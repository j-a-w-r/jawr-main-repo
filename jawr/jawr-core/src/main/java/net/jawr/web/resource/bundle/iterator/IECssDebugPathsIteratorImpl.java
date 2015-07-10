/**
 * Copyright 2012 Ibrahim Chaehoi
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.jawr.web.resource.bundle.JoinableResourceBundle;

/**
 * Implementation of ResourceBundlePathsIterator for IE CSS bundle in debug mode. Uses a ConditionalCommentCallbackHandler
 * to signal the use of conditional comments. It is meant to use in production mode, thus the 
 * paths returned are those of the bundled files. 
 * 
 * @author Ibrahim Chaehoi
 */
public class IECssDebugPathsIteratorImpl extends PathsIteratorImpl {

	/**
	 * Constructor
	 * @param bundles the bundle
	 * @param commentCallbackHandler the comment callback handler
	 * @param variants the variant map
	 */
	public IECssDebugPathsIteratorImpl(List<JoinableResourceBundle> bundles,
			ConditionalCommentCallbackHandler commentCallbackHandler,
			Map<String, String> variants) {
		super(bundles, commentCallbackHandler, variants);
	}

	/**
	 * Filters the bundle to render
	 * @param bundles the list of bundles
	 * @return the list of filtered bundle
	 */
	protected List<JoinableResourceBundle> filterBundlesToRender(
			List<JoinableResourceBundle> bundles) {
		List<JoinableResourceBundle> filteredBundles = new ArrayList<JoinableResourceBundle>();
		for(JoinableResourceBundle bundle : bundles){
			if(bundle.getInclusionPattern().isIncludeOnDebug()){
				filteredBundles.add(bundle);
			}
		}
		return filteredBundles;
	}

}
