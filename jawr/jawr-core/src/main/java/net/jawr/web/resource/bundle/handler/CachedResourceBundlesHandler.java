/**
 * Copyright 2007-2016 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.handler;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import net.jawr.web.DebugMode;
import net.jawr.web.cache.JawrCacheManager;
import net.jawr.web.cache.CacheManagerFactory;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.iterator.ConditionalCommentCallbackHandler;
import net.jawr.web.resource.bundle.iterator.ResourceBundlePathsIterator;
import net.jawr.web.resource.bundle.lifecycle.BundlingProcessLifeCycleListener;
import net.jawr.web.resource.watcher.ResourceWatcher;

/**
 * ResourceBundlesHandler wrapper implementation that uses a ConcurrentHashMap
 * to cache the resources. Each resource is loaded only once from the
 * ResourceBundlesHandler, then it is stored in cache and retrieved from there
 * in subsequent calls. Every method call not related to retrieving data is
 * delegated to the wrapped implementation.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 * 
 */
public class CachedResourceBundlesHandler implements ResourceBundlesHandler {

	/** The prefix for text element in cache */
	private static final String TEXT_CACHE_PREFIX = "TEXT.";

	/** The prefix for zipped element in cache */
	private static final String ZIP_CACHE_PREFIX = "ZIP.";

	/** The resource bundle handler */
	private final ResourceBundlesHandler rsHandler;

	/** The cache manager */
	private final JawrCacheManager cacheMgr;

	/**
	 * Build a cached wrapper around the supplied ResourceBundlesHandler.
	 * 
	 * @param rsHandler
	 */
	public CachedResourceBundlesHandler(ResourceBundlesHandler rsHandler) {
		super();
		this.rsHandler = rsHandler;
		cacheMgr = CacheManagerFactory.getCacheManager(rsHandler.getConfig(), rsHandler.getResourceType());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getResourceType()
	 */
	@Override
	public String getResourceType() {

		return rsHandler.getResourceType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getGlobalBundles ()
	 */
	@Override
	public List<JoinableResourceBundle> getGlobalBundles() {
		return rsHandler.getGlobalBundles();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getContextBundles ()
	 */
	@Override
	public List<JoinableResourceBundle> getContextBundles() {
		return rsHandler.getContextBundles();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getBundlePaths (java.lang.String,
	 * net.jawr.web.resource.bundle.iterator.ConditionalCommentCallbackHandler,
	 * java.util.Map)
	 */
	@Override
	public ResourceBundlePathsIterator getBundlePaths(String bundleId,
			ConditionalCommentCallbackHandler commentCallbackHandler, Map<String, String> variants) {
		return rsHandler.getBundlePaths(bundleId, commentCallbackHandler, variants);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.ResourceBundlesHandler#getBundlePaths(java
	 * .lang.String)
	 */
	@Override
	public ResourceBundlePathsIterator getBundlePaths(DebugMode debugMode, String bundleId,
			ConditionalCommentCallbackHandler commentCallbackHandler, Map<String, String> variants) {
		return rsHandler.getBundlePaths(debugMode, bundleId, commentCallbackHandler, variants);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.ResourceBundlesHandler#getConfig()
	 */
	@Override
	public JawrConfig getConfig() {
		return rsHandler.getConfig();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.ResourceBundlesHandler#initAllBundles()
	 */
	@Override
	public void initAllBundles() {
		rsHandler.initAllBundles();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.ResourceBundlesHandler#resolveBundleForPath
	 * (java.lang.String)
	 */
	@Override
	public JoinableResourceBundle resolveBundleForPath(String path) {
		return rsHandler.resolveBundleForPath(path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.ResourceBundlesHandler#streamBundleTo(java
	 * .lang.String, java.io.OutputStream)
	 */
	@Override
	public void streamBundleTo(String bundlePath, OutputStream out) throws ResourceNotFoundException {

		try {
			// byte[] gzip = gzipCache.get(bundlePath);
			byte[] gzip = (byte[]) cacheMgr.get(ZIP_CACHE_PREFIX + bundlePath);
			// If it's not cached yet
			if (null == gzip) {
				// Stream the stored data
				ByteArrayOutputStream baOs = new ByteArrayOutputStream();
				BufferedOutputStream bfOs = new BufferedOutputStream(baOs);
				rsHandler.streamBundleTo(bundlePath, bfOs);

				// Copy the data into the ByteBuffer
				bfOs.close();
				gzip = baOs.toByteArray();

				// Cache the byte array
				cacheMgr.put(ZIP_CACHE_PREFIX + bundlePath, gzip);
			}

			// Write bytes to the outputstream
			IOUtils.write(gzip, out);

		} catch (IOException e) {
			throw new BundlingProcessException("Unexpected IOException writing bundle[" + bundlePath + "]", e);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.ResourceBundlesHandler#writeBundleTo(java
	 * .lang.String, java.io.Writer)
	 */
	@Override
	public void writeBundleTo(String bundlePath, Writer writer) throws ResourceNotFoundException {
		
		String text = (String) cacheMgr.get(TEXT_CACHE_PREFIX + bundlePath);
		try {
			// If it's not cached yet
			if (null == text) {
				String charsetName = rsHandler.getConfig().getResourceCharset().name();
				ByteArrayOutputStream baOs = new ByteArrayOutputStream();
				WritableByteChannel wrChannel = Channels.newChannel(baOs);
				Writer tempWriter = Channels.newWriter(wrChannel, charsetName);
				rsHandler.writeBundleTo(bundlePath, tempWriter);
				text = baOs.toString(charsetName);
				cacheMgr.put(TEXT_CACHE_PREFIX + bundlePath, text);
			}

			// Write the text to the outputstream
			writer.write(text);
			writer.flush();

		} catch (IOException e) {
			throw new BundlingProcessException("Unexpected IOException writing bundle[" + bundlePath + "]", e);
		}finally {
			IOUtils.close(writer);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getClientSideHandler()
	 */
	@Override
	public ClientSideHandlerGenerator getClientSideHandler() {
		return rsHandler.getClientSideHandler();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getGlobalResourceBundlePaths(java.lang.String,
	 * net.jawr.web.resource.bundle.iterator.ConditionalCommentCallbackHandler,
	 * java.util.Map)
	 */
	@Override
	public ResourceBundlePathsIterator getGlobalResourceBundlePaths(String bundleId,
			ConditionalCommentCallbackHandler commentCallbackHandler, Map<String, String> variants) {

		return rsHandler.getGlobalResourceBundlePaths(bundleId, commentCallbackHandler, variants);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getGlobalResourceBundlePaths(boolean,
	 * net.jawr.web.resource.bundle.iterator.ConditionalCommentCallbackHandler,
	 * java.util.Map)
	 */
	@Override
	public ResourceBundlePathsIterator getGlobalResourceBundlePaths(DebugMode debugMode,
			ConditionalCommentCallbackHandler commentCallbackHandler, Map<String, String> variants) {

		return rsHandler.getGlobalResourceBundlePaths(debugMode, commentCallbackHandler, variants);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getGlobalResourceBundlePaths
	 * (net.jawr.web.resource.bundle.iterator.ConditionalCommentCallbackHandler,
	 * java.util.Map)
	 */
	@Override
	public ResourceBundlePathsIterator getGlobalResourceBundlePaths(
			ConditionalCommentCallbackHandler commentCallbackHandler, Map<String, String> variants) {
		return rsHandler.getGlobalResourceBundlePaths(commentCallbackHandler, variants);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * isGlobalResourceBundle(java.lang.String)
	 */
	@Override
	public boolean isGlobalResourceBundle(String resourceBundleId) {
		return rsHandler.isGlobalResourceBundle(resourceBundleId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getTypeBundleHashcode(java.lang.String)
	 */
	@Override
	public BundleHashcodeType getBundleHashcodeType(String requestedPath) {
		return rsHandler.getBundleHashcodeType(requestedPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getBundleTextDirPath()
	 */
	@Override
	public String getBundleTextDirPath() {
		return rsHandler.getBundleTextDirPath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getBundleZipDirPath()
	 */
	@Override
	public String getBundleZipDirPath() {
		return rsHandler.getBundleZipDirPath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * notifyModification(java.util.List)
	 */
	@Override
	public void notifyModification(List<JoinableResourceBundle> bundles) {
		rsHandler.notifyModification(bundles);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * rebuildModifiedBundles()
	 */
	@Override
	public void rebuildModifiedBundles() {
		rsHandler.rebuildModifiedBundles();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * bundlesNeedToBeRebuild()
	 */
	@Override
	public boolean bundlesNeedToBeRebuild() {
		return rsHandler.bundlesNeedToBeRebuild();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getDirtyBundleNames()
	 */
	@Override
	public List<String> getDirtyBundleNames() {
		return rsHandler.getDirtyBundleNames();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * setResourceWatcher(net.jawr.web.resource.watcher.ResourceWatcher)
	 */
	@Override
	public void setResourceWatcher(ResourceWatcher watcher) {
		rsHandler.setResourceWatcher(watcher);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * isProcessingBundle()
	 */
	@Override
	public AtomicBoolean isProcessingBundle() {
		return rsHandler.isProcessingBundle();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * setBundlingProcessLifeCycleListeners(java.util.List)
	 */
	@Override
	public void setBundlingProcessLifeCycleListeners(List<BundlingProcessLifeCycleListener> listeners) {
		this.rsHandler.setBundlingProcessLifeCycleListeners(listeners);
	}
}
