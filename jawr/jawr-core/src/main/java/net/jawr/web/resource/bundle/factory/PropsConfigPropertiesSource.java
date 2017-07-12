/**
 * Copyright 2009-2017 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.factory;

import java.util.Properties;

import net.jawr.web.resource.bundle.factory.util.ConfigPropertiesSource;
import net.jawr.web.resource.bundle.factory.util.PropsFilePropertiesSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConfigPropertiesSource implementation that reads its values from a Properties
 * object.
 * 
 * @author Ibrahim Chaehoi
 */
public class PropsConfigPropertiesSource implements ConfigPropertiesSource {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(PropsFilePropertiesSource.class.getName());

	/** The configuration properties */
	private final Properties configProps;

	/** The properties hashcode */
	protected Integer propsHashCode;

	/**
	 * Constructor
	 * 
	 * @param props
	 *            the properties
	 */
	public PropsConfigPropertiesSource(Properties props) {
		this.configProps = props;
		
		// Initialize hashcode of newly loaded properties.
		this.propsHashCode = props.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.factory.util.ConfigPropertiesSource#
	 * configChanged()
	 */
	@Override
	public boolean configChanged() {
		int currentConfigHash = this.configProps.hashCode();
		boolean configChanged = propsHashCode!=null && this.propsHashCode != currentConfigHash;

		if (configChanged && LOGGER.isDebugEnabled()){
			LOGGER.debug("Changes in Jawr configuration properties detected.");
		}
		
		this.propsHashCode = currentConfigHash;

		return configChanged;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.factory.util.ConfigPropertiesSource#
	 * getConfigProperties()
	 */
	@Override
	public Properties getConfigProperties() {
		return configProps;
	}

}
