package test.net.jawr.web.resource.bundle.generator.classpath.webjars;

import net.jawr.web.resource.bundle.generator.classpath.webjars.WebJarsJSGenerator;
import net.jawr.web.resource.bundle.generator.classpath.webjars.WebJarsLocatorJSGenerator;

public class WebJarsLocatorJsGeneratorTestCase extends WebJarsJsGeneratorTestCase {

    @Override
    protected String getResourceName() {
        return "webjars:bootstrap.js";
    }

    @Override
    protected WebJarsJSGenerator createGenerator() {
        return new WebJarsLocatorJSGenerator();
    }

}
