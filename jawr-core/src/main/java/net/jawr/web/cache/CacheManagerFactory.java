/**
 * Copyright 2011-2012 Ibrahim Chaehoi
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

/**
 * The cache manager factory
 * 
 * @author Ibrahim Chaehoi
 */
public class CacheManagerFactory {

	/**
	 * Retrieves the cache manager for a resource type
	 * @param config the jawr config
	 * @param resourceType the resource type
	 * @return the cache manager for a resource type 
	 */
	public static JawrCacheManager getCacheManager(JawrConfig config, String resourceType){
	
		String cacheMgrAttributeName = "JAWR."+resourceType.toUpperCase()+".CACHE.MANAGER";
		JawrCacheManager cacheManager = (JawrCacheManager) config.getContext().getAttribute(cacheMgrAttributeName);
		if(cacheManager == null){
			String cacheManagerClass = config.getProperty("jawr.cache.manager", BasicCacheManager.class.getName());
			cacheManager = (JawrCacheManager) ClassLoaderResourceUtils.buildObjectInstance(cacheManagerClass, new Object[]{config});
			config.getContext().setAttribute(cacheMgrAttributeName, cacheManager);
		}
		return cacheManager;
	}
	
	/**
	 * Resets the cache manager for a resource type
	 * @param config the jawr config
	 * @param resourceType the resource type
	 * @return the cache manager for a resource type
	 */
	public static synchronized JawrCacheManager resetCacheManager(JawrConfig config, String resourceType){
		
		String cacheMgrAttributeName = "JAWR."+resourceType.toUpperCase()+".CACHE.MANAGER";
		JawrCacheManager cacheManager = (JawrCacheManager) config.getContext().getAttribute(cacheMgrAttributeName);
		if(cacheManager != null){
			cacheManager.clear();
			config.getContext().removeAttribute(cacheMgrAttributeName);
		}
		
		return getCacheManager(config, resourceType);
	}
	
}
