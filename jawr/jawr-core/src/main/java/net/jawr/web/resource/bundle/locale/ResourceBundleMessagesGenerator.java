/**
 * Copyright 2008-2016 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.locale;

import static net.jawr.web.JawrConstant.JAWR_LOCALE_GENERATOR_ADD_QUOTE_TO_MSG_KEY;
import static net.jawr.web.JawrConstant.JAWR_LOCALE_GENERATOR_FALLBACK_TO_SYSTEM_LOCALE;
import static net.jawr.web.JawrConstant.JAWR_LOCALE_GENERATOR_RESOURCE_BUNDLE_CHARSET;

import java.io.File;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.jawr.web.JawrConstant;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.generator.AbstractJavascriptGenerator;
import net.jawr.web.resource.bundle.generator.CachedGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolverFactory;
import net.jawr.web.resource.bundle.generator.variant.VariantResourceGenerator;
import net.jawr.web.resource.bundle.locale.message.MessageBundleScriptCreator;
import net.jawr.web.resource.bundle.mappings.FilePathMapping;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.util.FileUtils;
import net.jawr.web.util.StringUtils;

/**
 * A generator that creates a script from message bundles. The generated script
 * can be used to reference the message literals easily from javascript.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 * 
 */
@CachedGenerator(name = "ResourceBundle Message", cacheDirectory = "i18nMessages", mappingFileName = "resourceBundleMessageMapping.txt")
public class ResourceBundleMessagesGenerator extends AbstractJavascriptGenerator

		implements VariantResourceGenerator {

	/** The properties file suffix */
	private static final String PROPERTIES_FILE_SUFFIX = ".properties";

	/** The default resource bundle charset */
	private static final String DEFAULT_RESOURCE_BUNDLE_CHARSET = "ISO-8859-1";

	/** The default value for the fallback to system locale property */
	private static final boolean DEFAULT_FALLBACK_TO_SYSTEM_LOCALE = true;

	/** The default value for the property "add quote to message key" */
	private static final boolean DEFAULT_VALUE_ADD_QUOTE_TO_MSG_KEY = false;

	/** The resolver */
	private final ResourceGeneratorResolver resolver;

	/** The cache for the list of available locale per resource */
	private final Map<String, List<String>> cachedAvailableLocalePerResource = new ConcurrentHashMap<>();

	/** The message bundle control */
	protected MessageBundleControl control;

	/**
	 * Constructor
	 */
	public ResourceBundleMessagesGenerator() {

		this.resolver = ResourceGeneratorResolverFactory.createPrefixResolver(GeneratorRegistry.MESSAGE_BUNDLE_PREFIX);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.AbstractCachedGenerator#
	 * afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {

		super.afterPropertiesSet();

		boolean fallbackToSystemLocale = config.getBooleanProperty(
				JawrConstant.JAWR_LOCALE_GENERATOR_FALLBACK_TO_SYSTEM_LOCALE, DEFAULT_FALLBACK_TO_SYSTEM_LOCALE);

		String charsetName = config.getProperty(JawrConstant.JAWR_LOCALE_GENERATOR_RESOURCE_BUNDLE_CHARSET,
				DEFAULT_RESOURCE_BUNDLE_CHARSET);

		Charset charset = Charset.forName(charsetName);

		control = new MessageBundleControl(fallbackToSystemLocale, charset);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.BaseResourceGenerator#
	 * getPathMatcher ()
	 */
	@Override
	public ResourceGeneratorResolver getResolver() {

		return resolver;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.ResourceGenerator#createResource
	 * (java.lang.String, java.nio.charset.Charset)
	 */
	@Override
	public Reader generateResource(String path, GeneratorContext context) {

		MessageBundleScriptCreator creator = new MessageBundleScriptCreator(context, control);
		addLinkedResources(path, context);
		return creator.createScript(context.getCharset());
	}

	/**
	 * Adds the linked resources
	 * 
	 * @param path
	 *            the resource path
	 * @param context
	 *            the generator context
	 */
	protected void addLinkedResources(String path, GeneratorContext context) {
		List<Locale> locales = new ArrayList<>();
		Locale currentLocale = context.getLocale();
		if (currentLocale != null) {
			locales.add(currentLocale);
			if (StringUtils.isNotEmpty(currentLocale.getVariant())) {
				locales.add(new Locale(currentLocale.getCountry(), currentLocale.getLanguage()));
			}
			if (StringUtils.isNotEmpty(currentLocale.getLanguage())) {
				locales.add(new Locale(currentLocale.getCountry()));
			}
		}

		// Adds fallback locale
		locales.add(control.getFallbackLocale());
		Locale baseLocale = new Locale("", "");
		if (!locales.contains(baseLocale)) {
			locales.add(baseLocale);
		}

		List<FilePathMapping> fMappings = getFileMappings(path, context, locales);
		addLinkedResources(path, context, fMappings);
		/**
		 * JoinableResourceBundle bundle = context.getBundle(); if (bundle !=
		 * null) {
		 * 
		 * List<FilePathMapping> bundleFMappings = bundle.getFilePathMappings();
		 * for (FilePathMapping fMapping : fMappings) { FilePathMapping fm = new
		 * FilePathMapping(bundle, fMapping.getPath(),
		 * fMapping.getLastModified()); if (!bundleFMappings.contains(fm)) {
		 * bundleFMappings.add(fm); } } }
		 **/
	}

	/**
	 * Returns the list of file path mapping associate to the resource bundles
	 * locales
	 * 
	 * @param path
	 *            the resource path
	 * @param context
	 *            the generator context
	 * @param locales
	 *            the list of locales
	 * @return the list of file path mapping associate to the resource bundles
	 *         locales
	 */
	protected List<FilePathMapping> getFileMappings(String path, GeneratorContext context, List<Locale> locales) {

		List<FilePathMapping> fMappings = new ArrayList<>();
		FilePathMapping fMapping = null;
		String fileSuffix = PROPERTIES_FILE_SUFFIX;

		String[] names = path.split("\\|");

		for (String resourcePath : names) {

			resourcePath = resourcePath.replace(".", "/");
			for (Locale locale : locales) {

				String resourceBundlePath = control.toBundleName(resourcePath, locale) + fileSuffix;
				URL rbURL = LocaleUtils.getResourceBundleURL(resourceBundlePath, context.getServletContext());
				if (rbURL != null) {
					File f = FileUtils.urlToFile(rbURL);
					String fileName = f.getAbsolutePath();
					if (StringUtils.isNotEmpty(fileName)) {
						long lastModified = rsHandler.getLastModified(fileName);
						fMapping = new FilePathMapping(fileName, lastModified);
						fMappings.add(fMapping);
					}
				}
			}
		}

		return fMappings;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.AbstractCachedGenerator#resetCache
	 * ()
	 */
	@Override
	protected void resetCache() {
		super.resetCache();
		cacheProperties.put(JAWR_LOCALE_GENERATOR_FALLBACK_TO_SYSTEM_LOCALE,
				Boolean.toString(config.getBooleanProperty(JAWR_LOCALE_GENERATOR_FALLBACK_TO_SYSTEM_LOCALE,
						DEFAULT_FALLBACK_TO_SYSTEM_LOCALE)));

		cacheProperties.put(JAWR_LOCALE_GENERATOR_RESOURCE_BUNDLE_CHARSET,
				config.getProperty(JAWR_LOCALE_GENERATOR_RESOURCE_BUNDLE_CHARSET, DEFAULT_RESOURCE_BUNDLE_CHARSET));

		cacheProperties.put(JAWR_LOCALE_GENERATOR_ADD_QUOTE_TO_MSG_KEY, Boolean.toString(config
				.getBooleanProperty(JAWR_LOCALE_GENERATOR_ADD_QUOTE_TO_MSG_KEY, DEFAULT_VALUE_ADD_QUOTE_TO_MSG_KEY)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.AbstractCachedGenerator#
	 * isCacheValid()
	 */
	@Override
	protected boolean isCacheValid() {
		return super.isCacheValid()
				&& StringUtils.equals(cacheProperties.getProperty(JAWR_LOCALE_GENERATOR_FALLBACK_TO_SYSTEM_LOCALE),
						config.getProperty(JAWR_LOCALE_GENERATOR_FALLBACK_TO_SYSTEM_LOCALE,
								Boolean.toString(DEFAULT_FALLBACK_TO_SYSTEM_LOCALE)))
				&& StringUtils.equals(cacheProperties.getProperty(JAWR_LOCALE_GENERATOR_RESOURCE_BUNDLE_CHARSET),
						config.getProperty(JAWR_LOCALE_GENERATOR_RESOURCE_BUNDLE_CHARSET,
								DEFAULT_RESOURCE_BUNDLE_CHARSET))
				&& StringUtils.equals(cacheProperties.getProperty(JAWR_LOCALE_GENERATOR_ADD_QUOTE_TO_MSG_KEY),
						config.getProperty(JAWR_LOCALE_GENERATOR_ADD_QUOTE_TO_MSG_KEY,
								Boolean.toString(DEFAULT_VALUE_ADD_QUOTE_TO_MSG_KEY)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.ResourceGenerator#
	 * getDebugModeBuildTimeGenerationPath(java.lang.String)
	 */
	public String getDebugModeBuildTimeGenerationPath(String path) {

		String debugPath = path.replaceFirst(GeneratorRegistry.PREFIX_SEPARATOR, JawrConstant.URL_SEPARATOR);
		if (debugPath.endsWith("@")) {
			debugPath = debugPath.replaceAll("@", "");
		} else {
			debugPath = debugPath.replaceAll("@", "_");
			debugPath = debugPath.replaceAll("\\|", "_");
		}
		return debugPath + "." + JawrConstant.JS_TYPE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.handler.LocaleAwareResourceReader#
	 * getAvailableLocales (java.lang.String)
	 */
	public List<String> getAvailableLocales(String resource) {

		List<String> availableLocales = cachedAvailableLocalePerResource.get(resource);
		if (availableLocales != null) {
			return availableLocales;
		}
		availableLocales = findAvailableLocales(resource);
		cachedAvailableLocalePerResource.put(resource, availableLocales);
		return availableLocales;

	}

	/**
	 * Finds the available locales
	 * 
	 * @param resource
	 *            the resource
	 * @return the available locales for the resource
	 */
	protected List<String> findAvailableLocales(String resource) {
		List<String> availableLocales;
		availableLocales = LocaleUtils.getAvailableLocaleSuffixesForBundle(resource);
		return availableLocales;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.variant.VariantResourceGenerator
	 * #getAvailableVariants(java.lang.String)
	 */
	public Map<String, VariantSet> getAvailableVariants(String resource) {

		List<String> localeVariants = getAvailableLocales(resource);
		if (localeVariants.isEmpty()) {
			throw new BundlingProcessException("Enable to find the resource bundle : " + resource);
		}
		Map<String, VariantSet> variants = new HashMap<String, VariantSet>();
		VariantSet variantSet = new VariantSet(JawrConstant.LOCALE_VARIANT_TYPE, "", localeVariants);
		variants.put(JawrConstant.LOCALE_VARIANT_TYPE, variantSet);
		return variants;
	}

}
