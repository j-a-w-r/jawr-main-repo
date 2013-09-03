/**
 * Copyright 2007-2012 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.postprocess.impl;

import java.io.IOException;

import net.jawr.web.minification.CSSMinifier;
import net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.PostProcessFactoryConstant;

/**
 * Performs minification on CSS files by removing newlines, expendable whitespace and comments. 
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class CSSMinPostProcessor extends AbstractChainedResourceBundlePostProcessor {
	
	/** The CSS minifier */
	private CSSMinifier minifier;
	
	/**
	 * Constructor
	 */
	public CSSMinPostProcessor() {
		super(PostProcessFactoryConstant.CSS_MINIFIER);
		this.minifier = new CSSMinifier();
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.postprocess.impl.AbstractChainedResourceBundlePostProcessor#doPostProcessBundle(net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus, java.lang.StringBuffer)
	 */
	protected StringBuffer doPostProcessBundle(BundleProcessingStatus status, StringBuffer bundleData) throws IOException {
		try{
			return minifier.minifyCSS(bundleData);
		}catch(StackOverflowError e){
			throw new Error("An error occured while processing the bundle '"+status.getCurrentBundle().getName()+"'" ,e);
		}
		
	}

}
