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
 * Interface for classes which act as a source for getting jawr configuration properties. 
 * An implementation should be able to read a properties file and load it.  
 * @author Jordi Hernández Sellés
 */
public interface ConfigPropertiesSource {
	
	/**
	 * Read/modify configuration from a source (such as a .properties file) and return it. 
	 * @return
	 */
	public abstract Properties getConfigProperties();
	
	/**
	 * Determine if configuration is changed to reconfigure Jawr during development 
	 * without having to restart the server. 
	 * @return
	 */
	public abstract boolean configChanged();

}
