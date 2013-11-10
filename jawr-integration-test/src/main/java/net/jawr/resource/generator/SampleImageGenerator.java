package net.jawr.resource.generator;

import java.io.InputStream;

import javax.servlet.ServletContext;

import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.StreamResourceGenerator;
import net.jawr.web.resource.bundle.generator.resolver.PrefixedPathResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;

public class SampleImageGenerator implements StreamResourceGenerator {

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.StreamResourceGenerator#createResourceAsStream(net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	public InputStream createResourceAsStream(GeneratorContext context) {
		
		ServletContext servletContext = context.getConfig().getContext();
		return servletContext.getResourceAsStream("/img/"+context.getPath());
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.BaseResourceGenerator#getDebugModeRequestPath()
	 */
	public String getDebugModeRequestPath() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.BaseResourceGenerator#getResolver()
	 */
	public ResourceGeneratorResolver getResolver() {
		return new PrefixedPathResolver("img");
	}

}
