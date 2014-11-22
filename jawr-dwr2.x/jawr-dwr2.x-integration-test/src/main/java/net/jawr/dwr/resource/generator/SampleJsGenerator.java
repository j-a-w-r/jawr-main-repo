/**
 * 
 */
package net.jawr.dwr.resource.generator;

import java.io.Reader;
import java.io.StringReader;

import net.jawr.web.resource.bundle.generator.AbstractJavascriptGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.resolver.PrefixedPathResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;

/**
 * A sample generator
 * @author ibrahim Chaehoi
 *
 */
public class SampleJsGenerator extends AbstractJavascriptGenerator {

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.ResourceGenerator#createResource(net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	public Reader createResource(GeneratorContext context) {
		
		String result = ";function foo(){};";
		return new StringReader(result);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.BaseResourceGenerator#getResolver()
	 */
	public ResourceGeneratorResolver getResolver() {
		return new PrefixedPathResolver("testJs");
	}

}
