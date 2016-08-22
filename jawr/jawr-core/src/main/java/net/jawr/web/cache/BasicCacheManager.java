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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.jawr.web.config.JawrConfig;

/**
 * This class defines a basic cache manager which use an in-memory map
 * 
 * @author Ibrahim Chaehoi
 */
public class BasicCacheManager extends JawrCacheManager {

	/** The cache hash map */
	private final Map<String, Object> cache = new ConcurrentHashMap<>();

	/**
	 * Constructor
	 * 
	 * @param config
	 *            the jawr configuration
	 */
	public BasicCacheManager(JawrConfig config) {
		super(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.cache.AbstractCacheManager#put(java.lang.String,
	 * java.lang.Object)
	 */
	@Override
	public void put(String key, Object value) {
		cache.put(key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.cache.AbstractCacheManager#get(java.lang.String)
	 */
	@Override
	public Object get(String key) {

		return cache.get(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.cache.AbstractCacheManager#remove(java.lang.String)
	 */
	@Override
	public Object remove(String key) {

		return cache.remove(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.cache.AbstractCacheManager#clear()
	 */
	@Override
	public void clear() {

		cache.clear();
	}

}
