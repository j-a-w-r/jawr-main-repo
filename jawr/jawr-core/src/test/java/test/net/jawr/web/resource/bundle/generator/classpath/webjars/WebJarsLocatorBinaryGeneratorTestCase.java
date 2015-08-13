package test.net.jawr.web.resource.bundle.generator.classpath.webjars;

import net.jawr.web.resource.bundle.generator.classpath.webjars.WebJarsBinaryResourceGenerator;
import net.jawr.web.resource.bundle.generator.classpath.webjars.WebJarsLocatorBinaryResourceGenerator;

public class WebJarsLocatorBinaryGeneratorTestCase extends WebJarsBinaryGeneratorTestCase {

    @Override
    protected String getResourceName() {
        return "webjars:glyphicons-halflings-regular.eot";
    }

    @Override
    protected WebJarsBinaryResourceGenerator createGenerator() {
        return new WebJarsLocatorBinaryResourceGenerator();
    }

}
