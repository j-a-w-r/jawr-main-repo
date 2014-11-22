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
import net.jawr.web.resource.bundle.generator.classpath.ClassPathImgResourceGenerator;

/**
 * This class defines the generator for webjars binary resources (images, fonts, ..)
 * 
 * @author Ibrahim Chaehoi
 */
public class WebJarsBinaryResourceGenerator extends
		ClassPathImgResourceGenerator {

	/**
	 * Constructor
	 */
	public WebJarsBinaryResourceGenerator() {
		
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.classpath.ClasspathJSGenerator#getClassPathGeneratorHelperPrefix()
	 */
	@Override
	protected String getClassPathGeneratorHelperPrefix() {
		return GeneratorRegistry.WEBJARS_GENERATOR_HELPER_PREFIX;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.classpath.ClasspathJSGenerator#getGeneratorPrefix()
	 */
	@Override
	protected String getGeneratorPrefix() {
		return GeneratorRegistry.WEBJARS_GENERATOR_PREFIX;
	}
}
