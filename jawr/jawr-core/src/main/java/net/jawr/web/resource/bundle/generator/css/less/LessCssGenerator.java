/**
 * Copyright 2012-2014 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator.css.less;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.generator.AbstractCSSGenerator;
import net.jawr.web.resource.bundle.generator.ConfigurationAwareResourceGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.PostInitializationAwareResourceGenerator;
import net.jawr.web.resource.bundle.generator.ResourceReaderHandlerAwareResourceGenerator;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolverFactory;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.util.StringUtils;

import com.asual.lesscss.LessEngine;
import com.asual.lesscss.LessException;
import com.asual.lesscss.LessOptions;
import com.asual.lesscss.loader.ChainedResourceLoader;
import com.asual.lesscss.loader.CssProcessingResourceLoader;
import com.asual.lesscss.loader.ResourceLoader;
import com.asual.lesscss.loader.UnixNewlinesResourceLoader;

/**
 * This class defines the Less CSS generator
 * 
 * @author Ibrahim Chaehoi
 */
public class LessCssGenerator extends AbstractCSSGenerator implements
		ILessCssResourceGenerator, ConfigurationAwareResourceGenerator,
		ResourceReaderHandlerAwareResourceGenerator,
		PostInitializationAwareResourceGenerator {

	/** The less script location property name */
	private static final String LESS_SCRIPT_LOCATION_PROPERTY_NAME = "jawr.css.less.generator.less.script.location";

	/** The less resource loader class property name */
	private static final String LESS_RESOURCE_LOADER_PROPERTY_NAME = "jawr.css.less.generator.resource.loader.class";

	/** The Less suffix */
	private static final String LESS_SUFFIX = "less";

	/** The resolver */
	private ResourceGeneratorResolver resolver;

	/** The ResourceReaderHandler */
	private ResourceReaderHandler rsHandler;

	/** The Less engine */
	private LessEngine engine;

	/** The Jawr config */
	private JawrConfig config;

	/** The LESS options */
	private LessOptions options = new LessOptions();

	/**
	 * Constructor
	 */
	public LessCssGenerator() {
		resolver = ResourceGeneratorResolverFactory
				.createSuffixResolver(LESS_SUFFIX);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.ConfigurationAwareResourceGenerator
	 * #setConfig(net.jawr.web.config.JawrConfig)
	 */
	public void setConfig(JawrConfig config) {
		this.config = config;
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
	 * ResourceReaderHandlerAwareResourceGenerator
	 * #setResourceReaderHandler(net.jawr
	 * .web.resource.handler.reader.ResourceReaderHandler)
	 */
	@Override
	public void setResourceReaderHandler(ResourceReaderHandler rsHandler) {
		this.rsHandler = rsHandler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.
	 * PostInitializationAwareResourceGenerator#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {

		String lessScript = config
				.getProperty(LESS_SCRIPT_LOCATION_PROPERTY_NAME);
		if (lessScript != null) {
			URL lessURL = getResourceURL(lessScript);
			if (lessURL != null) {
				options.setLess(lessURL);
			}
		}

		engine = new LessEngine(options,
				buildResourceLoader(options, rsHandler));
	}

	/**
	 * Builds the resource loader
	 * 
	 * @param options
	 * @param rsReaderHandler
	 * @return
	 */
	private ResourceLoader buildResourceLoader(LessOptions options,
			ResourceReaderHandler rsReaderHandler) {

		String resourceLoaderClass = config
				.getProperty(LESS_RESOURCE_LOADER_PROPERTY_NAME);
		ResourceLoader resourceLoader = null;
		if (StringUtils.isNotEmpty(resourceLoaderClass)) {
			ResourceLoader customResourceLoader = (ResourceLoader) ClassLoaderResourceUtils
					.buildObjectInstance(resourceLoaderClass);
			resourceLoader = new ChainedResourceLoader(new JawrResourceLoader(
					rsReaderHandler), customResourceLoader);
		} else {
			resourceLoader = new JawrResourceLoader(rsReaderHandler);
		}

		if (options.isCss()) {
			return new CssProcessingResourceLoader(resourceLoader);
		}
		resourceLoader = new UnixNewlinesResourceLoader(resourceLoader);
		return resourceLoader;
	}

	/**
	 * Returns the resource input stream
	 * 
	 * @param path
	 *            the resource path
	 * @return the resource input stream
	 */
	public URL getResourceURL(String path) {
		URL url = null;
		try {
			url = config.getContext().getResource(path);

			if (url == null) {
				url = ClassLoaderResourceUtils.getResourceURL(path, this);
			}
		} catch (MalformedURLException e) {
			throw new BundlingProcessException(e);
		} catch (ResourceNotFoundException e1) {
			throw new BundlingProcessException(e1);
		}

		return url;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.AbstractCSSGenerator#
	 * generateResourceForBundle
	 * (net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	@Override
	protected Reader generateResourceForBundle(GeneratorContext context) {

		String path = context.getPath();

		Reader rd = null;
		try {
			List<Class<?>> excluded = new ArrayList<Class<?>>();
			excluded.add(ILessCssResourceGenerator.class);
			rd = context.getResourceReaderHandler().getResource(path, false,
					excluded);

			if (rd == null) {
				throw new ResourceNotFoundException(path);
			}
			StringWriter swr = new StringWriter();
			IOUtils.copy(rd, swr);

			String result = compile(swr.toString(), path);
			rd = new StringReader(result);

		} catch (ResourceNotFoundException e) {
			throw new BundlingProcessException(
					"Unable to generate content for resource path : '" + path
							+ "'", e);
		} catch (IOException e) {
			throw new BundlingProcessException(
					"Unable to generate content for resource path : '" + path
							+ "'", e);
		}

		return rd;
	}

	/**
	 * Compile the LESS source to a CSS source
	 * 
	 * @param content
	 *            the resource content to compile
	 * @param path
	 *            the compiled resource path
	 * @return the compiled CSS content
	 */
	public String compile(String content, String path) {
		try {
			return engine.compile(content, path);

		} catch (LessException e) {
			throw new RuntimeException(
					"Problem compiling Less CSS from Resource '" + path + "'",
					e);
		}
	}

}
