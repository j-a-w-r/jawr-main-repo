/**
 * Copyright 2009-2014 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.factory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import net.jawr.web.resource.bundle.DebugInclusion;
import net.jawr.web.resource.bundle.InclusionPattern;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.JoinableResourceBundleImpl;
import net.jawr.web.resource.bundle.factory.postprocessor.PostProcessorChainFactory;
import net.jawr.web.resource.bundle.factory.util.PropertiesConfigHelper;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.util.StringUtils;

/**
 * This factory is used to build JoinableResourceBundle from the generated properties mapping file, 
 * which contains all calculated information about the bundle.
 * 
 * @author Ibrahim Chaehoi
 * 
 */
public class FullMappingPropertiesBasedBundlesHandlerFactory {

	/** The post processor chain factory */
	private PostProcessorChainFactory chainFactory;

	/** The resource type */
	private String resourceType;
	
	/** The resource handler */
	private ResourceReaderHandler rsReaderHandler;
	
	/** The generator registry */
	private GeneratorRegistry generatorRegistry;
	
	/**
	 * Create a PropertiesBasedBundlesHandlerFactory using the specified properties.
	 * 
	 * @param resourceType js or css
	 * @param rsHandler ResourceHandler to access files.
	 * @param generatorRegistry the generator registry
	 * @param chainFactory the post processor chain factory
	 */
	public FullMappingPropertiesBasedBundlesHandlerFactory(String resourceType, 
			ResourceReaderHandler rsHandler, GeneratorRegistry generatorRegistry,
			PostProcessorChainFactory chainFactory) {

		this.resourceType = resourceType;
		this.chainFactory = chainFactory;
		this.rsReaderHandler = rsHandler;
		this.generatorRegistry = generatorRegistry;
	}

	/**
	 * Returns the list of joinable resource bundle
	 * 
	 * @return the list of joinable resource bundle
	 */
	public List<JoinableResourceBundle> getResourceBundles(Properties properties) {
		
		PropertiesConfigHelper props = new PropertiesConfigHelper(properties, resourceType);
		String fileExtension = "." + resourceType;

		// Initialize custom bundles
		List<JoinableResourceBundle> customBundles = new ArrayList<JoinableResourceBundle>();
		// Check if we should use the bundle names property or
		// find the bundle name using the bundle id declaration :
		// jawr.<type>.bundle.<name>.id
		if (null != props
				.getProperty(PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_NAMES)) {
			StringTokenizer tk = new StringTokenizer(
					props
							.getProperty(PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_NAMES),
					",");
			while (tk.hasMoreTokens()) {
				customBundles.add(buildJoinableResourceBundle(props, tk.nextToken()
						.trim(), fileExtension, rsReaderHandler));
			}
		} else {
			Iterator<String> bundleNames = props.getPropertyBundleNameSet().iterator();
			while (bundleNames.hasNext()) {
				customBundles.add(buildJoinableResourceBundle(props, 
						bundleNames.next(), fileExtension,
						rsReaderHandler));
			}
		}
		
		// Initialize the bundles dependencies
		Iterator<String> bundleNames = props.getPropertyBundleNameSet().iterator();
		while (bundleNames.hasNext()) {
			String bundleName = (String) bundleNames.next();
			List<String> bundleNameDependencies = props.getCustomBundlePropertyAsList(bundleName,
					PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEPENDENCIES);
			if(!bundleNameDependencies.isEmpty()){
				JoinableResourceBundle bundle = getBundleFromName(bundleName, customBundles);
				List<JoinableResourceBundle> bundleDependencies = getBundlesFromName(bundleNameDependencies, customBundles);
				bundle.setDependencies(bundleDependencies);
			}
		}
		
		return customBundles;
	}

	/**
	 * Returns a bundle using the bundle name from a list of bundles 
	 * @param bundleName the bundle name
	 * @param bundles the list of bundle
	 * @return a bundle
	 */
	private JoinableResourceBundle getBundleFromName(String bundleName, List<JoinableResourceBundle> bundles) {
		
		JoinableResourceBundle bundle = null;
		List<String> names = new ArrayList<String>();
		names.add(bundleName);
		List<JoinableResourceBundle> result = getBundlesFromName(names, bundles);
		if(!result.isEmpty()){
			bundle = result.get(0);
		}
		return bundle;
	}

	/**
	 * Returns a list of bundles using the bundle names from a list of bundles 
	 * @param names the list of bundle name
	 * @param bundles the list of bundle
	 * @return a list of bundles
	 */
	private List<JoinableResourceBundle> getBundlesFromName(List<String> names, List<JoinableResourceBundle> bundles) {
		
		List<JoinableResourceBundle> resultBundles = new ArrayList<JoinableResourceBundle>();
		for (Iterator<String> iterator = names.iterator(); iterator.hasNext();) {
			String name = iterator.next();
			for (Iterator<JoinableResourceBundle> itBundle = bundles.iterator(); itBundle.hasNext();) {
				JoinableResourceBundle bundle = itBundle.next();
				if(bundle.getName().equals(name)){
					resultBundles.add(bundle);
				}
			}
		}
		
		return resultBundles;
	}

	/**
	 * Create a JoinableResourceBundle based on the properties file.
	 * 
	 * @param props the properties config helper
	 * @param bundleName the bundle name
	 * @param rsHandler the resource handler
	 * @return the Resource Bundle
	 */
	private JoinableResourceBundle buildJoinableResourceBundle(
			PropertiesConfigHelper props, String bundleName, String fileExtension,
			ResourceReaderHandler rsHandler) {

		// Id for the bundle
		String bundleId = props.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ID);

		String bundlePrefix = props.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_BUNDLE_PREFIX);

		InclusionPattern inclusionPattern = getInclusionPattern(props, bundleName);
		JoinableResourceBundleImpl bundle = new JoinableResourceBundleImpl(
				bundleId, bundleName, bundlePrefix, fileExtension, inclusionPattern,
				rsHandler, generatorRegistry);
		
		// Override bundle postprocessor
		String bundlePostProcessors = props.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_POSTPROCESSOR);
		if (StringUtils.isNotEmpty(bundlePostProcessors)) {
			bundle.setBundlePostProcessor(chainFactory
					.buildPostProcessorChain(bundlePostProcessors));
		}

		// Override unitary postprocessor
		String unitaryPostProcessors = props
				.getCustomBundleProperty(
						bundleName,
						PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_FILE_POSTPROCESSOR);
		if (StringUtils.isNotEmpty(unitaryPostProcessors)) {
			bundle.setUnitaryPostProcessor(chainFactory
					.buildPostProcessorChain(unitaryPostProcessors));
		}

		// Set conditional comment for IE, in case one is specified
		String explorerConditionalCondition = props
				.getCustomBundleProperty(
						bundleName,
						PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_IE_CONDITIONAL_EXPRESSION);
		if (StringUtils.isNotEmpty(explorerConditionalCondition)) {
			bundle
					.setExplorerConditionalExpression(explorerConditionalCondition);
		}

		// Sets the alternate URL for production mode.
		String alternateProductionURL = props
				.getCustomBundleProperty(
						bundleName,
						PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PRODUCTION_ALT_URL);
		if (StringUtils.isNotEmpty(alternateProductionURL)) {
			bundle
					.setAlternateProductionURL(props
							.getCustomBundleProperty(
									bundleName,
									PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PRODUCTION_ALT_URL));
		}
		
		// Sets the licence path lists.
		Set<String> licencePathList = props
				.getCustomBundlePropertyAsSet(
						bundleName,
						PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_LICENCE_PATH_LIST);
		if (!licencePathList.isEmpty()) {
			bundle.setLicensesPathList(licencePathList);
		}

		List<String> mappings = props.getCustomBundlePropertyAsList(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_MAPPINGS);
		if (mappings.isEmpty()) {
			throw new IllegalArgumentException(
					"No mappings were defined for the bundle with name:"
							+ bundleName
							+ ". Please specify at least one in configuration. ");
		}
		
		// Add the mappings
		bundle.setMappings(mappings);
		
		Map<String, VariantSet> variants = props.getCustomBundleVariantSets(bundleName);
		bundle.setVariants(variants);
		for (Iterator<String> iterator = bundle.getVariantKeys().iterator(); iterator.hasNext();) {
			String variantKey = iterator.next();
			if(StringUtils.isNotEmpty(variantKey)){
				String hashcode = props.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE_VARIANT+variantKey);
				bundle.setBundleDataHashCode(variantKey, hashcode);
			}
		}
		
		String hashcode = props.getCustomBundleProperty(bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_HASHCODE);
		bundle.setBundleDataHashCode(null, hashcode);
	
		return bundle;
	}

	/**
	 * Returns the inclusion pattern for a bundle
	 * 
	 * @param props the properties helper
	 * @param bundleName the bundle name
	 * @return the inclusion pattern for a bundle
	 */
	private InclusionPattern getInclusionPattern(PropertiesConfigHelper props, String bundleName) {
		// Wether it's global or not
		boolean isGlobal = Boolean
				.valueOf(
						props
								.getCustomBundleProperty(
										bundleName,
										PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_GLOBAL_FLAG,
										"false")).booleanValue();

		// Set order if its a global bundle
		int order = 0;
		if (isGlobal) {
			order = Integer.parseInt(props.getCustomBundleProperty(bundleName,
					PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ORDER, "0"));
		}

		// Use only with debug mode on
		boolean isDebugOnly = Boolean
				.valueOf(
						props
								.getCustomBundleProperty(
										bundleName,
										PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGONLY,
										"false")).booleanValue();

		// Use only with debug mode off
		boolean isDebugNever = Boolean
				.valueOf(
						props
								.getCustomBundleProperty(
										bundleName,
										PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGNEVER,
										"false")).booleanValue();

		return new InclusionPattern(isGlobal, order, DebugInclusion.get(isDebugOnly, isDebugNever));
	}

}
