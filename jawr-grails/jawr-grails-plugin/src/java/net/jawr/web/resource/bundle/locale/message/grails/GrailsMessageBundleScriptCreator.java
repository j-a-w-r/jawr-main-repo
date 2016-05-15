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
package net.jawr.web.resource.bundle.locale.message.grails;

import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.jawr.web.JawrConstant;
import net.jawr.web.JawrGrailsConstant;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.locale.GrailsLocaleUtils;
import net.jawr.web.resource.bundle.locale.MessageBundleControl;
import net.jawr.web.resource.bundle.locale.message.MessageBundleScriptCreator;
import net.jawr.web.resource.handler.reader.grails.GrailsServletContextResourceReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.ServletContextResourceLoader;

/**
 * This is a specialized subclass of MessageBundleScriptCreator that takes in
 * account the special scheme used by grails to manage message bundles. Said
 * speciality comes from the fact that the applications get deployed differently
 * in development mode and production mode (run-app vs run-war), and message
 * properties go to different places each time. Besides, a regular
 * ResourceBundle cannot be used since the encoding is 'fixed' by grails to
 * avoid users the pain of encoding special characters in the properties files.
 * Therefore Spring's MessageSource implementations are used to actually access
 * the messages.
 * 
 * @author Jordi Hern�ndez Sell�s
 * @author ibrahim Chaehoi
 */
public class GrailsMessageBundleScriptCreator extends
		MessageBundleScriptCreator {

	/** The Logger */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(GrailsMessageBundleScriptCreator.class);

	/** The prefix for URI absolute file */
	private static final String URI_ABSOLUTE_FILE_PREFIX = "file:///";
	
	/** The prefix for URI relative file */
	private static final String URI_RELATIVE_FILE_PREFIX = "file:./";
	
	/** The WEB-INF directory path */
	private static final String WEB_INF_DIR = "/WEB-INF";

	/** The regular expression for dot character */
	private String REGEX_DOT_CHARACTER = "\\.";
	
	/** The i18n standard directory path */
	private static final String PROPERTIES_DIR = WEB_INF_DIR+"/grails-app/i18n/";
	
	/** The plugin path map */
	private Map<String, String> pluginMsgPathMap;

	/** The Grails resource reader */
	private GrailsServletContextResourceReader rsReader;
	
	/**
	 * Constructor
	 * 
	 * @param context
	 *            the generator context
	 */
	@SuppressWarnings("unchecked")
	public GrailsMessageBundleScriptCreator(GeneratorContext context, MessageBundleControl control) {
		super(context, control);
		pluginMsgPathMap = (Map<String, String>) servletContext
				.getAttribute(JawrGrailsConstant.JAWR_GRAILS_PLUGIN_MSG_PATHS);
		if (pluginMsgPathMap == null) {
			throw new BundlingProcessException(
					"No grails plugin message paths map defined in the servlet context");
		}
		rsReader = new GrailsServletContextResourceReader(JawrGrailsConstant.JAWR_GRAILS_PLUGIN_MSG_PATHS);
		rsReader.init(context.getServletContext(), context.getConfig());
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Using Grails i18n messages generator.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.locale.message.MessageBundleScriptCreator
	 * #createScript(java.nio.charset.Charset)
	 */
	public Reader createScript(Charset charset) {

		// Determine wether this is run-app or run-war style of runtime.
		boolean warDeployed = ((Boolean) this.servletContext
				.getAttribute(JawrGrailsConstant.GRAILS_WAR_DEPLOYED))
				.booleanValue();

		// Spring message bundle object, the same used by grails.
		GrailsBundleMessageSource messageSource = new GrailsBundleMessageSource(
				warDeployed);
		
		messageSource.setFallbackToSystemLocale(control.isFallbackToSystemLocale());
		messageSource.setFilters(filterList);

		if (warDeployed) {
			messageSource.setResourceLoader(new ServletContextResourceLoader(
					this.servletContext));

		}
		messageSource.setBasenames(getBaseNames(warDeployed));

		Properties props = messageSource.getAllMessages(locale);
		return doCreateScript(props);
	}

	/**
	 * Returns the basenames
	 * 
	 * @return the base names for the bundle resources
	 */
	public String[] getBaseNames(boolean warDeployed) {

		String[] names = configParam.split(GrailsLocaleUtils.RESOURCE_BUNDLE_SEPARATOR);
		List<String> baseNames = new ArrayList<String>();

		for (String baseName : names) {
			
			// Read the properties files to find out the available message keys. It
			// is done differently
			// for run-app or run-war style of runtimes.
			boolean isPluginResoucePath = GrailsLocaleUtils
					.isPluginResoucePath(baseName);

			if (warDeployed) {

				if (isPluginResoucePath) {
					baseName = WEB_INF_DIR+rsReader.getRealResourcePath(baseName);
					
				} else {
					baseName = PROPERTIES_DIR
							+ baseName.substring(baseName.lastIndexOf('.') + 1);
				}

			} else {

				if (isPluginResoucePath) {
					baseName = URI_ABSOLUTE_FILE_PREFIX
							+ rsReader.getRealResourcePath(baseName);
				} else {
					baseName = URI_RELATIVE_FILE_PREFIX + baseName.replaceAll(REGEX_DOT_CHARACTER, JawrConstant.URL_SEPARATOR);
				}
			}
			baseNames.add(baseName);
		}

		return baseNames.toArray(new String[] {});
	}
}
