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

import java.util.Set;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.WebJarsLocatorPathResolver;

/**
 * This class defines the generator for webjars JS resources which use the
 * WebJarsLocator. So to reference a resource you only need the short path
 * reference instead of the full path one. <br/>
 * For example : webjars:jquery.js instead of webjars:/jquery/2.1.4/jquery.js
 * To avoid resource reference collision if there multiple resource with the same name in different webjars,
 * like below :</br>
 * webjars:/jquery.js[jquery]
 *
 * @author (Original) Ted Liang (https://github.com/tedliang)
 * @author Ibrahim Chaehoi
 */
public class WebJarsLocatorJSGenerator extends WebJarsJSGenerator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.classpath.ClasspathJSGenerator
	 * #createResolver(java.lang.String)
	 */
	@Override
	protected ResourceGeneratorResolver createResolver(String generatorPrefix) {
		return new WebJarsLocatorPathResolver(generatorPrefix, true, false);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.classpath.ClasspathJSGenerator#getResourceNames(java.lang.String)
	 */
	@Override
	public Set<String> getResourceNames(String path) {
		return ((WebJarsLocatorPathResolver)resolver).getResourceNames(path);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.classpath.ClasspathJSGenerator#isDirectory(java.lang.String)
	 */
	@Override
	public boolean isDirectory(String path) {
		return path.endsWith(JawrConstant.URL_SEPARATOR);
	}

}
