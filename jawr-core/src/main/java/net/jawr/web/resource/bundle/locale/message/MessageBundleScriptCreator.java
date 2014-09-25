/**
 * Copyright 2007-2014 Jordi Hernández Sellés, Ibrahim Chaehoi
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
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;

import net.jawr.web.JawrConstant;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.factory.util.PropertiesConfigHelper;
import net.jawr.web.resource.bundle.factory.util.RegexUtil;
import net.jawr.web.resource.bundle.generator.GeneratorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

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
	private static final Logger LOGGER = LoggerFactory
			.getLogger(MessageBundleScriptCreator.class.getName());

	public static final String DEFAULT_NAMESPACE = "messages";

	private static final String SCRIPT_TEMPLATE = "/net/jawr/web/resource/bundle/message/messages.js";

	private static final String DEFAULT_RESOURCE_BUNDLE_CHARSET = "ISO-8859-1";

	protected static StringBuffer template;
	protected String configParam;
	protected String namespace;
	private String filter;
	protected Locale locale;
	protected List<String> filterList;
	protected ServletContext servletContext;
	protected boolean fallbackToSystemLocale = true;
	protected boolean addQuoteToMessageKey = false;
	protected Charset resourceBundleCharset;

	public MessageBundleScriptCreator(GeneratorContext context) {
		super();
		this.servletContext = context.getServletContext();
		if (null == template)
			template = loadScriptTemplate();

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
		this.fallbackToSystemLocale = PropertiesConfigHelper.getBooleanValue(
				configProperties,
				JawrConstant.JAWR_LOCALE_GENERATOR_FALLBACK_TO_SYSTEM_LOCALE,
				true);

		this.addQuoteToMessageKey = PropertiesConfigHelper.getBooleanValue(
				configProperties,
				JawrConstant.JAWR_LOCALE_GENERATOR_ADD_QUOTE_TO_MSG_KEY,
				false);

		String charsetName = context.getConfig().getProperty(
				JawrConstant.JAWR_LOCALE_GENERATOR_RESOURCE_BUNDLE_CHARSET,
				DEFAULT_RESOURCE_BUNDLE_CHARSET);

		resourceBundleCharset = Charset.forName(charsetName);
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
			is = ClassLoaderResourceUtils.getResourceAsStream(SCRIPT_TEMPLATE,
					this);
			IOUtils.copy(is, sw);
		} catch (IOException e) {
			Marker fatal = MarkerFactory.getMarker("FATAL");
			LOGGER.error(fatal,
					"a serious error occurred when initializing MessageBundleScriptCreator");
			throw new BundlingProcessException(
					"Classloading issues prevent loading the message template to be loaded. ",
					e);
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

		MessageBundleControl control = new MessageBundleControl(
				fallbackToSystemLocale, resourceBundleCharset);
		for (int x = 0; x < names.length; x++) {

			ResourceBundle bundle;

			try {
				bundle = ResourceBundle.getBundle(names[x], currentLocale,
						control);
			} catch (MissingResourceException ex) {
				// Fixes problems with some servers, e.g. WLS 10
				try {
					bundle = ResourceBundle.getBundle(names[x], currentLocale,
							getClass().getClassLoader(), control);
				} catch (Exception e) {
					bundle = ResourceBundle.getBundle(names[x], currentLocale,
							Thread.currentThread().getContextClassLoader(),
							control);
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
			if (fallbackToSystemLocale) {
				currentLocale = Locale.getDefault();
			} else {
				currentLocale = new Locale("", "");
			}
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
	public void updateProperties(ResourceBundle bundle, Properties props,
			Charset charset) {

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
		script = script.replaceFirst("@namespace",
				RegexUtil.adaptReplacementToMatcher(namespace));
		script = script.replaceFirst("@messages",
				RegexUtil.adaptReplacementToMatcher(messages));

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
			for (Iterator<String> it = filterList.iterator(); it.hasNext()
					&& !rets;)
				rets = key.startsWith(it.next());
		}
		return rets;

	}

	public class MessageBundleControl extends ResourceBundle.Control {

		private boolean fallbackToSystemLocale = true;
		private Charset charset;

		/**
		 * Constructor
		 * 
		 * @param fallbackToSystemLocale
		 *            the flag indicating if the fallback local is the System
		 *            locale or not
		 * @param charset
		 *            the ResourceBundle charset
		 */
		public MessageBundleControl(boolean fallbackToSystemLocale,
				Charset charset) {

			this.fallbackToSystemLocale = fallbackToSystemLocale;
			this.charset = charset;
		}

		@Override
		public Locale getFallbackLocale(String baseName, Locale locale) {
			if (baseName == null) {
				throw new NullPointerException();
			}
			Locale defaultLocale = Locale.getDefault();

			if (fallbackToSystemLocale) {
				defaultLocale = Locale.getDefault();
			} else {
				defaultLocale = new Locale("", "");
			}

			return locale.equals(defaultLocale) ? null : defaultLocale;
		}

		@Override
		public ResourceBundle newBundle(String baseName, Locale locale,
				String format, ClassLoader loader, boolean reload)
				throws IllegalAccessException, InstantiationException,
				IOException {

			String bundleName = toBundleName(baseName, locale);
			ResourceBundle bundle = null;
			if (format.equals("java.class")) {
				try {
					@SuppressWarnings("unchecked")
					Class<? extends ResourceBundle> bundleClass = (Class<? extends ResourceBundle>) loader
							.loadClass(bundleName);

					// If the class isn't a ResourceBundle subclass, throw a
					// ClassCastException.
					if (ResourceBundle.class.isAssignableFrom(bundleClass)) {
						bundle = bundleClass.newInstance();
					} else {
						throw new ClassCastException(bundleClass.getName()
								+ " cannot be cast to ResourceBundle");
					}
				} catch (ClassNotFoundException e) {
				}
			} else if (format.equals("java.properties")) {
				final String resourceName = toResourceName(bundleName,
						"properties");
				final ClassLoader classLoader = loader;
				final boolean reloadFlag = reload;
				InputStream stream = null;
				Reader rd = null;
				try {
					stream = AccessController
							.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
								public InputStream run() throws IOException {
									InputStream is = null;
									if (reloadFlag) {
										URL url = classLoader
												.getResource(resourceName);
										if (url != null) {
											URLConnection connection = url
													.openConnection();
											if (connection != null) {
												// Disable caches to get fresh
												// data for
												// reloading.
												connection.setUseCaches(false);
												is = connection
														.getInputStream();
											}
										}
									} else {
										is = classLoader
												.getResourceAsStream(resourceName);
									}
									return is;
								}
							});
				} catch (PrivilegedActionException e) {
					throw (IOException) e.getException();
				}
				if (stream != null) {
					try {
						rd = new InputStreamReader(stream, charset);
						bundle = new PropertyResourceBundle(rd);
					} finally {
						IOUtils.close(rd);
						IOUtils.close(stream);
					}
				}
			} else {
				throw new IllegalArgumentException("unknown format: " + format);
			}
			return bundle;
		}

	}
}
