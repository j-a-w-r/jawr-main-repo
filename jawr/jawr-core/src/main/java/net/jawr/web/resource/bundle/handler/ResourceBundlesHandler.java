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

import java.io.OutputStream;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import net.jawr.web.DebugMode;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.iterator.ConditionalCommentCallbackHandler;
import net.jawr.web.resource.bundle.iterator.ResourceBundlePathsIterator;
import net.jawr.web.resource.bundle.lifecycle.BundlingProcessLifeCycleListener;
import net.jawr.web.resource.watcher.ResourceWatcher;

/**
 * Main interface to work with resource bundles. It helps in resolving groups of
 * resources which are served as a single one, and provides methods to generate
 * urls that point to either the full bundle or its individual resources.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 *
 */
public interface ResourceBundlesHandler {

	/**
	 * Returns the managed resource type
	 * 
	 * @return the managed resource type
	 */
	public String getResourceType();

	/**
	 * Determines which bundle corresponds to a path. The path may be a
	 * directory or file path. This path will not include any prefix, it is
	 * intended to be the path normally used for a tag library.
	 * 
	 * @param path
	 * @return String The bundle ID that can be used to retrieve it.
	 */
	public JoinableResourceBundle resolveBundleForPath(String path);

	/**
	 * Returns true if the bundle Id is the Id a global resource bundle
	 * 
	 * @param resourceBundleId
	 *            the resource bundle ID
	 * @return the global resource bundle path iterator
	 */
	public boolean isGlobalResourceBundle(String resourceBundleId);

	/**
	 * Returns the global resource bundle path iterator
	 * 
	 * @param commentCallbackHandler
	 *            the comment callback handler
	 * @param variants
	 *            the variant map
	 * @return the global resource bundle path iterator
	 */
	public ResourceBundlePathsIterator getGlobalResourceBundlePaths(
			ConditionalCommentCallbackHandler commentCallbackHandler, Map<String, String> variants);

	/**
	 * Returns the global resource bundle path iterator
	 * 
	 * @param debugMode
	 *            the debug mode
	 * @param commentCallbackHandler
	 *            the comment callback handler
	 * @param variants
	 *            the variant map
	 * @return the global resource bundle path iterator
	 */
	public ResourceBundlePathsIterator getGlobalResourceBundlePaths(DebugMode debugMode,
			ConditionalCommentCallbackHandler commentCallbackHandler, Map<String, String> variants);

	/**
	 * Returns the global resource bundle path iterator for one global bundle
	 * 
	 * @param bundlePath
	 *            the bundle path
	 * @param commentCallbackHandler
	 *            the comment callback handler
	 * @param variants
	 *            the variant map
	 * @return the global resource bundle path iterator
	 */
	public ResourceBundlePathsIterator getGlobalResourceBundlePaths(String bundlePath,
			ConditionalCommentCallbackHandler commentCallbackHandler, Map<String, String> variants);

	/**
	 * Returns an ordered list of the paths to use when accesing a resource
	 * bundle. Each implementation may return one or several paths depending on
	 * wether all resources are unified into one or several bundles. The paths
	 * returned should include the prefix that uniquely identify the bundle
	 * contents.
	 * 
	 * @param bundleId
	 *            the bundle ID
	 * @param commentCallbackHandler
	 *            the comment callback handler
	 * @param variants
	 *            the variant map
	 * @return the iterator of bundle paths
	 */
	public ResourceBundlePathsIterator getBundlePaths(String bundleId,
			ConditionalCommentCallbackHandler commentCallbackHandler, Map<String, String> variants);

	/**
	 * Returns an ordered list of the paths to use when accesing a resource
	 * bundle. Each implementation may return one or several paths depending on
	 * wether all resources are unified into one or several bundles. The paths
	 * returned should include the prefix that uniquely identify the bundle
	 * contents.
	 * 
	 * @param debugMode
	 *            the debug mode
	 * @param bundleId
	 *            the bundle ID
	 * @param commentCallbackHandler
	 *            the comment callback handler
	 * @param variants
	 *            the variant map
	 * @return the iterator of bundle paths
	 */
	public ResourceBundlePathsIterator getBundlePaths(DebugMode debugMode, String bundleId,
			ConditionalCommentCallbackHandler commentCallbackHandler, Map<String, String> variants);

	/**
	 * Writes data using the supplied writer, representing a unified bundle of
	 * resources.
	 * 
	 * @param bundlePath
	 *            the bundle path
	 * @param writer
	 *            the writer
	 * @throws net.jawr.web.exception.ResourceNotFoundException
	 *             if the resource is not found
	 */
	public void writeBundleTo(String bundlePath, Writer writer) throws ResourceNotFoundException;

	/**
	 * Writes the bytes of a bundle to the specified OutputStream. This method
	 * is used to copy the gzip data in the output stream.
	 * 
	 * @param bundlePath
	 *            the bundle path
	 * @param out
	 *            the output stream
	 * @throws net.jawr.web.exception.ResourceNotFoundException
	 *             if the resource is not found
	 */
	public void streamBundleTo(String bundlePath, OutputStream out) throws ResourceNotFoundException;

	/**
	 * Returns the global bundles
	 * 
	 * @return the global bundles
	 */
	public List<JoinableResourceBundle> getGlobalBundles();

	/**
	 * Returns the context bundles
	 * 
	 * @return the context bundles
	 */
	public List<JoinableResourceBundle> getContextBundles();

	/**
	 * Generates all file bundles so that they will be ready to attend requests.
	 */
	public void initAllBundles();

	/**
	 * Retrieves the configuration for this bundler
	 * 
	 * @return
	 */
	public JawrConfig getConfig();

	/**
	 * Returns the client side handler generator
	 * 
	 * @return the client side handler generator
	 */
	public ClientSideHandlerGenerator getClientSideHandler();

	/**
	 * Returns the type of bundle hashcode (valid, invalid, unknown bunle) for
	 * the requested path given in parameter
	 * 
	 * @param requestedPath
	 *            the requested path
	 * @return true if the requested path contains a valid bundle hashcode
	 */
	public BundleHashcodeType getBundleHashcodeType(String requestedPath);

	/**
	 * Returns the path of the directory containing the generated text bundles
	 * 
	 * @return the path of the directory containing the generated text bundles
	 */
	public String getBundleTextDirPath();

	/**
	 * Returns the path of the directory containing the generated zipped bundles
	 * 
	 * @return the path of the directory containing the generated zipped bundles
	 */
	public String getBundleZipDirPath();

	/**
	 * Notify modification to the bundles
	 * 
	 * @param bundles
	 *            the bundles to notify
	 */
	public void notifyModification(List<JoinableResourceBundle> bundles);

	/**
	 * Returns true if there are dirty bundles
	 * 
	 * @return true if there are dirty bundles
	 */
	public boolean bundlesNeedToBeRebuild();

	/**
	 * Rebuilds the bundles which needs to be rebuild
	 */
	public void rebuildModifiedBundles();

	/**
	 * Returns the names of dirty bundles
	 * 
	 * @return the names of dirty bundles
	 */
	public List<String> getDirtyBundleNames();

	/**
	 * Sets the resource watcher
	 * 
	 * @param watcher
	 *            the resource watcher to set
	 */
	public void setResourceWatcher(ResourceWatcher watcher);

	/**
	 * Returns true if the bundle are being processed
	 * 
	 * @return true if the bundle are being processed
	 */
	public AtomicBoolean isProcessingBundle();

	/**
	 * Sets the bundling life cycle listeners
	 * 
	 * @param listeners
	 *            the listeners to set
	 */
	public void setBundlingProcessLifeCycleListeners(List<BundlingProcessLifeCycleListener> listeners);

}
