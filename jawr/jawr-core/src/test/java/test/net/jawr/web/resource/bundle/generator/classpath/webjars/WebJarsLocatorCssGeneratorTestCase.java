package test.net.jawr.web.resource.bundle.generator.classpath.webjars;

import net.jawr.web.resource.bundle.generator.classpath.webjars.WebJarsCssGenerator;
import net.jawr.web.resource.bundle.generator.classpath.webjars.WebJarsLocatorCssGenerator;

public class WebJarsLocatorCssGeneratorTestCase extends WebJarsCssGeneratorTestCase {

    @Override
    protected String getResourceName() {
        return "webjars:bootstrap.css";
    }

    @Override
    protected WebJarsCssGenerator createGenerator() {
        return new WebJarsLocatorCssGenerator();
    }

}
