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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.util.StringUtils;

/**
 * Standard implementation of ResourceBundlePathsIterator. Uses a ConditionalCommentCallbackHandler
 * to signal the use of conditional comments. It is meant to use in production mode, thus the 
 * paths returned are those of the bundled files. 
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class PathsIteratorImpl extends AbstractPathsIterator implements ResourceBundlePathsIterator {

	/** The bundle iterator */
	private Iterator<JoinableResourceBundle> bundlesIterator;
	
	/** The current bundle */
	private JoinableResourceBundle currentBundle;
	
	/**
	 * Constructor
	 * @param bundles the bundle
	 * @param commentCallbackHandler the comment callback handler
	 * @param variants the variant map
	 */
	public PathsIteratorImpl(List<JoinableResourceBundle> bundles,ConditionalCommentCallbackHandler commentCallbackHandler, 
			Map<String, String> variants) {
		super(commentCallbackHandler,variants);
		
		List<JoinableResourceBundle> nonDebugOnlyBundles = filterBundlesToRender(bundles);
		this.bundlesIterator = nonDebugOnlyBundles.iterator();
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
			if(!bundle.getInclusionPattern().isIncludeOnlyOnDebug()){
				filteredBundles.add(bundle);
			}
		}
		return filteredBundles;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.iterator.ResourceBundlePathsIterator#addIterationEventListener(net.jawr.web.resource.bundle.iterator.BundleIterationListener)
	 */
	public void addIterationEventListener(ConditionalCommentCallbackHandler listener) {
		this.commentCallbackHandler = listener;

	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.iterator.ResourceBundlePathsIterator#nextPath()
	 */
	public BundlePath nextPath() {
		
		currentBundle = bundlesIterator.next();
		
		if(null != currentBundle.getExplorerConditionalExpression())
			commentCallbackHandler.openConditionalComment(currentBundle.getExplorerConditionalExpression());
		
		String name = currentBundle.getId();
	
		BundlePath bundlePath = null;
		
		String productionURL = currentBundle.getAlternateProductionURL();
		if(StringUtils.isEmpty(productionURL)){
			bundlePath = new BundlePath(currentBundle.getBundlePrefix(), PathNormalizer.joinPaths(currentBundle.getURLPrefix(variants),name), false);
		}else{
			bundlePath = new BundlePath(currentBundle.getBundlePrefix(), productionURL, true);
		}
		
		return bundlePath;
	}


	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		
		boolean hasNext = bundlesIterator.hasNext();
		if(null != currentBundle && null != currentBundle.getExplorerConditionalExpression())
			commentCallbackHandler.closeConditionalComment();
		
		return hasNext;
	}

}
