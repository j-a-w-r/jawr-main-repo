/**
 * Copyright 2016 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator.css.sass.ruby;

import static net.jawr.web.JawrConstant.SASS_GENERATOR_ABSOLUTE_URL_MODE;
import static net.jawr.web.JawrConstant.SASS_GENERATOR_RELATIVE_URL_MODE;
import static net.jawr.web.JawrConstant.SASS_GENERATOR_URL_MODE;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.generator.AbstractCSSGenerator;
import net.jawr.web.resource.bundle.generator.CachedGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.css.sass.ISassResourceGenerator;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolverFactory;
import net.jawr.web.util.StringUtils;

/**
 * The Saas Generator based on ruby sass-gems.
 * 
 * @author Ibrahim Chaehoi
 */
@CachedGenerator(name = "sass", cacheDirectory = "sassRubyCss", mappingFileName = "sassGeneratorCache.txt")
public class SassRubyGenerator extends AbstractCSSGenerator
		implements ISassResourceGenerator {

	/** The Jawr Importer for the Sass Ruby engine */
	private static final String JAWR_IMPORTER_RB = "/net/jawr/web/resource/bundle/generator/css/sass/jawr-sass.rb";

	/** The jawr resolver variable */
	private static final String JAWR_RESOLVER_VAR = "@jawrResolver";

	/** The default Sass Ruby Url mode */
	public static final String SASS_GENERATOR_DEFAULT_URL_MODE = SASS_GENERATOR_ABSOLUTE_URL_MODE;
	
	/** The resolver */
	private ResourceGeneratorResolver resolver;

	/** The ruby engine */
	private ScriptEngine rubyEngine;

	/** The flag indicating if we must use absolute URL when referencing binary resources */
	private boolean useAbsoluteURL = false;

	/**
	 * Constructor
	 */
	public SassRubyGenerator() {
		System.setProperty("org.jruby.embed.compat.version", "JRuby1.9");
		System.setProperty("org.jruby.embed.localvariable.behavior", "transient");
		resolver = ResourceGeneratorResolverFactory.createSuffixResolver(GeneratorRegistry.SASS_GENERATOR_SUFFIX);
		rubyEngine = new ScriptEngineManager().getEngineByName("jruby");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.ResourceGenerator#getResolver()
	 */
	@Override
	public ResourceGeneratorResolver getResolver() {
		return resolver;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.AbstractCachedGenerator#setConfig(net.jawr.web.config.JawrConfig)
	 */
	@Override
	public void setConfig(JawrConfig config) {
	
		super.setConfig(config);
		String value = this.config.getProperty(SASS_GENERATOR_URL_MODE, SASS_GENERATOR_DEFAULT_URL_MODE);
		if(!value.equalsIgnoreCase(SASS_GENERATOR_ABSOLUTE_URL_MODE) && !value.equalsIgnoreCase(SASS_GENERATOR_RELATIVE_URL_MODE)){
			throw new BundlingProcessException("The value '"+value+"' is not allowed for '"+SASS_GENERATOR_URL_MODE+"' in the Saas Ruby generator");
		}
		useAbsoluteURL = value.equalsIgnoreCase(SASS_GENERATOR_ABSOLUTE_URL_MODE);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.AbstractCachedGenerator#resetCache()
	 */
	@Override
	protected void resetCache() {
		super.resetCache();
		cacheProperties.put(JawrConstant.SASS_GENERATOR_URL_MODE, useAbsoluteURL ? SASS_GENERATOR_ABSOLUTE_URL_MODE : SASS_GENERATOR_RELATIVE_URL_MODE);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.AbstractCachedGenerator#isCacheValid()
	 */
	@Override
	protected boolean isCacheValid() {
		
		String cachedUrlMode = cacheProperties.getProperty(JawrConstant.SASS_GENERATOR_URL_MODE);
		return StringUtils.equals(cachedUrlMode, config.getProperty(JawrConstant.SASS_GENERATOR_URL_MODE, SASS_GENERATOR_DEFAULT_URL_MODE));
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.AbstractCachedGenerator#
	 * generateResource(java.lang.String,
	 * net.jawr.web.resource.bundle.generator.GeneratorContext)
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

		} catch (ResourceNotFoundException | ScriptException | IOException e) {
			throw new BundlingProcessException("Unable to generate content for resource path : '" + path + "'", e);
		}

		return rd;
	}

	/**
	 * Compile the Sass content
	 * 
	 * @param bundle
	 *            the bundle
	 * @param content
	 *            the content to compile
	 * @param path
	 *            the path
	 * @param charset
	 *            the current charset
	 * @return the compiled Sass content
	 * @throws ScriptException
	 *             if a ScriptException occurs
	 * @throws IOException
	 *             if an IOExceptions occurs
	 */
	private String compile(JoinableResourceBundle bundle, String content, String path, Charset charset)
			throws ScriptException, IOException {

		// rubyEngine.put("jawrResolver", new JawrSassResolver(rsHandler));
		InputStream is = getResourceInputStream(JAWR_IMPORTER_RB);
		String script = IOUtils.toString(is);
		rubyEngine.eval(script);
		SimpleBindings bindings = new SimpleBindings();
		bindings.put(JAWR_RESOLVER_VAR, new JawrSassResolver(bundle, path, rsHandler, useAbsoluteURL));
		
		return rubyEngine.eval(buildUpdateScript(path, content), bindings).toString();
	}

	private String buildUpdateScript(String path, String content) {

		StringBuilder script = new StringBuilder();
		
		script.append("require 'rubygems'\n"
				+ "require 'sass/plugin'\n"
				+ "require 'sass/engine'\n");
		
		content = SassRubyUtils.normalizeMultiByteString(content);
		
		script.append(String.format(
				"customImporter = Sass::Importers::JawrImporter.new(@jawrResolver) \n" + "name = \"%s\"\n"
						+ "result = Sass::Engine.new(\"%s\", {:importer => customImporter, :filename => name, :syntax => :scss, :cache => false}).render",
						path,
						content.replace("\"", "\\\"").replace("#", "\\#")));
		return script.toString();
	}

	/**
	 * Returns the resource input stream
	 * 
	 * @param path
	 *            the resource path
	 * @return the resource input stream
	 */
	private InputStream getResourceInputStream(String path) {
		InputStream is = config.getContext().getResourceAsStream(path);
		if (is == null) {
			try {
				is = ClassLoaderResourceUtils.getResourceAsStream(path, this);
			} catch (FileNotFoundException e) {
				throw new BundlingProcessException(e);
			}
		}

		return is;
	}

}
