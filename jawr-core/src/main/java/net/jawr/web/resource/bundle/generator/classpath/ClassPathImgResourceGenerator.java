/**
 * Copyright 2009-2012 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator.classpath;

import java.io.InputStream;

import net.jawr.web.resource.FileNameUtils;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.ResourceGenerator;
import net.jawr.web.resource.bundle.generator.StreamResourceGenerator;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolverFactory;

/**
 * This class defines the resource generator which loads image resources from
 * the classpath.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class ClassPathImgResourceGenerator implements StreamResourceGenerator {

	/** the class path generator helper */
	private static final String CLASSPATH_GENERATOR_HELPER_PREFIX = "";

	/** The resolver */
	private ResourceGeneratorResolver resolver;

	/** The classpath generator helper */
	private ClassPathGeneratorHelper helper;

	/**
	 * Constructor.
	 */
	public ClassPathImgResourceGenerator() {
		helper = new ClassPathGeneratorHelper(
				getClassPathGeneratorHelperPrefix());
		resolver = createResolver(getGeneratorPrefix());
	}

	/**
	 * create the resource generator resolver
	 *
	 * @param  generatorPrefix the generator prefix
	 * @return the resource generator resolver
	 */
	protected ResourceGeneratorResolver createResolver(String generatorPrefix) {
		return ResourceGeneratorResolverFactory.createPrefixResolver(generatorPrefix);
	}

	/**
	 * Returns the generator prefix
	 * 
	 * @return the generator prefix
	 */
	protected String getGeneratorPrefix() {
		return GeneratorRegistry.CLASSPATH_RESOURCE_BUNDLE_PREFIX;
	}

	/**
	 * Returns the class path generator helper prefix
	 * 
	 * @return the class path generator helper prefix
	 */
	protected String getClassPathGeneratorHelperPrefix() {
		return CLASSPATH_GENERATOR_HELPER_PREFIX;
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
	 * @see net.jawr.web.resource.bundle.generator.BaseResourceGenerator#
	 * getDebugModeRequestPath()
	 */
	public String getDebugModeRequestPath() {

		return ResourceGenerator.IMG_DEBUGPATH;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.StreamResourceGenerator#
	 * createResourceAsStream
	 * (net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	public InputStream createResourceAsStream(GeneratorContext context) {

		InputStream is = null;
		if (FileNameUtils.hasImageExtension(context.getPath())) {
			is = helper.createStreamResource(context);
		}

		return is;
	}

}
