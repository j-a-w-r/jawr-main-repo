/**
 * Copyright 2012-2015 Ibrahim Chaehoi
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
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.DefaultLessCompiler;

import net.jawr.web.JawrConstant;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.generator.AbstractCSSGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.PostInitializationAwareResourceGenerator;
import net.jawr.web.resource.bundle.generator.ResourceReaderHandlerAwareResourceGenerator;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolverFactory;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.util.StopWatch;

/**
 * This class defines the Less CSS generator
 * 
 * @author Ibrahim Chaehoi
 */
public class LessCssGenerator extends AbstractCSSGenerator implements ILessCssResourceGenerator,
		ResourceReaderHandlerAwareResourceGenerator, PostInitializationAwareResourceGenerator {

	/** The Logger */
	private static Logger PERF_LOGGER = LoggerFactory.getLogger(JawrConstant.PERF_PROCESSING_LOGGER);

	/** The Less suffix */
	private static final String LESS_SUFFIX = "less";

	/** The resolver */
	private ResourceGeneratorResolver resolver;

	/** The ResourceReaderHandler */
	private ResourceReaderHandler rsHandler;

	/** The Less compiler */
	private LessCompiler compiler;

	/** The Less compiler config */
	private Configuration lessConfig;

	/**
	 * Constructor
	 */
	public LessCssGenerator() {
		resolver = ResourceGeneratorResolverFactory.createSuffixResolver(LESS_SUFFIX);
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

		compiler = new DefaultLessCompiler();
		lessConfig = new Configuration();
		lessConfig.getSourceMapConfiguration().setLinkSourceMap(false);
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
			if (context.isContentProvided()) {
				content = context.getProvidedSourceContent();
			} else {
				List<Class<?>> excluded = new ArrayList<Class<?>>();
				excluded.add(ILessCssResourceGenerator.class);
				rd = context.getResourceReaderHandler().getResource(path, false, excluded);

				if (rd == null) {
					throw new ResourceNotFoundException(path);
				}
				content = IOUtils.toString(rd);
			}

			String result = compile(content, path);
			rd = new StringReader(result);

		} catch (ResourceNotFoundException e) {
			throw new BundlingProcessException("Unable to generate content for resource path : '" + path + "'", e);
		} catch (IOException e) {
			throw new BundlingProcessException("Unable to generate content for resource path : '" + path + "'", e);
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
		StopWatch stopWatch = new StopWatch("Compiling resource '" + path + "' with Less generator");
		stopWatch.start();
		LessSource source = new JawrLessSource(content, path, rsHandler);
		try {
			CompilationResult result = compiler.compile(source, lessConfig);
			return result.getCss();
		} catch (Less4jException e) {
			throw new BundlingProcessException("Unable to generate content for resource path : '" + path + "'", e);
		} finally {
			stopWatch.stop();
			if (PERF_LOGGER.isDebugEnabled()) {
				PERF_LOGGER.debug(stopWatch.shortSummary());
			}
		}

	}

}
