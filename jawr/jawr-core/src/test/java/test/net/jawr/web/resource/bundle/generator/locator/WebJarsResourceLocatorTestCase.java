package test.net.jawr.web.resource.bundle.generator.locator;

import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import org.junit.Assert;
import org.junit.Test;

public class WebJarsResourceLocatorTestCase {

    private GeneratorRegistry generatorRegistry = new GeneratorRegistry();

    private final static String JS_MAPPING = "webjars:/bootstrap/3.2.0/js/bootstrap.js";

    private final static String CSS_MAPPING = "webjars:/bootstrap/3.2.0/css/bootstrap.css";

    private final static String BINARY_MAPPING = "webjars:/bootstrap/3.2.0/fonts/glyphicons-halflings-regular.eot";

    @Test
    public void testJsMappingWithFullPath() {
        String result = generatorRegistry.locateResource(JS_MAPPING);
        Assert.assertEquals(JS_MAPPING, result);
    }

    @Test
    public void testCssMappingWithFullPath() {
        String result = generatorRegistry.locateResource(CSS_MAPPING);
        Assert.assertEquals(CSS_MAPPING, result);
    }

    @Test
    public void testBinaryMappingWithFullPath() {
        String result = generatorRegistry.locateResource(BINARY_MAPPING);
        Assert.assertEquals(BINARY_MAPPING, result);
    }

    @Test
    public void testJsMappingWithNoPath() {
        String result = generatorRegistry.locateResource("webjars:bootstrap.js");
        Assert.assertEquals(JS_MAPPING, result);
    }

    @Test
    public void testCssMappingWithNoPath() {
        String result = generatorRegistry.locateResource("webjars:bootstrap.css");
        Assert.assertEquals(CSS_MAPPING, result);
    }

    @Test
    public void testBinaryMappingWithNoPath() {
        String result = generatorRegistry.locateResource("webjars:glyphicons-halflings-regular.eot");
        Assert.assertEquals(BINARY_MAPPING, result);
    }

    @Test
    public void testJsMappingWithNoVersion() {
        String result = generatorRegistry.locateResource("webjars:js/bootstrap.js");
        Assert.assertEquals(JS_MAPPING, result);
    }

    @Test
    public void testCssMappingWithNoVersion() {
        String result = generatorRegistry.locateResource("webjars:css/bootstrap.css");
        Assert.assertEquals(CSS_MAPPING, result);
    }

    @Test
    public void testBinaryMappingWithNoVersion() {
        String result = generatorRegistry.locateResource("webjars:fonts/glyphicons-halflings-regular.eot");
        Assert.assertEquals(BINARY_MAPPING, result);
    }

}
