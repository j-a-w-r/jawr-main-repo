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

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.jawr.web.JawrConstant;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.generator.AbstractJavascriptGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolverFactory;
import net.jawr.web.resource.bundle.generator.variant.VariantResourceGenerator;
import net.jawr.web.resource.bundle.locale.message.MessageBundleScriptCreator;
import net.jawr.web.resource.bundle.variant.VariantSet;

/**
 * A generator that creates a script from message bundles. The generated script
 * can be used to reference the message literals easily from javascript.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 * 
 */
public class ResourceBundleMessagesGenerator extends
		AbstractJavascriptGenerator implements VariantResourceGenerator {

	/** The resolver */
	private ResourceGeneratorResolver resolver;

	/** The cache for the list of available locale per resource */
	private final Map<String, List<String>> cachedAvailableLocalePerResource = new ConcurrentHashMap<String, List<String>>();

	/**
	 * Constructor
	 */
	public ResourceBundleMessagesGenerator() {

		resolver = ResourceGeneratorResolverFactory
				.createPrefixResolver(GeneratorRegistry.MESSAGE_BUNDLE_PREFIX);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.BaseResourceGenerator#getPathMatcher
	 * ()
	 */
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
	public Reader generateResource(String path, GeneratorContext context) {
		MessageBundleScriptCreator creator = new MessageBundleScriptCreator(
				context);
		return creator.createScript(context.getCharset());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.ResourceGenerator#
	 * getDebugModeBuildTimeGenerationPath(java.lang.String)
	 */
	public String getDebugModeBuildTimeGenerationPath(String path) {

		String debugPath = path.replaceFirst(
				GeneratorRegistry.PREFIX_SEPARATOR, JawrConstant.URL_SEPARATOR);
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
	 * @see
	 * net.jawr.web.resource.handler.LocaleAwareResourceReader#getAvailableLocales
	 * (java.lang.String)
	 */
	public List<String> getAvailableLocales(String resource) {

		List<String> availableLocales = cachedAvailableLocalePerResource
				.get(resource);
		if (availableLocales != null) {
			return availableLocales;
		}
		availableLocales = findAvailableLocales(resource);
		cachedAvailableLocalePerResource.put(resource, availableLocales);
		return availableLocales;

	}

	/**
	 * Finds the available locales
	 * @param resource the resource
	 * @return the available locales for the resource
	 */
	protected List<String> findAvailableLocales(String resource) {
		List<String> availableLocales;
		availableLocales = LocaleUtils
				.getAvailableLocaleSuffixesForBundle(resource);
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
			throw new BundlingProcessException(
					"Enable to find the resource bundle : " + resource);
		}
		Map<String, VariantSet> variants = new HashMap<String, VariantSet>();
		VariantSet variantSet = new VariantSet(
				JawrConstant.LOCALE_VARIANT_TYPE, "", localeVariants);
		variants.put(JawrConstant.LOCALE_VARIANT_TYPE, variantSet);
		return variants;
	}

}
