package net.jawr.web.resource.bundle.generator.resolver;

import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import org.webjars.WebJarAssetLocator;

public class WebJarsLocatorPathResolver extends PrefixedPathResolver {

    private final WebJarAssetLocator locator;

    /**
     * Constructor
     *
     * @param prefix  the path prefix
     */
    public WebJarsLocatorPathResolver(String prefix) {
        super(prefix);
        this.locator = new WebJarAssetLocator();
    }

    /* (non-Javadoc)
     * @see net.jawr.web.resource.bundle.generator.resolver.PrefixedPathResolver#getResourcePath(java.lang.String)
     */
    @Override
    public String getResourcePath(String requestedPath) {
        String resourcePath = super.getResourcePath(requestedPath);
        resourcePath = locator.getFullPath(resourcePath);
        return resourcePath.substring(GeneratorRegistry.WEBJARS_GENERATOR_HELPER_PREFIX.length() - 2);
    }

}
