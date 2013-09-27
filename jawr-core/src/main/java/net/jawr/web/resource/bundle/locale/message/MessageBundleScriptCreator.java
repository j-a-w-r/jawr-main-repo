/**
 * Copyright 2007-2011 Jordi Hernández Sellés, Ibrahim Chaehoi
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

import net.jawr.web.JawrConstant;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.factory.util.RegexUtil;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.util.StringUtils;

import org.apache.log4j.Logger;

/**
 * Creates a script which holds the data from a message bundle(s). The script is
 * such that properties can be accessed as functions (i.e.:
 * alert(com.mycompany.mymessage()); ).
 * 
 * @author Jordi Hernández Sellés
 * @author ibrahim Chaehoi
 */
public class MessageBundleScriptCreator {

	private static final Logger LOGGER = Logger
			.getLogger(MessageBundleScriptCreator.class.getName());

	public static final String DEFAULT_NAMESPACE = "messages";

	private static final String SCRIPT_TEMPLATE = "/net/jawr/web/resource/bundle/message/messages.js";

	private static final String CHARSET_ISO_8859_1 = "ISO-8859-1";

	protected static StringBuffer template;
	protected String configParam;
	protected String namespace;
	private String filter;
	protected Locale locale;
	private List<String> filterList;
	protected ServletContext servletContext;
	private boolean fallbackToSystemLocale = true;

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

		String fallbackToSystemLocaleProperty = context.getConfig()
				.getProperty(JawrConstant.JAWR_LOCALE_GENERATOR_FALLBACK_TO_SYSTEM_LOCALE);
		if (StringUtils.isNotEmpty(fallbackToSystemLocaleProperty)) {
			this.fallbackToSystemLocale = Boolean
					.valueOf(fallbackToSystemLocaleProperty);
		}
	}

	/**
	 * Loads a template containing the functions which convert properties into
	 * methods.
	 * 
	 * @return
	 */
	private StringBuffer loadScriptTemplate() {
		StringWriter sw = new StringWriter();
		InputStream is = null;
		try {
			is = ClassLoaderResourceUtils.getResourceAsStream(SCRIPT_TEMPLATE,
					this);
			IOUtils.copy(is, sw);
		} catch (IOException e) {
			LOGGER.fatal("a serious error occurred when initializing MessageBundleScriptCreator");
			throw new BundlingProcessException(
					"Classloading issues prevent loading the message template to be loaded. ",
					e);
		} finally {
			IOUtils.close(is);
		}

		return sw.getBuffer();
	}

	/**
	 * Loads the message resource bundles specified and uses a
	 * BundleStringJasonifier to generate the properties.
	 * 
	 * @return
	 */
	public Reader createScript(Charset charset) {

		String[] names = configParam.split("\\|");
		Properties props = new Properties();

		Locale currentLocale = locale;

		// TODO Use ResourceBundle.Control to handle fallbackToSystemLocale when
		// upgrading to Java 6
		if (currentLocale == null) {
			if (fallbackToSystemLocale) {
				currentLocale = Locale.getDefault();
			} else {
				currentLocale = new Locale("", "");
			}
		}

		for (int x = 0; x < names.length; x++) {

			ResourceBundle bundle;

			try {
				bundle = ResourceBundle.getBundle(names[x], currentLocale);
			} catch (MissingResourceException ex) {
				// Fixes problems with some servers, e.g. WLS 10
				try {
					bundle = ResourceBundle.getBundle(names[x], currentLocale,
							getClass().getClassLoader());
				} catch (Exception e) {
					bundle = ResourceBundle.getBundle(names[x], currentLocale,
							Thread.currentThread().getContextClassLoader());
				}
			}

			updateProperties(bundle, props, charset);
		}
		return doCreateScript(props);
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
				String value;
				try {
					// Use the property encoding of the file
					value = new String(bundle.getString(key).getBytes(
							CHARSET_ISO_8859_1), charset.displayName());
					props.put(key, value);
				} catch (UnsupportedEncodingException e) {
					LOGGER.warn("Unable to convert value of message bundle associated to key '"
							+ key + "' because the charset is unknown");
				}
			}
		}
	}

	/**
	 * @return
	 */
	protected Reader doCreateScript(Properties props) {
		BundleStringJsonifier bsj = new BundleStringJsonifier(props);
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
	 * @return
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

}
