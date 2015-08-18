/**
 * Copyright 2008-2014 Jordi Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.http.HttpServletRequest;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.generator.classpath.ClassPathCSSGenerator;
import net.jawr.web.resource.bundle.generator.classpath.ClassPathImgResourceGenerator;
import net.jawr.web.resource.bundle.generator.classpath.ClasspathJSGenerator;
import net.jawr.web.resource.bundle.generator.classpath.webjars.WebJarsBinaryResourceGenerator;
import net.jawr.web.resource.bundle.generator.classpath.webjars.WebJarsCssGenerator;
import net.jawr.web.resource.bundle.generator.classpath.webjars.WebJarsJSGenerator;
import net.jawr.web.resource.bundle.generator.css.less.LessCssGenerator;
import net.jawr.web.resource.bundle.generator.img.SpriteGenerator;
import net.jawr.web.resource.bundle.generator.js.coffee.CoffeeScriptGenerator;
import net.jawr.web.resource.bundle.generator.locator.ResourceLocator;
import net.jawr.web.resource.bundle.generator.locator.WebJarsResourceLocator;
import net.jawr.web.resource.bundle.generator.resolver.PrefixedPathResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolverWrapper;
import net.jawr.web.resource.bundle.generator.resolver.SuffixedPathResolver;
import net.jawr.web.resource.bundle.generator.validator.CommonsValidatorGenerator;
import net.jawr.web.resource.bundle.generator.variant.VariantResourceGenerator;
import net.jawr.web.resource.bundle.generator.variant.css.CssSkinGenerator;
import net.jawr.web.resource.bundle.locale.ResourceBundleMessagesGenerator;
import net.jawr.web.resource.bundle.variant.VariantResolver;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.resource.handler.reader.ResourceReader;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.resource.handler.reader.WorkingDirectoryLocationAware;
import net.jawr.web.servlet.JawrRequestHandler;
import net.jawr.web.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry for resource generators, which create scripts or CSS data
 * dynamically, as opposed to the usual behavior of reading a resource from the
 * war file. It provides methods to determine if a path mapping should be
 * handled by a generator, and to actually render the resource using the
 * appropriate generator. Path mappings which require generation will use a
 * prefix (preferably one which ends with a colon, such as 'messages:').
 * Generators provided with Jawr will be automatically mapped.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class GeneratorRegistry implements Serializable {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(GeneratorRegistry.class);

	/** The serial version UID */
	private static final long serialVersionUID = -7988265144352433701L;

	/** The message bundle prefix */
	public static final String MESSAGE_BUNDLE_PREFIX = "messages";

	/** The classpath resource bundle prefix */
	public static final String CLASSPATH_RESOURCE_BUNDLE_PREFIX = "jar";

	/** The webjars generator prefix */
	public static final String WEBJARS_GENERATOR_PREFIX = "webjars";

	/** The webjars asset locator classname*/
	public static final String WEBJARS_LOCATOR_CLASSNAME = "org.webjars.WebJarAssetLocator";

	/** The commons validator bundle prefix */
	public static final String COMMONS_VALIDATOR_PREFIX = "acv";

	/** The IE CSS generator bundle prefix */
	public static final String IE_CSS_GENERATOR_PREFIX = "ieCssGen";

	/** The sprite generator prefix */
	public static final String SPRITE_GENERATOR_PREFIX = "sprite";

	/** The skin generator prefix */
	public static final String SKIN_GENERATOR_PREFIX = "skin";

	/** The skin switcher generator prefix */
	public static final String SKIN_SWTICHER_GENERATOR_PREFIX = "skinSwitcher";

	/** The coffee script suffix */
	public static final String COFEESCRIPT_GENERATOR_SUFFIX = "coffee";

	/** The coffee script suffix */
	public static final String LESS_GENERATOR_SUFFIX = "less";

	/** The generator prefix separator */
	public static final String PREFIX_SEPARATOR = ":";

	/** The common generators */
	private final Map<ResourceGeneratorResolver, Class<?>> commonGenerators = new ConcurrentHashMap<ResourceGeneratorResolver, Class<?>>();

	/** The generator registry */
	private final List<ResourceGenerator> resourceGeneratorRegistry = new CopyOnWriteArrayList<ResourceGenerator>();

	/** The generator resolver registry */
	private final List<ResourceGeneratorResolverWrapper> resolverRegistry = new CopyOnWriteArrayList<ResourceGeneratorResolverWrapper>();

	/** The CSS image resource prefix registry */
	private final List<ResourceGenerator> cssImageResourceGeneratorRegistry = new CopyOnWriteArrayList<ResourceGenerator>();

	/** The binary resource prefix registry */
	private final List<ResourceGenerator> binaryResourceGeneratorRegistry = new CopyOnWriteArrayList<ResourceGenerator>();

	/** The resource locator registry */
	private final List<ResourceLocator> resourceLocatorRegistry = new CopyOnWriteArrayList<ResourceLocator>();

	/** The resource type */
	private String resourceType;

	/** The Jawr config */
	private JawrConfig config;

	/** The resource handler */
	private ResourceReaderHandler rsHandler;

	/** The map of variant resolvers */
	private final Map<String, VariantResolver> variantResolvers = new ConcurrentHashMap<String, VariantResolver>();

	/** the webjar class path generator helper */
	public static final String WEBJARS_GENERATOR_HELPER_PREFIX = "/META-INF/resources/webjars/";

	/**
	 * Use only for testing purposes.
	 */
	public GeneratorRegistry() {
		this(JawrConstant.JS_TYPE);
	}

	/**
	 * Constructor
	 */
	public GeneratorRegistry(String resourceType) {
		this.resourceType = resourceType;

		commonGenerators.put(new PrefixedPathResolver(MESSAGE_BUNDLE_PREFIX),
				ResourceBundleMessagesGenerator.class);
		Class<?> classPathGeneratorClass = null;
		Class<?> webJarsGeneratorClass = null;
		if (resourceType.equals(JawrConstant.JS_TYPE)) {
			classPathGeneratorClass = ClasspathJSGenerator.class;
			webJarsGeneratorClass = WebJarsJSGenerator.class;
		} else if (resourceType.equals(JawrConstant.CSS_TYPE)) {
			classPathGeneratorClass = ClassPathCSSGenerator.class;
			webJarsGeneratorClass = WebJarsCssGenerator.class;
		} else {
			classPathGeneratorClass = ClassPathImgResourceGenerator.class;
			webJarsGeneratorClass = WebJarsBinaryResourceGenerator.class;
		}

		commonGenerators.put(new PrefixedPathResolver(
				CLASSPATH_RESOURCE_BUNDLE_PREFIX), classPathGeneratorClass);
		commonGenerators.put(
				new PrefixedPathResolver(WEBJARS_GENERATOR_PREFIX),
				webJarsGeneratorClass);

		if (resourceType.equals(JawrConstant.JS_TYPE)) {
			commonGenerators.put(new PrefixedPathResolver(
					COMMONS_VALIDATOR_PREFIX), CommonsValidatorGenerator.class);
			commonGenerators.put(new PrefixedPathResolver(
					SKIN_SWTICHER_GENERATOR_PREFIX),
					SkinSwitcherJsGenerator.class);
			commonGenerators.put(new SuffixedPathResolver(
					COFEESCRIPT_GENERATOR_SUFFIX), CoffeeScriptGenerator.class);
		}

		if (resourceType.equals(JawrConstant.CSS_TYPE)) {
			commonGenerators.put(new PrefixedPathResolver(
					IE_CSS_GENERATOR_PREFIX), IECssBundleGenerator.class);
			commonGenerators.put(
					new PrefixedPathResolver(SKIN_GENERATOR_PREFIX),
					CssSkinGenerator.class);
			commonGenerators.put(
					new SuffixedPathResolver(LESS_GENERATOR_SUFFIX),
					LessCssGenerator.class);
		}

		if ((resourceType.equals(JawrConstant.CSS_TYPE) || resourceType
				.equals(JawrConstant.BINARY_TYPE))) {
			commonGenerators.put(new PrefixedPathResolver(
					SPRITE_GENERATOR_PREFIX), SpriteGenerator.class);
		}

		if (ClassLoaderResourceUtils.isClassPresent(WEBJARS_LOCATOR_CLASSNAME)) {
			resourceLocatorRegistry.add(new WebJarsResourceLocator());
		}
	}

	/**
	 * Set the Jawr config
	 * 
	 * @param config
	 *            the config to set
	 */
	public void setConfig(JawrConfig config) {
		this.config = config;
	}

	/**
	 * Sets the resource handler
	 * 
	 * @param rsHandler
	 *            the rsHandler to set
	 */
	public void setResourceReaderHandler(ResourceReaderHandler rsHandler) {
		this.rsHandler = rsHandler;
	}

	/**
	 * Lazy loads generators, to avoid the need for undesired dependencies.
	 * 
	 * @param generatorKey
	 *            the generator key
	 * 
	 * @return the resource generator
	 */
	private ResourceGenerator loadCommonGenerator(String resourcePath) {
		ResourceGenerator generator = null;

		for (Iterator<Entry<ResourceGeneratorResolver, Class<?>>> iterator = commonGenerators
				.entrySet().iterator(); iterator.hasNext();) {
			Entry<ResourceGeneratorResolver, Class<?>> entry = iterator.next();
			ResourceGeneratorResolver resolver = entry.getKey();
			if (resolver.matchPath(resourcePath)) {
				generator = (ResourceGenerator) ClassLoaderResourceUtils
						.buildObjectInstance(entry.getValue());
				if (!generator.getResolver().isSameAs(resolver)) {
					throw new BundlingProcessException(
							"The resolver defined for "
									+ generator.getClass().getName()
									+ " is different from the one expected by Jawr.");
				}
			}
		}

		if (generator != null) {
			initGenerator(generator);
		}

		return generator;
	}

	/**
	 * Initialize the generator
	 * 
	 * @param generator
	 *            the generator to intialize
	 */
	private void initGenerator(ResourceGenerator generator) {

		initializeGeneratorProperties(generator);
		updateRegistries(generator);
		ResourceReader proxy = ResourceGeneratorReaderProxyFactory
				.getResourceReaderProxy(generator, rsHandler, config);
		rsHandler.addResourceReaderToStart(proxy);
	}

	/**
	 * Update the registries with the generator given in parameter
	 * 
	 * @param generator
	 *            the generator
	 */
	private void updateRegistries(ResourceGenerator generator) {

		resourceGeneratorRegistry.add(generator);
		
		resolverRegistry.add(new ResourceGeneratorResolverWrapper(generator,
				generator.getResolver()));
		
		if (generator instanceof StreamResourceGenerator) {
			binaryResourceGeneratorRegistry.add(generator);
		}
		if (generator instanceof CssResourceGenerator) {
			if (((CssResourceGenerator) generator).isHandlingCssImage()) {
				cssImageResourceGeneratorRegistry.add(generator);
			}
		}
	}

	/**
	 * Register a variant resolver
	 * 
	 * @param clazz
	 *            the class of the variant resolver
	 */
	public void registerVariantResolver(String clazz) {

		VariantResolver resolver = (VariantResolver) ClassLoaderResourceUtils
				.buildObjectInstance(clazz);
		registerVariantResolver(resolver);
	}

	/**
	 * Register a variant resolver
	 * 
	 * @param resolver
	 *            the variant resolver
	 */
	public void registerVariantResolver(VariantResolver resolver) {

		for (Iterator<VariantResolver> itResolver = variantResolvers.values()
				.iterator(); itResolver.hasNext();) {
			VariantResolver variantResolver = itResolver.next();
			if (StringUtils.isEmpty(resolver.getVariantType())) {
				throw new IllegalStateException(
						"The getVariantType() method must return something at "
								+ resolver.getClass());
			}

			if (resolver.getVariantType().equals(
					variantResolver.getVariantType())) {
				throw new IllegalStateException(
						"There are 2 resolvers defined for the variant type '"
								+ resolver.getVariantType() + "' : "
								+ variantResolver.getClass() + ";"
								+ resolver.getClass());
			}
		}
		variantResolvers.put(resolver.getVariantType(), resolver);
	}

	/**
	 * Returns the variant resolver corresponding to the variant type given in
	 * parameter
	 * 
	 * @param variantType
	 *            the variant type
	 * @return the variant resolver
	 */
	public VariantResolver getVariantResolver(String variantType) {
		return variantResolvers.get(variantType);
	}

	/**
	 * Register a generator mapping it to the specified prefix.
	 * 
	 * @param clazz
	 *            the classname of the generator
	 */
	public void registerGenerator(String clazz) {

		ResourceGenerator generator = (ResourceGenerator) ClassLoaderResourceUtils
				.buildObjectInstance(clazz);

		if (null == generator.getResolver()) {
			throw new IllegalStateException(
					"The getResolver() method must return something at "
							+ clazz);
		}

		ResourceGeneratorResolver resolver = generator.getResolver();

		// Checks if another generator is already define with the same resolver
		for (ResourceGeneratorResolver resourceGeneratorResolver : resolverRegistry) {
			if (resourceGeneratorResolver.isSameAs(resolver)) {
				String generatorName = generator.getClass().getName();
				if (!clazz.equals(generatorName)) {
					String errorMsg = "Cannot register the generator of class "
							+ generator.getClass().getName()
							+ " since the same resolver is being used by "
							+ generatorName
							+ ". Please specify a different resolver in the getResolver() method.";
					throw new IllegalStateException(errorMsg);
				}
			}
		}

		// Warns the user about if the generator override a built-in generator
		Set<ResourceGeneratorResolver> commonResolvers = commonGenerators
				.keySet();
		for (ResourceGeneratorResolver commonGeneratorResolver : commonResolvers) {
			if (commonGeneratorResolver.isSameAs(resolver)) {
				String generatorName = generator.getClass().getName();
				LOGGER.warn("The custom generator '" + generatorName
						+ "' override a built-in generator");
			}
		}

		initGenerator(generator);
	}

	/**
	 * Initializes the generator properties.
	 * 
	 * @param generator
	 *            the generator
	 */
	private void initializeGeneratorProperties(ResourceGenerator generator) {
		// Initialize the generator
		if (generator instanceof InitializingResourceGenerator) {
			if (generator instanceof ConfigurationAwareResourceGenerator) {
				((ConfigurationAwareResourceGenerator) generator)
						.setConfig(config);
			}
			if (generator instanceof TypeAwareResourceGenerator) {
				((TypeAwareResourceGenerator) generator)
						.setResourceType(resourceType);
			}
			if (generator instanceof ResourceReaderHandlerAwareResourceGenerator) {
				((ResourceReaderHandlerAwareResourceGenerator) generator)
						.setResourceReaderHandler(rsHandler);
			}
			if (generator instanceof WorkingDirectoryLocationAware) {
				((WorkingDirectoryLocationAware) generator)
						.setWorkingDirectory(rsHandler.getWorkingDirectory());
			}
			if (generator instanceof PostInitializationAwareResourceGenerator) {
				((PostInitializationAwareResourceGenerator) generator)
						.afterPropertiesSet();
			}
		}
	}

	/**
	 * Determines wether a path is to be handled by a generator.
	 * 
	 * @param path
	 *            the resource path
	 * @return true if the path could be handled by a generator
	 */
	public boolean isPathGenerated(String path) {
		return null != resolveResourceGenerator(path);
	}

	/**
	 * Returns the path to use in the generation URL for debug mode.
	 * 
	 * @param path
	 *            the resource path
	 * @return the path to use in the generation URL for debug mode.
	 */
	public String getDebugModeGenerationPath(String path) {

		ResourceGenerator resourceGenerator = resolveResourceGenerator(path);
		return resourceGenerator.getDebugModeRequestPath();
	}

	/**
	 * Returns the path to use in the "build time process" to generate the
	 * resource path for debug mode.
	 * 
	 * @param path
	 *            the resource path
	 * @return the path to use in the "build time process" to generate the
	 *         resource path for debug mode.
	 */
	public String getDebugModeBuildTimeGenerationPath(String path) {

		int idx = path.indexOf("?");
		String debugModeGeneratorPath = path.substring(0, idx);
		debugModeGeneratorPath = debugModeGeneratorPath.replaceAll("\\.", "/");

		int jawrGenerationParamIdx = path
				.indexOf(JawrRequestHandler.GENERATION_PARAM);
		String parameter = path.substring(jawrGenerationParamIdx
				+ JawrRequestHandler.GENERATION_PARAM.length() + 1); // Add 1
																		// for
																		// the
																		// '='
																		// character
		ResourceGenerator resourceGenerator = resolveResourceGenerator(parameter);
		String suffixPath = null;
		if (resourceGenerator instanceof SpecificCDNDebugPathResourceGenerator) {
			suffixPath = ((SpecificCDNDebugPathResourceGenerator) resourceGenerator)
					.getDebugModeBuildTimeGenerationPath(parameter);
		} else {
			suffixPath = parameter.replaceFirst(
					GeneratorRegistry.PREFIX_SEPARATOR,
					JawrConstant.URL_SEPARATOR);
		}
		return debugModeGeneratorPath + "/" + suffixPath;
	}

	/**
	 * Finds the resource generator which will handle the resource, whose the
	 * path is given in parameter
	 * 
	 * @param path
	 *            the resource path
	 * @return the resource generator
	 */
	private ResourceGenerator resolveResourceGenerator(String path) {

		ResourceGenerator resourceGenerator = null;
		for (Iterator<ResourceGeneratorResolverWrapper> iterator = resolverRegistry
				.iterator(); iterator.hasNext();) {
			ResourceGeneratorResolverWrapper resolver = iterator.next();
			if (resolver.matchPath(path)) {
				resourceGenerator = resolver.getResourceGenerator();
				break;
			}
		}

		// Lazy load generator
		if (resourceGenerator == null) {
			resourceGenerator = loadCommonGenerator(path);
		}

		return resourceGenerator;
	}

	/**
	 * Returns the resource generator for the path given in parameter
	 * 
	 * @param path
	 *            the path
	 * @return the resource generator for the path given in parameter
	 */
	public ResourceGenerator getResourceGenerator(String path) {

		ResourceGenerator resourceGenerator = null;
		for (Iterator<ResourceGenerator> iterator = resourceGeneratorRegistry
				.iterator(); iterator.hasNext();) {
			ResourceGenerator rsGenerator = (ResourceGenerator) iterator.next();
			if (rsGenerator.getResolver().matchPath(path)) {
				resourceGenerator = rsGenerator;
				break;
			}
		}
		if (resourceGenerator == null) {
			throw new BundlingProcessException(
					"No ResourceGenerator found for the path :" + path);
		}
		return resourceGenerator;
	}

	/**
	 * Loads the generator which corresponds to the specified path.
	 * 
	 * @param path
	 *            the resource path
	 */
	public void loadGeneratorIfNeeded(String path) {

		resolveResourceGenerator(path);
	}

	/**
	 * Returns the available variant for a bundle
	 * 
	 * @param bundle
	 *            the bundle
	 * @return the available variant for a bundle
	 */
	public Map<String, VariantSet> getAvailableVariants(String bundle) {

		Map<String, VariantSet> availableVariants = new TreeMap<String, VariantSet>();
		ResourceGenerator generator = resolveResourceGenerator(bundle);
		if (generator != null) {
			if (generator instanceof VariantResourceGenerator) {

				Map<String, VariantSet> tempResult = ((VariantResourceGenerator) generator)
						.getAvailableVariants(generator.getResolver()
								.getResourcePath(bundle));
				if (tempResult != null) {
					availableVariants = tempResult;
				}
			} else if (generator instanceof LocaleAwareResourceGenerator) {
				List<String> availableLocales = ((LocaleAwareResourceGenerator) generator)
						.getAvailableLocales(generator.getResolver()
								.getResourcePath(bundle));
				if (availableLocales != null) {
					VariantSet variantSet = new VariantSet(
							JawrConstant.LOCALE_VARIANT_TYPE, "",
							availableLocales);
					availableVariants.put(JawrConstant.LOCALE_VARIANT_TYPE,
							variantSet);
				}
			}
		}

		return availableVariants;
	}

	/**
	 * Returns the variant types for a generated resource
	 * 
	 * @param path
	 *            the path
	 * @return he variant types for a generated resource
	 */
	public Set<String> getGeneratedResourceVariantTypes(String path) {

		Set<String> variantTypes = new HashSet<String>();
		ResourceGenerator generator = resolveResourceGenerator(path);
		if (generator != null) {
			if (generator instanceof VariantResourceGenerator) {

				Set<String> tempResult = ((VariantResourceGenerator) generator)
						.getAvailableVariants(
								generator.getResolver().getResourcePath(path))
						.keySet();
				if (tempResult != null) {
					variantTypes = tempResult;
				}
			} else if (generator instanceof LocaleAwareResourceGenerator) {
				variantTypes = new HashSet<String>();
				variantTypes.add(JawrConstant.LOCALE_VARIANT_TYPE);
			}
		}

		return variantTypes;
	}

	/**
	 * Returns true if the generator associated to the css resource path handle
	 * also CSS image.
	 * 
	 * @param cssResourcePath
	 *            the Css resource path
	 * @return true if the generator associated to the css resource path handle
	 *         also CSS image.
	 */
	public boolean isHandlingCssImage(String cssResourcePath) {

		boolean isHandlingCssImage = false;

		ResourceGenerator generator = resolveResourceGenerator(cssResourcePath);
		if (generator != null
				&& cssImageResourceGeneratorRegistry.contains(generator)) {
			isHandlingCssImage = true;
		}

		return isHandlingCssImage;
	}

	/**
	 * Returns true if the generator associated to the binary resource path is
	 * an Image generator.
	 * 
	 * @param resourcePath
	 *            the binary resource path
	 * @return true if the generator associated to the binary resource path is
	 *         an Image generator.
	 */
	public boolean isGeneratedBinaryResource(String resourcePath) {

		boolean isGeneratedImage = false;

		ResourceGenerator generator = resolveResourceGenerator(resourcePath);
		if (generator != null
				&& binaryResourceGeneratorRegistry.contains(generator)) {
			isGeneratedImage = true;
		}

		return isGeneratedImage;
	}

	/**
	 * Resolve the variants for the request passed in parameter
	 * 
	 * @param request
	 *            the request
	 * @return the map of variants defined in the request
	 */
	public Map<String, String> resolveVariants(HttpServletRequest request) {

		Map<String, String> variants = new TreeMap<String, String>();
		for (Iterator<VariantResolver> itVariantResolver = variantResolvers
				.values().iterator(); itVariantResolver.hasNext();) {
			VariantResolver resolver = itVariantResolver.next();
			String value = resolver.resolveVariant(request);
			if (value != null) {
				variants.put(resolver.getVariantType(), value);
			}
		}

		return variants;
	}

	/**
	 * Returns the available variants.
	 *
	 * @param variants
	 *            the current bundle variants
	 * @param curVariants
	 *            the current variant
	 * @return the available variants
	 */
	public Map<String, String> getAvailableVariantMap(
			Map<String, VariantSet> variants, Map<String, String> curVariants) {
		
		Map<String, String> availableVariantMap = new HashMap<String, String>();
		for (Iterator<Entry<String, VariantSet>> iterator = variants.entrySet()
				.iterator(); iterator.hasNext();) {
			Entry<String, VariantSet> entry = iterator.next();
			String variantType = entry.getKey();
			VariantSet variantSet = entry.getValue();
			String variant = variantSet.getDefaultVariant();
			if (curVariants.containsKey(variantType)) {
				String curVariant = curVariants.get(variantType);
				VariantResolver resolver = variantResolvers.get(variantType);
				if (resolver != null) {
					variant = resolver.getAvailableVariant(curVariant,
							variants.get(variantType));
					if (variant == null) {
						variant = variants.get(variantType).getDefaultVariant();
					}
				} else {
					throw new BundlingProcessException(
							"Unable to find variant resolver for variant type '"
									+ variantType + "'");
				}
			}
			availableVariantMap.put(variantType, variant);
		}
		return availableVariantMap;
	}

	/**
	 * Locate resource mapping
	 *
	 * @param mapping the mapping from bundle definition
	 * @return a fully qualified mapping to the resource or the original mapping
	 *         passed in if not supported
	 */
	public String locateResource(String mapping) {

		for (ResourceLocator locator : resourceLocatorRegistry) {
			if (locator.support(mapping)) {
				mapping = locator.getFullMapping(mapping);
				break;
			}
		}
		return mapping;
	}
}
