/**
 * Copyright 2008-2016  Jordi Hernández Sellés, Ibrahim Chaehoi
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

import java.io.Serializable;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jawr.web.exception.InterruptBundlingProcessException;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;

/**
 * A threaded component that periodically checks for updates to the
 * configuration of Jawr.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class ConfigChangeListenerThread extends Thread implements Serializable {

	/** The serial version UID */
	private static final long serialVersionUID = -7816209592970823852L;

	/** The logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigChangeListenerThread.class.getName());

	/** The wait duration in millisecond */
	private final long waitMillis;

	/** The configuration source */
	private final ConfigPropertiesSource propertiesSource;

	/** The overridden properties */
	private final Properties overriddenProperties;

	/** The listener for configuration changes */
	private final ConfigChangeListener listener;

	/** The resource bundles handler */
	private final ResourceBundlesHandler bundlesHandler;

	/** The flag indicating if we should continue the check */
	private boolean continuePolling;

	/**
	 * Constructor
	 * 
	 * @param resourceType
	 *            the resource type
	 * @param propertiesSource
	 *            the properties source
	 * @param overriddenProperties
	 *            the overridden properties
	 * @param listener
	 *            the listener
	 * @param bundlesHandler
	 *            the bundles handler
	 * @param secondsToWait
	 *            the second to wait between each check
	 */
	public ConfigChangeListenerThread(String resourceType, ConfigPropertiesSource propertiesSource,
			Properties overriddenProperties, ConfigChangeListener listener, ResourceBundlesHandler bundlesHandler,
			long secondsToWait) {
		super(resourceType + " Config Change listener Thread");
		this.propertiesSource = propertiesSource;
		this.overriddenProperties = overriddenProperties;
		this.listener = listener;
		this.bundlesHandler = bundlesHandler;
		this.waitMillis = secondsToWait * 1000;
		continuePolling = true;
		this.setDaemon(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		// Flag to avoid checking the very first time, when the request handler
		// has just started.
		boolean firstRun = true;

		while (continuePolling) {
			try {
				// Must check before sleeping, otherwise stopPolling does not
				// work.
				if (!firstRun) {
					if (propertiesSource.configChanged()) {
						Properties props = propertiesSource.getConfigProperties();
						if (overriddenProperties != null) {
							props.putAll(overriddenProperties);
						}
						listener.configChanged(props);
					} else if (bundlesHandler != null && bundlesHandler.bundlesNeedToBeRebuild()) {
						listener.rebuildDirtyBundles();
					}
				}
				sleep(waitMillis);
				firstRun = false;

			} catch (InterruptedException e) {
				// Nothing to do
			} catch (InterruptBundlingProcessException e) {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Bundling processed stopped");
				}
			}
		}
	}

	/**
	 * Causes the thread to stop polling for changes.
	 */
	public void stopPolling() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Stopping the configuration change polling");
		}

		continuePolling = false;
	}

}
