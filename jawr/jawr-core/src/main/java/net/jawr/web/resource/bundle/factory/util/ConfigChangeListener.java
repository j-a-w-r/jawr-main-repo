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

/**
 * Interface for a listener to changes in Jawr configuration.  
 * 
 * @author Jordi Hernández Sellés
 */
public interface ConfigChangeListener {

	/**
	 * To be invoked when configuration is changed. 
	 * @param newConfig New properties representing new configuration. 
	 */
	public abstract void configChanged(Properties newConfig);
	
}
