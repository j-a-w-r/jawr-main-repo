package net.jawr.web.resource.bundle.generator.locator;

/**
 * This interface defines the resource locator which will be used to determine
 * a fully qualified mapping to the resource
 *
 * @author ted liang
 *
 */
public interface ResourceLocator {

    /**
     * Checks if the mapping is supported
     *
     * @param mapping the resource mapping
     *
     * @return true if the path matches
     */
    boolean support(String mapping);

    /**
     * Returns the fully qualified resource mapping
     *
     * @param mapping the resource mapping
     *
     * @return the fully qualified resource mapping
     */
    String getFullMapping(String mapping);
}
