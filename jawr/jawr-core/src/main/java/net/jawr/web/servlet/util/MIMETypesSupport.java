/**
 * Copyright 2008-2014 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.servlet.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;

/**
 * Holds a reference to the supported media types paired with their
 * corresponding MIME types.
 * 
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 *
 */
public class MIMETypesSupport {

	/** The properties of supported MIME types */
	private static Properties supportedMIMETypes;

	/** The mime type properties file location */
	private static final String MIME_PROPS_LOCATION = "net/jawr/web/resource/mimetypes.properties";

	/**
	 * Returns a Map object containing all the supported media extensions,
	 * paired to their MIME type.
	 * 
	 * @param ref
	 *            An object reference to anchor the classpath (any 'this'
	 *            reference does).
	 * @return
	 */
	public static Map<Object, Object> getSupportedProperties(Object ref) {

		if (null == supportedMIMETypes) {
			synchronized (MIMETypesSupport.class) {
				if (null == supportedMIMETypes) {
					// Load the supported MIME types out of a properties file
					try (InputStream is = ClassLoaderResourceUtils.getResourceAsStream(MIME_PROPS_LOCATION, ref)) {
						supportedMIMETypes = new Properties();
						supportedMIMETypes.load(is);
					} catch (FileNotFoundException e) {
						throw new BundlingProcessException(
								"Error retrieving " + MIME_PROPS_LOCATION + ". Please check your classloader settings");
					} catch (IOException e) {
						throw new BundlingProcessException(
								"Error retrieving " + MIME_PROPS_LOCATION + ". Please check your classloader settings");
					}
				}
			}
		}

		return supportedMIMETypes;
	}

}
