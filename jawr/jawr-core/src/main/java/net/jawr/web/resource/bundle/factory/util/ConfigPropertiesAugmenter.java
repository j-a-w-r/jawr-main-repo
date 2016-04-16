/**
 * Copyright 2008 Jordi Hernández Sellés
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
package net.jawr.web.resource.bundle.factory.util;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import net.jawr.web.resource.bundle.factory.PropertiesBundleConstant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class which can be used to merge several configuration sources. It
 * merges the properties from a base Properties object with another one.
 * Mappings, children of composites and other properties are extended rather
 * than overwritten, so that an existing bundle from the source can get
 * additional mappings.
 * 
 * @author Jordi Hernández Sellés
 */
public class ConfigPropertiesAugmenter {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ConfigPropertiesAugmenter.class);

	/** The configuration properties */
	private final Properties configProperties;

	/** The set of private configuration properties */
	private Set<String> privateConfigProperties;

	/**
	 * Creates an instance of the augmenter which uses configProperties as the
	 * base configuration to augment, but prevents the overwriting of any
	 * property stated in the privateConfigProperties list.
	 * 
	 * @param configProperties
	 *            Base configuration.
	 * @param privateConfigProperties
	 *            Set of names of properties which may not be overridden.
	 */
	public ConfigPropertiesAugmenter(Properties configProperties,
			Set<String> privateConfigProperties) {
		super();
		this.configProperties = configProperties;
		this.privateConfigProperties = privateConfigProperties;
	}

	/**
	 * Constructor
	 * 
	 * @param configProperties
	 *            the configuration properties
	 */
	public ConfigPropertiesAugmenter(final Properties configProperties) {
		super();
		this.configProperties = configProperties;
	}

	/**
	 * Augments the base configuration with the properties specified as
	 * parameter.
	 * 
	 * @param configToAdd
	 *            the configuration properties to add
	 */
	public void augmentConfiguration(Properties configToAdd) {
		for (Iterator<Entry<Object, Object>> it = configToAdd.entrySet()
				.iterator(); it.hasNext();) {

			Entry<Object, Object> entry = it.next();
			String configKey = (String) entry.getKey();
			String configValue = (String) entry.getValue();

			// Skip the property is it is not overridable
			if (null != privateConfigProperties
					&& privateConfigProperties.contains(configKey)) {
				LOGGER.warn("The property "
						+ configKey
						+ " can not be overridden. It will remain with a value of "
						+ configProperties.get(configKey));
				continue;
			}

			// Augment mappings
			if (isAugmentable(configKey)) {
				String currentValue = configProperties.getProperty(configKey);
				currentValue += "," + configValue;
				configProperties.put(configKey, currentValue);
			} else
				// replace properties
				configProperties.put(configKey, configValue);
		}
	}

	/**
	 * Determine wether a property is augmentable (so instead of overriding,
	 * values are appended to the current values).
	 * 
	 * @param configKey
	 *            the configuration key
	 * @return true if the property is augmentable or not.
	 */
	protected boolean isAugmentable(String configKey) {
		boolean rets = false;
		rets = (configKey
				.endsWith(PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_NAMES)
				|| // Bundles
				configKey
						.endsWith(PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_MAPPINGS)
				|| // mappings
				configKey
						.endsWith(PropertiesBundleConstant.BUNDLE_FACTORY_CUSTOM_COMPOSITE_NAMES)
				|| // children of composites
				configKey.equals(PropertiesBundleConstant.CUSTOM_POSTPROCESSORS
						+ PropertiesBundleConstant.CUSTOM_POSTPROCESSORS_NAMES) || // Postprocessors
																					// definition
		configKey.equals(PropertiesBundleConstant.PROPS_PREFIX
				+ PropertiesBundleConstant.CUSTOM_GENERATORS)); // Generators
																// definition

		rets = rets && configProperties.containsKey(configKey);
		return rets;
	}

}
