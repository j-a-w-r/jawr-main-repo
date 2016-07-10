/**
 * Copyright 2011-2016 Ibrahim Chaehoi
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
package net.jawr.web.cache;

import net.jawr.web.config.JawrConfig;

/**
 * This class defines the Abstract cache manager
 * 
 * @author Ibrahim Chaehoi
 */
public abstract class JawrCacheManager {

	/**
	 * Constructor
	 * 
	 * @param config
	 *            the config
	 */
	public JawrCacheManager(JawrConfig config) {

	}

	/**
	 * Put an element in cache
	 * 
	 * @param key
	 *            the element key
	 * @param value
	 *            the element to put in cache
	 */
	public abstract void put(String key, Object value);

	/**
	 * Retrieve an element from the cache using its key
	 * 
	 * @param key
	 *            the element key
	 * @return an element from the cache
	 */
	public abstract Object get(String key);

	/**
	 * Remove an element from the cache using its key
	 * 
	 * @param key
	 *            the element key
	 * @return an element from the cache
	 */
	public abstract Object remove(String key);

	/**
	 * Clear the cache content
	 */
	public abstract void clear();

}
