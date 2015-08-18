package net.jawr.web.resource.bundle.generator.locator;

import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import org.webjars.WebJarAssetLocator;

public class WebJarsResourceLocator implements ResourceLocator {

    private final static String prefix = GeneratorRegistry.WEBJARS_GENERATOR_PREFIX
            + GeneratorRegistry.PREFIX_SEPARATOR;

    private final WebJarAssetLocator locator;

    /**
     * Constructor
     */
    public WebJarsResourceLocator() {
        this.locator = new WebJarAssetLocator();
    }

    public boolean support(String mapping) {
        return mapping.startsWith(prefix);
    }

    @Override
    public String getFullMapping(String mapping) {
        String path = mapping.substring(prefix.length());
        path = locator.getFullPath(path);
        return prefix + path.substring(GeneratorRegistry.WEBJARS_GENERATOR_HELPER_PREFIX.length() - 2);
    }

}
