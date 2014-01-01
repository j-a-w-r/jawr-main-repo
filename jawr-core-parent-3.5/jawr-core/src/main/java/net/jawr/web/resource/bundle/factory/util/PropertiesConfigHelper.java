/**
 * Copyright 2007-2011 Jordi Hernández Sellés, Ibrahim Chaehoi
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package net.jawr.web.resource.bundle.factory.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jawr.web.resource.bundle.factory.PropertiesBundleConstant;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.util.StringUtils;

/**
 * Helper class to make properties access less verbose.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 * 
 */
public class PropertiesConfigHelper {
	
	/** The properties */
	private Properties props;
	
	/** The prefix of the properties */
	private String prefix;
	
	/** The bundle name pattern */
	private Pattern bundleNamePattern;
	
	/** The post processor class name pattern */
	private Pattern postProcessorClassPattern = Pattern.compile("(jawr\\.custom\\.postprocessors\\.)([-_a-zA-Z0-9]+).class");

	/** The global preprocessor class name pattern */
	private Pattern globalPreProcessorClassPattern = Pattern.compile("(jawr\\.custom\\.global\\.preprocessor\\.)([-_a-zA-Z0-9]+).class");
	
	/** The global postprocessor class name pattern */
	private Pattern globalPostProcessorClassPattern = Pattern.compile("(jawr\\.custom\\.global\\.postprocessor\\.)([-_a-zA-Z0-9]+).class");
	
	/**
	 * Build a properties wrapper that appends 'jawr.' and the specified
	 * resourceType to a a supplied key before retrieveing its value from the
	 * properties.
	 * 
	 * @param props
	 *            Properties to wrap
	 * @param resourceType
	 *            resource type to use.
	 */
	public PropertiesConfigHelper(Properties props, String resourceType) {
		super();
		this.props = props;
		this.prefix = PropertiesBundleConstant.PROPS_PREFIX + resourceType + ".";
		String bundle = prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PROPERTY;
		String pattern = "(" + bundle.replaceAll("\\.", "\\\\.")
				+ ")([-_a-zA-Z0-9]+)\\.id";
		this.bundleNamePattern = Pattern.compile(pattern);
	}

	/**
	 * Returns the value of the common property, or the default value if no value is defined
	 * instead.
	 * @param key the key of the property
	 * @param defaultValue the default value
	 * @return the value of the common property
	 */
	public String getCommonProperty(String key, String defaultValue) {
		return props.getProperty(PropertiesBundleConstant.PROPS_PREFIX + key, defaultValue);
	}

	/**
	 * Returns the value of the common property
	 * @param key the key of the property
	 * @return the value of the common property
	 */
	public String getCommonProperty(String key) {
		return props.getProperty(PropertiesBundleConstant.PROPS_PREFIX + key);
	}

	/**
	 * Returns as a set, the comma separated values of a property 
	 * @param key the key of the property
	 * @return a set of the comma separated values of a property 
	 */
	public Set<String> getCommonPropertyAsSet(String key) {
		Set<String> propertiesSet = new HashSet<String>();
		StringTokenizer tk = new StringTokenizer(props.getProperty(PropertiesBundleConstant.PROPS_PREFIX+key, ""),
				",");
		while (tk.hasMoreTokens())
			propertiesSet.add(tk.nextToken().trim());
		return propertiesSet;
	}
	
	/**
	 * Returns the value of the custom bundle property, or the default value if no value is defined
	 * @param bundleName the bundle name
	 * @param key the key of the property
	 * @param defaultValue the default value
	 * @return the value of the custom bundle property, or the default value if no value is defined
	 */
	public String getCustomBundleProperty(String bundleName, String key,
			String defaultValue) {
		return props.getProperty(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PROPERTY
				+ bundleName + key, defaultValue);
	}

	/**
	 * Returns the value of the custom bundle property, or the default value if no value is defined
	 * @param bundleName the bundle name
	 * @param key the key of the property
	 * @return the value of the custom bundle property
	 */
	public String getCustomBundleProperty(String bundleName, String key) {
		return props.getProperty(prefix + PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PROPERTY
				+ bundleName + key);
	}

	/**
	 * Returns as a list, the comma separated values of a property 
	 * @param key the key of the property
	 * @return a list of the comma separated values of a property 
	 */
	public List<String> getCustomBundlePropertyAsList(String bundleName, String key) {
		List<String> propertiesList = new ArrayList<String>();
		StringTokenizer tk = new StringTokenizer(getCustomBundleProperty(bundleName, key, ""),
				",");
		while (tk.hasMoreTokens())
			propertiesList.add(tk.nextToken().trim());
		return propertiesList;
	}
	
	/**
	 * Returns as a set, the comma separated values of a property 
	 * @param key the key of the property
	 * @return a set of the comma separated values of a property 
	 */
	public Set<String> getCustomBundlePropertyAsSet(String bundleName, String key) {
		Set<String> propertiesSet = new HashSet<String>();
		StringTokenizer tk = new StringTokenizer(getCustomBundleProperty(bundleName, key, ""),
				",");
		while (tk.hasMoreTokens())
			propertiesSet.add(tk.nextToken().trim());
		return propertiesSet;
	}
	
	/**
	 * Returns as a set, the comma separated values of a property 
	 * @param key the key of the property
	 * @return a set of the comma separated values of a property 
	 */
	public Map<String,List<String>> getCustomBundlePropertyAsMap(String bundleName, String key) {
		Map<String,List<String>> propertiesMap = new HashMap<String,List<String>>();
		
		StringTokenizer tk = new StringTokenizer(getCustomBundleProperty(bundleName, key, ""),
				";");
		while (tk.hasMoreTokens()){
			String[] mapEntry = tk.nextToken().trim().split(":");
			
			String mapKey = mapEntry[0];
			String values = mapEntry[1];
			StringTokenizer valueTk = new StringTokenizer(values, ",");
			List<String> valueList = new ArrayList<String>();
			while (valueTk.hasMoreTokens()){
				valueList.add(valueTk.nextToken().trim());
			}
			propertiesMap.put(mapKey, valueList);
		}
		return propertiesMap;
	}
	
	/**
	 * Returns the map of variantSet for the bundle 
	 * @param bundleName the bundle name
	 * @return the map of variantSet for the bundle 
	 */
	public Map<String, VariantSet> getCustomBundleVariantSets(String bundleName) {
		Map<String, VariantSet> variantSets = new HashMap<String, VariantSet>();
		
		StringTokenizer tk = new StringTokenizer(getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_VARIANTS, ""),
				";");
		while (tk.hasMoreTokens()){
			String[] mapEntry = tk.nextToken().trim().split(":");
			
			String type = mapEntry[0];
			String defaultVariant = mapEntry[1];
			String values = mapEntry[2];
			String[] variantsArray = StringUtils.split(values, ",");
			List<String> variants = new ArrayList<String>();
			for (int i = 0; i < variantsArray.length; i++) {
				variants.add(variantsArray[i]);
			}
			
			VariantSet variantSet = new VariantSet(type, defaultVariant, variants);
			variantSets.put(type, variantSet);
		}
		return variantSets;
	}
	
	/**
	 * Returns as a set, the comma separated values of a property 
	 * @param key the key of the property
	 * @return a set of the comma separated values of a property 
	 */
	public Set<String> getPropertyAsSet(String key) {
		Set<String> propertiesSet = new HashSet<String>();
		StringTokenizer tk = new StringTokenizer(props.getProperty(prefix+key, ""),
				",");
		while (tk.hasMoreTokens())
			propertiesSet.add(tk.nextToken().trim());
		return propertiesSet;
	}

	/**
	 * Returns the value of a property, or the default value if no value is defined
	 * @param key the key of the property
	 * @param defaultValue the default value
	 * @return the value of a property, or the default value if no value is defined
	 */
	public String getProperty(String key, String defaultValue) {
		return props.getProperty(prefix + key, defaultValue);
	}

	/**
	 * Returns the set of names for the bundles 
	 * @return the set of names for the bundles 
	 */
	public Set<String> getPropertyBundleNameSet() {
		
		Set<String> bundleNameSet = new HashSet<String>();

		for (Iterator<Object> it = props.keySet().iterator();it.hasNext();) {
			Object key = it.next();
			Matcher matcher = bundleNamePattern.matcher((String) key);
			if (matcher.matches()) {

				String id = matcher.group(2);
				bundleNameSet.add(id);
			}
		}
		return bundleNameSet;
	}

	/**
	 * Returns the set of post processor name based on the class definition
	 * @return the set of post processor name based on the class definition
	 */
	public Map<String,String> getCustomPostProcessorMap() {
		return getCustomMap(postProcessorClassPattern);
	}
	
	/**
	 * Returns the map of custom global preprocessor
	 * @return the map of custom global preprocessor
	 */
	public Map<String,String> getCustomGlobalPreprocessorMap() {
		return getCustomMap(globalPreProcessorClassPattern);
	}
	
	/**
	 * Returns the map of custom global preprocessor
	 * @return the map of custom global preprocessor
	 */
	public Map<String,String> getCustomGlobalPostprocessorMap() {
		return getCustomMap(globalPostProcessorClassPattern);
	}
	
	/**
	 * Returns the map, where the key is the 2 group of the pattern and the value is the property value
	 * @param keyPattern the pattern of the key
	 * @return the map.
	 */
	private Map<String,String> getCustomMap(Pattern keyPattern) {
		Map<String,String> map = new HashMap<String,String>();

		for (Iterator<Object> it = props.keySet().iterator();it.hasNext();) {
			String key = (String) it.next();
			Matcher matcher = keyPattern.matcher(key);
			if (matcher.matches()) {

				String id = matcher.group(2);
				String propertyValue = props.getProperty(key);
				map.put(id, propertyValue);
			}
		}
		return map;
	}
	
	/**
	 * Appends the prefix (jawr.) to the specified key and reads it from the
	 * properties object.
	 * 
	 * @param key the suffix of the key property 
	 * @return the value of the property jawr.+key
	 */
	public String getProperty(String key) {
		return props.getProperty(prefix + key);
	}

}
