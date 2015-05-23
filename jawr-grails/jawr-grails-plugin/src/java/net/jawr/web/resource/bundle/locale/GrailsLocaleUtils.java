/**
 * Copyright 2007-2015 Jordi Hern�ndez Sell�s, Ibrahim Chaehoi
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.factory.util.RegexUtil;
import net.jawr.web.resource.handler.reader.grails.GrailsServletContextResourceReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for locale.
 * 
 * are allowed within the url.
 * 
 * @author Jordi Hern�ndez Sell�s
 * @author Ibrahim Chaehoi
 */
public final class GrailsLocaleUtils {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(GrailsLocaleUtils.class);

	/** The resource bundle separator for the generator message */
	public static final String RESOURCE_BUNDLE_SEPARATOR = "\\|";

	/** The WEB-INF */
	private static final String WEB_INF_DIR = "/WEB-INF/";

	/** The message resource bundle suffix */
	public static final String MSG_RESOURCE_BUNDLE_SUFFIX = ".properties";

	/** The pattern of the plugin resource path */
	private static Pattern PLUGIN_RESOURCE_PATTERN = Pattern
			.compile("^(/plugins/([a-zA-Z0-9_\\-\\.]*)/grails-app/i18n/)");

	/** The available locale suffixes */
	public static final Set<String> LOCALE_SUFFIXES = GrailsLocaleUtils
			.getAvailableLocaleSuffixes();

	/**
	 * Constructor
	 */
	private GrailsLocaleUtils() {

	}

	/**
	 * Returns the list of available locale suffixes for a message resource
	 * bundle
	 * 
	 * @param messageBundlePath
	 *            the resource bundle path
	 * @return the list of available locale suffixes for a message resource
	 *         bundle
	 */
	public static List<String> getAvailableLocaleSuffixesForBundle(
			String messageBundlePath) {

		return getAvailableLocaleSuffixesForBundle(messageBundlePath,
				MSG_RESOURCE_BUNDLE_SUFFIX, null);
	}

	/**
	 * Returns the list of available locale suffixes for a message resource
	 * bundle
	 * 
	 * @param messageBundlePath
	 *            the resource bundle path
	 * @param rsReader
	 *            the grails servlet context resource reader
	 * @return the list of available locale suffixes for a message resource
	 *         bundle
	 */
	public static List<String> getAvailableLocaleSuffixesForBundle(
			String messageBundlePath,
			GrailsServletContextResourceReader rsReader) {

		return getAvailableLocaleSuffixesForBundle(messageBundlePath,
				MSG_RESOURCE_BUNDLE_SUFFIX, rsReader);
	}

	/**
	 * Returns the list of available locale suffixes for a message resource
	 * bundle
	 * 
	 * @param messageBundlePath
	 *            the resource bundle path
	 * @param fileSuffix
	 *            the file suffix
	 * @param rsReader
	 *            the grails servlet context resource reader
	 * @return the list of available locale suffixes for a message resource
	 *         bundle
	 */
	public static List<String> getAvailableLocaleSuffixesForBundle(
			String messageBundlePath, String fileSuffix,
			GrailsServletContextResourceReader rsReader) {

		int idxNameSpace = messageBundlePath.indexOf("(");
		int idxFilter = messageBundlePath.indexOf("[");
		int idx = -1;
		if (idxNameSpace != -1 && idxFilter != -1) {
			idx = Math.min(idxNameSpace, idxFilter);
		} else if (idxNameSpace != -1 && idxFilter == -1) {
			idx = idxNameSpace;
		} else if (idxNameSpace == -1 && idxFilter != -1) {
			idx = idxFilter;
		}

		String messageBundle = null;
		if (idx > 0) {
			messageBundle = messageBundlePath.substring(0, idx);
		} else {
			messageBundle = messageBundlePath;
		}
		return getAvailableLocaleSuffixes(messageBundle, fileSuffix, rsReader);
	}

	/**
	 * Returns the list of available locale suffixes for a message resource
	 * bundle
	 * 
	 * @param messageBundle
	 *            the resource bundle path
	 * @param fileSuffix
	 *            the file suffix
	 * @param rsReader
	 *            the grails servlet context resource reader
	 * @return the list of available locale suffixes for a message resource
	 *         bundle
	 */
	public static List<String> getAvailableLocaleSuffixes(
			String messageBundles, String fileSuffix,
			GrailsServletContextResourceReader rsReader) {
		List<String> availableLocaleSuffixes = new ArrayList<String>();
		Locale[] availableLocales = Locale.getAvailableLocales();

		String[] msgBundleArray = messageBundles
				.split(RESOURCE_BUNDLE_SEPARATOR);

		for (String messageBundle : msgBundleArray) {

			addSuffixIfAvailable(messageBundle, availableLocaleSuffixes, null,
					fileSuffix, rsReader);

			for (int i = 0; i < availableLocales.length; i++) {
				Locale locale = availableLocales[i];
				addSuffixIfAvailable(messageBundle, availableLocaleSuffixes,
						locale, fileSuffix, rsReader);
			}
		}
		return availableLocaleSuffixes;
	}

	/**
	 * Add the locale suffix if the message resource bundle file exists.
	 * 
	 * @param messageBundlePath
	 *            the message resource bundle path
	 * @param availableLocaleSuffixes
	 *            the list of available locale suffix to update
	 * @param locale
	 *            the locale to check.
	 * @param fileSuffix
	 *            the file suffix
	 * @param rsReader
	 *            the grails servlet context resource reader
	 */
	private static void addSuffixIfAvailable(String messageBundlePath,
			List<String> availableLocaleSuffixes, Locale locale,
			String fileSuffix, GrailsServletContextResourceReader rsReader) {

		String localMsgResourcePath = toBundleName(messageBundlePath, locale)
				+ fileSuffix;

		boolean isFileSystemResourcePath = rsReader
				.isFileSystemPath(localMsgResourcePath);

		boolean resourceFound = false;
		String path = localMsgResourcePath;
		if (!isFileSystemResourcePath) {
			// Try to retrieve the resource from the servlet context (used in war mode)
			path = WEB_INF_DIR + localMsgResourcePath;
		}
		InputStream is = null;
		try{
			InputStream is = rsReader.getResourceAsStream(path);
			resourceFound =  is != null;
		}finally{
			IOUtils.close(is);
		}
		
		if (resourceFound) {

			String suffix = localMsgResourcePath.substring(messageBundlePath
					.length());
			if (suffix.length() > 0) {

				if (suffix.length() == fileSuffix.length()) {
					suffix = "";
				} else {
					// remove the "_" before the suffix "_en_US" => "en_US"
					suffix = suffix.substring(1,
							suffix.length() - fileSuffix.length());
				}
			}
			availableLocaleSuffixes.add(suffix);
		}
	}

	/**
	 * Converts the given <code>baseName</code> and <code>locale</code> to the
	 * bundle name. This method is called from the default implementation of the
	 * {@link #newBundle(String, Locale, String, ClassLoader, boolean)
	 * newBundle} and
	 * {@link #needsReload(String, Locale, String, ClassLoader, ResourceBundle, long)
	 * needsReload} methods.
	 * 
	 * <p>
	 * This implementation returns the following value:
	 * 
	 * <pre>
	 * baseName + &quot;_&quot; + language + &quot;_&quot; + country + &quot;_&quot; + variant
	 * </pre>
	 * 
	 * where <code>language</code>, <code>country</code> and
	 * <code>variant</code> are the language, country and variant values of
	 * <code>locale</code>, respectively. Final component values that are empty
	 * Strings are omitted along with the preceding '_'. If all of the values
	 * are empty strings, then <code>baseName</code> is returned.
	 * 
	 * <p>
	 * For example, if <code>baseName</code> is <code>"baseName"</code> and
	 * <code>locale</code> is <code>Locale("ja",&nbsp;"",&nbsp;"XX")</code>,
	 * then <code>"baseName_ja_&thinsp;_XX"</code> is returned. If the given
	 * locale is <code>Locale("en")</code>, then <code>"baseName_en"</code> is
	 * returned.
	 * 
	 * <p>
	 * Overriding this method allows applications to use different conventions
	 * in the organization and packaging of localized resources.
	 * 
	 * @param baseName
	 *            the base name of the resource bundle, a fully qualified class
	 *            name
	 * @param locale
	 *            the locale for which a resource bundle should be loaded
	 * @return the bundle name for the resource bundle
	 * @exception NullPointerException
	 *                if <code>baseName</code> or <code>locale</code> is
	 *                <code>null</code>
	 */
	public static String toBundleName(String bundleBaseName, Locale locale) {

		String baseName = bundleBaseName;
		if (!isPluginResoucePath(bundleBaseName)) {
			baseName = bundleBaseName.replace('.', '/');
		}
		if (locale == null) {
			return baseName;
		}

		String language = locale.getLanguage();
		String country = locale.getCountry();
		String variant = locale.getVariant();

		if (language == "" && country == "" && variant == "") {
			return baseName;
		}

		StringBuffer sb = new StringBuffer(baseName);
		sb.append('_');
		if (variant != "") {
			sb.append(language).append('_').append(country).append('_')
					.append(variant);
		} else if (country != "") {
			sb.append(language).append('_').append(country);
		} else {
			sb.append(language);
		}
		return sb.toString();
	}

	/**
	 * Returns the set of available locale suffixes
	 * 
	 * @return the set of available locale suffixes
	 */
	public static Set<String> getAvailableLocaleSuffixes() {

		Set<String> availableLocaleSuffixes = new HashSet<String>();
		Locale[] availableLocales = Locale.getAvailableLocales();
		for (int i = 0; i < availableLocales.length; i++) {
			Locale locale = availableLocales[i];

			StringBuffer sb = new StringBuffer();
			if (locale != null) {

				String language = locale.getLanguage();
				String country = locale.getCountry();
				String variant = locale.getVariant();

				if (variant != "") {
					sb.append(language).append('_').append(country).append('_')
							.append(variant);
				} else if (country != "") {
					sb.append(language).append('_').append(country);
				} else {
					sb.append(language);
				}
			}
			availableLocaleSuffixes.add(sb.toString());
		}
		return availableLocaleSuffixes;
	}

	/**
	 * Returns true is the resource path is a plugin path
	 * 
	 * @param resourcePath
	 *            the resource path
	 * @return true is the resource path is a plugin path
	 */
	public static boolean isPluginResoucePath(String resourcePath) {

		Matcher matcher = PLUGIN_RESOURCE_PATTERN.matcher(resourcePath);
		return matcher.find();
	}

	/**
	 * Handle the mapping of the resource path to the right one. This can be the
	 * case for plugin resources. It will returns the file system path or the
	 * real path or the same path if the path has not been remapped
	 * 
	 * @param path
	 *            the path
	 * @param pluginMsgPathMap
	 *            the map for plugin path
	 * @return the real path or the same path if the path has not been remapped
	 */
	public static String getRealResourcePath(String path,
			Map<String, String> pluginMsgPathMap) {
		String realPath = path;
		Matcher matcher = PLUGIN_RESOURCE_PATTERN.matcher(path);
		StringBuffer sb = new StringBuffer();

		if (matcher.find()) {

			String pluginName = matcher.group(2);
			String pluginPath = pluginMsgPathMap.get(pluginName);
			if (pluginPath != null) {
				matcher.appendReplacement(sb,
						RegexUtil.adaptReplacementToMatcher(pluginPath + "/"));
				matcher.appendTail(sb);
				realPath = sb.toString();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Plugin path '" + path + "' mapped to '"
							+ realPath + "'");
				}
			} else {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("No Plugin path found for '" + pluginName);
				}
			}
		}

		return realPath;
	}
}
