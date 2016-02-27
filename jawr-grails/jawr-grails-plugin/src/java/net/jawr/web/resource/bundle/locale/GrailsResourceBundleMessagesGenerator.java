/**
 * Copyright 2008-2014 Jordi Hern�ndez Sell�s, Ibrahim Chaehoi
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package net.jawr.web.resource.bundle.locale;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

import net.jawr.web.JawrGrailsConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.generator.ConfigurationAwareResourceGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolverFactory;
import net.jawr.web.resource.bundle.locale.message.MessageBundleScriptCreator;
import net.jawr.web.resource.bundle.locale.message.grails.GrailsMessageBundleScriptCreator;
import net.jawr.web.resource.handler.reader.grails.GrailsServletContextResourceReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A generator that creates a script from message bundles. The generated script
 * can be used to reference the message literals easily from javascript.
 * 
 * @author Jordi Hern�ndez Sell�s
 * @author Ibrahim Chaehoi
 * 
 */
public class GrailsResourceBundleMessagesGenerator extends
		ResourceBundleMessagesGenerator implements
		ConfigurationAwareResourceGenerator {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(GrailsResourceBundleMessagesGenerator.class);

	/** The resource bundle separator for the generator message */
	public static final String RESOURCE_BUNDLE_SEPARATOR = "\\|";

	/** The generator prefix */
	private String generatorPrefix = "messages";

	/** The servlet context */
	private ServletContext servletContext;

	/** The flag indicating if we are in a grails context */
	private boolean grailsContext;

	/** The resolver */
	private ResourceGeneratorResolver resolver;

	/** The plugin path map */
	private Map<String, String> pluginMsgPathMap;
	
	/** The grails servlet context resource reader handler */
	private GrailsServletContextResourceReader rsHandler;
	
	/**
	 * Constructor
	 */
	public GrailsResourceBundleMessagesGenerator() {

		resolver = ResourceGeneratorResolverFactory
				.createPrefixResolver(generatorPrefix);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.BaseResourceGenerator#getPathMatcher
	 * ()
	 */
	public ResourceGeneratorResolver getResolver() {

		return resolver;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.ConfigurationAwareResourceGenerator
	 * #setConfig(net.jawr.web.config.JawrConfig)
	 */
	@SuppressWarnings("unchecked")
	public void setConfig(JawrConfig config) {
		
		servletContext = config.getContext();
		grailsContext = servletContext
				.getAttribute(JawrGrailsConstant.GRAILS_WAR_DEPLOYED) != null;
		if (grailsContext) {
			LOGGER.info("Grails war deployed");
		}
		
		pluginMsgPathMap = (Map<String, String>) servletContext.getAttribute(JawrGrailsConstant.JAWR_GRAILS_PLUGIN_MSG_PATHS);
		if(pluginMsgPathMap == null){
			throw new BundlingProcessException("No grails plugin message paths map defined in the servlet context");
		}
		
		rsHandler = new GrailsServletContextResourceReader(JawrGrailsConstant.JAWR_GRAILS_PLUGIN_MSG_PATHS);
		rsHandler.init(servletContext, config);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.locale.ResourceBundleMessagesGenerator#generateResource(net.jawr.web.resource.bundle.generator.GeneratorContext, java.lang.String)
	 */
	public Reader generateResource(String path, GeneratorContext context) {
	
		MessageBundleScriptCreator creator = new GrailsMessageBundleScriptCreator(
				context);
		return creator.createScript(context.getCharset());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.locale.ResourceBundleMessagesGenerator#findAvailableLocales
	 * (java.lang.String)
	 */
	@Override
	protected List<String> findAvailableLocales(String resource) {

		Set<String> result = new HashSet<String>();
		
		String[] msgBundleArray = resource
				.split(RESOURCE_BUNDLE_SEPARATOR);
		for (String msgResource : msgBundleArray) {
			List<String> availableLocales = GrailsLocaleUtils
					.getAvailableLocaleSuffixesForBundle(msgResource,
							rsHandler);
			result.addAll(availableLocales);
		}
		
		LOGGER.debug("Available locales for "+resource+" : "+result);
		return new ArrayList<String>(result);
	}

	
}
