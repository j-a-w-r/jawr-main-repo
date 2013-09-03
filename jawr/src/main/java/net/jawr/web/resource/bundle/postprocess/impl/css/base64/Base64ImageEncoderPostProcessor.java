/**
 * Copyright 2010-2012 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.postprocess.impl.css.base64;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.PostProcessFactoryConstant;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.impl.CSSURLPathRewriterPostProcessor;
import net.jawr.web.resource.bundle.postprocess.impl.PostProcessorCssImageUrlRewriter;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.util.StringUtils;

import org.apache.log4j.Logger;

/**
 * This class defines the post processor which encodes resources (background images) using base64 encoding.
 * 
 * @author Gerben Jorna
 * @author Ibrahim Chaehoi
 */
public class Base64ImageEncoderPostProcessor extends
	CSSURLPathRewriterPostProcessor implements ResourceBundlePostProcessor {

	/** The logger */
	protected static final Logger LOGGER = 
		Logger.getLogger(Base64ImageEncoderPostProcessor.class);

	/** Tab */
	protected static final String TAB = "\t";

	/** Boundary separator */
	protected static final String BOUNDARY_SEPARATOR = "JAWR_BASE64_ENCODED_IMAGES";

	/** The background property url value start */
	protected static final String PROPERTY_URL_VALUE_START = "url(";
	
	/** The background property url value end */
	protected static final String PROPERTY_URL_VALUE_END = ")";
	
	/** Boundary separator prefix */
	protected static final String BOUNDARY_SEPARATOR_PREFIX = "--";
		
	/** The mhtml prefix */
	protected static final String MHTML_PREFIX = "mhtml:";

	/**
	 * Constructor
	 */
	public Base64ImageEncoderPostProcessor() {
		super(PostProcessFactoryConstant.BASE64_IMAGE_ENCODER);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.postprocess.impl.CSSURLPathRewriterPostProcessor#createImageUrlRewriter(net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus)
	 */
	protected PostProcessorCssImageUrlRewriter createImageUrlRewriter(
			BundleProcessingStatus status) {
		
		return new Base64PostProcessorCssImageUrlRewriter(status);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.postprocess.impl.CSSURLPathRewriterPostProcessor#doPostProcessBundle(net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus, java.lang.StringBuffer)
	 */
	@SuppressWarnings("unchecked")
	protected StringBuffer doPostProcessBundle(BundleProcessingStatus status, StringBuffer bundleData) throws IOException {
	
		if(LOGGER.isInfoEnabled()){
			LOGGER.info("Base64 encoding resources - '" + status.getLastPathAdded() + "'");
		}
		
		Map<String, Base64EncodedResource> encodedResources = (Map<String, Base64EncodedResource>) status.getData(JawrConstant.BASE64_ENCODED_RESOURCES);
		if(encodedResources == null){
			encodedResources = new HashMap<String, Base64EncodedResource>();
			status.putData(JawrConstant.BASE64_ENCODED_RESOURCES, encodedResources);
		}
		
		StringBuffer sb = bundleData;
		if(status.getProcessingType().equals(BundleProcessingStatus.FILE_PROCESSING_TYPE)){
			sb = super.doPostProcessBundle(status, bundleData);
		}
		
		if(!encodedResources.isEmpty() && status.isSearchingPostProcessorVariants()){
			VariantSet variantSet = new VariantSet(JawrConstant.BROWSER_VARIANT_TYPE, "", new String[]{"", JawrConstant.BROWSER_IE6,JawrConstant.BROWSER_IE7});
			status.addPostProcessVariant(JawrConstant.BROWSER_VARIANT_TYPE, variantSet);
			variantSet = new VariantSet(JawrConstant.CONNECTION_TYPE_VARIANT_TYPE, "", new String[]{"", JawrConstant.SSL});
			status.addPostProcessVariant(JawrConstant.CONNECTION_TYPE_VARIANT_TYPE, variantSet);
		}
		
		if(!status.isSearchingPostProcessorVariants() && status.getProcessingType().equals(BundleProcessingStatus.BUNDLE_PROCESSING_TYPE)){
				
			Map<String, String> bundleVariants = status.getBundleVariants();
			if(bundleVariants != null){
				String browser = bundleVariants.get(JawrConstant.BROWSER_VARIANT_TYPE);
				if(StringUtils.isNotEmpty(browser) && (JawrConstant.BROWSER_IE6.equals(browser) || JawrConstant.BROWSER_IE7.equals(browser))){
					prependBase64EncodedResources(sb, encodedResources);
				}
			}
		}
		
		if(LOGGER.isInfoEnabled()){
			LOGGER.info("Base64 Postprocessing done");
		}
		return sb;
	}

	
	/**
	 * Prepend the base64 encoded resources to the bundle data
	 * @param sb the string buffer containing the processed bundle data
	 * @param encodedImages a map of encoded images
	 */
	protected void prependBase64EncodedResources(StringBuffer sb, Map<String, Base64EncodedResource> encodedImages) {
		Iterator<Entry<String, Base64EncodedResource>> it = encodedImages.entrySet().iterator();
		StringBuffer mhtml = new StringBuffer();
		String lineSeparator = net.jawr.web.util.StringUtils.LINE_SEPARATOR;
		mhtml.append("/*!" + lineSeparator);
		mhtml.append("Content-Type: multipart/related; boundary=\"" + BOUNDARY_SEPARATOR + "\"" + lineSeparator + lineSeparator);
		
		while (it.hasNext()) {
			Entry<String, Base64EncodedResource> pair = it.next();
	        Base64EncodedResource encodedResource = (Base64EncodedResource) pair.getValue();
	        mhtml.append(BOUNDARY_SEPARATOR_PREFIX + BOUNDARY_SEPARATOR + lineSeparator);
	        mhtml.append("Content-Type:" + encodedResource.getType() + lineSeparator);
	        mhtml.append("Content-Location:" + encodedResource.getId() + lineSeparator);
	        mhtml.append("Content-Transfer-Encoding:base64" + lineSeparator + lineSeparator);
	        mhtml.append(encodedResource.getBase64Encoding() + lineSeparator + lineSeparator);
	    }		
		
		mhtml.append(BOUNDARY_SEPARATOR_PREFIX + BOUNDARY_SEPARATOR + BOUNDARY_SEPARATOR_PREFIX + lineSeparator);
		mhtml.append("*/" + lineSeparator + lineSeparator);
        sb.insert(0, mhtml.toString());
	}
}
