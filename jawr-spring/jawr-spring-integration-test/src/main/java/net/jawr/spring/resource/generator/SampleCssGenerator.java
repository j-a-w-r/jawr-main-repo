/**
 * 
 */
package net.jawr.spring.resource.generator;

import java.io.Reader;
import java.io.StringReader;

import net.jawr.web.resource.bundle.generator.AbstractCSSGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.resolver.PrefixedPathResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;

/**
 * A sample generator
 * @author ibrahim Chaehoi
 *
 */
public class SampleCssGenerator extends AbstractCSSGenerator {

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.ResourceGenerator#createResource(net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	public Reader createResource(GeneratorContext context) {
		
		String result = ".generatedContent { color : black; }";
		return new StringReader(result);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.BaseResourceGenerator#getResolver()
	 */
	public ResourceGeneratorResolver getResolver() {
		return new PrefixedPathResolver("testCss");
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.AbstractCSSGenerator#generateResourceForBundle(net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	@Override
	protected Reader generateResourceForBundle(GeneratorContext ctx) {
		
		return createResource(ctx);
	}

}
