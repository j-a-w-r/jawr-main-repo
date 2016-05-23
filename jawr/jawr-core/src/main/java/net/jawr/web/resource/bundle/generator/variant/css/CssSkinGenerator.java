/**
 * Copyright 2010-2016 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator.variant.css;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.FileNameUtils;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.css.CssImageUrlRewriter;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.factory.util.PropertiesConfigHelper;
import net.jawr.web.resource.bundle.generator.AbstractCSSGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.PostInitializationAwareResourceGenerator;
import net.jawr.web.resource.bundle.generator.ResourceReaderHandlerAwareResourceGenerator;
import net.jawr.web.resource.bundle.generator.StreamResourceGenerator;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolverFactory;
import net.jawr.web.resource.bundle.generator.variant.VariantResourceGenerator;
import net.jawr.web.resource.bundle.locale.LocaleUtils;
import net.jawr.web.resource.bundle.variant.VariantResourceReaderStrategy;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.resource.handler.reader.ResourceBrowser;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.util.StringUtils;

/**
 * This class defines the generator which will handle CSS skin.
 * 
 * 
 * @author Ibrahim Chaehoi
 */
public class CssSkinGenerator extends AbstractCSSGenerator implements VariantResourceGenerator, ResourceBrowser,
		StreamResourceGenerator, ResourceReaderHandlerAwareResourceGenerator,
		PostInitializationAwareResourceGenerator {

	/** The resource browser */
	private ResourceBrowser rsBrowser;

	/** The resource generator resolver */
	private ResourceGeneratorResolver resolver;

	/** The CSS skin resolver */
	private AbstractCssSkinVariantResolver cssSkinResolver;

	/** The CSS URL rewriter */
	private CssImageUrlRewriter urlRewriter;

	/**
	 * This property defines the type of skin mapping. So it defines how the
	 * resources are mapped in the directory. By default the value is
	 * 
	 * skin_locale => /winter/en_US /winter/fr_FR /summer/en_US /summer/fr_US
	 * 
	 * locale_skin => /en_US/summer /en_US/winter /fr_FR/summer /fr_FR/winter
	 */
	private String skinMappingType = JawrConstant.SKIN_TYPE_MAPPING_SKIN_LOCALE;

	/** The skin mapping */
	private Map<String, Map<String, VariantSet>> skinMapping = new HashMap<String, Map<String, VariantSet>>();

	/** The resource strategy class */
	private Class<?> resourceProviderStrategyClass;

	/**
	 * Constructor
	 * 
	 */
	public CssSkinGenerator() {

		resolver = ResourceGeneratorResolverFactory.createPrefixResolver(JawrConstant.SKIN_VARIANT_TYPE);
	}

	/**
	 * Constructor
	 * 
	 * @param rsBrowser
	 *            the resource browser
	 * @param config
	 *            the config
	 */
	public CssSkinGenerator(ResourceBrowser rsBrowser, JawrConfig config) {

		this(rsBrowser, config, true);
	}

	/**
	 * Constructor
	 * 
	 * @param rsBrowser
	 *            the resource browser
	 * @param config
	 *            the jawr config
	 * @param initSkinMapping
	 *            the flag indicating if we should initialize the skin mapping
	 *            or not.
	 */
	public CssSkinGenerator(ResourceBrowser rsBrowser, JawrConfig config, boolean initSkinMapping) {

		this.rsBrowser = rsBrowser;
		this.config = config;
		cssSkinResolver = (AbstractCssSkinVariantResolver) config.getGeneratorRegistry()
				.getVariantResolver(JawrConstant.SKIN_VARIANT_TYPE);
		if (cssSkinResolver == null) {
			cssSkinResolver = new CssSkinVariantResolver();
			config.getGeneratorRegistry().registerVariantResolver(cssSkinResolver);
		}
		if (initSkinMapping) {
			String skinMappingType = config.getProperty(JawrConstant.SKIN_TYPE_MAPPING_CONFIG_PARAM);
			if (StringUtils.isNotEmpty(skinMappingType)) {
				if (skinMappingType.equals(JawrConstant.SKIN_TYPE_MAPPING_LOCALE_SKIN)
						|| skinMappingType.equals(JawrConstant.SKIN_TYPE_MAPPING_SKIN_LOCALE)) {
					this.skinMappingType = skinMappingType;
				} else {
					throw new IllegalArgumentException(
							"The value for the '" + JawrConstant.SKIN_TYPE_MAPPING_LOCALE_SKIN + "' " + "property ["
									+ skinMappingType + "] is invalid. " + "Please check the docs for valid values ");
				}
			}
			this.skinMapping = getSkinMapping(rsBrowser, config);
			resourceProviderStrategyClass = CssSkinVariantResourceProviderStrategy.class;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.
	 * ResourceReaderHandlerAwareResourceGenerator
	 * #setResourceReaderHandler(net.jawr
	 * .web.resource.handler.reader.ResourceReaderHandler)
	 */
	public void setResourceReaderHandler(ResourceReaderHandler rsHandler) {

		rsBrowser = rsHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.
	 * PostInitializationAwareResourceGenerator#afterPropertiesSet()
	 */
	public void afterPropertiesSet() {

		this.urlRewriter = new CssImageUrlRewriter(config);
		cssSkinResolver = (AbstractCssSkinVariantResolver) config.getGeneratorRegistry()
				.getVariantResolver(JawrConstant.SKIN_VARIANT_TYPE);
		if (cssSkinResolver == null) {
			cssSkinResolver = new CssSkinVariantResolver();
			config.getGeneratorRegistry().registerVariantResolver(cssSkinResolver);
		}

		// Init skin mapping
		String skinMappingType = config.getProperty(JawrConstant.SKIN_TYPE_MAPPING_CONFIG_PARAM);
		if (StringUtils.isNotEmpty(skinMappingType)) {
			if (skinMappingType.equals(JawrConstant.SKIN_TYPE_MAPPING_LOCALE_SKIN)
					|| skinMappingType.equals(JawrConstant.SKIN_TYPE_MAPPING_SKIN_LOCALE)) {
				this.skinMappingType = skinMappingType;
			} else {
				throw new IllegalArgumentException(
						"The value for the '" + JawrConstant.SKIN_TYPE_MAPPING_LOCALE_SKIN + "' " + "property ["
								+ skinMappingType + "] is invalid. " + "Please check the docs for valid values ");
			}
		}
		this.skinMapping = getSkinMapping(rsBrowser, config);
		resourceProviderStrategyClass = CssSkinVariantResourceProviderStrategy.class;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.BaseResourceGenerator#
	 * getPathMatcher ()
	 */
	public ResourceGeneratorResolver getResolver() {

		return resolver;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.variant.VariantResourceGenerator
	 * #getAvailableVariants(java.lang.String)
	 */
	public Map<String, VariantSet> getAvailableVariants(String mapping) {

		Map<String, VariantSet> availableVariants = new HashMap<String, VariantSet>();
		String skinRootDir = getSkinRootDir(mapping, skinMapping.keySet());
		if (skinRootDir != null) {
			Map<String, VariantSet> variantSets = skinMapping.get(skinRootDir);
			for (Iterator<VariantSet> it = variantSets.values().iterator(); it.hasNext();) {
				VariantSet variantSet = it.next();
				availableVariants.put(variantSet.getType(), variantSet);
			}
		}

		return availableVariants;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.AbstractCachedGenerator#generateResource(net.jawr.web.resource.bundle.generator.GeneratorContext, java.lang.String)
	 */
	@Override
	protected Reader generateResource(String path, GeneratorContext context) {
		
		Reader reader = null;
		ResourceReaderHandler readerHandler = context.getResourceReaderHandler();

		String skinRootDir = getSkinRootDir(path, skinMapping.keySet());
		Map<String, VariantSet> ctxVariantSetMap = skinMapping.get(skinRootDir);

		String skinVariantPath = path.substring(skinRootDir.length());
		String[] paths = skinVariantPath.split(JawrConstant.URL_SEPARATOR);

		VariantResourceReaderStrategy strategy = getVariantStrategy(context, ctxVariantSetMap);
		Map<String, String> variantMap = null;
		do {
			variantMap = strategy.nextVariantMapConbination();
			if (variantMap != null) {
				JoinableResourceBundle bundle = context.getBundle();
				reader = getResourceReader(bundle, path, readerHandler, skinRootDir, paths, variantMap);
			}
		} while (variantMap != null && reader == null);

		return reader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.handler.reader.ResourceBrowser#getResourceNames
	 * (java.lang.String)
	 */
	public Set<String> getResourceNames(String path) {

		String resourcePath = getResolver().getResourcePath(path);
		return rsBrowser.getResourceNames(resourcePath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.handler.reader.ResourceBrowser#isDirectory(java
	 * .lang.String)
	 */
	public boolean isDirectory(String path) {

		String resourcePath = getResolver().getResourcePath(path);
		return rsBrowser.isDirectory(resourcePath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.handler.reader.ResourceBrowser#getFilePath(java.
	 * lang.String)
	 */
	@Override
	public String getFilePath(String resourcePath) {
		String path = getResolver().getResourcePath(resourcePath);
		return rsBrowser.getFilePath(path);
	}

	/**
	 * Returns the skin mapping from the Jawr config
	 * 
	 * @param the
	 *            resource browser
	 * @param config
	 *            the Jawr config
	 * @return the skin mapping
	 */
	public Map<String, Map<String, VariantSet>> getSkinMapping(ResourceBrowser rsBrowser, JawrConfig config) {

		Map<String, Map<String, VariantSet>> skinMapping = new HashMap<String, Map<String, VariantSet>>();
		PropertiesConfigHelper props = new PropertiesConfigHelper(config.getConfigProperties(), JawrConstant.CSS_TYPE);
		Set<String> skinRootDirectories = props.getPropertyAsSet(JawrConstant.SKIN_DEFAULT_ROOT_DIRS);
		if (skinMappingType.equals(JawrConstant.SKIN_TYPE_MAPPING_SKIN_LOCALE)) {
			updateSkinMappingUsingTypeSkinLocale(rsBrowser, config, skinMapping, skinRootDirectories);
		} else {
			updateSkinMappingUsingTypeLocaleSkin(rsBrowser, config, skinMapping, skinRootDirectories);
		}

		return skinMapping;
	}

	/**
	 * Returns the skin mapping from the Jawr config, here the resource mapping
	 * type is skin_locale
	 * 
	 * @param rsBrowser
	 *            the resource browser
	 * @param config
	 *            the jawr config
	 * @param skinMapping
	 *            the skin mapping
	 * @param skinRootDirectories
	 *            the skin root directories
	 */
	private void updateSkinMappingUsingTypeSkinLocale(ResourceBrowser rsBrowser, JawrConfig config,
			Map<String, Map<String, VariantSet>> skinMapping, Set<String> skinRootDirectories) {

		String defaultSkinName = null;
		String defaultLocaleName = null;

		// Check if there are no directory which is contained in another root
		// directory
		for (Iterator<String> itRootDir = skinRootDirectories.iterator(); itRootDir.hasNext();) {
			String defaultSkinDir = PathNormalizer.asDirPath(itRootDir.next());
			String defaultSkinDirName = PathNormalizer.getPathName(defaultSkinDir);
			String skinRootDir = PathNormalizer.getParentPath(defaultSkinDir);

			String skinName = null;
			String localeName = null;

			if (LocaleUtils.LOCALE_SUFFIXES.contains(defaultSkinDirName)) { // The
																			// rootDir
																			// point
																			// to
																			// a
																			// locale
																			// directory
				localeName = defaultSkinDirName;
				skinName = PathNormalizer.getPathName(skinRootDir);
				skinRootDir = PathNormalizer.getParentPath(skinRootDir);

			} else {
				skinName = defaultSkinDirName;
			}

			if (defaultSkinName == null) {
				defaultSkinName = skinName;
			} else if (!defaultSkinName.equals(skinName)) {
				throw new BundlingProcessException(
						"The default skin for the skin root directories are not the same. Please check your configuration.");
			}

			if (defaultLocaleName == null) {
				defaultLocaleName = localeName;
			} else if (defaultLocaleName != null && !defaultLocaleName.equals(localeName)) {
				throw new BundlingProcessException(
						"The default locale for the skin root directories are not the same. Please check your configuration.");
			}

			checkRootDirectoryNotOverlap(defaultSkinDir, skinRootDirectories);

			Map<String, VariantSet> variantsMap = getVariants(rsBrowser, skinRootDir, skinName, localeName, true);

			skinMapping.put(skinRootDir, variantsMap);
		}

		// Init resolver
		cssSkinResolver.setDefaultSkin(defaultSkinName);
		cssSkinResolver.setSkinCookieName(config.getSkinCookieName());
	}

	/**
	 * Returns the skin mapping from the Jawr config, here the resource mapping
	 * type is locale_skin
	 * 
	 * @param rsBrowser
	 *            the resource browser
	 * @param config
	 *            the jawr config
	 * @param skinMapping
	 *            the skin mapping
	 * @param skinRootDirectories
	 *            the skin root directories
	 */
	private void updateSkinMappingUsingTypeLocaleSkin(ResourceBrowser rsBrowser, JawrConfig config,
			Map<String, Map<String, VariantSet>> skinMapping, Set<String> skinRootDirectories) {

		String defaultSkinName = null;
		String defaultLocaleName = null;

		// Check if there are no directory which is contained in another root
		// directory
		for (Iterator<String> itRootDir = skinRootDirectories.iterator(); itRootDir.hasNext();) {
			String defaultLocaleDir = PathNormalizer.asDirPath(itRootDir.next());
			String defaultLocaleDirName = PathNormalizer.getPathName(defaultLocaleDir);
			String localeRootDir = PathNormalizer.getParentPath(defaultLocaleDir);

			String skinName = null;
			String localeName = null;

			if (!LocaleUtils.LOCALE_SUFFIXES.contains(defaultLocaleDirName)) { // The
																				// rootDir
																				// point
																				// to
																				// a
																				// skin
																				// directory
				skinName = defaultLocaleDirName;
				localeName = PathNormalizer.getPathName(localeRootDir);
				localeRootDir = PathNormalizer.getParentPath(localeRootDir);

			} else {
				localeName = defaultLocaleDirName;
			}

			if (defaultSkinName == null) {
				defaultSkinName = skinName;
			} else if (!defaultSkinName.equals(skinName)) {
				throw new BundlingProcessException(
						"The default skin for the skin root directories are not the same. Please check your configuration.");
			}

			if (defaultLocaleName == null) {
				defaultLocaleName = localeName;
			} else if (defaultLocaleName != null && !defaultLocaleName.equals(localeName)) {
				throw new BundlingProcessException(
						"The default locale for the skin root directories are not the same. Please check your configuration.");
			}

			checkRootDirectoryNotOverlap(defaultLocaleDir, skinRootDirectories);

			Map<String, VariantSet> variantsMap = getVariants(rsBrowser, localeRootDir, skinName, localeName, false);

			skinMapping.put(localeRootDir, variantsMap);
		}

		CssSkinVariantResolver resolver = (CssSkinVariantResolver) config.getGeneratorRegistry()
				.getVariantResolver(JawrConstant.SKIN_VARIANT_TYPE);
		if (resolver == null) {
			resolver = new CssSkinVariantResolver(defaultSkinName, config.getSkinCookieName());
			config.getGeneratorRegistry().registerVariantResolver(resolver);
		} else {
			resolver.setDefaultSkin(defaultSkinName);
			resolver.setSkinCookieName(config.getSkinCookieName());
		}

	}

	/**
	 * Returns the skin root dir of the path given in parameter
	 * 
	 * @param path
	 *            the resource path
	 * @param the
	 *            set of skin root directories
	 * @return the skin root dir
	 */
	public String getSkinRootDir(String path, Set<String> skinRootDirs) {
		String skinRootDir = null;
		for (Iterator<String> itSkinDir = skinRootDirs.iterator(); itSkinDir.hasNext();) {
			String skinDir = itSkinDir.next();
			if (path.startsWith(skinDir)) {
				skinRootDir = skinDir;
			}
		}
		return skinRootDir;
	}

	/**
	 * Initialize the skinMapping from the parent path
	 * 
	 * @param rsBrowser
	 *            the resource browser
	 * @param rootDir
	 *            the skin root dir path
	 * @param defaultSkinName
	 *            the default skin name
	 * @param defaultLocaleName
	 *            the default locale name
	 */
	private Map<String, VariantSet> getVariants(ResourceBrowser rsBrowser, String rootDir, String defaultSkinName,
			String defaultLocaleName, boolean mappingSkinLocale) {
		Set<String> paths = rsBrowser.getResourceNames(rootDir);
		Set<String> skinNames = new HashSet<String>();
		Set<String> localeVariants = new HashSet<String>();
		for (Iterator<String> itPath = paths.iterator(); itPath.hasNext();) {
			String path = rootDir + itPath.next();
			if (rsBrowser.isDirectory(path)) {
				String dirName = PathNormalizer.getPathName(path);
				if (mappingSkinLocale) {
					skinNames.add(dirName);
					// check if there are locale variants for this skin,
					// and update the localeVariants if needed
					updateLocaleVariants(rsBrowser, path, localeVariants);
				} else {
					if (LocaleUtils.LOCALE_SUFFIXES.contains(dirName)) {
						localeVariants.add(dirName);
						// check if there are skin variants for this locales,
						// and update the skinVariants if needed
						updateSkinVariants(rsBrowser, path, skinNames);
					}
				}
			}
		}

		// Initialize the variant mapping for the skin root directory
		return getVariants(defaultSkinName, skinNames, defaultLocaleName, localeVariants);
	}

	/**
	 * Returns the skin variants
	 * 
	 * @param defaultSkin
	 *            the default skin
	 * @param skinNames
	 *            the skin names
	 * @param defaultLocaleName
	 *            the default locale name
	 * @param localeVariants
	 *            the locale variants
	 * @return the skin variants
	 */
	private Map<String, VariantSet> getVariants(String defaultSkin, Set<String> skinNames, String defaultLocaleName,
			Set<String> localeVariants) {

		Map<String, VariantSet> skinVariants = new HashMap<String, VariantSet>();
		if (!skinNames.isEmpty()) {
			skinVariants.put(JawrConstant.SKIN_VARIANT_TYPE,
					new VariantSet(JawrConstant.SKIN_VARIANT_TYPE, defaultSkin, skinNames));
		}
		if (!localeVariants.isEmpty()) {

			skinVariants.put(JawrConstant.LOCALE_VARIANT_TYPE,
					new VariantSet(JawrConstant.LOCALE_VARIANT_TYPE, defaultLocaleName, localeVariants));
		}

		return skinVariants;
	}

	/**
	 * Update the locale variants from the directory path given in parameter
	 * 
	 * @param rsBrowser
	 *            the resource browser
	 * @param path
	 *            the skin path
	 * @param localeVariants
	 *            the set of locale variants to update
	 */
	private void updateLocaleVariants(ResourceBrowser rsBrowser, String path, Set<String> localeVariants) {

		Set<String> skinPaths = rsBrowser.getResourceNames(path);
		for (Iterator<String> itSkinPath = skinPaths.iterator(); itSkinPath.hasNext();) {
			String skinPath = path + itSkinPath.next();
			if (rsBrowser.isDirectory(skinPath)) {
				String skinDirName = PathNormalizer.getPathName(skinPath);
				if (LocaleUtils.LOCALE_SUFFIXES.contains(skinDirName)) {
					localeVariants.add(skinDirName);
				}
			}
		}
	}

	/**
	 * Update the skin variants from the directory path given in parameter
	 * 
	 * @param rsBrowser
	 *            the resource browser
	 * @param path
	 *            the skin path
	 * @param skinVariants
	 *            the set of skin variants to update
	 */
	private void updateSkinVariants(ResourceBrowser rsBrowser, String path, Set<String> skinVariants) {

		Set<String> skinPaths = rsBrowser.getResourceNames(path);
		for (Iterator<String> itSkinPath = skinPaths.iterator(); itSkinPath.hasNext();) {
			String skinPath = path + itSkinPath.next();
			if (rsBrowser.isDirectory(skinPath)) {
				String skinDirName = PathNormalizer.getPathName(skinPath);
				skinVariants.add(skinDirName);
			}
		}
	}

	/**
	 * Check if there are no directory which is contained in another root
	 * directory,
	 * 
	 * @param dir
	 *            the root directory to check
	 * @param skinRootDirectories
	 *            the root directories
	 */
	private void checkRootDirectoryNotOverlap(String dir, Set<String> skinRootDirectories) {

		String rootDir = removeLocaleSuffixIfExist(dir);
		for (Iterator<String> itSkinDir = skinRootDirectories.iterator(); itSkinDir.hasNext();) {
			String skinDir = PathNormalizer.asDirPath(itSkinDir.next());
			if (!skinDir.equals(dir)) {
				skinDir = removeLocaleSuffixIfExist(skinDir);
				if (skinDir.startsWith(rootDir)) {
					throw new BundlingProcessException(
							"There is a misconfiguration. It is not allowed to have a skin root directory containing another one.");
				}
			}
		}
	}

	/**
	 * Removes from the directory path the locale suffix if it exists
	 * 
	 * @param dir
	 *            the directory path
	 * 
	 *            <pre>
	 * removeLocaleSuffixIfExist( "/css/panel/default/" )         = "/css/panel/default/"
	 * removeLocaleSuffixIfExist( "/css/panel/default/en_US" )   = "/css/panel/default/"
	 *            </pre>
	 * 
	 * @return the directory path, with the locale suffix removed if it exists
	 */
	private String removeLocaleSuffixIfExist(String dir) {

		String result = dir;
		String dirName = PathNormalizer.getPathName(dir);
		if (LocaleUtils.LOCALE_SUFFIXES.contains(dirName)) {
			result = dir.substring(0, dir.indexOf("/" + dirName) + 1);
		}
		return result;
	}

	/**
	 * Returns the variant strategy
	 * 
	 * @param context
	 *            the generator context
	 * @param variantSetMap
	 *            the variantSet map for the current path
	 * @return the variant strategy
	 */
	private VariantResourceReaderStrategy getVariantStrategy(GeneratorContext context,
			Map<String, VariantSet> variantSetMap) {

		VariantResourceReaderStrategy strategy = null;
		try {
			strategy = (VariantResourceReaderStrategy) resourceProviderStrategyClass.newInstance();
			strategy.initVariantProviderStrategy(context, variantSetMap);
		} catch (InstantiationException e) {
			throw new BundlingProcessException(e);
		} catch (IllegalAccessException e) {
			throw new BundlingProcessException(e);
		}

		return strategy;
	}

	/**
	 * Returns the reader for the resource path define in parameter
	 * 
	 * @param bundle
	 *            the resource bundle
	 * @param originalPath
	 *            the original path
	 * @param skinRootDir
	 *            the root directory
	 * @param paths
	 *            the array of path
	 * @param variantMap
	 *            the variant map
	 * @return the reader
	 */
	private Reader getResourceReader(JoinableResourceBundle bundle, String originalPath,
			ResourceReaderHandler readerHandler, String skinRootDir, String[] paths, Map<String, String> variantMap) {

		Reader reader = null;
		StringBuffer path = new StringBuffer(skinRootDir);
		String skinVariant = (String) variantMap.get(JawrConstant.SKIN_VARIANT_TYPE);
		String localeVariant = (String) variantMap.get(JawrConstant.LOCALE_VARIANT_TYPE);

		if (skinMappingType.equals(JawrConstant.SKIN_TYPE_MAPPING_SKIN_LOCALE)) {
			paths[0] = skinVariant;
			if (localeVariant != null) {
				paths[1] = localeVariant;
			}
		} else {
			paths[0] = localeVariant;
			if (skinVariant != null) {
				paths[1] = skinVariant;
			}
		}

		for (int i = 0; i < paths.length; i++) {
			path.append(paths[i]);
			if (i + 1 < paths.length) {
				path.append(JawrConstant.URL_SEPARATOR);
			}
		}

		try {
			String finalPath = path.toString();
			reader = readerHandler.getResource(bundle, finalPath);

			// Rewrite URL if the final CSS path is not the original one
			if (!originalPath.equals(finalPath)) {
				String content = IOUtils.toString(reader);
				StringBuffer result = urlRewriter.rewriteUrl(finalPath, originalPath, content);
				reader = new StringReader(result.toString());
			}
		} catch (ResourceNotFoundException e) {
			// Nothing to do
		} catch (IOException e) {
			// Nothing to do
		}

		return reader;
	}

	/**
	 * Set the variant resource reader strategy
	 * 
	 * @param strategy
	 *            the resource reader strategy to set
	 */
	public void setVariantResourceReaderStrategy(Class<?> strategyClass) {

		resourceProviderStrategyClass = strategyClass;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.StreamResourceGenerator#
	 * createResourceAsStream
	 * (net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	@Override
	public InputStream createResourceAsStream(GeneratorContext context) {

		String path = context.getPath();
		InputStream is = null;
		if (FileNameUtils.hasImageExtension(path)) {
			try {
				is = context.getResourceReaderHandler().getResourceAsStream(path, false);
			} catch (ResourceNotFoundException e) {
				// Nothing to do
			}
		}

		return is;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.AbstractCSSGenerator#
	 * isHandlingCssImage()
	 */
	@Override
	public boolean isHandlingCssImage() {
		return false;
	}

}
