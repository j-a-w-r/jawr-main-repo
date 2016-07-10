/**
 * Copyright 2014-2016 Ibrahim Chaehoi
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
package net.jawr.web.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines utilities methods to handle Properties.
 * 
 * @author Ibrahim Chaehoi
 */
public final class PropertiesUtils {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtils.class);

	/**
	 * Constructor
	 */
	private PropertiesUtils() {

	}

	/**
	 * Filters the properties file using the prefix given in parameter.
	 * 
	 * @param props
	 *            the properties
	 * @param prefix
	 *            the prefix of the property to retrieve
	 * @param removePrefix
	 *            the flag indicating if the prefix should be removed from the
	 *            property key.
	 * @return the filtered properties
	 */
	public static Map<String, String> filterProperties(Properties props, String prefix, boolean removePrefix) {

		List<String> excludedProperties = Collections.emptyList();
		return filterProperties(props, prefix, removePrefix, excludedProperties);
	}

	/**
	 * Filters the properties file using the prefix given in parameter.
	 * 
	 * @param props
	 *            the properties
	 * @param prefix
	 *            the prefix of the property to retrieve
	 * @param removePrefix
	 *            the flag indicating if the prefix should be removed from the
	 *            property key.
	 * @param excludedProperties
	 *            the properties which are excluded from the filtered properties
	 * @return the filtered properties
	 */
	public static Map<String, String> filterProperties(Properties props, String prefix, boolean removePrefix,
			List<String> excludedProperties) {

		Map<String, String> filteredProps = new HashMap<>();
		Set<Entry<Object, Object>> entrySet = props.entrySet();
		for (Entry<Object, Object> propEntry : entrySet) {
			String key = (String) propEntry.getKey();
			if (key.startsWith(prefix)) {

				String newKey = key;
				if (removePrefix) {
					newKey = key.substring(prefix.length());
				}
				if (!excludedProperties.contains(key)) {
					filteredProps.put(newKey, (String) propEntry.getValue());
				} else {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("The property '" + key + "' has been excluded.");
					}
				}
			}
		}

		return filteredProps;
	}
}
