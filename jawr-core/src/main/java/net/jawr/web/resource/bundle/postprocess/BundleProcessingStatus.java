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
package net.jawr.web.resource.bundle.postprocess;

import java.util.HashMap;
import java.util.Map;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.resource.bundle.variant.VariantUtils;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

/**
 * This class encapsulates the status of a bundling process. It is meant to let 
 * postprocessors have metadata available about the processed data
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class BundleProcessingStatus {
	
	/** */
	public static final String FILE_PROCESSING_TYPE = "file";
	
	public static final String BUNDLE_PROCESSING_TYPE = "bundle";
	
	/** The current bundle */
	private final JoinableResourceBundle currentBundle;
	
	/** The resource reader */
	private final ResourceReaderHandler rsReader;
	
	/** The Jawr config */
	private final JawrConfig jawrConfig;
	
	/** The last path added */
	private String lastPathAdded;
	
	/** The flag indicating if the post processor must search for post process variants or not */
	private boolean searchingPostProcessorVariants = true;
	
	/** The map of current variants of the bundle to process  */
	private Map<String, String> bundleVariants = new HashMap<String, String>();
	
	/** The map of variants which must be generated from the post processors */
	private Map<String, VariantSet> postProcessVariants = new HashMap<String, VariantSet>();
	
	/** The map containing the data used by the processor */
	private Map<String, Object> dataMap = new HashMap<String, Object>();
	
	/** The processing type (bundle, file)*/
	private String processingType;
	
	/**
	 * Constructor
	 * @param currentBundle the current bundle
	 * @param rsHandler the resource handler
	 * @param jawrConfig the Jawr config
	 */
	public BundleProcessingStatus(final String processingType, final JoinableResourceBundle currentBundle,
			final ResourceReaderHandler rsHandler,final JawrConfig jawrConfig) {
		super();
		this.processingType = processingType;
		this.currentBundle = currentBundle;
		this.rsReader = rsHandler;
		this.jawrConfig = jawrConfig;
	}
	
	/**
	 * Constructor
	 * @param status the bundle processing status
	 */
	public BundleProcessingStatus(BundleProcessingStatus status) {
		super();
		this.processingType = status.processingType;
		this.currentBundle = status.currentBundle;
		this.rsReader = status.rsReader;
		this.jawrConfig = status.jawrConfig;
		this.dataMap = status.dataMap;
		this.bundleVariants = status.bundleVariants;
		this.lastPathAdded = status.lastPathAdded;
		this.searchingPostProcessorVariants = status.searchingPostProcessorVariants;
	}
	
	/**
	 * Returns the processing type
	 * @return the processingType
	 */
	public String getProcessingType() {
		return processingType;
	}

	/**
	 * Set the processing type
	 * @param processingType the processingType to set
	 */
	public void setProcessingType(String processingType) {
		this.processingType = processingType;
	}

	/**
	 * Returns the last (current) resource path added to the bundle. 
	 * @return The last (current) resource path added to the bundle. 
	 */
	public String getLastPathAdded() {
		return lastPathAdded;
	}
	
	/**
	 * Sets the last (current) resource path added to the bundle. 
	 * @param lastPathAdded the path to set
	 */
	public void setLastPathAdded(String lastPathAdded) {
		this.lastPathAdded = lastPathAdded;
	}
	
	/**
	 * Returns the currently processed bundle. 
	 * @return currently processed bundle. 
	 */
	public JoinableResourceBundle getCurrentBundle() {
		return currentBundle;
	}
	
	/**
	 * Returns the resource handler
	 * @return the resource handler
	 */
	public ResourceReaderHandler getRsReader() {
		return rsReader;
	}

	/**
	 * Returns the current Jawr config
	 * @return the current Jawr config
	 */
	public JawrConfig getJawrConfig() {
		return jawrConfig;
	}
	
	/**
	 * Returns true if we are searching for post processor variants.
	 * @return true if we are searching for post processor variants.
	 */
	public boolean isSearchingPostProcessorVariants() {
		return searchingPostProcessorVariants;
	}

	/**
	 * Sets the flag indicating if we are searching for post processor variants.
	 * @param searchingPostProcessVariants the flag to set
	 */
	public void setSearchingPostProcessorVariants(boolean searchingPostProcessorVariants) {
		this.searchingPostProcessorVariants = searchingPostProcessorVariants;
	}

	/**
	 * Returns the current bundle variants used for the processing
	 * @return the bundle variants
	 */
	public Map<String, String> getBundleVariants() {
		return bundleVariants;
	}

	/**
	 * Sets the current bundle variants used for the processing
	 * @param bundleVariants the bundle variants to set
	 */
	public void setBundleVariants(Map<String, String> bundleVariants) {
		this.bundleVariants = bundleVariants;
	}

	/**
	 * Returns the current variant for the variant type specified in parameter
	 * @param variantType the variant type
	 * @return the current variant
	 */
	public String getVariant(String variantType) {
		String variant = null;
		if (bundleVariants != null) {
			variant = (String) bundleVariants.get(variantType);
		}
		return variant;
	}
	
	/**
	 * Returns the extra bundle variants generated by the post processors
	 * @return the variants
	 */
	public Map<String, VariantSet> getPostProcessVariants() {
		return postProcessVariants;
	}

	/**
	 * Add a post process variant
	 * @param variantType the variant type
	 * @param variantSet the variant set
	 */
	public void addPostProcessVariant(String variantType, VariantSet variantSet){
		
		Map<String, VariantSet> variantMap = new HashMap<String, VariantSet>();
		variantMap.put(variantType, variantSet);
		addPostProcessVariant(variantMap);
	}
	
	/**
	 * Add a post process variant
	 * @param variantType the variant type
	 * @param variantSet the variant set
	 */
	public void addPostProcessVariant(Map<String, VariantSet> variants){
		
		if(!searchingPostProcessorVariants){
			throw new BundlingProcessException("You are not allowed to define post process variants if we are not searching for post processor variants.");
		}
		postProcessVariants = VariantUtils.concatVariants(postProcessVariants, variants);
	}
	
	/**
	 * Sets the data using the key
	 * @param key the key
	 * @param value the value
	 */
	public void putData(String key, Object value){
		dataMap.put(key, value);
	}
	
	/**
	 * Gets the data from its key
	 * @param key th key
	 * @return the data
	 */
	public Object getData(String key){
		return dataMap.get(key);
	}
	
}
