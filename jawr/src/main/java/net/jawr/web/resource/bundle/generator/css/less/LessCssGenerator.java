/**
 * Copyright 2012 Ibrahim Chaehoi
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
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
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolverFactory;


/**
 * This class defines the Less CSS generator
 * 
 * @author Ibrahim Chaehoi
 */
public class LessCssGenerator extends AbstractCSSGenerator implements ILessCssResourceGenerator, ConfigurationAwareResourceGenerator, PostInitializationAwareResourceGenerator {

	/** The resolver */
	private ResourceGeneratorResolver resolver;
	
	/** The Less engine */
	private LessEngine engine;

	/** The Jawr config */
	private JawrConfig config;
	
	/**
	 * Constructor
	 */
	public LessCssGenerator() {
		resolver = ResourceGeneratorResolverFactory.createSuffixResolver("less");
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.ConfigurationAwareResourceGenerator#setConfig(net.jawr.web.config.JawrConfig)
	 */
	public void setConfig(JawrConfig config) {
		this.config = config;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.BaseResourceGenerator#getResolver()
	 */
	@Override
	public ResourceGeneratorResolver getResolver() {
		return resolver;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.PostInitializationAwareResourceGenerator#afterPropertiesSet()
	 */
	public void afterPropertiesSet() {
		
		String lessScript = config.getProperty("jawr.css.less.generator.less.script.location", "net/jawr/web/resource/bundle/generator/css/less/less.js");
		InputStream isLessScript = getResourceInputStream(lessScript);
		
		String engineScript = config.getProperty("jawr.css.less.generator.engine.script.location", "net/jawr/web/resource/bundle/generator/css/less/engine.js");
		InputStream isEngineScript = getResourceInputStream(engineScript);
		
		String browserScript = config.getProperty("jawr.css.less.generator.browser.script.location", "net/jawr/web/resource/bundle/generator/css/less/browser.js");
		InputStream isBrowserScript = getResourceInputStream(browserScript);
		
		//config.getAllowedExtensions().add("less");
		engine =  new LessEngine(isLessScript, isEngineScript, isBrowserScript);
    }

	/**
	 * Returns the resource input stream
	 * @param path the resource path
	 * @return the resource input stream
	 */
	public InputStream getResourceInputStream(String path) {
		InputStream is = config.getContext().getResourceAsStream(path);
		if(is == null){
			try {
				is = ClassLoaderResourceUtils.getResourceAsStream(path, this);
			} catch (FileNotFoundException e) {
				throw new BundlingProcessException(e);
			}
		}
		
		return is;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.AbstractCSSGenerator#generateResourceForBundle(net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	@Override
	protected Reader generateResourceForBundle(GeneratorContext context) {
		
		String path = context.getPath();
		
		Reader rd = null;
		try {
			List<Class<?>> excluded = new ArrayList<Class<?>>();
			excluded.add(ILessCssResourceGenerator.class);
			rd = context.getResourceReaderHandler().getResource(path, false, excluded);
			
			if(rd == null){
				throw new ResourceNotFoundException(path);
			}
			StringWriter swr = new StringWriter();
			IOUtils.copy(rd, swr);
			
			String result = compile(path, swr.toString());
			rd = new StringReader(result);
			
		} catch (ResourceNotFoundException e) {
			throw new BundlingProcessException("Unable to generate content for resource path : '"+path+"'", e);
		} catch (IOException e) {
			throw new BundlingProcessException("Unable to generate content for resource path : '"+path+"'", e);
		}
		
		return rd;
	}
	
	/**
	 * Compile the LESS source to a CSS source
	 * @param path the compiled resource path
	 * @param content the resource content to compile
	 * @return the compiled CSS content
	 */
	public String compile(String path, String content) {
		try {
			return engine.compile(content);
			
		} catch (LessException e) {
			throw new RuntimeException("Problem compiling Less CSS from Resource '"
					+ path + "'", e);
		}
	}
	
}
