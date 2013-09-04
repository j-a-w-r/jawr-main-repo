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

import org.apache.log4j.Logger;

/**
 * This class defines the EhCache manager
 * 
 * @author Ibrahim Chaehoi
 */
public class EhCacheManager extends AbstractCacheManager {

	/** The logger */
	private static final Logger LOGGER = Logger
			.getLogger(EhCacheManager.class);
	
	/** The cache */
	private Cache cache;
	
	/**
	 * The constructor
	 * @param config the jawr config
	 */
	public EhCacheManager(JawrConfig config) {
	
		super(config);
		String configPath = config.getProperty("jawr.ehcache.config.path");
		String cacheName = config.getProperty("jawr.ehcache.cache.name");
		try {
			CacheManager cacheMgr = CacheManager.create(ClassLoaderResourceUtils.getResourceAsStream(configPath, this));
			cache = cacheMgr.getCache(cacheName);
		} catch (Exception e) {
			LOGGER.error("Unable to load EHCACHE configuration file", e);
		}

	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.cache.AbstractCacheManager#put(java.lang.String, java.lang.Object)
	 */
	public void put(String key, Object obj) {
	
		cache.put(new Element(key, obj));
	}

	 
	/* (non-Javadoc)
	 * @see net.jawr.web.cache.AbstractCacheManager#get(java.lang.String)
	 */
	public Object get(String key) {
	
		Element element = cache.get(key);
	     if (element != null) {
	         return element.getValue();
	     }
	     return null;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.cache.AbstractCacheManager#remove(java.lang.String)
	 */
	public Object remove(String key) {
	
		Element element = cache.get(key);
		 if (element != null) {
	         cache.remove(key);
	     }
	     return element;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.cache.AbstractCacheManager#clear()
	 */
	public void clear() {
		cache.removeAll();
	}

}
