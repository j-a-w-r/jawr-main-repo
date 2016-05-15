/**
 * Copyright 2008-2016 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletContext;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

/**
 * Encapsulates the parameters needed in Generators.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class GeneratorContext {

	/** The resource bundle */
	private JoinableResourceBundle bundle;

	/** The generator mapping helper */
	private GeneratorMappingHelper helper;

	/** The current Jawr config */
	private JawrConfig config;

	/** The variant map */
	private Map<String, String> variantMap;

	/** The variant sets for the current path */
	private Map<String, VariantSet> variantSets;

	/** The locale */
	private Locale locale;

	/** The resource handler */
	private ResourceReaderHandler resourceReaderHandler;

	/**
	 * This flag indicates if we are currently processing the bundle or not.
	 * This flag has been added so the generator have different behavior
	 * depending on this flag. It is used in the ClassPathCSSGenerator to know
	 * where to retrieve the resources.
	 * <ul>
	 * <li>If we are processing the bundle, the resource will be retrieve from
	 * the classpath</li>
	 * <li>If we are not in processing mode, and we are using the CSS image
	 * servlet, in that case the resource will be retrieved if we are in in
	 * debug mode from the temporary folder for the CSS classpath resources.
	 * </li>
	 */
	private boolean processingBundle;

	/**
	 * This flag is updated after the generation process. The value is
	 * <b>true</b> is the generated resource has been retrieved from cache
	 */
	private boolean retrievedFromCache;

	/**
	 * Constructor
	 * 
	 * @param bundle
	 *            the resource bundle
	 * @param config
	 *            the Jawr config
	 * @param path
	 *            the requested path
	 */
	public GeneratorContext(JoinableResourceBundle bundle, JawrConfig config, String requestedPath) {

		this.bundle = bundle;
		this.config = config;
		this.helper = new GeneratorMappingHelper(requestedPath);
	}

	/**
	 * Returns the bundle
	 * 
	 * @return the bundle
	 */
	public JoinableResourceBundle getBundle() {
		return bundle;
	}

	/**
	 * Get the locale.
	 * 
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * Set the locale.
	 * 
	 * @param locale
	 *            the locale to set
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * Returns the variant map
	 * 
	 * @return the variant map
	 */
	public Map<String, String> getVariantMap() {

		return variantMap;
	}

	/**
	 * Returns the variant map
	 * 
	 * @return the variant map
	 */
	public void setVariantMap(Map<String, String> variantMap) {

		this.variantMap = variantMap;
	}

	/**
	 * Returns the variant sets for the current path
	 * 
	 * @return the variantSets
	 */
	public Map<String, VariantSet> getVariantSets() {
		return variantSets;
	}

	/**
	 * Sets the variant sets for the current path
	 * 
	 * @param variantSets
	 *            the variantSets to set
	 */
	public void setVariantSets(Map<String, VariantSet> variantSets) {
		this.variantSets = variantSets;
	}

	/**
	 * Get the path.
	 * 
	 * @return the path
	 */
	public String getPath() {
		return helper.getPath();
	}

	/**
	 * Get the Jawr config.
	 * 
	 * @return the Jawr config
	 */
	public JawrConfig getConfig() {
		return config;
	}

	/**
	 * Get the resource handler
	 * 
	 * @return the resourceHandler
	 */
	public ResourceReaderHandler getResourceReaderHandler() {
		return resourceReaderHandler;
	}

	/**
	 * Sets the resource handler
	 * 
	 * @param resourceHandler
	 *            the resourceHandler to set
	 */
	public void setResourceReaderHandler(ResourceReaderHandler resourceHandler) {
		this.resourceReaderHandler = resourceHandler;
	}

	/**
	 * Returns the processing bundle flag
	 * 
	 * @return the processing bundle flag
	 */
	public boolean isProcessingBundle() {
		return processingBundle;
	}

	/**
	 * Sets the processing bundle flag
	 * 
	 * @param processingBundle
	 *            the flag to set
	 */
	public void setProcessingBundle(boolean processingBundle) {
		this.processingBundle = processingBundle;
	}

	/**
	 * Returns the flag indicating if the generated content has been retrieved from cache
	 * @return true if the generated content has been retrieved from cache
	 */
	public boolean isRetrievedFromCache() {
		return retrievedFromCache;
	}

	/**
	 * Sets the flag indicating if the generated content has been retrieved from cache
	 * @param retrievedFromCache the flag to set
	 */
	public void setRetrievedFromCache(boolean retrievedFromCache) {
		this.retrievedFromCache = retrievedFromCache;
	}

	/**
	 * Get the servlet context.
	 * 
	 * @return the servletcontext
	 */
	public ServletContext getServletContext() {
		return config.getContext();
	}

	/**
	 * Get the charset in which to generate resources.
	 * 
	 * @return The charset in which to generate resources.
	 */
	public Charset getCharset() {
		return config.getResourceCharset();
	}

	/**
	 * Get the values in parentheses.
	 * 
	 * @return the values in parentheses.
	 */
	public String getParenthesesParam() {
		return helper.getParenthesesParam();
	}

	/**
	 * Get the values in brackets.
	 * 
	 * @return the values in brackets.
	 */
	public String getBracketsParam() {
		return helper.getBracketsParam();
	}

}
