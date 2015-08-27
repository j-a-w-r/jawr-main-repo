/**
 * Copyright 2015 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator.css.sass;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.ScssContext.UrlMode;
import com.vaadin.sass.internal.ScssStylesheet;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.generator.AbstractCSSGenerator;
import net.jawr.web.resource.bundle.generator.ConfigurationAwareResourceGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.ResourceReaderHandlerAwareResourceGenerator;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolverFactory;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.util.StopWatch;

/**
 * This class defines the Sass generator
 * 
 * @author Ibrahim Chaehoi
 */
public class SassGenerator extends AbstractCSSGenerator implements ISassResourceGenerator,
		ResourceReaderHandlerAwareResourceGenerator, ConfigurationAwareResourceGenerator {

	/** The Logger */
	private static Logger PERF_LOGGER = LoggerFactory.getLogger(JawrConstant.PERF_PROCESSING_LOGGER);

	/** The Sass generator URL mode property name */
	public static final String SAAS_GENERATOR_URL_MODE = "jawr.css.saas.generator.urlMode";

	/** The Sass generator URL mode default value */
	public static final String SASS_GENERATOR_DEFAULT_URL_MODE = "MIXED";

	/** The resolver */
	private ResourceGeneratorResolver resolver;

	/** The ResourceReaderHandler */
	private JawrScssResolver scssResolver;

	/**
	 * The URL mode handling for binary resource URL present in the Scss file
	 */
	private ScssContext.UrlMode urlMode;

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
	 * ResourceReaderHandlerAwareResourceGenerator
	 * #setResourceReaderHandler(net.jawr
	 * .web.resource.handler.reader.ResourceReaderHandler)
	 */
	@Override
	public void setResourceReaderHandler(ResourceReaderHandler rsHandler) {
		this.scssResolver = new JawrScssResolver(rsHandler);
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

		String value = config.getProperty(SAAS_GENERATOR_URL_MODE, SASS_GENERATOR_DEFAULT_URL_MODE);
		urlMode = UrlMode.valueOf(value);
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
		String content = null;
		Reader rd = null;

		try {
			List<Class<?>> excluded = new ArrayList<Class<?>>();
			excluded.add(ISassResourceGenerator.class);
			rd = context.getResourceReaderHandler().getResource(path, false, excluded);

			if (rd == null) {
				throw new ResourceNotFoundException(path);
			}
			content = IOUtils.toString(rd);

			String result = compile(content, path, context.getCharset());
			rd = new StringReader(result);

		} catch (ResourceNotFoundException e) {
			throw new BundlingProcessException("Unable to generate content for resource path : '" + path + "'", e);
		} catch (IOException e) {
			throw new BundlingProcessException("Unable to generate content for resource path : '" + path + "'", e);
		}

		return rd;
	}

	/**
	 * Compile the SASS source to a CSS source
	 * 
	 * @param content
	 *            the resource content to compile
	 * @param path
	 *            the compiled resource path
	 * @return the compiled CSS content
	 */
	public String compile(String content, String path, Charset charset) {
		StopWatch stopWatch = new StopWatch("Compiling resource '" + path + "' with Sass generator");
		stopWatch.start();

		try {
			ScssStylesheet sheet = new JawrScssStylesheet(content, path, scssResolver, charset);
			sheet.compile(urlMode);
			String parsedScss = sheet.printState();
			return parsedScss;
		} catch (Exception e) {
			throw new BundlingProcessException("Unable to generate content for resource path : '" + path + "'", e);
		} finally {
			stopWatch.stop();
			if (PERF_LOGGER.isDebugEnabled()) {
				PERF_LOGGER.debug(stopWatch.shortSummary());
			}
		}

	}

}
