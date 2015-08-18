/**
 * Copyright 2007-2014 Jordi Hernández Sellés, Ibrahim Chaehoi
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundleDependencyException;
import net.jawr.web.exception.DuplicateBundlePathException;
import net.jawr.web.resource.bundle.factory.util.PropertiesConfigHelper;
import net.jawr.web.resource.bundle.factory.util.ResourceBundleDefinition;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.resource.bundle.variant.VariantUtils;
import net.jawr.web.resource.handler.bundle.ResourceBundleHandler;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

/**
 * Properties based configuration entry point.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 * 
 */
public class PropertiesBasedBundlesHandlerFactory {

	/** The properties configuration helper */
	private PropertiesConfigHelper props;
	
	/** The bundle handler factory */
	private BundlesHandlerFactory factory;

	/**
	 * Constructor 
	 */
	protected PropertiesBasedBundlesHandlerFactory(){
		
	}
			
	/**
	 * Create a PropertiesBasedBundlesHandlerFactory using the specified
	 * properties.
	 * 
	 * @param properties the properties
	 * @param resourceType
	 *            js or css
	 * @param rsHandler
	 *            ResourceHandler to access files.
	 */
	public PropertiesBasedBundlesHandlerFactory(Properties properties,
			String resourceType, ResourceReaderHandler rsHandler, ResourceBundleHandler rsBundleHandler,
			JawrConfig jawrConfig) {
		this.props = new PropertiesConfigHelper(properties, resourceType);
		
		// Create the BundlesHandlerFactory
		factory = new BundlesHandlerFactory(jawrConfig);
		factory.setResourceReaderHandler(rsHandler);
		factory.setResourceBundleHandler(rsBundleHandler);
		factory.setBundlesType(resourceType);

		// Root resources dir
		factory.setBaseDir(props.getProperty(PropertiesBundleConstant.RESOURCES_BASEDIR, "/"));

		// Use cache by default
		factory.setUseCacheManager(Boolean.valueOf(
				props.getProperty(PropertiesBundleConstant.RESOURCES_USE_CACHE, "true")).booleanValue());

		// Postprocessor definitions
		factory.setGlobalPostProcessorKeys(props
				.getProperty(PropertiesBundleConstant.BUNDLE_FACTORY_POSTPROCESSOR));
		factory.setUnitPostProcessorKeys(props
				.getProperty(PropertiesBundleConstant.BUNDLE_FACTORY_FILE_POSTPROCESSOR));
		factory.setGlobalCompositePostProcessorKeys(props
				.getProperty(PropertiesBundleConstant.COMPOSITE_BUNDLE_FACTORY_POSTPROCESSOR));
		factory.setUnitCompositePostProcessorKeys(props
				.getProperty(PropertiesBundleConstant.COMPOSITE_BUNDLE_FACTORY_FILE_POSTPROCESSOR));
		factory.setResourceTypePreprocessorKeys(props
				.getProperty(PropertiesBundleConstant.BUNDLE_FACTORY_GLOBAL_PREPROCESSORS));
		factory.setResourceTypePostprocessorKeys(props
				.getProperty(PropertiesBundleConstant.BUNDLE_FACTORY_GLOBAL_POSTPROCESSORS));
		
		// Single or multiple bundle for orphans settings.
		factory.setUseSingleResourceFactory(Boolean.valueOf(
				props.getProperty(PropertiesBundleConstant.FACTORY_USE_SINGLE_BUNDLE, "false"))
				.booleanValue());
		factory.setSingleFileBundleName(props
				.getProperty(PropertiesBundleConstant.FACTORY_SINGLE_FILE_NAME));
		
		// Use orphans resolution at all, on by default. FACTORY_PROCESS_ORPHANS
		factory.setScanForOrphans(Boolean.valueOf(
				props.getCommonProperty(PropertiesBundleConstant.FACTORY_PROCESS_ORPHANS, "true")).booleanValue());

		// Use the automatic directory-as-bundle mapper.
		factory.setUseDirMapperFactory(Boolean.valueOf(
				props.getProperty(PropertiesBundleConstant.FACTORY_USE_DIR_MAPPER, "false"))
				.booleanValue());
		factory.setExludedDirMapperDirs(props
				.getPropertyAsSet(PropertiesBundleConstant.FACTORY_DIR_MAPPER_EXCLUSION));

		// Initialize custom generators
		Iterator<String> generators = props.getCommonPropertyAsSet(PropertiesBundleConstant.CUSTOM_GENERATORS)
				.iterator();
		GeneratorRegistry generatorRegistry = jawrConfig.getGeneratorRegistry();
		while (generators.hasNext()) {
			String generatorClass = (String) generators.next();
			generatorRegistry.registerGenerator(generatorClass);
		}
		
		// Initialize variant resolvers
		Iterator<String> resolvers = props.getCommonPropertyAsSet(PropertiesBundleConstant.CUSTOM_RESOLVERS)
				.iterator();
		while (resolvers.hasNext()) {
			String resolverClass = (String) resolvers.next();
			generatorRegistry.registerVariantResolver(resolverClass);
		}

		// Initialize custom bundles
		Set<ResourceBundleDefinition> customBundles = new HashSet<ResourceBundleDefinition>();
		// Check if we should use the bundle names property or
		// find the bundle name using the bundle id declaration :
		// jawr.<type>.bundle.<name>.id
		if(null != props.getProperty(PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_NAMES)) {
			StringTokenizer tk = new StringTokenizer(props
					.getProperty(PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_NAMES), JawrConstant.COMMA_SEPARATOR);
			while (tk.hasMoreTokens()) {
				customBundles.add(buildCustomBundleDefinition(tk.nextToken()
						.trim(), false, generatorRegistry));
			}
		} else {
			Iterator<String> bundleNames = props.getPropertyBundleNameSet().iterator();
			while (bundleNames.hasNext()) {
				customBundles.add(buildCustomBundleDefinition(
						(String) bundleNames.next(), false, generatorRegistry));
			}
		}
		
		factory.setBundleDefinitions(customBundles);

		// Set global bundle preprocessor
		factory.setCustomGlobalPreprocessors(props.getCustomGlobalPreprocessorMap());
		
		// Set global bundle postprocessor
		factory.setCustomGlobalPostprocessors(props.getCustomGlobalPostprocessorMap());
		
		// Set custom postprocessor
		factory.setCustomPostprocessors(props.getCustomPostProcessorMap());
		
	}

	
	

	/**
	 * Build a resources handler based on the configuration.
	 * 
	 * @param jawrConfig the jawr config
	 * @return a resources handler based on the configuration.
	 * @throws DuplicateBundlePathException
	 * @throws BundleDependencyException  if an error exists in the dependency definition
	 */
	public ResourceBundlesHandler buildResourceBundlesHandler() throws DuplicateBundlePathException, BundleDependencyException {
		return factory.buildResourceBundlesHandler();
	}

	/**
	 * Create a BundleDefinition based on the properties file.
	 * 
	 * @param bundleName the bundle name
	 * @param generatorRegistry the generator registry
	 * @return the bundleDefinition
	 */
	private ResourceBundleDefinition buildCustomBundleDefinition(
			String bundleName, boolean isChildBundle, GeneratorRegistry generatorRegistry) {

		// Id for the bundle
		String bundleId = props.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ID);
		if (null == bundleId && !isChildBundle)
			throw new IllegalArgumentException(
					"No id defined for the bundle with name:" + bundleName
							+ ". Please specify one in configuration. ");

		// Wether it's a composite or not
		boolean isComposite = Boolean.valueOf(
				props.getCustomBundleProperty(bundleName,
						PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_COMPOSITE_FLAG, "false"))
				.booleanValue();

		// Create definition and set its id
		ResourceBundleDefinition bundle = new ResourceBundleDefinition();
		bundle.setBundleId(bundleId);
		bundle.setBundleName(bundleName);
		bundle.setBundlePrefix(props.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_BUNDLE_PREFIX));
		
		// Wether it's global or not
		Boolean isGlobal = Boolean.valueOf(props.getCustomBundleProperty(
				bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_GLOBAL_FLAG, "false"));
		bundle.setGlobal(isGlobal.booleanValue());

		// Set order if its a global bundle
		if (isGlobal.booleanValue()) {
			Integer order = Integer.valueOf(props.getCustomBundleProperty(
					bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_ORDER, "0"));
			bundle.setInclusionOrder(order.intValue());
		}

		// Override bundle postprocessor
		if (null != props.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_POSTPROCESSOR))
			bundle.setBundlePostProcessorKeys(props.getCustomBundleProperty(
					bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_POSTPROCESSOR));

		// Override unitary postprocessor
		if (null != props.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_FILE_POSTPROCESSOR))
			bundle.setUnitaryPostProcessorKeys(props.getCustomBundleProperty(
					bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_FILE_POSTPROCESSOR));

		// Use only with debug mode on
		Boolean isDebugOnly = Boolean.valueOf(props.getCustomBundleProperty(
				bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGONLY, "false"));
		bundle.setDebugOnly(isDebugOnly.booleanValue());

		// Use only with debug mode off
		Boolean isDebugNever = Boolean.valueOf(props.getCustomBundleProperty(
				bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEBUGNEVER, "false"));
		bundle.setDebugNever(isDebugNever.booleanValue());

		// Set conditional comment for IE, in case one is specified
		if (null != props.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_IE_CONDITIONAL_EXPRESSION))
			bundle.setIeConditionalExpression(props
					.getCustomBundleProperty(bundleName,
							PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_IE_CONDITIONAL_EXPRESSION));

		// Sets the alternate URL for production mode. 
		if (null != props.getCustomBundleProperty(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PRODUCTION_ALT_URL))
			bundle.setAlternateProductionURL(props.getCustomBundleProperty(bundleName,
					PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_PRODUCTION_ALT_URL));
			
		
		if (isComposite) {
			String childBundlesProperty = props.getCustomBundleProperty(
					bundleName, PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_COMPOSITE_NAMES);
			if (null == childBundlesProperty)
				throw new IllegalArgumentException(
						"No child bundle names were defined for the composite bundle with name:"
								+ bundleName
								+ ". Please specify at least one in configuration. ");

			bundle.setComposite(true);

			// add children
			List<ResourceBundleDefinition> children = new ArrayList<ResourceBundleDefinition>();
			StringTokenizer tk = new StringTokenizer(childBundlesProperty, JawrConstant.COMMA_SEPARATOR);
			while (tk.hasMoreTokens()) {
				ResourceBundleDefinition childDef = buildCustomBundleDefinition(
						tk.nextToken().trim(), true, generatorRegistry);
				childDef.setBundleId(bundleId);
				children.add(childDef);
			}
			bundle.setChildren(children);
		} else {
			String mappingsProperty = props.getCustomBundleProperty(bundleName,
					PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_MAPPINGS);
			if (null == mappingsProperty)
				throw new IllegalArgumentException(
						"No mappings were defined for the bundle with name:"
								+ bundleName
								+ ". Please specify at least one in configuration. ");

			// Add the mappings
			List<String> mappings = new ArrayList<String>();
			Map<String, VariantSet> variants = new TreeMap<String, VariantSet>();
			StringTokenizer tk = new StringTokenizer(mappingsProperty, JawrConstant.COMMA_SEPARATOR);
			while (tk.hasMoreTokens()){
				String mapping = tk.nextToken().trim();
				mapping = generatorRegistry.locateResource(mapping);
				mappings.add(mapping);
				// Add local variants
				variants = VariantUtils.concatVariants(variants, generatorRegistry.getAvailableVariants(mapping));
			}
			bundle.setMappings(mappings);
			bundle.setVariants(variants);
		}

		// dependencies
		List<String> dependencies = props.getCustomBundlePropertyAsList(bundleName,
				PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_DEPENDENCIES);
		bundle.setDependencies(dependencies);
		
		return bundle;
	}

}
