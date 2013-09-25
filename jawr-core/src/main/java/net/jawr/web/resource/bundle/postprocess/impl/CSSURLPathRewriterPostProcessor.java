/**
 * Copyright 2007-2012 Jordi Hern�ndez Sell�s, Ibrahim Chaehoi
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

import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.generator.ResourceGenerator;
import net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.PostProcessFactoryConstant;

/**
 * Single file postprocessor used to rewrite CSS URLs according to the new relative locations of the references when
 * added to a bundle. Since the path changes, the URLs must be rewritten accordingly.  
 * URLs in css files are expected to be according to the css spec (see http://www.w3.org/TR/REC-CSS2/syndata.html#value-def-uri). 
 * Thus, single double, or no quotes enclosing the url are allowed (and remain as they are after rewriting). Escaped parens and quotes 
 * are allowed within the url.   
 *  
 * @author Jordi Hern�ndez Sell�s
 * @author Ibrahim Chaehoi
 */
public class CSSURLPathRewriterPostProcessor extends
		AbstractChainedResourceBundlePostProcessor {
	
	/** This variable is used to fake the gzip prefix for the full bundle path */
	private static final String FAKE_BUNDLE_PREFIX = "/prefix/";

	/**
	 * Constructor
	 */
	public CSSURLPathRewriterPostProcessor() {
		super(PostProcessFactoryConstant.URL_PATH_REWRITER);
	}
	
	/**
	 * Constructor
	 * The post processor ID
	 */
	protected CSSURLPathRewriterPostProcessor(String id) {
		super(id);
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.postprocess.impl.AbstractChainedResourceBundlePostProcessor#doPostProcessBundle(net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus, java.lang.StringBuffer)
	 */
	protected StringBuffer doPostProcessBundle(BundleProcessingStatus status,
			StringBuffer bundleData) throws IOException {
		
		// Retrieve the full bundle path, so we will be able to define the relative path for the css images
		String fullBundlePath = getFinalFullBundlePath(status);
		PostProcessorCssImageUrlRewriter urlRewriter = createImageUrlRewriter(status);
		return urlRewriter.rewriteUrl(status.getLastPathAdded(), fullBundlePath, bundleData.toString());
	}

	/**
	 * Creates the image URL rewriter
	 * @param status the bundle processing status
	 * @return the image URL rewriter
	 */
	protected PostProcessorCssImageUrlRewriter createImageUrlRewriter(
			BundleProcessingStatus status) {
		return new PostProcessorCssImageUrlRewriter(status);
	}
	
	/**
	 * Returns the full path for the CSS bundle, taking in account the css servlet path if defined, 
	 * the caching prefix, and the url context path overriden
	 *   
	 * @return the full bundle path
	 */
	public String getFinalFullBundlePath(BundleProcessingStatus status) {

		JawrConfig jawrConfig = status.getJawrConfig();
		String fullBundlePath = null;
		String bundleName = status.getCurrentBundle().getId();
		
		// Generation the bundle prefix
		String bundlePrefix = "";
		if(!bundleName.equals(ResourceGenerator.CSS_DEBUGPATH)){
			bundlePrefix = FAKE_BUNDLE_PREFIX;
		}
		
		// Add path reference for the servlet mapping if it exists 
		if(! "".equals(jawrConfig.getServletMapping())){
			bundlePrefix = PathNormalizer.asPath(jawrConfig.getServletMapping()+bundlePrefix)+"/";
			
		}  
		
		// Concatenate the bundle prefix and the bundle name
		fullBundlePath = PathNormalizer.concatWebPath(bundlePrefix, bundleName);
		
		return fullBundlePath;
	}

}
