/**
 * Copyright 2009-2016 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator.img;

import java.io.InputStream;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.generator.ConfigurationAwareResourceGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.PostInitializationAwareResourceGenerator;
import net.jawr.web.resource.bundle.generator.ResourceGenerator;
import net.jawr.web.resource.bundle.generator.StreamResourceGenerator;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolverFactory;
import net.jawr.web.resource.bundle.global.preprocessor.css.smartsprites.CssSmartSpritesResourceReader;
import net.jawr.web.resource.handler.reader.StreamResourceReader;
import net.jawr.web.resource.handler.reader.WorkingDirectoryLocationAware;

/**
 * The sprite generator.
 * 
 * @author Ibrahim CHAEHOI
 * 
 */
public class SpriteGenerator implements StreamResourceGenerator, ConfigurationAwareResourceGenerator,
		WorkingDirectoryLocationAware, PostInitializationAwareResourceGenerator {

	/** The stream resource handle for image sprite */
	private StreamResourceReader rd;

	/** The working directory */
	private String workingDirectory;

	/** The resource generator resolver */
	private final ResourceGeneratorResolver resolver;

	/** The jawr config */
	private JawrConfig config;

	/**
	 * Constructor
	 */
	public SpriteGenerator() {
		resolver = ResourceGeneratorResolverFactory.createPrefixResolver(GeneratorRegistry.SPRITE_GENERATOR_PREFIX);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.handler.reader.WorkingDirectoryLocationAware#
	 * setWorkingDirectory(java.lang.String)
	 */
	@Override
	public void setWorkingDirectory(String workingDir) {
		workingDirectory = workingDir;
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.
	 * PostInitializationAwareResourceGenerator#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {

		rd = new CssSmartSpritesResourceReader(workingDirectory, config);
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
		return rd.getResourceAsStream(path);
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
	 * @see net.jawr.web.resource.bundle.generator.BaseResourceGenerator#
	 * getDebugModeRequestPath()
	 */
	@Override
	public String getDebugModeRequestPath() {

		return ResourceGenerator.IMG_DEBUGPATH;
	}

}
