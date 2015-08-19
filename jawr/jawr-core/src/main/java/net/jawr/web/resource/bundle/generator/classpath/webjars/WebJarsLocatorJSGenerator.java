package net.jawr.web.resource.bundle.generator.classpath.webjars;

import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.WebJarsLocatorPathResolver;

/**
 * This class defines the generator for webjars JS resources which use the
 * WebJarsLocator. So to reference a ressource you only need the short path
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

}
