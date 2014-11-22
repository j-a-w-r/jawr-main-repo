/**
 * Copyright 2009 @author Matt Ruby, Ibrahim Chaehoi
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
package net.jawr.web.context;

import javax.management.ObjectName;

/**
 * This class defines the Jawr context.
 * 
 * @author Matt Ruby
 * @author Ibrahim Chaehoi
 *
 */
public class JawrContext {
	
	/** The object name for the MBean of the Jawr config manager */
	private ObjectName jawrConfigMgrObjectName;
	
	/** The flag indicating if we should override the debug setting */
	private boolean debugOverriden;
	
	/** The flag indicating that we are using making a bundle processing at build time */
	private boolean bundleProcessingAtBuildTime;
	
	/** The current request URL*/
	private String requestURL;
	
	/**
	 * Constructor. 
	 */
	public JawrContext() {
		
	}

	/**
	 * Returns the object name for the MBean of the Jawr config manager
	 * @return the object name for the MBean of the Jawr config manager.
	 */
	public ObjectName getJawrConfigMgrObjectName() {
		return jawrConfigMgrObjectName;
	}


	/**
	 * Sets the flag indicating if we should override the debug setting
	 * @return the debugOverride flag
	 */
	public boolean isDebugOverriden() {
		return debugOverriden;
	}

	/**
	 * Sets the object name for the MBean of the Jawr config manager.
	 * @param mbeanObjectName the mbeanObjectName to set
	 */
	public void setJawrConfigMgrObjectName(ObjectName mbeanObjectName) {
		this.jawrConfigMgrObjectName = mbeanObjectName;
	}
	
	/**
	 * Sets the flag indicating if we should override the debug setting
	 * @param debugOverriden the debugOverride flag to set
	 */
	public void setDebugOverriden(boolean debugOverriden) {
		this.debugOverriden = debugOverriden;
	}
	
	/**
	 * Returns the flag indicating that we are using making a bundle processing at build time
	 * @return the flag indicating that we are using making a bundle processing at build time
	 */
	public boolean isBundleProcessingAtBuildTime() {
		return bundleProcessingAtBuildTime;
	}

	/**
	 * Sets the flag indicating that we are using making a bundle processing at build time
	 * @param bundleProcessingAtBuildTime the flqg to set
	 */
	public void setBundleProcessingAtBuildTime(boolean bundleProcessingAtBuildTime) {
		this.bundleProcessingAtBuildTime = bundleProcessingAtBuildTime;
	}

	/**
	 * Returns the current request URL
	 * @return the current request URL
	 */
	public String getRequestURL() {
		return requestURL;
	}

	/**
	 * Sets the current request URL
	 * @param request the request URL to set
	 */
	public void setRequestURL(String requestURL) {
		this.requestURL = requestURL;
	}

	/**
	 * Reset the context. 
	 */
	public void reset(){
		
		this.jawrConfigMgrObjectName = null;
		this.debugOverriden = false;
		this.bundleProcessingAtBuildTime = false;
		this.requestURL = null;
	}

}