/**
 * Copyright 2012 Ibrahim Chaehoi
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
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines the EhCache manager
 * 
 * @author Ibrahim Chaehoi
 */
public class EhCacheManager extends JawrCacheManager {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(EhCacheManager.class);

	/** The EhCache config file path */
	private static final String JAWR_EHCACHE_CONFIG_PATH = "jawr.ehcache.config.path";

	/** The EhCache name */
	private static final String JAWR_EHCACHE_CACHE_NAME = "jawr.ehcache.cache.name";

	/** The default EhCache config path */
	private static final String DEFAULT_EHCACHE_CONFIG_PATH = "ehcache.xml";

	/** The cache */
	private Cache cache;

	/**
	 * The constructor
	 * 
	 * @param config
	 *            the jawr config
	 */
	public EhCacheManager(JawrConfig config) {

		super(config);
		String configPath = config.getProperty(JAWR_EHCACHE_CONFIG_PATH,
				DEFAULT_EHCACHE_CONFIG_PATH);
		String cacheName = config.getProperty(JAWR_EHCACHE_CACHE_NAME);
		try {
			CacheManager cacheMgr = CacheManager
					.create(ClassLoaderResourceUtils.getResourceAsStream(
							configPath, this));
			cache = cacheMgr.getCache(cacheName);

		} catch (Exception e) {
			LOGGER.error("Unable to load EHCACHE configuration file", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.cache.AbstractCacheManager#put(java.lang.String,
	 * java.lang.Object)
	 */
	public void put(String key, Object obj) {

		cache.put(new Element(key, obj));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.cache.AbstractCacheManager#get(java.lang.String)
	 */
	public Object get(String key) {

		Element element = cache.get(key);
		if (element != null) {
			return element.getObjectValue();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.cache.AbstractCacheManager#remove(java.lang.String)
	 */
	public Object remove(String key) {

		Element element = cache.get(key);
		if (element != null) {
			cache.remove(key);
		}
		return element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.cache.AbstractCacheManager#clear()
	 */
	public void clear() {
		cache.removeAll();
	}

}
