/**
 * Copyright 2009 Ibrahim Chaehoi
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.PostProcessFactoryConstant;

import org.apache.log4j.Logger;

/**
 * This class defines the Css Bundle Charset cleaner.
 * For every external CSS, the user can defines a charset using @charset "name-of-the-charset".
 * Only one charset declaration is authorized in a Css file.
 * When Jawr create a bundle for the CSS, the charset declaration are by default concatenated in the bundle.
 * So by default, at the end of the bundling process, the bundle contains multiple declaration of the charset.
 * This post processor will remove all the charset declaration except the first one.  
 *  
 * @author Ibrahim Chaehoi
 *
 */
public class CssCharsetFilterPostProcessor  extends AbstractChainedResourceBundlePostProcessor {

	/** The logger */
	private static Logger LOGGER = Logger.getLogger(CssCharsetFilterPostProcessor.class);
	
	private static final String CHARSET_DECLARATION_SUFFIX = "\";";

	private static final String CHARSET_DECLARATION_PREFIX = "@charset \"";

	/** The charset declaration pattern */
	private static Pattern CHARSET_DECLARATION = Pattern.compile("@charset \"(.+)\";");
	
	/**
	 * Constructor
	 */
	public CssCharsetFilterPostProcessor() {
		super(PostProcessFactoryConstant.CSS_CHARSET_FILTER);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor#doPostProcessBundle(net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus, java.lang.StringBuffer)
	 */
	protected StringBuffer doPostProcessBundle(BundleProcessingStatus status,
			StringBuffer bundleData) throws IOException {
		
		Matcher matcher = CHARSET_DECLARATION.matcher(bundleData.toString());
		StringBuffer sb = new StringBuffer();
		String currentCharset = null;
		boolean charsetDefinedAtBegining = false;
		while(matcher.find()) {
			if(matcher.start() == 0){
				currentCharset = matcher.group(1);
				charsetDefinedAtBegining = true;
			}else{
				if(currentCharset != null){
					if(!currentCharset.equalsIgnoreCase(matcher.group(1))){
						LOGGER.warn("The bundle '"+status.getCurrentBundle().getId()+"' contains CSS with different charset declaration.");
					}
				}else{
					currentCharset = matcher.group(1);
					LOGGER.warn("For the bundle '"+status.getCurrentBundle().getId()+"', the charset declaration is not defined at the top. The charset which will be set is '"+currentCharset+"'.");
				}
				
				matcher.appendReplacement(sb, "");
			}
		}
		matcher.appendTail(sb);
		
		// Put the declaration on top
		if(currentCharset!= null && !charsetDefinedAtBegining){
			sb = new StringBuffer(CHARSET_DECLARATION_PREFIX+currentCharset+CHARSET_DECLARATION_SUFFIX+"\n"+sb.toString());
		}
		return sb;
	}

}
