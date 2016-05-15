/**
 * Copyright 2007-2016 Jordi Hernández Sellés, Ibrahim Chaehoi
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
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import net.jawr.web.JawrConstant;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.factory.util.PropertiesConfigHelper;
import net.jawr.web.resource.bundle.factory.util.RegexUtil;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.locale.MessageBundleControl;

/**
 * Creates a script which holds the data from a message bundle(s). The script is
 * such that properties can be accessed as functions (i.e.:
 * alert(com.mycompany.mymessage()); ).
 * 
 * @author Jordi Hernández Sellés
 * @author ibrahim Chaehoi
 */
public class MessageBundleScriptCreator {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(MessageBundleScriptCreator.class.getName());

	/** The default namespace for messages */
	public static final String DEFAULT_NAMESPACE = "messages";

	/** The script template */
	private static final String SCRIPT_TEMPLATE = "/net/jawr/web/resource/bundle/message/messages.js";

	/** The template */
	protected static StringBuffer template;

	/** The configuration parameter */
	protected String configParam;

	/** The namespace */
	protected String namespace;

	/** The filter */
	private String filter;

	/** The locale */
	protected Locale locale;

	/** The list of filters */
	protected List<String> filterList;

	/** The servlet context */
	protected ServletContext servletContext;

	/**
	 * The flag indicating if a quote character should be added to the message
	 * key
	 */
	private boolean addQuoteToMessageKey = false;

	/** The message resource bundle control */
	protected MessageBundleControl control;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            the generator context
	 * @param control
	 *            the message resource bundle control
	 */
	public MessageBundleScriptCreator(GeneratorContext context, MessageBundleControl control) {
		super();
		this.servletContext = context.getServletContext();
		if (null == template)
			template = loadScriptTemplate();

		this.control = control;
		this.locale = context.getLocale();

		// Set the namespace
		namespace = context.getParenthesesParam();
		namespace = null == namespace ? DEFAULT_NAMESPACE : namespace;

		// Set the filter
		filter = context.getBracketsParam();
		if (null != filter) {
			StringTokenizer tk = new StringTokenizer(filter, "\\|");
			filterList = new ArrayList<String>();
			while (tk.hasMoreTokens())
				filterList.add(tk.nextToken());
		}

		this.configParam = context.getPath();

		Properties configProperties = context.getConfig().getConfigProperties();

		this.addQuoteToMessageKey = PropertiesConfigHelper.getBooleanValue(configProperties,
				JawrConstant.JAWR_LOCALE_GENERATOR_ADD_QUOTE_TO_MSG_KEY, false);
	}

	/**
	 * Loads a template containing the functions which convert properties into
	 * methods.
	 * 
	 * @return the template containing the functions which convert properties
	 *         into methods.
	 */
	private StringBuffer loadScriptTemplate() {
		StringWriter sw = new StringWriter();
		InputStream is = null;
		try {
			is = ClassLoaderResourceUtils.getResourceAsStream(SCRIPT_TEMPLATE, this);
			IOUtils.copy(is, sw);
		} catch (IOException e) {
			Marker fatal = MarkerFactory.getMarker("FATAL");
			LOGGER.error(fatal, "a serious error occurred when initializing MessageBundleScriptCreator");
			throw new BundlingProcessException(
					"Classloading issues prevent loading the message template to be loaded. ", e);
		} finally {
			IOUtils.close(is);
		}

		return sw.getBuffer();
	}

	/**
	 * Create the message resource bundles specified and uses a
	 * BundleStringJsonifier to generate the properties.
	 * 
	 * @return the script
	 */
	public Reader createScript(Charset charset) {

		String[] names = configParam.split("\\|");
		Properties props = new Properties();

		Locale currentLocale = getLocaleToApply();

		for (int x = 0; x < names.length; x++) {

			ResourceBundle bundle;

			try {
				bundle = ResourceBundle.getBundle(names[x], currentLocale, control);
			} catch (MissingResourceException ex) {
				// Fixes problems with some servers, e.g. WLS 10
				try {
					bundle = ResourceBundle.getBundle(names[x], currentLocale, getClass().getClassLoader(), control);
				} catch (Exception e) {
					bundle = ResourceBundle.getBundle(names[x], currentLocale,
							Thread.currentThread().getContextClassLoader(), control);
				}
			}

			updateProperties(bundle, props, charset);
		}
		return doCreateScript(props);
	}

	/**
	 * Returns the locale to use to retrieve the ResourceBundle
	 * 
	 * @return the locale to use to retrieve the ResourceBundle
	 */
	protected Locale getLocaleToApply() {
		Locale currentLocale = locale;

		if (currentLocale == null) {
			// TODO check if this is still relevant (See
			// JawrMessageBundleControl)
			currentLocale = control.getFallbackLocale();
		}
		return currentLocale;
	}

	/**
	 * Loads the message resource bundles specified and uses a
	 * BundleStringJasonifier to generate the properties.
	 * 
	 * @return
	 */
	public Reader createScript(Charset charset, ResourceBundle bundle) {

		Properties props = new Properties();
		updateProperties(bundle, props, charset);
		return doCreateScript(props);
	}

	/**
	 * Loads the message resource bundles specified and uses a
	 * BundleStringJasonifier to generate the properties.
	 * 
	 * @param charset
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public void updateProperties(ResourceBundle bundle, Properties props, Charset charset) {

		Enumeration<String> keys = bundle.getKeys();

		while (keys.hasMoreElements()) {
			String key = keys.nextElement();

			if (matchesFilter(key)) {
				String value = bundle.getString(key);
				props.put(key, value);
			}
		}
	}

	/**
	 * Returns the JS script from the message properties
	 * 
	 * @param props
	 *            the message properties
	 * @return the JS script from the message properties
	 */
	protected Reader doCreateScript(Properties props) {
		BundleStringJsonifier bsj = new BundleStringJsonifier(props, addQuoteToMessageKey);
		String script = template.toString();
		String messages = bsj.serializeBundles().toString();
		script = script.replaceFirst("@namespace", RegexUtil.adaptReplacementToMatcher(namespace));
		script = script.replaceFirst("@messages", RegexUtil.adaptReplacementToMatcher(messages));

		return new StringReader(script);
	}

	/**
	 * Determines wether a key matches any of the set filters.
	 * 
	 * @param key
	 *            the property key
	 * @return true if the key matches any of the set filters.
	 */
	protected boolean matchesFilter(String key) {
		boolean rets = (null == filterList);
		if (!rets) {
			for (Iterator<String> it = filterList.iterator(); it.hasNext() && !rets;)
				rets = key.startsWith(it.next());
		}
		return rets;

	}

}
