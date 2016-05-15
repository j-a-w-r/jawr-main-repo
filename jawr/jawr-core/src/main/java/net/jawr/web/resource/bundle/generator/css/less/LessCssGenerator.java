/**
 * Copyright 2012-2016 Ibrahim Chaehoi
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

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.core.DefaultLessCompiler;

import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.AbstractCSSGenerator;
import net.jawr.web.resource.bundle.generator.CachedGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolverFactory;

/**
 * This class defines the Less CSS generator
 * 
 * @author Ibrahim Chaehoi
 */
@CachedGenerator(name = "less", cacheDirectory = "lessCss", mappingFileName = "lessGeneratorCache.txt")
public class LessCssGenerator extends AbstractCSSGenerator implements ILessCssResourceGenerator {

	/** The resolver */
	private ResourceGeneratorResolver resolver;

	/** The Less compiler */
	private LessCompiler compiler;

	/** The Less compiler config */
	private Configuration lessConfig;

	/**
	 * Constructor
	 */
	public LessCssGenerator() {
		resolver = ResourceGeneratorResolverFactory.createSuffixResolver(GeneratorRegistry.LESS_GENERATOR_SUFFIX);
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
	 * PostInitializationAwareResourceGenerator#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {

		super.afterPropertiesSet();
		compiler = new DefaultLessCompiler();
		lessConfig = new Configuration();
		lessConfig.getSourceMapConfiguration().setLinkSourceMap(false);
	}

	/**
	 * Generates the less resource
	 * 
	 * @param path
	 *            the path
	 * @param context
	 *            the generator context
	 * 
	 * @return the generated resource
	 */
	protected Reader generateResource(String path, GeneratorContext context) {

		Reader rd = null;
		try {
			List<Class<?>> excluded = new ArrayList<Class<?>>();
			excluded.add(ILessCssResourceGenerator.class);
			JoinableResourceBundle bundle = context.getBundle();
			rd = context.getResourceReaderHandler().getResource(bundle, path, false, excluded);

			if (rd == null) {
				throw new ResourceNotFoundException(path);
			}
			String content = IOUtils.toString(rd);
			String result = compile(bundle, content, path, context);
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
	 * @param bundle
	 *            the bundle
	 * @param content
	 *            the resource content to compile
	 * @param path
	 *            the compiled resource path
	 * @param context
	 *            the generator context
	 * @return the compiled CSS content
	 */
	public String compile(JoinableResourceBundle bundle, String content, String path, GeneratorContext context) {

		JawrLessSource source = new JawrLessSource(bundle, content, path, rsHandler);
		try {
			CompilationResult result = compiler.compile(source, lessConfig);
			addLinkedResources(path, context, source.getLinkedResources());
			return result.getCss();
		} catch (Less4jException e) {
			throw new BundlingProcessException("Unable to generate content for resource path : '" + path + "'", e);
		}

	}

}
