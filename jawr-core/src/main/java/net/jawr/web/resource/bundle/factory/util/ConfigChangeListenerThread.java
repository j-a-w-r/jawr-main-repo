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

import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * A threaded component that periodically checks for updates to the configuration of Jawr. 
 * 
 * @author Jordi Hernández Sellés
 */
public class ConfigChangeListenerThread extends Thread {
	
	private static final Logger LOGGER = Logger.getLogger(ConfigChangeListenerThread.class.getName());
	
	private long waitMillis;
	private ConfigPropertiesSource propertiesSource;
	private Properties overrideProperties;
	private ConfigChangeListener listener;
	private boolean continuePolling;
	
	public ConfigChangeListenerThread(ConfigPropertiesSource propertiesSource,
			Properties overrideProperties, ConfigChangeListener listener, long secondsToWait ) {
		super();
		this.propertiesSource = propertiesSource;
		this.overrideProperties = overrideProperties;
		this.listener = listener;
		this.waitMillis = secondsToWait * 1000;
		continuePolling = true;
		this.setDaemon(true);
	}


	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		// Flag to avoid checking the very first time, when the request handler has just started. 
		boolean firstRun = true;
		
		while(continuePolling) {
			try {
				// Must check before sleeping, otherwise stopPolling does not work.  
				if(!firstRun && propertiesSource.configChanged()){
					Properties props = propertiesSource.getConfigProperties();
					if(overrideProperties != null){
						props.putAll(overrideProperties);
					}
					listener.configChanged(props);
				}
				sleep(waitMillis);
				firstRun = false;				
				/* It is painful to show a log statement every certain amount of seconds...		  
				 if(log.isDebugEnabled())
					log.debug("Verifying wether properties are changed...");
					*/
			} catch (InterruptedException e) {
				LOGGER.error("Failure at config reloading checker thread.");
			}
		}
	}
	
	/**
	 * Causes the thread to stop polling for changes. 
	 */
	public void stopPolling() {
		if(LOGGER.isDebugEnabled())
			LOGGER.debug("Stopping the configuration change polling");
		
		continuePolling = false;
	}

}
