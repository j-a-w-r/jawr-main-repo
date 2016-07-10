/**
 * Copyright 2008-2016 Jordi Hernández Sellés, Ibrahim Chaehoi
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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;

/**
 * This class defines a config properties source from multiple file
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class MultipleFileConfigSource extends PropsFilePropertiesSource
		implements ConfigPropertiesSource, ServletContextAware {

	/** The servlet context additional configuration parameter */
	private static final String SERVLET_CONTEXT_ADDITIONAL_CONFIG_PARAM = "jawr.config.sources";

	/** The property base names */
	protected List<String> propertyBaseNames;

	/**
	 * Set of private configuration properties which should not be overridden.
	 * Subclasses may initialize this collection to fit specific needs. The
	 * default implementation does not initialize this Set.
	 */
	protected Set<String> privateConfigProperties;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.factory.util.ConfigPropertiesSource#
	 * getConfigProperties()
	 */
	@Override
	public Properties doReadConfig() {
		Properties baseConfig = super.doReadConfig();
		ConfigPropertiesAugmenter augmenter;
		if (null == privateConfigProperties)
			augmenter = new ConfigPropertiesAugmenter(baseConfig);
		else
			augmenter = new ConfigPropertiesAugmenter(baseConfig, privateConfigProperties);

		for (String nextConfigSource : propertyBaseNames) {
			Properties additionalConfig = readConfigFile(nextConfigSource);
			augmenter.augmentConfiguration(additionalConfig);
		}

		return baseConfig;
	}

	/**
	 * Initializes the propertyBaseNames list by reading the jawr.config.sources
	 * servlet context param. Subclasses may override this method to use a
	 * different strategy.
	 * 
	 * @param context
	 */
	protected void initAdditionalPropertyBaseNames(ServletContext context) {
		String propertyNames = context.getInitParameter(SERVLET_CONTEXT_ADDITIONAL_CONFIG_PARAM);
		propertyBaseNames = new ArrayList<>();
		if (null != propertyNames) {
			StringTokenizer tk = new StringTokenizer(propertyNames, ",");
			while (tk.hasMoreTokens())
				propertyBaseNames.add(tk.nextToken());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.factory.util.ServletContextAware#
	 * setServletContext(javax.servlet.ServletContext)
	 */
	@Override
	public void setServletContext(ServletContext context) {
		initAdditionalPropertyBaseNames(context);
	}

}
