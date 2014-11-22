/**
 * 
 */
package net.jawr.dwr.resource.generator;

import java.io.Reader;
import java.util.List;
import java.util.ResourceBundle;

import net.jawr.web.resource.bundle.generator.AbstractJavascriptGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.LocaleAwareResourceGenerator;
import net.jawr.web.resource.bundle.generator.resolver.PrefixedPathResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.locale.LocaleUtils;
import net.jawr.web.resource.bundle.locale.message.MessageBundleScriptCreator;

/**
 * @author ibrahim
 *
 */
public class SampleLocaleAwareResourceGenerator extends AbstractJavascriptGenerator 
	implements LocaleAwareResourceGenerator {

	private static final String XML_MESSAGES_GENERATOR_PREFIX = "xmlMessages";
	private static final String XML_FILE_SUFFIX = ".xml";

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.LocaleAwareResourceReader#getAvailableLocales(java.lang.String)
	 */
	public List<String> getAvailableLocales(String mapping) {
	
		return LocaleUtils.getAvailableLocaleSuffixesForBundle(mapping, XML_FILE_SUFFIX);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.ResourceGenerator#createResource(net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	public Reader createResource(GeneratorContext context) {
		
		MessageBundleScriptCreator creator = new MessageBundleScriptCreator(context);
		ResourceBundle bundle = null;
		if(context.getLocale() == null){
			bundle = ResourceBundle.getBundle(context.getPath(), new XmlResourceBundleControl());
		}else{
			bundle = ResourceBundle.getBundle(context.getPath(), context.getLocale(), new XmlResourceBundleControl());
		}
		return creator.createScript(context.getCharset(), bundle);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.BaseResourceGenerator#getResolver()
	 */
	public ResourceGeneratorResolver getResolver() {
		return new PrefixedPathResolver(XML_MESSAGES_GENERATOR_PREFIX);
	}

}
