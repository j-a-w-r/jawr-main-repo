/**
 * Copyright 2008-2010 Jordi Hernández Sellés, Ibrahim Chaehoi 
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
package net.jawr.web.resource.bundle.locale.message;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import net.jawr.web.JawrConstant;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.generator.GeneratorContext;

import org.apache.log4j.Logger;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.context.support.ServletContextResourceLoader;

/**
 * This is a specialized subclass of MessageBundleScriptCreator that takes in account the 
 * special scheme used by grails to manage message bundles. Said speciality comes from the 
 * fact that the applications get deployed differently in development mode and production 
 * mode (run-app vs run-war), and message properties go to different places each time. 
 * Besides, a regular ResourceBundle cannot be used since the encoding is 'fixed' by grails 
 * to avoid users the pain of encoding special characters in the properties files. Therefore
 * Spring's MessageSource implementations are used to actually access the messages.  
 * 
 * @author Jordi Hernández Sellés
 * @author ibrahim Chaehoi
 */
public class GrailsMessageBundleScriptCreator extends MessageBundleScriptCreator{
	
	private static final Logger LOGGER = Logger.getLogger(GrailsMessageBundleScriptCreator.class);
	private static final String PROPERTIES_DIR = "/WEB-INF/grails-app/i18n/";
	private static final String PROPERTIES_EXT =".properties";
	private static final String CHARSET_ISO_8859_1 = "ISO-8859-1";
	
	public GrailsMessageBundleScriptCreator(GeneratorContext context) {
		super(context);
		if(LOGGER.isDebugEnabled())
			LOGGER.debug("Using Grails i18n messages generator.");
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.locale.message.MessageBundleScriptCreator#createScript(java.nio.charset.Charset)
	 */
	public Reader createScript(Charset charset) {
		
		// Determine wether this is run-app or run-war style of runtime. 
		boolean warDeployed = ((Boolean)this.servletContext.getAttribute(JawrConstant.GRAILS_WAR_DEPLOYED)).booleanValue();
		
		// Spring message bundle object, the same used by grails. 
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setFallbackToSystemLocale(false);
		Set<String> allPropertyNames = null;
		
		// Read the properties files to find out the available message keys. It is done differently 
		// for run-app or run-war style of runtimes. 
		if(warDeployed){
			allPropertyNames = getPropertyNamesFromWar();
			if(LOGGER.isDebugEnabled())
				LOGGER.debug("found a total of " + allPropertyNames.size() + " distinct message keys.");
			messageSource.setResourceLoader(new ServletContextResourceLoader(this.servletContext));
			messageSource.setBasename(PROPERTIES_DIR + configParam.substring(configParam.lastIndexOf('.')+1));
		}
		else
		{
			ResourceBundle bundle;
			if(null != locale)
				bundle = ResourceBundle.getBundle(configParam,locale);
			else bundle = ResourceBundle.getBundle(configParam);
			allPropertyNames = new HashSet<String>();
			Enumeration<String> keys = bundle.getKeys();
			while(keys.hasMoreElements())
				allPropertyNames.add(keys.nextElement());
			String basename = "file:./" + configParam.replaceAll("\\.", "/");
			messageSource.setBasename(basename);
		}		
		
		// Pass all messages to a properties file. 
		Properties props = new Properties();
		for(Iterator<String> it = allPropertyNames.iterator();it.hasNext();) {
			String key = it.next();
			if(matchesFilter(key)){
				try {
					// Use the property encoding of the file
					String msg = new String(messageSource.getMessage(key, new Object[0], locale).getBytes(
							CHARSET_ISO_8859_1), charset.displayName());
					props.put(key, msg);
				} catch (NoSuchMessageException e) {
					// This is expected, so it's OK to have an empty catch block. 
					if(LOGGER.isDebugEnabled())
						LOGGER.debug("Message key [" + key + "] not found.");
				} catch (UnsupportedEncodingException e) {
					LOGGER.warn("Unable to convert value of message bundle associated to key '"
							+ key + "' because the charset is unknown");
				}
			}
		}
		
		return doCreateScript(props);
	}

	/**
	 * Reads the property names of the resourcebundle for the current locale from the war file. 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Set<String> getPropertyNamesFromWar() {
		Set<String> filenames = this.servletContext.getResourcePaths(PROPERTIES_DIR);
		Set<String> allPropertyNames = new HashSet<String>();

		for(Iterator<String> it = filenames.iterator();it.hasNext();){
			String name = it.next();
			if(matchesConfigParam(name)) {
				try {
					Properties props = new Properties(); 
					props.load(servletContext.getResourceAsStream(name));
					if(LOGGER.isDebugEnabled()){
						LOGGER.debug("Found " + props.keySet().size() + " message keys at " + name + ".");
					}
					for (Object key : props.keySet()) {
						allPropertyNames.add((String) key);
					}
					
				} catch (IOException e) {
					throw new BundlingProcessException("Unexpected error retrieving i18n grails properties file", e);
				}
			}			
		}
		return allPropertyNames;
	}

	/**
	 * Determines if a file found at the locale directory matches the locale for the bundle. 
	 * 
	 * @param fileName
	 * @return
	 */
	private boolean matchesConfigParam(String fileName){
		String configValue = configParam.substring(configParam.lastIndexOf('.')+1);
		String fileNameWOPath = fileName.substring(fileName.lastIndexOf('/') + 1 );
		
		// List all the names of files which might have values used by the current locale
		List<String> allowedNames = new ArrayList<String>(4);
		allowedNames.add(configValue + PROPERTIES_EXT);
		if(null != locale) {
			allowedNames.add(configValue + "_" + locale.getLanguage() + PROPERTIES_EXT);
			allowedNames.add(configValue + "_" + locale.getLanguage()+ "_" + locale.getCountry() + PROPERTIES_EXT);
			allowedNames.add(configValue + "_" + locale.getLanguage()+ "_" + locale.getCountry()+ "_" + locale.getVariant() + PROPERTIES_EXT);
		}
		return allowedNames.contains(fileNameWOPath);
	}
	
	
}
