package net.jawr.web.resource.bundle.generator.classpath.webjars;

import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.WebJarsLocatorPathResolver;

public class WebJarsLocatorCssGenerator extends WebJarsCssGenerator {

    /* (non-Javadoc)
     * @see net.jawr.web.resource.bundle.generator.classpath.ClassPathCSSGenerator#createResolver(java.lang.String)
     */
    @Override
    protected ResourceGeneratorResolver createResolver(String generatorPrefix) {
        return new WebJarsLocatorPathResolver(generatorPrefix);
    }

}
