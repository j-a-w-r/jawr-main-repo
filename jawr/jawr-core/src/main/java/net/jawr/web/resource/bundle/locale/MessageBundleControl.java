/**
 * Copyright 2016 Ibrahim Chaehoi
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import net.jawr.web.resource.bundle.IOUtils;

/**
 * The Jawr message bundle Control
 * 
 * @author Ibrahim Chaehoi
 */
public class MessageBundleControl extends ResourceBundle.Control {

	/** The flag indicating if we should fall back to the system locale */
	private boolean fallbackToSystemLocale = true;

	/** The charset */
	private Charset charset;

	/**
	 * Constructor
	 * 
	 * @param fallbackToSystemLocale
	 *            the flag indicating if the fallback local is the System locale
	 *            or not
	 * @param charset
	 *            the ResourceBundle charset
	 */
	public MessageBundleControl(boolean fallbackToSystemLocale, Charset charset) {

		this.fallbackToSystemLocale = fallbackToSystemLocale;
		this.charset = charset;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ResourceBundle.Control#getFallbackLocale(java.lang.String,
	 * java.util.Locale)
	 */
	@Override
	public Locale getFallbackLocale(String baseName, Locale locale) {
		if (baseName == null) {
			throw new NullPointerException();
		}
		Locale defaultLocale = Locale.getDefault();

		if (!fallbackToSystemLocale) {
			defaultLocale = new Locale("", "");
		}

		return locale.equals(defaultLocale) ? null : defaultLocale;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ResourceBundle.Control#newBundle(java.lang.String,
	 * java.util.Locale, java.lang.String, java.lang.ClassLoader, boolean)
	 */
	@Override
	public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
			throws IllegalAccessException, InstantiationException, IOException {

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
					throw new ClassCastException(bundleClass.getName() + " cannot be cast to ResourceBundle");
				}
			} catch (ClassNotFoundException e) {
			}
		} else if (format.equals("java.properties")) {
			final String resourceName = toResourceName(bundleName, "properties");
			final ClassLoader classLoader = loader;
			final boolean reloadFlag = reload;
			InputStream stream = null;
			Reader rd = null;
			try {
				stream = AccessController.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
					public InputStream run() throws IOException {
						InputStream is = null;
						if (reloadFlag) {
							URL url = classLoader.getResource(resourceName);
							if (url != null) {
								URLConnection connection = url.openConnection();
								if (connection != null) {
									// Disable caches to get fresh
									// data for
									// reloading.
									connection.setUseCaches(false);
									is = connection.getInputStream();
								}
							}
						} else {
							is = classLoader.getResourceAsStream(resourceName);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.ResourceBundle.Control#getTimeToLive(java.lang.String,
	 * java.util.Locale)
	 */
	@Override
	public long getTimeToLive(String baseName, Locale locale) {
		if (baseName == null || locale == null) {
			throw new NullPointerException();
		}
		return TTL_DONT_CACHE;
	}

	/**
	 * Returns true if it should fallback to the system locale
	 * 
	 * @return true if it should fallback to the system locale
	 */
	public boolean isFallbackToSystemLocale() {
		return fallbackToSystemLocale;
	}

	/**
	 * Returns the fall-back locale
	 * 
	 * @return the fall-back locale
	 */
	public Locale getFallbackLocale() {

		Locale locale = null;
		if (this.fallbackToSystemLocale) {
			locale = Locale.getDefault();
		} else {
			locale = new Locale("", "");
		}

		return locale;
	}

}