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

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jawr.web.DebugMode;
import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.context.ThreadLocalJawrContext;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.exception.InterruptBundlingProcessException;
import net.jawr.web.resource.BinaryResourcesHandler;
import net.jawr.web.resource.bundle.CheckSumUtils;
import net.jawr.web.resource.bundle.CompositeResourceBundle;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.JoinableResourceBundleContent;
import net.jawr.web.resource.bundle.JoinableResourceBundlePropertySerializer;
import net.jawr.web.resource.bundle.factory.global.postprocessor.GlobalPostProcessingContext;
import net.jawr.web.resource.bundle.factory.global.preprocessor.GlobalPreprocessingContext;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.global.processor.GlobalProcessor;
import net.jawr.web.resource.bundle.hashcode.BundleHashcodeGenerator;
import net.jawr.web.resource.bundle.iterator.BundlePath;
import net.jawr.web.resource.bundle.iterator.ConditionalCommentCallbackHandler;
import net.jawr.web.resource.bundle.iterator.DebugModePathsIteratorImpl;
import net.jawr.web.resource.bundle.iterator.IECssDebugPathsIteratorImpl;
import net.jawr.web.resource.bundle.iterator.PathsIteratorImpl;
import net.jawr.web.resource.bundle.iterator.ResourceBundlePathsIterator;
import net.jawr.web.resource.bundle.lifecycle.BundlingProcessLifeCycleListener;
import net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.sorting.GlobalResourceBundleComparator;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.resource.bundle.variant.VariantUtils;
import net.jawr.web.resource.handler.bundle.ResourceBundleHandler;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.resource.watcher.ResourceWatcher;
import net.jawr.web.util.StopWatch;
import net.jawr.web.util.StringUtils;
import net.jawr.web.util.bom.UnicodeBOMReader;

/**
 * Default implementation of ResourceBundlesHandler
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class ResourceBundlesHandlerImpl implements ResourceBundlesHandler {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceBundlesHandler.class);

	/**
	 * The bundles that this handler manages.
	 */
	private List<JoinableResourceBundle> bundles;

	/**
	 * Global bundles, to include in every page
	 */
	private List<JoinableResourceBundle> globalBundles;

	/**
	 * Bundles to include upon request
	 */
	private List<JoinableResourceBundle> contextBundles;

	/** The map which map a child bundle to a composite parent bundle */
	private Map<String, List<JoinableResourceBundle>> compositeResourceBundleMap = new ConcurrentHashMap<>();

	/** The list of bundle prefixes */
	private List<String> bundlePrefixes;

	/**
	 * The bundles that will be processed once when the server will be up and
	 * running.
	 */
	private List<String> liveProcessBundles = new ArrayList<>();

	/** The resource handler */
	private ResourceReaderHandler resourceHandler;

	/** The resource handler */
	private ResourceBundleHandler resourceBundleHandler;

	/** The Jawr config */
	private JawrConfig config;

	/** The post processor */
	private ResourceBundlePostProcessor postProcessor;

	/** The unitary post processor */
	private ResourceBundlePostProcessor unitaryPostProcessor;

	/** The post processor for composite bundle */
	private ResourceBundlePostProcessor compositePostProcessor;

	/** The unitary post processor for composite bundle */
	private ResourceBundlePostProcessor unitaryCompositePostProcessor;

	/** The resourceTypeBundle global preprocessor */
	private GlobalProcessor<GlobalPreprocessingContext> resourceTypePreprocessor;

	/** The resourceTypeBundle global postprocessor */
	private GlobalProcessor<GlobalPostProcessingContext> resourceTypePostprocessor;

	/** The client side handler generator */
	private ClientSideHandlerGenerator clientSideHandlerGenerator;

	/** The bundle hashcode generator */
	private BundleHashcodeGenerator bundleHashcodeGenerator;

	/** The bundle mapping */
	private Properties bundleMapping;

	/** The flag indicating if we are processing bundles */
	private final AtomicBoolean processingBundle = new AtomicBoolean(false);

	/** The resource watcher */
	private ResourceWatcher watcher;

	/** The life cycle listeners */
	private final List<BundlingProcessLifeCycleListener> lifeCycleListeners = new CopyOnWriteArrayList<>();

	/** The flag indicating if we need to search for variant in post process */
	private boolean needToSearchForVariantInPostProcess;

	/**
	 * Build a ResourceBundlesHandler.
	 * 
	 * @param bundles
	 *            List The JoinableResourceBundles to use for this handler.
	 * @param resourceHandler
	 *            The file system access handler.
	 * @param resourceBundleHandler
	 *            the resource bundle handler
	 * @param config
	 *            Configuration for this handler.
	 */
	public ResourceBundlesHandlerImpl(List<JoinableResourceBundle> bundles, ResourceReaderHandler resourceHandler,
			ResourceBundleHandler resourceBundleHandler, JawrConfig config) {
		this(bundles, resourceHandler, resourceBundleHandler, config, null, null, null, null, null, null);
	}

	/**
	 * Build a ResourceBundlesHandler which will use the specified
	 * postprocessor.
	 * 
	 * @param bundles
	 *            List The JoinableResourceBundles to use for this handler.
	 * @param resourceHandler
	 *            The file system access handler.
	 * @param resourceBundleHandler
	 * @param config
	 *            Configuration for this handler.
	 * @param postProcessor
	 *            the bundle postprocessor
	 * @param unitaryPostProcessor
	 *            the unitary postprocessor
	 * @param compositePostProcessor
	 *            the composite postprocesor
	 * @param unitaryCompositePostProcessor
	 *            the unitary composite postprocessor
	 * @param resourceTypePreprocessor
	 *            the resource type preprocessor
	 * @param resourceTypePostprocessor
	 *            the resource type postprocessor
	 */
	public ResourceBundlesHandlerImpl(List<JoinableResourceBundle> bundles, ResourceReaderHandler resourceHandler,
			ResourceBundleHandler resourceBundleHandler, JawrConfig config, ResourceBundlePostProcessor postProcessor,
			ResourceBundlePostProcessor unitaryPostProcessor, ResourceBundlePostProcessor compositePostProcessor,
			ResourceBundlePostProcessor unitaryCompositePostProcessor,
			GlobalProcessor<GlobalPreprocessingContext> resourceTypePreprocessor,
			GlobalProcessor<GlobalPostProcessingContext> resourceTypePostprocessor) {
		super();
		this.resourceHandler = resourceHandler;
		this.resourceBundleHandler = resourceBundleHandler;
		this.config = config;
		this.bundleHashcodeGenerator = config.getBundleHashcodeGenerator();
		this.postProcessor = postProcessor;
		this.unitaryPostProcessor = unitaryPostProcessor;
		this.compositePostProcessor = compositePostProcessor;
		this.unitaryCompositePostProcessor = unitaryCompositePostProcessor;
		this.resourceTypePreprocessor = resourceTypePreprocessor;
		this.resourceTypePostprocessor = resourceTypePostprocessor;
		this.bundles = new CopyOnWriteArrayList<>();
		this.bundles.addAll(bundles);
		splitBundlesByType(bundles);

		this.clientSideHandlerGenerator = (ClientSideHandlerGenerator) ClassLoaderResourceUtils
				.buildObjectInstance(config.getClientSideHandlerGeneratorClass());
		this.clientSideHandlerGenerator.init(config, globalBundles, contextBundles);

		this.needToSearchForVariantInPostProcess = isSearchingForVariantInPostProcessNeeded();

		// register bundle life cycle listeners
		List<BundlingProcessLifeCycleListener> generatorLifeCycleListeners = config.getGeneratorRegistry()
				.getBundlingProcessLifeCycleListeners();
		lifeCycleListeners.addAll(generatorLifeCycleListeners);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * isProcessingBundle()
	 */
	@Override
	public AtomicBoolean isProcessingBundle() {
		return processingBundle;
	}

	/**
	 * Checks if it is needed to search for variant in post process
	 * 
	 * @return true if it is needed to search for variant in post process
	 */
	private boolean isSearchingForVariantInPostProcessNeeded() {
		boolean needToSearch = false;

		ResourceBundlePostProcessor[] postprocessors = new ResourceBundlePostProcessor[] { postProcessor,
				unitaryCompositePostProcessor, compositePostProcessor, unitaryCompositePostProcessor };
		for (ResourceBundlePostProcessor resourceBundlePostProcessor : postprocessors) {
			if (resourceBundlePostProcessor != null
					&& ((AbstractChainedResourceBundlePostProcessor) resourceBundlePostProcessor)
							.isVariantPostProcessor()) {
				needToSearch = true;
				break;
			}
		}

		return needToSearch;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getResourceType ()
	 */
	@Override
	public String getResourceType() {

		return resourceBundleHandler.getResourceType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getContextBundles ()
	 */
	@Override
	public List<JoinableResourceBundle> getContextBundles() {
		return contextBundles;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getGlobalBundles ()
	 */
	@Override
	public List<JoinableResourceBundle> getGlobalBundles() {
		return globalBundles;
	}

	/**
	 * Splits the bundles in two lists, one for global lists and other for the
	 * remaining bundles.
	 */
	private void splitBundlesByType(List<JoinableResourceBundle> bundles) {
		// Temporary lists (CopyOnWriteArrayList does not support sort())
		List<JoinableResourceBundle> tmpGlobal = new ArrayList<>();
		List<JoinableResourceBundle> tmpContext = new ArrayList<>();

		for (JoinableResourceBundle bundle : bundles) {
			if (bundle.getInclusionPattern().isGlobal()) {
				tmpGlobal.add(bundle);
			} else {
				tmpContext.add(bundle);
			}
		}

		// Sort the global bundles
		Collections.sort(tmpGlobal, new GlobalResourceBundleComparator());

		globalBundles = new CopyOnWriteArrayList<>();
		globalBundles.addAll(tmpGlobal);

		contextBundles = new CopyOnWriteArrayList<>();
		contextBundles.addAll(tmpContext);

		initBundlePrefixes();

		initCompositeBundleMap(globalBundles);
		initCompositeBundleMap(contextBundles);
	}

	/**
	 * Initialize the bundle prefixes
	 */
	protected void initBundlePrefixes() {
		bundlePrefixes = new CopyOnWriteArrayList<>();
		for (JoinableResourceBundle bundle : globalBundles) {
			if (StringUtils.isNotEmpty(bundle.getBundlePrefix())) {
				bundlePrefixes.add(bundle.getBundlePrefix());
			}
		}
		for (JoinableResourceBundle bundle : contextBundles) {
			if (StringUtils.isNotEmpty(bundle.getBundlePrefix())) {
				bundlePrefixes.add(bundle.getBundlePrefix());
			}
		}
	}

	/**
	 * Initialize the composite bundle map
	 * 
	 * @param bundles
	 *            list of resource bundle
	 */
	private void initCompositeBundleMap(List<JoinableResourceBundle> bundles) {
		for (JoinableResourceBundle bundle : bundles) {
			if (bundle.isComposite()) {
				List<JoinableResourceBundle> childBundles = ((CompositeResourceBundle) bundle).getChildBundles();
				for (JoinableResourceBundle childBundle : childBundles) {
					List<JoinableResourceBundle> associatedBundles = compositeResourceBundleMap
							.get(childBundle.getId());
					if (associatedBundles == null) {
						associatedBundles = new ArrayList<>();
					}
					associatedBundles.add(bundle);
					compositeResourceBundleMap.put(childBundle.getId(), associatedBundles);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * isGlobalResourceBundle(java.lang.String)
	 */
	@Override
	public boolean isGlobalResourceBundle(String resourceBundleId) {

		boolean isGlobalResourceBundle = false;
		for (JoinableResourceBundle bundle : globalBundles) {
			if (bundle.getId().equals(resourceBundleId)) {
				isGlobalResourceBundle = true;
			}
		}

		return isGlobalResourceBundle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getGlobalResourceBundlePaths
	 * (net.jawr.web.resource.bundle.iterator.ConditionalCommentCallbackHandler,
	 * java.lang.String)
	 */
	@Override
	public ResourceBundlePathsIterator getGlobalResourceBundlePaths(DebugMode debugMode,
			ConditionalCommentCallbackHandler commentCallbackHandler, Map<String, String> variants) {

		return getBundleIterator(debugMode, globalBundles, commentCallbackHandler, variants);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getGlobalResourceBundlePaths
	 * (net.jawr.web.resource.bundle.iterator.ConditionalCommentCallbackHandler,
	 * java.lang.String)
	 */
	@Override
	public ResourceBundlePathsIterator getGlobalResourceBundlePaths(
			ConditionalCommentCallbackHandler commentCallbackHandler, Map<String, String> variants) {

		return getBundleIterator(getDebugMode(), globalBundles, commentCallbackHandler, variants);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getGlobalResourceBundlePaths
	 * (net.jawr.web.resource.bundle.iterator.ConditionalCommentCallbackHandler,
	 * java.lang.String)
	 */
	@Override
	public ResourceBundlePathsIterator getGlobalResourceBundlePaths(String bundleId,
			ConditionalCommentCallbackHandler commentCallbackHandler, Map<String, String> variants) {

		List<JoinableResourceBundle> currentBundles = new ArrayList<>();
		for (JoinableResourceBundle bundle : globalBundles) {
			if (bundle.getId().equals(bundleId)) {
				currentBundles.add(bundle);
				break;
			}
		}
		return getBundleIterator(getDebugMode(), currentBundles, commentCallbackHandler, variants);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.ResourceCollector#getBundlePaths(java.lang
	 * .String)
	 */
	@Override
	public ResourceBundlePathsIterator getBundlePaths(String bundleId,
			ConditionalCommentCallbackHandler commentCallbackHandler, Map<String, String> variants) {

		return getBundlePaths(getDebugMode(), bundleId, commentCallbackHandler, variants);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getBundlePaths (boolean, java.lang.String,
	 * net.jawr.web.resource.bundle.iterator.ConditionalCommentCallbackHandler,
	 * java.lang.String)
	 */
	@Override
	public ResourceBundlePathsIterator getBundlePaths(DebugMode debugMode, String bundleId,
			ConditionalCommentCallbackHandler commentCallbackHandler, Map<String, String> variants) {

		List<JoinableResourceBundle> currentBundles = new ArrayList<>();

		// if the path did not correspond to a global bundle, find the requested
		// one.
		if (!isGlobalResourceBundle(bundleId)) {
			for (JoinableResourceBundle bundle : contextBundles) {
				if (bundle.getId().equals(bundleId)) {

					currentBundles.add(bundle);
					break;
				}
			}
		}

		return getBundleIterator(debugMode, currentBundles, commentCallbackHandler, variants);
	}

	/**
	 * Returns the bundle iterator
	 * 
	 * @param debugMode
	 *            the flag indicating if we are in debug mode or not
	 * @param commentCallbackHandler
	 *            the comment callback handler
	 * @param variants
	 *            the variant map
	 * @return the bundle iterator
	 */
	private ResourceBundlePathsIterator getBundleIterator(DebugMode debugMode, List<JoinableResourceBundle> bundles,
			ConditionalCommentCallbackHandler commentCallbackHandler, Map<String, String> variants) {
		ResourceBundlePathsIterator bundlesIterator;
		if (debugMode.equals(DebugMode.DEBUG)) {
			bundlesIterator = new DebugModePathsIteratorImpl(bundles, commentCallbackHandler, variants);
		} else if (debugMode.equals(DebugMode.FORCE_NON_DEBUG_IN_IE)) {
			bundlesIterator = new IECssDebugPathsIteratorImpl(bundles, commentCallbackHandler, variants);
		} else
			bundlesIterator = new PathsIteratorImpl(bundles, commentCallbackHandler, variants);
		return bundlesIterator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#writeBundleTo
	 * (java.lang.String, java.io.Writer)
	 */
	@Override
	public void writeBundleTo(String bundlePath, Writer writer) throws ResourceNotFoundException {

		Reader rd = null;

		try {

			// If debug mode is on, resources are retrieved one by one.
			if (config.isDebugModeOn()) {

				rd = resourceHandler.getResource(null, bundlePath);
			} else {

				for (String prefix : bundlePrefixes) {
					if (bundlePath.startsWith(prefix)) {
						bundlePath = bundlePath.substring(prefix.length());
						break;
					}
				}

				// Prefixes are used only in production mode
				String path = PathNormalizer.removeVariantPrefixFromPath(bundlePath);
				rd = resourceBundleHandler.getResourceBundleReader(path);
				if (liveProcessBundles.contains(path)) {
					rd = processInLive(rd);
				}
			}

			IOUtils.copy(rd, writer);
			writer.flush();
		} catch (IOException e) {
			throw new BundlingProcessException("Unexpected IOException writing bundle[" + bundlePath + "]", e);
		} finally {
			IOUtils.close(rd);
			IOUtils.close(writer);
		}
	}

	/**
	 * Process the bundle content in live
	 * 
	 * @param reader
	 *            the reader
	 * @return the processed bundle content
	 * @throws IOException
	 *             if an IOException occured
	 */
	private StringReader processInLive(Reader reader) throws IOException {

		String requestURL = ThreadLocalJawrContext.getRequestURL();
		StringWriter swriter = new StringWriter();
		IOUtils.copy(reader, swriter, true);
		String updatedContent = swriter.getBuffer().toString();
		if (requestURL != null) {

			updatedContent = updatedContent.replaceAll(JawrConstant.JAWR_BUNDLE_PATH_PLACEHOLDER_PATTERN, requestURL);
		}
		return new StringReader(updatedContent);
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

		// Remove prefix, which are used only in production mode
		String path = PathNormalizer.removeVariantPrefixFromPath(bundlePath);
		ReadableByteChannel data = null;
		try {
			if (liveProcessBundles.contains(path)) {

				Reader rd = null;
				try {
					rd = resourceBundleHandler.getResourceBundleReader(path);
					StringReader strRd = processInLive(rd);
					StringWriter strWriter = new StringWriter();
					IOUtils.copy(strRd, strWriter);

					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					try (GZIPOutputStream gzOut = new GZIPOutputStream(bos)) {
						byte[] byteData = strWriter.getBuffer().toString().getBytes(config.getResourceCharset().name());
						gzOut.write(byteData, 0, byteData.length);
					}
					ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
					data = Channels.newChannel(bis);
				} finally {
					IOUtils.close(rd);
				}

			} else {
				data = resourceBundleHandler.getResourceBundleChannel(path);
			}

			WritableByteChannel outChannel = Channels.newChannel(out);
			IOUtils.copy(data, outChannel);

		} catch (IOException e) {
			throw new BundlingProcessException("Unexpected IOException writing bundle [" + path + "]", e);
		} finally {
			IOUtils.close(data);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.ResourceCollector#getConfig()
	 */
	@Override
	public JawrConfig getConfig() {
		return config;
	}

	/**
	 * Returns the current debug mode
	 * 
	 * @return the current debug mode
	 */
	private DebugMode getDebugMode() {
		return config.isDebugModeOn() ? DebugMode.DEBUG : DebugMode.NO_DEBUG;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.ResourceCollector#initAllBundles()
	 */
	@Override
	public void initAllBundles() {

		stopProcessIfNeeded();

		if (config.getUseBundleMapping()) {
			bundleMapping = resourceBundleHandler.getJawrBundleMapping();
		}

		boolean mappingFileExists = resourceBundleHandler.isExistingMappingFile();
		boolean processBundleFlag = !config.getUseBundleMapping() || !mappingFileExists;

		StopWatch stopWatch = ThreadLocalJawrContext.getStopWatch();

		List<JoinableResourceBundle> bundleToProcess = this.bundles;
		boolean forceStoreJawrBundleMapping = false;
		if (!processBundleFlag) {
			String storeJawrConfigHashcode = resourceBundleHandler.getJawrBundleMapping()
					.getProperty(JawrConstant.JAWR_CONFIG_HASHCODE);
			String jawrConfigHashcode = getJawrConfigHashcode();
			boolean rebuildAllBundles = !config.getUseSmartBundling()
					|| (!jawrConfigHashcode.equals(storeJawrConfigHashcode));
			if (!rebuildAllBundles) {
				bundleToProcess = getBundlesToRebuild();
				if (!bundleToProcess.isEmpty() && LOGGER.isDebugEnabled()) {

					StringBuilder msg = new StringBuilder(
							"Jawr has detect changes on the following bundles, which will be updated :\n");
					for (JoinableResourceBundle b : bundleToProcess) {
						msg.append(b.getName()).append("\n");
					}
					LOGGER.debug(msg.toString());
				}
			} else {
				bundleMapping.clear();
				if (LOGGER.isDebugEnabled() && !jawrConfigHashcode.equals(storeJawrConfigHashcode)) {
					LOGGER.debug("Jawr config has changed since last bundling process. All bundles will be processed.");
				}
			}

			forceStoreJawrBundleMapping = !bundleToProcess.isEmpty();
		}

		// Execute processing
		build(bundleToProcess, forceStoreJawrBundleMapping, stopWatch);
	}

	/**
	 * Returns the jawr config hashcode
	 * 
	 * @return the jawr config hashcode
	 */
	protected String getJawrConfigHashcode() {
		try {
			return CheckSumUtils.getMD5Checksum(config.getConfigProperties().toString());
		} catch (IOException e) {
			throw new BundlingProcessException("Unable to calculate Jawr config checksum", e);
		}
	}

	/**
	 * Executes the global preprocessing
	 * 
	 * @param bundlesToBuild
	 *            The list of bundles to rebuild
	 * 
	 * @param processBundleFlag
	 *            the flag indicating if the bundles needs to be processed
	 * @param stopWatch
	 *            the stopWatch
	 */
	private void executeGlobalPreprocessing(List<JoinableResourceBundle> bundlesToBuild, boolean processBundleFlag,
			StopWatch stopWatch) {

		stopProcessIfNeeded();

		if (resourceTypePreprocessor != null) {
			if (stopWatch != null) {
				stopWatch.start("Global preprocessing");
			}
			GlobalPreprocessingContext ctx = new GlobalPreprocessingContext(config, resourceHandler, processBundleFlag);
			resourceTypePreprocessor.processBundles(ctx, bundles);

			// Update the list of bundle to rebuild if new bundles have been
			// detected as dirty in the global preprocessing phase
			List<JoinableResourceBundle> currentBundles = getBundlesToRebuild();
			for (JoinableResourceBundle b : currentBundles) {
				if (!bundlesToBuild.contains(b)) {
					bundlesToBuild.add(b);
				}
			}
			if (stopWatch != null) {
				stopWatch.stop();
			}
		}
	}

	/**
	 * Rebuilds the bundles given in parameter
	 * 
	 */
	@Override
	public synchronized void rebuildModifiedBundles() {

		stopProcessIfNeeded();

		StopWatch stopWatch = ThreadLocalJawrContext.getStopWatch();

		if (config.getUseSmartBundling()) {

			// Wait until all watch event has been processed
			if (watcher != null) {
				while (!watcher.hasNoEventToProcess()) {
					try {
						if (LOGGER.isInfoEnabled()) {
							LOGGER.info("Wait until there is no more watch event to process");
						}
						Thread.sleep(config.getSmartBundlingDelayAfterLastEvent());
					} catch (InterruptedException e) {
						// Do nothing
					}
				}
			}

			List<JoinableResourceBundle> bundlesToRebuild = getBundlesToRebuild();
			for (JoinableResourceBundle bundle : bundlesToRebuild) {
				bundle.resetBundleMapping();
			}
			build(bundlesToRebuild, true, stopWatch);

		} else {
			LOGGER.warn("You should turn on \"smart bundling\" feature to be able to rebuild modified bundles.");
		}
	}

	/**
	 * Returns the bundles which needs to be rebuild
	 * 
	 * @return the bundles which needs to be rebuild
	 */
	@Override
	public List<JoinableResourceBundle> getBundlesToRebuild() {
		List<JoinableResourceBundle> bundlesToRebuild = new ArrayList<>();

		if (config.getUseSmartBundling()) {

			for (JoinableResourceBundle bundle : globalBundles) {
				if (bundle.isDirty()) {
					bundlesToRebuild.add(bundle);
				}
			}
			for (JoinableResourceBundle bundle : contextBundles) {
				if (bundle.isDirty()) {
					bundlesToRebuild.add(bundle);
				}
			}
		}
		return bundlesToRebuild;
	}

	/**
	 * Builds the bundles given in parameter
	 * 
	 * @param bundlesToBuild
	 *            the list of bundle to build
	 * @param forceWriteBundleMapping
	 *            the flag indicating if the bundle mapping must be written in
	 *            any case
	 * @param stopWatch
	 *            the stop watch
	 */
	public void build(List<JoinableResourceBundle> bundlesToBuild, boolean forceWriteBundleMapping,
			StopWatch stopWatch) {

		stopProcessIfNeeded();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Starting bundle processing");
		}

		notifyStartBundlingProcess();

		boolean mappingFileExists = resourceBundleHandler.isExistingMappingFile();
		boolean processBundleFlag = !config.getUseBundleMapping() || !mappingFileExists;

		// Global preprocessing
		executeGlobalPreprocessing(bundlesToBuild, processBundleFlag, stopWatch);

		for (JoinableResourceBundle bundle : bundlesToBuild) {

			stopProcessIfNeeded();

			if (stopWatch != null) {
				stopWatch.start("Processing bundle '" + bundle.getName() + "'");
			}

			if (!ThreadLocalJawrContext.isBundleProcessingAtBuildTime() && null != bundle.getAlternateProductionURL()) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("No bundle generated for '" + bundle.getId()
							+ "' because a production URL is defined for this bundle.");
				}
			}
			if (bundle instanceof CompositeResourceBundle) {
				joinAndStoreCompositeResourcebundle((CompositeResourceBundle) bundle);
			} else {
				joinAndStoreBundle(bundle);
			}

			if (config.getUseBundleMapping()) {
				JoinableResourceBundlePropertySerializer.serializeInProperties(bundle,
						resourceBundleHandler.getResourceType(), bundleMapping);
			}

			bundle.setDirty(false);

			if (stopWatch != null) {
				stopWatch.stop();
			}
		}

		executeGlobalPostProcessing(processBundleFlag, stopWatch);
		storeJawrBundleMapping(resourceBundleHandler.isExistingMappingFile(), true);

		// Update the watcher with the path to watch
		try {
			if (watcher != null) {
				watcher.initPathToResourceBundleMap(bundlesToBuild);
			}
		} catch (IOException e) {
			throw new BundlingProcessException(e);
		}

		notifyEndBundlingProcess();
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("End of bundle processing");
		}

	}

	/**
	 * Stop the bundling process if needed
	 */
	protected void stopProcessIfNeeded() {
		if (ThreadLocalJawrContext.isInterruptingProcessingBundle()) {
			throw new InterruptBundlingProcessException();
		}
	}

	/**
	 * Notify the start of the bundling process
	 */
	protected void notifyStartBundlingProcess() {

		for (BundlingProcessLifeCycleListener listener : lifeCycleListeners) {
			listener.beforeBundlingProcess();
		}
		processingBundle.set(true);
		synchronized (processingBundle) {
			processingBundle.notifyAll();
		}
	}

	/**
	 * Notify the start of the bundling process
	 */
	protected void notifyEndBundlingProcess() {

		processingBundle.set(false);
		synchronized (processingBundle) {
			processingBundle.notifyAll();
		}

		for (BundlingProcessLifeCycleListener listener : lifeCycleListeners) {
			listener.afterBundlingProcess();
		}

	}

	/**
	 * Stores the Jawr bundle mapping
	 * 
	 * @param mappingFileExists
	 *            the flag indicating if the mapping file exists
	 * @param force
	 *            force the storage of Jawr bundle mapping
	 */
	private void storeJawrBundleMapping(boolean mappingFileExists, boolean force) {
		if (config.getUseBundleMapping() && (!mappingFileExists || force)) {
			bundleMapping.setProperty(JawrConstant.JAWR_CONFIG_HASHCODE, getJawrConfigHashcode());
			resourceBundleHandler.storeJawrBundleMapping(bundleMapping);

			if (resourceBundleHandler.getResourceType().equals(JawrConstant.CSS_TYPE)) {
				// Retrieve the image servlet mapping
				BinaryResourcesHandler binaryRsHandler = (BinaryResourcesHandler) config.getContext()
						.getAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE);
				if (binaryRsHandler != null) {
					// Here we update the image mapping if we are using the
					// build time bundle processor
					JawrConfig binaryJawrConfig = binaryRsHandler.getConfig();

					// If we use the full image bundle mapping and the jawr
					// working directory is not located inside the web
					// application
					// We store the image bundle maping which now contains the
					// mapping for CSS images
					String jawrWorkingDirectory = binaryJawrConfig.getJawrWorkingDirectory();
					if (binaryJawrConfig.getUseBundleMapping() && (jawrWorkingDirectory == null
							|| !jawrWorkingDirectory.startsWith(JawrConstant.URL_SEPARATOR))) {

						// Store the bundle mapping
						Properties props = new Properties();
						props.putAll(binaryRsHandler.getBinaryPathMap());
						props.setProperty(JawrConstant.JAWR_CONFIG_HASHCODE,
								Integer.toString(binaryJawrConfig.getConfigProperties().hashCode()));
						binaryRsHandler.getRsBundleHandler().storeJawrBundleMapping(props);

					}
				}
			}
		}
	}

	/**
	 * Execute the global post processing
	 * 
	 * @param processBundleFlag
	 *            the flag indicating if the bundle should be processed
	 * @param stopWatch
	 *            the stopWatch
	 */
	private void executeGlobalPostProcessing(boolean processBundleFlag, StopWatch stopWatch) {
		// Launch global postprocessing
		if (resourceTypePostprocessor != null) {
			if (stopWatch != null) {
				stopWatch.start("Global postprocessing");
			}
			GlobalPostProcessingContext ctx = new GlobalPostProcessingContext(config, this, resourceHandler,
					processBundleFlag);

			resourceTypePostprocessor.processBundles(ctx, this.bundles);
			if (stopWatch != null) {
				stopWatch.stop();
			}
		}
	}

	/**
	 * Joins the members of a composite bundle in all its variants, storing in a
	 * separate file for each variant.
	 * 
	 * @param composite
	 *            the composite resource bundle
	 */
	private void joinAndStoreCompositeResourcebundle(CompositeResourceBundle composite) {

		stopProcessIfNeeded();

		BundleProcessingStatus status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE,
				composite, resourceHandler, config);

		// Collect all variant names from child bundles
		Map<String, VariantSet> compositeBundleVariants = new HashMap<>();
		for (JoinableResourceBundle childbundle : composite.getChildBundles()) {
			if (childbundle.getVariants() != null)
				compositeBundleVariants = VariantUtils.concatVariants(compositeBundleVariants,
						childbundle.getVariants());
		}
		composite.setVariants(compositeBundleVariants);

		if (needToSearchForVariantInPostProcess || hasVariantPostProcessor(composite)) {
			status.setSearchingPostProcessorVariants(true);
			joinAndPostProcessBundle(composite, status);

			Map<String, VariantSet> postProcessVariants = status.getPostProcessVariants();
			if (!postProcessVariants.isEmpty()) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(
							"Post process variants found for bundle " + composite.getId() + ":" + postProcessVariants);
				}
				Map<String, VariantSet> newVariants = VariantUtils.concatVariants(composite.getVariants(),
						postProcessVariants);
				composite.setVariants(newVariants);
				status.setSearchingPostProcessorVariants(false);
				joinAndPostProcessBundle(composite, status);
			}
		} else {
			status.setSearchingPostProcessorVariants(false);
			joinAndPostProcessBundle(composite, status);
		}
	}

	/**
	 * Checks if the bundle has variant post processor
	 * 
	 * @param bundle
	 *            the bundle
	 * @return true if the bundle has variant post processor
	 */
	private boolean hasVariantPostProcessor(JoinableResourceBundle bundle) {

		boolean hasVariantPostProcessor = false;
		ResourceBundlePostProcessor bundlePostProcessor = bundle.getBundlePostProcessor();
		if (bundlePostProcessor != null
				&& ((AbstractChainedResourceBundlePostProcessor) bundlePostProcessor).isVariantPostProcessor()) {
			hasVariantPostProcessor = true;
		} else {
			bundlePostProcessor = bundle.getUnitaryPostProcessor();
			if (bundlePostProcessor != null
					&& ((AbstractChainedResourceBundlePostProcessor) bundlePostProcessor).isVariantPostProcessor()) {
				hasVariantPostProcessor = true;
			}
		}

		return hasVariantPostProcessor;
	}

	/**
	 * Joins and post process the variant composite bundle
	 * 
	 * @param composite
	 *            the composite bundle
	 * @param status
	 *            the status
	 * @param compositeBundleVariants
	 *            the variants
	 */
	private void joinAndPostProcessBundle(CompositeResourceBundle composite, BundleProcessingStatus status) {
		JoinableResourceBundleContent store;

		stopProcessIfNeeded();

		List<Map<String, String>> allVariants = VariantUtils.getAllVariants(composite.getVariants());
		// Add the default bundle variant (the non variant one)
		allVariants.add(null);
		// Process all variants
		for (Map<String, String> variants : allVariants) {
			status.setBundleVariants(variants);
			store = new JoinableResourceBundleContent();
			for (JoinableResourceBundle childbundle : composite.getChildBundles()) {
				if (!childbundle.getInclusionPattern().isIncludeOnlyOnDebug()) {
					JoinableResourceBundleContent childContent = joinAndPostprocessBundle(childbundle, variants,
							status);
					// Do unitary postprocessing.
					status.setProcessingType(BundleProcessingStatus.FILE_PROCESSING_TYPE);
					StringBuffer content = executeUnitaryPostProcessing(composite, status, childContent.getContent(),
							this.unitaryCompositePostProcessor);
					childContent.setContent(content);
					store.append(childContent);
				}
			}

			// Post process composite bundle as needed
			store = postProcessJoinedCompositeBundle(composite, store.getContent(), status);

			String variantKey = VariantUtils.getVariantKey(variants);
			String name = VariantUtils.getVariantBundleName(composite.getId(), variantKey, false);
			storeBundle(name, store);
			initBundleDataHashcode(composite, store, variantKey);
		}
	}

	/**
	 * Postprocess the composite bundle only if a composite bundle post
	 * processor is defined
	 * 
	 * @param composite
	 *            the composite bundle
	 * @param content
	 *            the content
	 * @param status
	 *            the status
	 * @return the content
	 */
	private JoinableResourceBundleContent postProcessJoinedCompositeBundle(CompositeResourceBundle composite,
			StringBuffer content, BundleProcessingStatus status) {

		JoinableResourceBundleContent store = new JoinableResourceBundleContent();
		StringBuffer processedContent = null;
		status.setProcessingType(BundleProcessingStatus.BUNDLE_PROCESSING_TYPE);
		ResourceBundlePostProcessor bundlePostProcessor = composite.getBundlePostProcessor();
		if (null != bundlePostProcessor) {

			processedContent = bundlePostProcessor.postProcessBundle(status, content);
		} else if (null != this.compositePostProcessor) {

			processedContent = this.compositePostProcessor.postProcessBundle(status, content);
		} else {
			processedContent = content;
		}

		store.setContent(processedContent);

		return store;
	}

	/**
	 * Initialize the bundle data hashcode and initialize the bundle mapping if
	 * needed
	 * 
	 * @param bundle
	 *            the bundle
	 * @param store
	 *            the data to store
	 */
	private void initBundleDataHashcode(JoinableResourceBundle bundle, JoinableResourceBundleContent store,
			String variant) {

		String bundleHashcode = bundleHashcodeGenerator.generateHashCode(config, store.getContent().toString());
		bundle.setBundleDataHashCode(variant, bundleHashcode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getTypeBundleHashcode(java.lang.String)
	 */
	@Override
	public BundleHashcodeType getBundleHashcodeType(String requestedPath) {

		BundleHashcodeType typeBundleHashcode = BundleHashcodeType.UNKNOW_BUNDLE;

		String[] pathInfos = PathNormalizer.extractBundleInfoFromPath(requestedPath, bundlePrefixes);

		if (pathInfos != null) {
			String bundlePrefix = pathInfos[0];

			String bundleId = pathInfos[1];
			String variantKey = pathInfos[2];
			String hashcode = pathInfos[3];

			JoinableResourceBundle bundle = resolveBundleForPath(bundleId);
			if (bundle != null) {
				String bundleHashcode = bundle.getBundleDataHashCode(variantKey);
				if (hashcode == null && bundleHashcode == null || hashcode != null && hashcode.equals(bundleHashcode)
						&& ((bundlePrefix == null && bundle.getBundlePrefix() == null)
								|| (bundlePrefix != null && bundlePrefix.equals(bundle.getBundlePrefix())))) {
					typeBundleHashcode = BundleHashcodeType.VALID_HASHCODE;
				} else {
					typeBundleHashcode = BundleHashcodeType.INVALID_HASHCODE;
				}
			}
		}
		return typeBundleHashcode;
	}

	/**
	 * Joins the members of a bundle and stores it
	 * 
	 * @param bundle
	 *            the bundle
	 * @param the
	 *            flag indicating if we should process the bundle or not
	 */
	private void joinAndStoreBundle(JoinableResourceBundle bundle) {

		BundleProcessingStatus status = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE, bundle,
				resourceHandler, config);
		JoinableResourceBundleContent store = null;

		// Process the bundle for searching variant
		if (needToSearchForVariantInPostProcess || hasVariantPostProcessor(bundle)) {
			status.setSearchingPostProcessorVariants(true);
			joinAndPostProcessBundle(bundle, status);

			// Process the bundles
			status.setSearchingPostProcessorVariants(false);
			Map<String, VariantSet> postProcessVariants = status.getPostProcessVariants();
			if (!postProcessVariants.isEmpty()) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(
							"Post process variants found for bundle " + bundle.getId() + ":" + postProcessVariants);
				}
				Map<String, VariantSet> newVariants = VariantUtils.concatVariants(bundle.getVariants(),
						postProcessVariants);
				bundle.setVariants(newVariants);
				joinAndPostProcessBundle(bundle, status);
			}
		} else {
			status.setSearchingPostProcessorVariants(false);
			joinAndPostProcessBundle(bundle, status);
		}

		// Store the collected resources as a single file, both in text and
		// gzip formats.
		store = joinAndPostprocessBundle(bundle, null, status);
		storeBundle(bundle.getId(), store);

		// Set the data hascode in the bundle, in case the prefix needs to
		// be generated
		initBundleDataHashcode(bundle, store, null);

	}

	/**
	 * Store the bundle
	 * 
	 * @param bundleId
	 *            the bundle Id to store
	 * @param store
	 *            the bundle
	 */
	private void storeBundle(String bundleId, JoinableResourceBundleContent store) {

		stopProcessIfNeeded();

		if (bundleMustBeProcessedInLive(store.getContent().toString())) {
			liveProcessBundles.add(bundleId);
		}
		resourceBundleHandler.storeBundle(bundleId, store);
	}

	/**
	 * Checks if the bundle must be processed in live
	 * 
	 * @param the
	 *            bundle content
	 * @return true if the bundle must be processed in live
	 */
	private boolean bundleMustBeProcessedInLive(String content) {
		return content.contains(JawrConstant.JAWR_BUNDLE_PATH_PLACEHOLDER);
	}

	/**
	 * Join and post process the bundle taking in account all its variants.
	 * 
	 * @param bundle
	 *            the bundle
	 * @param status
	 *            the bundle processing status
	 */
	private void joinAndPostProcessBundle(JoinableResourceBundle bundle, BundleProcessingStatus status) {

		JoinableResourceBundleContent store;
		List<Map<String, String>> allVariants = VariantUtils.getAllVariants(bundle.getVariants());
		// Add the default bundle variant (the non variant one)
		allVariants.add(null);

		for (Map<String, String> variantMap : allVariants) {
			status.setBundleVariants(variantMap);
			String variantKey = VariantUtils.getVariantKey(variantMap);
			String name = VariantUtils.getVariantBundleName(bundle.getId(), variantKey, false);
			store = joinAndPostprocessBundle(bundle, variantMap, status);
			storeBundle(name, store);
			initBundleDataHashcode(bundle, store, variantKey);
		}
	}

	/**
	 * Reads all the members of a bundle and executes all associated
	 * postprocessors.
	 * 
	 * @param bundle
	 *            the bundle
	 * @param variants
	 *            the variant map
	 * @param the
	 *            bundling processing status
	 * @param the
	 *            flag indicating if we should process the bundle or not
	 * @return the resource bundle content, where all postprocessors have been
	 *         executed
	 */
	private JoinableResourceBundleContent joinAndPostprocessBundle(JoinableResourceBundle bundle,
			Map<String, String> variants, BundleProcessingStatus status) {

		JoinableResourceBundleContent bundleContent = new JoinableResourceBundleContent();

		StringBuffer bundleData = new StringBuffer();
		StringBuffer store = null;

		try {

			boolean firstPath = true;
			// Run through all the files belonging to the bundle
			Iterator<BundlePath> pathIterator = null;
			if (bundle.getInclusionPattern().isIncludeOnlyOnDebug()) {
				pathIterator = bundle.getItemDebugPathList(variants).iterator();
			} else {
				pathIterator = bundle.getItemPathList(variants).iterator();
			}

			for (Iterator<BundlePath> it = pathIterator; it.hasNext();) {

				// File is first created in memory using a stringwriter.
				StringWriter writer = new StringWriter();
				BufferedWriter bwriter = new BufferedWriter(writer);

				String path = (String) it.next().getPath();
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Adding file [" + path + "] to bundle " + bundle.getId());

				// Get a reader on the resource, with appropriate encoding
				Reader rd = null;

				try {
					rd = resourceHandler.getResource(bundle, path, true);
				} catch (ResourceNotFoundException e) {
					// If a mapped file does not exist, a warning is issued and
					// process continues normally.
					LOGGER.warn("A mapped resource was not found: [" + path + "]. Please check your configuration");
					continue;
				}

				// Update the status.
				status.setLastPathAdded(path);

				rd = new UnicodeBOMReader(rd, config.getResourceCharset());
				if (!firstPath && ((UnicodeBOMReader) rd).hasBOM()) {
					((UnicodeBOMReader) rd).skipBOM();
				} else {
					firstPath = false;
				}

				IOUtils.copy(rd, bwriter, true);

				// Add new line at the end if it doesn't exist
				StringBuffer buffer = writer.getBuffer();

				if (!buffer.toString().endsWith(StringUtils.STR_LINE_FEED)) {
					buffer.append(StringUtils.STR_LINE_FEED);
				}

				// Do unitary postprocessing.
				status.setProcessingType(BundleProcessingStatus.FILE_PROCESSING_TYPE);
				bundleData.append(executeUnitaryPostProcessing(bundle, status, buffer, this.unitaryPostProcessor));
			}

			// Post process bundle as needed
			store = executeBundlePostProcessing(bundle, status, bundleData);

		} catch (IOException e) {
			throw new BundlingProcessException(
					"Unexpected IOException generating collected file [" + bundle.getId() + "].", e);
		}

		bundleContent.setContent(store);
		return bundleContent;
	}

	/**
	 * Executes the unitary resource post processing
	 * 
	 * @param bundle
	 *            the bundle
	 * @param status
	 *            the bundle processing status
	 * @param content
	 *            the content to process
	 * @return the processed content
	 */
	private StringBuffer executeUnitaryPostProcessing(JoinableResourceBundle bundle, BundleProcessingStatus status,
			StringBuffer content, ResourceBundlePostProcessor defaultPostProcessor) {

		StringBuffer bundleData = new StringBuffer();
		status.setProcessingType(BundleProcessingStatus.FILE_PROCESSING_TYPE);
		if (null != bundle.getUnitaryPostProcessor()) {
			StringBuffer resourceData = bundle.getUnitaryPostProcessor().postProcessBundle(status, content);
			bundleData.append(resourceData);
		} else if (null != defaultPostProcessor) {
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("POSTPROCESSING UNIT:" + status.getLastPathAdded());
			StringBuffer resourceData = defaultPostProcessor.postProcessBundle(status, content);
			bundleData.append(resourceData);
		} else {
			bundleData = content;
		}

		return bundleData;
	}

	/**
	 * Execute the bundle post processing
	 * 
	 * @param bundle
	 *            the bundle
	 * @param status
	 *            the status
	 * @param bundleData
	 *            the bundle data
	 * @return the processed content
	 */
	private StringBuffer executeBundlePostProcessing(JoinableResourceBundle bundle, BundleProcessingStatus status,
			StringBuffer bundleData) {

		StringBuffer store;
		status.setProcessingType(BundleProcessingStatus.BUNDLE_PROCESSING_TYPE);
		status.setLastPathAdded(bundle.getId());
		if (null != bundle.getBundlePostProcessor())
			store = bundle.getBundlePostProcessor().postProcessBundle(status, bundleData);
		else if (null != this.postProcessor)
			store = this.postProcessor.postProcessBundle(status, bundleData);
		else
			store = bundleData;
		return store;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * resolveBundleForPath(java.lang.String)
	 */
	@Override
	public JoinableResourceBundle resolveBundleForPath(String path) {

		JoinableResourceBundle theBundle = null;
		for (Iterator<JoinableResourceBundle> it = bundles.iterator(); it.hasNext();) {
			JoinableResourceBundle bundle = it.next();
			if (bundle.getId().equals(path) || bundle.belongsToBundle(path)) {
				theBundle = bundle;
				break;
			}
		}
		return theBundle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getClientSideHandler()
	 */
	@Override
	public ClientSideHandlerGenerator getClientSideHandler() {
		return this.clientSideHandlerGenerator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getBundleTextDirPath()
	 */
	@Override
	public String getBundleTextDirPath() {
		return this.resourceBundleHandler.getBundleTextDirPath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getBundleZipDirPath()
	 */
	@Override
	public String getBundleZipDirPath() {
		return this.resourceBundleHandler.getBundleZipDirPath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * notifyModification(java.util.List)
	 */
	@Override
	public void notifyModification(List<JoinableResourceBundle> bundles) {
		for (JoinableResourceBundle bundle : bundles) {
			if (LOGGER.isInfoEnabled() && !bundle.isDirty()) {
				LOGGER.info("The bundle '" + bundle.getId() + "' has been modified and needs to be rebuild.");
			}
			bundle.setDirty(true);

			// Update the composite bundles which are linked to this bundle if
			// they exists
			List<JoinableResourceBundle> linkedBundles = compositeResourceBundleMap.get(bundle.getId());
			if (linkedBundles != null) {
				for (JoinableResourceBundle compositeBundle : linkedBundles) {
					if (LOGGER.isInfoEnabled() && !compositeBundle.isDirty()) {
						LOGGER.info("The composite bundle '" + compositeBundle.getId()
								+ "', whose child has been modified, needs to be rebuild.");
					}
					compositeBundle.setDirty(true);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * bundlesNeedToBeRebuild()
	 */
	@Override
	public boolean bundlesNeedToBeRebuild() {
		return !getBundlesToRebuild().isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getDirtyBundleNames()
	 */
	@Override
	public List<String> getDirtyBundleNames() {

		List<String> bundleNames = new ArrayList<>();
		List<JoinableResourceBundle> bundlesToRebuild = getBundlesToRebuild();
		for (JoinableResourceBundle bundle : bundlesToRebuild) {
			bundleNames.add(bundle.getName());
		}
		return bundleNames;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * setResourceWatcher(net.jawr.web.resource.watcher.ResourceWatcher)
	 */
	@Override
	public void setResourceWatcher(ResourceWatcher watcher) {
		this.watcher = watcher;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * setBundlingProcessLifeCycleListeners(java.util.List)
	 */
	@Override
	public void setBundlingProcessLifeCycleListeners(List<BundlingProcessLifeCycleListener> listeners) {
		this.lifeCycleListeners.clear();
		this.lifeCycleListeners.addAll(listeners);
	}

}
