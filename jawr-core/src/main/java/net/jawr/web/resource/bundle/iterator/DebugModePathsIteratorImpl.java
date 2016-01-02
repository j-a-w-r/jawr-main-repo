/**
 * Copyright 2008-2016 Jordi Hernández Sellés, Ibrahim Chaehoi
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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.util.StringUtils;

/**
 * Debug mode implementation of ResourceBundlePathsIterator. Uses a ConditionalCommentCallbackHandler
 * to signal the use of conditional comments. The paths returned are those of the individual 
 * members of the bundle. 
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class DebugModePathsIteratorImpl extends AbstractPathsIterator implements ResourceBundlePathsIterator {

	/** The bundle iterator */
	private Iterator<JoinableResourceBundle> bundlesIterator;
	
	/** The path iterator */
	private Iterator<BundlePath> pathsIterator;
	
	/** The current bundle */
	private JoinableResourceBundle currentBundle;
	
	/**
	 * Constructor
	 * @param bundles the list of bundle
	 * @param callbackHandler the comment callback handler
	 * @param variants the variants
	 */
	public DebugModePathsIteratorImpl(List<JoinableResourceBundle> bundles,ConditionalCommentCallbackHandler callbackHandler,
			Map<String, String> variants) {
		super(callbackHandler,variants);
		this.bundlesIterator = bundles.iterator();
	}
	

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.iterator.ResourceBundlePathsIterator#nextPath()
	 */
	public BundlePath nextPath() {
		
		BundlePath path = null;
		if(null == pathsIterator || !pathsIterator.hasNext()) {
			currentBundle = (JoinableResourceBundle) bundlesIterator.next();
			
			if(null != currentBundle.getExplorerConditionalExpression()){
				commentCallbackHandler.openConditionalComment(currentBundle.getExplorerConditionalExpression());
			}
			
			if(StringUtils.isNotEmpty(currentBundle.getDebugURL())){
				pathsIterator = Arrays.asList(new BundlePath(currentBundle.getBundlePrefix(), currentBundle.getDebugURL(), true)).iterator();
			}else{
				pathsIterator = currentBundle.getItemDebugPathList(variants).iterator();
			}
		}
		
		
		if(pathsIterator != null && pathsIterator.hasNext()){
			path = pathsIterator.next();
		}
		
		return path;
	}


	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		if(null != pathsIterator && !pathsIterator.hasNext()) {
			if(null != currentBundle && null != currentBundle.getExplorerConditionalExpression())
				commentCallbackHandler.closeConditionalComment();
		}
		boolean rets = false;
		if(null != pathsIterator) {
			rets = pathsIterator.hasNext() || bundlesIterator.hasNext();
		}
		else{
			rets = bundlesIterator.hasNext();
		}
			
		return rets;
	}

}
