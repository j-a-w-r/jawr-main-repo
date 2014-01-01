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
package net.jawr.web.resource.bundle;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.variant.VariantSet;

/**
 * Represents a group of related resources which will be referred to by 
 * a single name.  
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public interface JoinableResourceBundle {
	
	/** The licencing file name */
	public static final String LICENSES_FILENAME = ".license";
	
	/** The sorting file name */
	public static final String SORT_FILE_NAME = ".sorting";
		
	/**
	 * Returns the bundle name 
	 * @return the bundle name
	 */
	public String getName();
	
	/**
	 * Returns the ID for this bundle. The URL, which will identify the bundle.
	 * It will normally end with .js or .css, 
	 * since it will be used to refer to the bundle in URLs. 
	 * @return the ID for this bundle
	 */
	public String getId();
	
	/**
	 * Returns true if the bundle if composite
	 * @return true if the bundle if composite
	 */
	public boolean isComposite();
	
	/**
	 * Get the InclusionPattern to determine when/if this bundle should be
	 * included with current configuration. 
	 * @return the InclusionPattern
	 */
	public InclusionPattern getInclusionPattern();
	
	/**
	 * Sets the mappings, which should be used for the bundle
	 * @param mappings the mapping of the resources of the bundle
	 */
	public void setMappings(List<String> mappings);
	
	/**
	 * Returns an ordered list with all the items pertaining to this bundle. 
	 * @return an ordered list with all the items pertaining to this bundle. 
	 */
	public List<String> getItemPathList();
	
	/**
	 * Returns an ordered list with all the items pertaining to this bundle. 
	 * @return an ordered list with all the items pertaining to this bundle. 
	 */
	public List<String> getItemDebugPathList();
	
	/**
	 * Returns a set with the license files to include with this bundle. 
	 * @return a set with the license files to include with this bundle. 
	 */
	public Set<String> getLicensesPathList();
	
	/**
	 * Determines if an item path belongs to the bundle. 
	 * @param itemPath the item path
	 * @return true if an item path belongs to the bundle. 
	 */
	public boolean belongsToBundle(String itemPath);
        
    /**
     * Get the URL prefix for this Bundle. It is used to force redownloading
     * when needed. 
     * @return the URL prefix for this Bundle.
     */
    public String getURLPrefix(Map<String, String> variants);
    
    /**
     * Get the postprocessor to use in resources before adding them to the bundle
     * @return the postprocessor to use in resources before adding them to the bundle
     */
    public ResourceBundlePostProcessor getUnitaryPostProcessor();
    
    
    /**
     * Get the postprocesor to use once all files are joined. 
     * @return the postprocesor to use once all files are joined. 
     */
    public ResourceBundlePostProcessor getBundlePostProcessor();
    
    /**
     * Returns the bundle data hashcode
     * @param variantKey the variant key
     * @return the bundle data hashcode
     */
    public String getBundleDataHashCode(String variantKey);
    
    /**
     * Set the hashcode of the string representing the bundled files. 
     * Used to generate an automatic version url prefix. 
     * 
     * @param the variant key
     * @param hashCode the string representation of the hash code where the minus is replace by a "N" character.
     */
    public void setBundleDataHashCode(String variantKey, String bundleDataHashCode);
	
    
    /**
     * For bundles to be included for IE only within a conditional 
     * comment, this method returns the expression to use.  
     * @return The expression for the conditional comment, or null 
     * if the bundle should be included for all browsers. 
     */
    public String getExplorerConditionalExpression();
    
       
    /**
     * Returns the map of variants by type for this bundle. 
     * @return the map of variants by type for this bundle. 
     */
    public Map<String, VariantSet> getVariants();
    
    /**
     * Sets the map of variants by type for this bundle. 
     * @param the map of variants by type for this bundle. 
     */
    public void setVariants(Map<String, VariantSet> variants);
    
    /**
     * Returns the list of variant keys. 
     * @return the list of variant keys. 
     */
    public List<String> getVariantKeys();
    
    /**
     * Returns an ordered list with all the items pertaining to this bundle, with the variations 
     * corresponding to the specified variant key. 
     * @param variants the variant map
     * @return an ordered list with all the items pertaining to this bundle
     */
    public List<String> getItemPathList(Map<String, String> variants);
    
    /**
     * Returns for the debug mode an ordered list with all the items pertaining to this bundle, with the variations 
     * corresponding to the specified variant key. 
     * @param variants the variant map
     * @return an ordered list with all the items pertaining to this bundle
     */
    public List<String> getItemDebugPathList(Map<String, String> variants);
    
    /**
	 * Returns the bundle dependencies
	 * @return the bundle dependencies
	 */
	public List<JoinableResourceBundle> getDependencies();

	/**
	 * Sets the bundle dependencies
	 * @param the bundle dependencies to set
	 */
	public void setDependencies(List<JoinableResourceBundle> bundleDependencies);

	/**
     * If set, it will force the tag libraries to render a static URL in production mode. 
     * @return
     */
    public String getAlternateProductionURL();

	/**
	 * Sets the bundle postprocessor
	 * @param bundlePostProcessor the bundle postprocessor to set
	 */
	public void setBundlePostProcessor(
			ResourceBundlePostProcessor bundlePostProcessor);

	/**
	 * Sets the unitary postprocessor
	 * @param unitaryPostProcessor the unitary postprocessor to set
	 */
	public void setUnitaryPostProcessor(
			ResourceBundlePostProcessor unitaryPostProcessor);
	
}
