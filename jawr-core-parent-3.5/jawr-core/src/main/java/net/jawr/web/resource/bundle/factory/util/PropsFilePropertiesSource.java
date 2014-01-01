/**
 * Copyright 2008  Jordi Hernández Sellés
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.jawr.web.resource.bundle.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConfigPropertiesSource implementation that reads its values from a
 * .properties file.
 * 
 * @author Jordi Hernández Sellés
 */
public class PropsFilePropertiesSource implements ConfigPropertiesSource {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(PropsFilePropertiesSource.class.getName());

	private String configLocation;
	protected int propsHashCode;
	protected static final String FILE_PREFIX = "file:";
	private boolean checking;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.factory.util.ConfigPropertiesSource#
	 * getConfigProperties()
	 */
	public final Properties getConfigProperties() {
		Properties props = doReadConfig();

		// Initialize hashcode of newly loaded properties.
		if (0 == this.propsHashCode) {
			this.propsHashCode = props.hashCode();
		}

		return props;
	}

	/**
	 * Implements the loic to load the properties. Subclasses will override this
	 * method to implement the specific logic to load their configuration
	 * properties.
	 * 
	 * @return
	 */
	protected Properties doReadConfig() {
		return readConfigFile(this.configLocation);
	}

	/**
	 * Reads a config file at the specified path. If the path starts with file:,
	 * it will be read from the filesystem. Otherwise it will be loaded from the
	 * classpath.
	 * 
	 * @param path
	 * @return
	 */
	protected Properties readConfigFile(String path) {
		Properties props = new Properties();
		// Load properties file
		InputStream is = null;
		try {
			if (path.startsWith(FILE_PREFIX)) {
				if (LOGGER.isDebugEnabled() && !checking)
					LOGGER.debug("Using filesystem properties file location at: "
							+ configLocation);
				is = new FileInputStream(new File(path.substring(FILE_PREFIX
						.length())));
			} else {
				if (LOGGER.isDebugEnabled() && !checking)
					LOGGER.debug("Reading properties from file at classpath: "
							+ configLocation);
				is = ClassLoaderResourceUtils.getResourceAsStream(path, this);
			}
			// load properties into a Properties object
			props.load(is);

		} catch (IOException e) {
			throw new IllegalArgumentException(
					"jawr configuration could not be found at " + path
							+ ". Make sure parameter is properly set "
							+ "in web.xml. ");
		} finally {
			IOUtils.close(is);
		}

		return props;
	}

	/**
	 * @param configLocation
	 *            the configLocation to set
	 */
	public void setConfigLocation(String configLocation) {
		this.configLocation = configLocation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.factory.util.ConfigPropertiesSource#
	 * configChanged()
	 */
	public final boolean configChanged() {
		checking = true;
		int currentConfigHash = doReadConfig().hashCode();
		boolean configChanged = this.propsHashCode != currentConfigHash;

		if (configChanged && LOGGER.isDebugEnabled())
			LOGGER.debug("Changes in configuration properties file detected.");

		this.propsHashCode = currentConfigHash;

		return configChanged;
	}

}
