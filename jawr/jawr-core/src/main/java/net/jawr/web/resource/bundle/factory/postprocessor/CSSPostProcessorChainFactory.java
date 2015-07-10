/**
 * Copyright 2007-2009 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.factory.postprocessor;

import net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.ChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.PostProcessFactoryConstant;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.impl.CSSCombineMediaPostProcessor;
import net.jawr.web.resource.bundle.postprocess.impl.CSSImportPostProcessor;
import net.jawr.web.resource.bundle.postprocess.impl.CSSMinPostProcessor;
import net.jawr.web.resource.bundle.postprocess.impl.CSSURLPathRewriterPostProcessor;
import net.jawr.web.resource.bundle.postprocess.impl.CssCharsetFilterPostProcessor;
import net.jawr.web.resource.bundle.postprocess.impl.css.base64.Base64ImageEncoderPostProcessor;
import net.jawr.web.resource.bundle.postprocess.impl.yui.YUICSSCompressor;

/**
 * PostProcessorChainFactory for css resources. 
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 *
 */
public class CSSPostProcessorChainFactory extends
		AbstractPostProcessorChainFactory implements PostProcessorChainFactory {

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.factory.processor.PostProcessorChainFactory#buildDefaultProcessorChain()
	 */
	public ResourceBundlePostProcessor buildDefaultProcessorChain() {
		ChainedResourceBundlePostProcessor processor = new CSSMinPostProcessor();
		processor.addNextProcessor(buildLicensesProcessor());
		return processor;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.factory.processor.PostProcessorChainFactory#buildDefaultUnitProcessorChain()
	 */
	public ResourceBundlePostProcessor buildDefaultUnitProcessorChain() {
		
		// The default unit post processor is CSSImport,CSSIrlPathRewriter
		ChainedResourceBundlePostProcessor processor = new CSSImportPostProcessor();
		processor.addNextProcessor(new CSSURLPathRewriterPostProcessor());
		return processor;
	}
	
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.factory.processor.PostProcessorChainFactory#getPostProcessor(java.lang.String)
	 */
	protected AbstractChainedResourceBundlePostProcessor buildProcessorByKey(String processorKey){
		
		if (PostProcessFactoryConstant.LICENSE_INCLUDER.equals(processorKey))
			return buildLicensesProcessor();
		else if(PostProcessFactoryConstant.CSS_MINIFIER.equals(processorKey))
			return new CSSMinPostProcessor();
		else if(PostProcessFactoryConstant.CSS_IMPORT.equals(processorKey))
			return new CSSImportPostProcessor();
		else if(PostProcessFactoryConstant.CSS_CHARSET_FILTER.equals(processorKey))
			return new CssCharsetFilterPostProcessor();
		else if(PostProcessFactoryConstant.CSS_COMBINE_MEDIA.equals(processorKey))
			return new CSSCombineMediaPostProcessor();
		else if (PostProcessFactoryConstant.URL_PATH_REWRITER.equals(processorKey))
			return new CSSURLPathRewriterPostProcessor();
		else if (PostProcessFactoryConstant.BASE64_IMAGE_ENCODER.equals(processorKey))
			return new Base64ImageEncoderPostProcessor();
		else if (PostProcessFactoryConstant.YUI_COMPRESSOR.equals(processorKey))
			return new YUICSSCompressor();
		
		else throw new IllegalArgumentException("The supplied key [" + processorKey + "] is not bound to any ResourceBundlePostProcessor. Please check the documentation for valid keys. ");
	}
}
