/**
 * Copyright 2009-2016 Ibrahim Chaehoi
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

import static net.jawr.web.JawrConstant.COMMA_SEPARATOR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import net.jawr.web.resource.bundle.factory.PropertiesBundleConstant;
import net.jawr.web.resource.bundle.mappings.FilePathMapping;
import net.jawr.web.resource.bundle.mappings.PathMapping;
import net.jawr.web.resource.bundle.postprocess.ChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.util.StringUtils;

/**
 * This class will manage the serialization of the joinable resource bundle.
 * 
 * @author Ibrahim Chaehoi
 * 
 */
public class JoinableResourceBundlePropertySerializer {

	/**
	 * The last modified separator
	 */
	public static final String LAST_MODIFIED_SEPARATOR = "#";
	
	

	/**
	 * Serializes the properties of the bundle in the Properties object
	 * 
	 * This method will serialize all bundle.  
	 * The properties associated to a bundle are the same as the one define in the jawr configuration file.
	 * Only the following properties are different from the standard configuration file :
	 *   - The file mappings, which will contains the file path to each resources which is available on filesystem 
	 *   - The variants which will be explicitly specified.
	 *   - The bundle hash codes will be define as properties, so we will not have to compute them.
	 *   For a bundle with local variants, there will be an hash code for each variant + one, which is the hash 
	 *   code of the default bundle.
	 *   - The licence path list.
	 *   
	 * @param bundle the bundle to serialize
	 * @param props the properties to update
	 */
	public static void serializeInProperties(JoinableResourceBundle bundle,
			String type, Properties props) {


		String bundleName = bundle.getName();
		String prefix = PropertiesBundleConstant.PROPS_PREFIX + type + "."+ PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PROPERTY + bundleName;
		InclusionPattern inclusion = bundle.getInclusionPattern();

		// Set the ID
		if(StringUtils.isNotEmpty(bundle.getId())){
			props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ID, bundle.getId());
		}
		
		if(StringUtils.isNotEmpty(bundle.getBundlePrefix())){
			props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_BUNDLE_PREFIX, bundle.getBundlePrefix());
		}
		props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ID, bundle.getId());
		
		if (inclusion.isGlobal()) {
			props
					.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_GLOBAL_FLAG, Boolean.toString(inclusion
							.isGlobal()));
		}
		if (inclusion.getInclusionOrder() != 0) {
			props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ORDER, Integer.toString(inclusion
					.getInclusionOrder()));
		}

		if (inclusion.isIncludeOnlyOnDebug()) {
			props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGONLY, Boolean.toString(inclusion
					.isIncludeOnlyOnDebug()));
		}
		if (inclusion.isExcludeOnDebug()) {
			props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGNEVER, Boolean.toString(inclusion
					.isExcludeOnDebug()));
		}
		if (StringUtils.isNotEmpty(bundle.getExplorerConditionalExpression())) {
			props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_IE_CONDITIONAL_EXPRESSION, bundle
					.getExplorerConditionalExpression());
		}
		if (StringUtils.isNotEmpty(bundle.getAlternateProductionURL())) {
			props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PRODUCTION_ALT_URL, bundle
					.getAlternateProductionURL());
		}
		if (StringUtils.isNotEmpty(bundle.getDebugURL())) {
			props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUG_URL, bundle
					.getDebugURL());
		}
		
		if (bundle.getBundlePostProcessor() != null) {
			props
					.put(prefix +PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_POSTPROCESSOR,
							getBundlePostProcessorsName((ChainedResourceBundlePostProcessor) bundle
									.getBundlePostProcessor()));
		}

		if (bundle.getUnitaryPostProcessor() != null) {
			props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_FILE_POSTPROCESSOR,
					getBundlePostProcessorsName((ChainedResourceBundlePostProcessor) bundle
							.getUnitaryPostProcessor()));
		}
		
		// Add variants and the bundle hashcode
		Map<String, VariantSet> variants = bundle.getVariants();
		if (variants != null && !variants.isEmpty()) {
			String serializedVariants = serializeVariantSets(variants);
			if (StringUtils.isNotEmpty(serializedVariants)) {
				props.put(prefix +  PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_VARIANTS, serializedVariants);
			}

			List<String> variantKeys = bundle.getVariantKeys();
			for (Iterator<String> iterator = variantKeys.iterator(); iterator
					.hasNext();) {
				String variantKey = iterator.next();
				if (StringUtils.isNotEmpty(variantKey)) {
					props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE_VARIANT + variantKey,
							bundle.getBundleDataHashCode(variantKey));
				}
			}
		} 
			
		String bundleHashcode = bundle.getBundleDataHashCode(null);
		if(bundleHashcode != null){
			props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE, bundleHashcode);
		}
		
		// mapping
		List<PathMapping> pathMappings= bundle.getMappings(); 
		if (pathMappings != null && !pathMappings.isEmpty()) {
			props
					.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_MAPPINGS,
							getCommaSeparatedStringForPathMapping(pathMappings));
		}
		
		List<FilePathMapping> filePathList = bundle.getFilePathMappings(); 
		
		if (filePathList != null && !filePathList.isEmpty()) {
			props
					.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_FILEPATH_MAPPINGS,
							getCommaSeparatedStringForFilePath(filePathList));
		}
		List<JoinableResourceBundle> dependencies = bundle.getDependencies();
		if (dependencies != null && !dependencies.isEmpty()) {
			List<String> dependenciesBundleName = getBundleNames(dependencies);
			props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEPENDENCIES, getCommaSeparatedString(dependenciesBundleName));
		}
		
		Set<String> licensesPathList = bundle.getLicensesPathList();
		if (licensesPathList != null && !licensesPathList.isEmpty()) {
			props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_LICENCE_PATH_LIST,
					getCommaSeparatedString(licensesPathList));
		}
		
		// Handle composite bundle
		if(bundle.isComposite()){
			props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_COMPOSITE_FLAG, Boolean.TRUE.toString());
			List<JoinableResourceBundle> children = ((CompositeResourceBundle) bundle).getChildBundles();
			List<String> bundleNames = getBundleNames(children);
			props.put(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_COMPOSITE_NAMES, getCommaSeparatedString(bundleNames));
			for (JoinableResourceBundle childBundle : children) {
				serializeInProperties(childBundle, type, props);
			}
		}
	}

	/**
	 * Serialize the variant sets.
	 * 
	 * @param map the map to serialize
	 * @return the serialized variant sets
	 */
	private static String serializeVariantSets(Map<String, VariantSet> map) {
		StringBuffer result = new StringBuffer();
		
		for (Iterator<Entry<String, VariantSet>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, VariantSet> entry = iterator.next();
			result.append(entry.getKey()+":");
			VariantSet variantSet = (VariantSet) entry.getValue();
			result.append(variantSet.getDefaultVariant()+":");
			result.append(getCommaSeparatedString(variantSet));
			result.append(";");
		}
		
		return result.toString();
	}

	/**
	 * Returns the list of bundle names
	 * @param bundles the bundles
	 * @return the list of bundle names
	 */
	private static List<String> getBundleNames(List<JoinableResourceBundle> bundles) {
		
		List<String> bundleNames = new ArrayList<String>();
		for (Iterator<JoinableResourceBundle> iterator = bundles.iterator(); iterator
				.hasNext();) {
			bundleNames.add(iterator.next().getName());
		}
		return bundleNames;
	}

	/**
	 * Returns the mapping list
	 * 
	 * @param bundlePathMapping.getItemPathList() the item path list
	 * @return the item path list
	 */
	private static String getCommaSeparatedString(Collection<String> coll) {

		StringBuilder buffer = new StringBuilder();
		for (Iterator<String> eltIterator = coll.iterator(); eltIterator.hasNext();) {
			String elt = eltIterator.next();
			buffer.append(elt);
			if(eltIterator.hasNext()){
				buffer.append(COMMA_SEPARATOR);
			}
		}
		return buffer.toString();
	}
	
	/**
	 * Returns the mapping list
	 * 
	 * @param bundlePathMapping.getItemPathList() the item path list
	 * @return the item path list
	 */
	private static String getCommaSeparatedStringForPathMapping(Collection<PathMapping> coll) {

		StringBuilder buffer = new StringBuilder();
		for (Iterator<PathMapping> eltIterator = coll.iterator(); eltIterator.hasNext();) {
			PathMapping mapping = eltIterator.next();
			buffer.append(mapping.getPath());
			if(mapping.isRecursive()){
				buffer.append("**");
			}
			if(eltIterator.hasNext()){
				buffer.append(COMMA_SEPARATOR);
			}
		}
		return buffer.toString();
	}
	
	/**
	 * Returns the mapping list
	 * 
	 * @param bundlePathMapping.getItemPathList() the item path list
	 * @return the item path list
	 */
	private static String getCommaSeparatedStringForFilePath(Collection<FilePathMapping> mappings) {

		StringBuffer buffer = new StringBuffer();
		for (Iterator<FilePathMapping> eltIterator = mappings.iterator(); eltIterator.hasNext();) {
			FilePathMapping mapping = eltIterator.next();
			buffer.append(mapping.getPath()+LAST_MODIFIED_SEPARATOR+mapping.getLastModified());
			if(eltIterator.hasNext()){
				buffer.append(COMMA_SEPARATOR);
			}
		}
		return buffer.toString();
	}

	/**
	 * Returns the bundle post processor name separated by a comma character
	 * 
	 * @param processor the post processor
	 * @return the bundle post processor name separated by a comma character
	 */
	private static String getBundlePostProcessorsName(
			ChainedResourceBundlePostProcessor processor) {

		String bundlePostProcessor = "";
		if (processor != null) {
			bundlePostProcessor = processor.getId();
		}

		return bundlePostProcessor;
	}
}
