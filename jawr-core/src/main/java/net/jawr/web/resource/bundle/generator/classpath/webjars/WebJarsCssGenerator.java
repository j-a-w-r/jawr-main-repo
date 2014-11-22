/**
 * Copyright 2014 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator.classpath.webjars;

import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.classpath.ClassPathCSSGenerator;

/**
 * This class defines the generator for webjar CSS resources
 * 
 * @author Ibrahim Chaehoi
 */
public class WebJarsCssGenerator extends ClassPathCSSGenerator {

	/**
	 * The name of the directory which contain the CSS defined using webjar for
	 * the DEBUG mode
	 */
	private static final String TEMP_CSS_CLASSPATH_SUBDIR = "webJarsCss";

	/**
	 * Constructor
	 */
	public WebJarsCssGenerator() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.classpath.ClassPathCSSGenerator
	 * #getGeneratorPrefix()
	 */
	@Override
	protected String getGeneratorPrefix() {

		return GeneratorRegistry.WEBJARS_GENERATOR_PREFIX;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.classpath.ClassPathCSSGenerator
	 * #getClassPathGeneratorHelperPrefix()
	 */
	@Override
	protected String getClassPathGeneratorHelperPrefix() {
		return GeneratorRegistry.WEBJARS_GENERATOR_HELPER_PREFIX;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.classpath.ClassPathCSSGenerator
	 * #getTempDirectoryName()
	 */
	@Override
	protected String getTempDirectoryName() {
		return TEMP_CSS_CLASSPATH_SUBDIR;
	}

}
