/**
 * 
 */
package net.jawr.wicket.resource.generator;

/**
 * @author ibrahim
 *
 */
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import net.jawr.web.resource.bundle.locale.MessageBundleControl;

public class XmlResourceBundleControl extends MessageBundleControl {

	/**
	 * Constructor
	 * 
	 */
	public XmlResourceBundleControl() {
		super(false, Charset.forName("UTF-8"));
	}

	private static String XML = "xml";

	public List<String> getFormats(String baseName) {
		return Collections.singletonList(XML);
	}

	public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
			throws IllegalAccessException, InstantiationException, IOException {

		if ((baseName == null) || (locale == null) || (format == null) || (loader == null)) {
			throw new NullPointerException();
		}
		ResourceBundle bundle = null;
		if (!format.equals(XML)) {
			return null;
		}

		String bundleName = toBundleName(baseName, locale);
		String resourceName = toResourceName(bundleName, format);
		URL url = loader.getResource(resourceName);
		if (url == null) {
			return null;
		}
		URLConnection connection = url.openConnection();
		if (connection == null) {
			return null;
		}
		if (reload) {
			connection.setUseCaches(false);
		}
		InputStream stream = connection.getInputStream();
		if (stream == null) {
			return null;
		}
		BufferedInputStream bis = new BufferedInputStream(stream);
		bundle = new XMLResourceBundle(bis);
		bis.close();

		return bundle;
	}

	private class XMLResourceBundle extends ResourceBundle {
		private Properties props;

		XMLResourceBundle(InputStream stream) throws IOException {
			props = new Properties();
			props.loadFromXML(stream);
		}

		protected Object handleGetObject(String key) {
			return props.getProperty(key);
		}

		public Enumeration<String> getKeys() {
			Set<String> handleKeys = props.stringPropertyNames();
			return Collections.enumeration(handleKeys);
		}
	}
}
