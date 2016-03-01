/**
 * Copyright 2015-2016 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator.css.sass.vaadin;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.ScssContext.UrlMode;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.AbstractCSSGenerator;
import net.jawr.web.resource.bundle.generator.CachedGenerator;
import net.jawr.web.resource.bundle.generator.ConfigurationAwareResourceGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolverFactory;
import net.jawr.web.util.StringUtils;

/**
 * This class defines the Sass generator
 * 
 * @author Ibrahim Chaehoi
 */
@CachedGenerator(name = "sass", cacheDirectory = "sassCss", mappingFileName = "sassGeneratorCache.txt")
public class SassGenerator extends AbstractCSSGenerator
		implements ISassResourceGenerator, ConfigurationAwareResourceGenerator {

	/** The Sass generator URL mode property name */
	public static final String SAAS_GENERATOR_URL_MODE = "jawr.css.saas.generator.urlMode";

	/** The Sass generator URL mode default value */
	public static final String SASS_GENERATOR_DEFAULT_URL_MODE = "MIXED";

	/** The resolver */
	private ResourceGeneratorResolver resolver;

	/** The URL mode handling for binary resource URL present in the Scss file */
	private ScssContext.UrlMode urlMode;

	/** The Jawr config */
	private JawrConfig config;
	
	/**
	 * Constructor
	 */
	public SassGenerator() {
		resolver = ResourceGeneratorResolverFactory.createSuffixResolver(GeneratorRegistry.SASS_GENERATOR_SUFFIX);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.BaseResourceGenerator#getResolver
	 * ()
	 */
	@Override
	public ResourceGeneratorResolver getResolver() {
		return resolver;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.
	 * ConfigurationAwareResourceGenerator#setConfig(net.jawr.web.config.
	 * JawrConfig)
	 */
	@Override
	public void setConfig(JawrConfig config) {

		this.config = config;
		String value = this.config.getProperty(SAAS_GENERATOR_URL_MODE, SASS_GENERATOR_DEFAULT_URL_MODE);
		urlMode = UrlMode.valueOf(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.AbstractCssCachedGenerator#
	 * generateResource(net.jawr.web.resource.bundle.generator.GeneratorContext,
	 * java.lang.String)
	 */
	@Override
	protected Reader generateResource(String path, GeneratorContext context) {

		Reader rd = null;

		try {
			List<Class<?>> excluded = new ArrayList<Class<?>>();
			excluded.add(ISassResourceGenerator.class);
			JoinableResourceBundle bundle = context.getBundle();
			rd = context.getResourceReaderHandler().getResource(bundle, path, false, excluded);

			if (rd == null) {
				throw new ResourceNotFoundException(path);
			}
			String content = IOUtils.toString(rd);

			String result = compile(bundle, content, path, context.getCharset());
			rd = new StringReader(result);

		} catch (ResourceNotFoundException e) {
			throw new BundlingProcessException("Unable to generate content for resource path : '" + path + "'", e);
		} catch (IOException e) {
			throw new BundlingProcessException("Unable to generate content for resource path : '" + path + "'", e);
		}

		return rd;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.AbstractCachedGenerator#resetCache()
	 */
	@Override
	protected void resetCache() {
		super.resetCache();
		cacheProperties.put(SAAS_GENERATOR_URL_MODE, urlMode.toString());
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.AbstractCachedGenerator#isCacheValid()
	 */
	@Override
	protected boolean isCacheValid() {
		
		String cachedUrlMode = cacheProperties.getProperty(SAAS_GENERATOR_URL_MODE);
		return StringUtils.equals(cachedUrlMode, config.getProperty(SAAS_GENERATOR_URL_MODE, SASS_GENERATOR_DEFAULT_URL_MODE));
	}

	/**
	 * Compile the SASS source to a CSS source
	 * 
	 * @param bundle
	 *            the bundle
	 * @param content
	 *            the resource content to compile
	 * @param path
	 *            the compiled resource path
	 * @return the compiled CSS content
	 */
	public String compile(JoinableResourceBundle bundle, String content, String path, Charset charset) {

		try {
			JawrScssResolver scssResolver = new JawrScssResolver(bundle, rsHandler);
			JawrScssStylesheet sheet = new JawrScssStylesheet(bundle, content, path, scssResolver, charset);
			scssResolver.setParentScssStyleSheet(sheet);
			sheet.compile(urlMode);
			String parsedScss = sheet.printState();
			addLinkedResources(path, sheet.getLinkedResources());

			return parsedScss;
		} catch (Exception e) {
			throw new BundlingProcessException("Unable to generate content for resource path : '" + path + "'", e);
		}
	}

}
