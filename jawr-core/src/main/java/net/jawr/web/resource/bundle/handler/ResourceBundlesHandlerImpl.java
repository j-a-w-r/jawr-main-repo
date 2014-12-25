/**
 * Copyright 2007-2014 Jordi Hernández Sellés, Ibrahim Chaehoi
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.GZIPOutputStream;

import net.jawr.web.DebugMode;
import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.context.ThreadLocalJawrContext;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.BinaryResourcesHandler;
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
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.sorting.GlobalResourceBundleComparator;
import net.jawr.web.resource.bundle.variant.VariantSet;
import net.jawr.web.resource.bundle.variant.VariantUtils;
import net.jawr.web.resource.handler.bundle.ResourceBundleHandler;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.util.StringUtils;
import net.jawr.web.util.bom.UnicodeBOMReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of ResourceBundlesHandler
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class ResourceBundlesHandlerImpl implements ResourceBundlesHandler {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ResourceBundlesHandler.class);

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

	/**
	 * The bundles that will be processed once when the server will be up and
	 * running.
	 */
	private List<String> liveProcessBundles = new ArrayList<String>();

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

	/**
	 * Build a ResourceBundlesHandler.
	 * 
	 * @param bundles List The JoinableResourceBundles to use for this handler.
	 * @param resourceHandler The file system access handler.
	 * @param config Configuration for this handler.
	 */
	public ResourceBundlesHandlerImpl(List<JoinableResourceBundle> bundles,
			ResourceReaderHandler resourceHandler,
			ResourceBundleHandler resourceBundleHandler, JawrConfig config) {
		this(bundles, resourceHandler, resourceBundleHandler, config, null,
				null, null, null, null, null);
	}

	/**
	 * Build a ResourceBundlesHandler which will use the specified
	 * postprocessor.
	 * 
	 * @param bundles List The JoinableResourceBundles to use for this handler.
	 * @param resourceHandler The file system access handler.
	 * @param config Configuration for this handler.
	 * @param postProcessor
	 */
	public ResourceBundlesHandlerImpl(
			List<JoinableResourceBundle> bundles,
			ResourceReaderHandler resourceHandler,
			ResourceBundleHandler resourceBundleHandler,
			JawrConfig config,
			ResourceBundlePostProcessor postProcessor,
			ResourceBundlePostProcessor unitaryPostProcessor,
			ResourceBundlePostProcessor compositePostProcessor,
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
		this.bundles = new CopyOnWriteArrayList<JoinableResourceBundle>();
		this.bundles.addAll(bundles);
		splitBundlesByType(bundles);

		this.clientSideHandlerGenerator = (ClientSideHandlerGenerator) ClassLoaderResourceUtils
				.buildObjectInstance(config
						.getClientSideHandlerGeneratorClass());
		this.clientSideHandlerGenerator.init(config, globalBundles,
				contextBundles);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#getResourceType
	 * ()
	 */
	public String getResourceType() {

		return resourceBundleHandler.getResourceType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#getContextBundles
	 * ()
	 */
	public List<JoinableResourceBundle> getContextBundles() {
		return contextBundles;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#getGlobalBundles
	 * ()
	 */
	public List<JoinableResourceBundle> getGlobalBundles() {
		return globalBundles;
	}

	/**
	 * Splits the bundles in two lists, one for global lists and other for the
	 * remaining bundles.
	 */
	private void splitBundlesByType(List<JoinableResourceBundle> bundles) {
		// Temporary lists (CopyOnWriteArrayList does not support sort())
		List<JoinableResourceBundle> tmpGlobal = new ArrayList<JoinableResourceBundle>();
		List<JoinableResourceBundle> tmpContext = new ArrayList<JoinableResourceBundle>();

		for (Iterator<JoinableResourceBundle> it = bundles.iterator(); it
				.hasNext();) {
			JoinableResourceBundle bundle = it.next();

			// Exclude/include debug only scripts
			// if (config.isDebugModeOn()
			// && bundle.getInclusionPattern().isExcludeOnDebug())
			// continue;
			// else if (!config.isDebugModeOn()
			// && bundle.getInclusionPattern().isIncludeOnDebug())
			// continue;

			if (bundle.getInclusionPattern().isGlobal())
				tmpGlobal.add(bundle);
			else
				tmpContext.add(bundle);
		}

		// Sort the global bundles
		Collections.sort(tmpGlobal, new GlobalResourceBundleComparator());

		globalBundles = new CopyOnWriteArrayList<JoinableResourceBundle>();
		globalBundles.addAll(tmpGlobal);

		contextBundles = new CopyOnWriteArrayList<JoinableResourceBundle>();
		contextBundles.addAll(tmpContext);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * isGlobalResourceBundle(java.lang.String)
	 */
	public boolean isGlobalResourceBundle(String resourceBundleId) {

		boolean isGlobalResourceBundle = false;
		for (Iterator<JoinableResourceBundle> it = globalBundles.iterator(); it
				.hasNext();) {
			JoinableResourceBundle bundle = it.next();
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
	public ResourceBundlePathsIterator getGlobalResourceBundlePaths(
			DebugMode debugMode,
			ConditionalCommentCallbackHandler commentCallbackHandler,
			Map<String, String> variants) {

		return getBundleIterator(debugMode, globalBundles,
				commentCallbackHandler, variants);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getGlobalResourceBundlePaths
	 * (net.jawr.web.resource.bundle.iterator.ConditionalCommentCallbackHandler,
	 * java.lang.String)
	 */
	public ResourceBundlePathsIterator getGlobalResourceBundlePaths(
			ConditionalCommentCallbackHandler commentCallbackHandler,
			Map<String, String> variants) {

		return getBundleIterator(getDebugMode(), globalBundles,
				commentCallbackHandler, variants);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getGlobalResourceBundlePaths
	 * (net.jawr.web.resource.bundle.iterator.ConditionalCommentCallbackHandler,
	 * java.lang.String)
	 */
	public ResourceBundlePathsIterator getGlobalResourceBundlePaths(
			String bundleId,
			ConditionalCommentCallbackHandler commentCallbackHandler,
			Map<String, String> variants) {

		List<JoinableResourceBundle> bundles = new ArrayList<JoinableResourceBundle>();
		for (Iterator<JoinableResourceBundle> it = globalBundles.iterator(); it
				.hasNext();) {
			JoinableResourceBundle bundle = it.next();
			if (bundle.getId().equals(bundleId)) {
				bundles.add(bundle);
				break;
			}
		}
		return getBundleIterator(getDebugMode(), bundles,
				commentCallbackHandler, variants);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.ResourceCollector#getBundlePaths(java.lang
	 * .String)
	 */
	public ResourceBundlePathsIterator getBundlePaths(String bundleId,
			ConditionalCommentCallbackHandler commentCallbackHandler,
			Map<String, String> variants) {

		return getBundlePaths(getDebugMode(), bundleId, commentCallbackHandler,
				variants);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#getBundlePaths
	 * (boolean, java.lang.String,
	 * net.jawr.web.resource.bundle.iterator.ConditionalCommentCallbackHandler,
	 * java.lang.String)
	 */
	public ResourceBundlePathsIterator getBundlePaths(DebugMode debugMode,
			String bundleId,
			ConditionalCommentCallbackHandler commentCallbackHandler,
			Map<String, String> variants) {

		List<JoinableResourceBundle> bundles = new ArrayList<JoinableResourceBundle>();

		// if the path did not correspond to a global bundle, find the requested
		// one.
		if (!isGlobalResourceBundle(bundleId)) {
			for (Iterator<JoinableResourceBundle> it = contextBundles
					.iterator(); it.hasNext();) {
				JoinableResourceBundle bundle = it.next();
				if (bundle.getId().equals(bundleId)) {

					bundles.add(bundle);
					break;
				}
			}
		}

		return getBundleIterator(debugMode, bundles, commentCallbackHandler,
				variants);
	}

	/**
	 * Returns the bundle iterator
	 * 
	 * @param debugMode the flag indicating if we are in debug mode or not
	 * @param commentCallbackHandler the comment callback handler
	 * @param variants the variant map
	 * @return the bundle iterator
	 */
	private ResourceBundlePathsIterator getBundleIterator(DebugMode debugMode,
			List<JoinableResourceBundle> bundles,
			ConditionalCommentCallbackHandler commentCallbackHandler,
			Map<String, String> variants) {
		ResourceBundlePathsIterator bundlesIterator;
		if (debugMode.equals(DebugMode.DEBUG)) {
			bundlesIterator = new DebugModePathsIteratorImpl(bundles,
					commentCallbackHandler, variants);
		} else if (debugMode.equals(DebugMode.FORCE_NON_DEBUG_IN_IE)) {
			bundlesIterator = new IECssDebugPathsIteratorImpl(bundles,
					commentCallbackHandler, variants);
		} else
			bundlesIterator = new PathsIteratorImpl(bundles,
					commentCallbackHandler, variants);
		return bundlesIterator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.ResourceCollector#writeBundleTo(java.lang
	 * .String, java.io.Writer)
	 */
	public void writeBundleTo(String bundlePath, Writer writer)
			throws ResourceNotFoundException {

		Reader rd = null;

		try {

			// If debug mode is on, resources are retrieved one by one.
			if (config.isDebugModeOn()) {

				rd = resourceHandler.getResource(bundlePath);
			} else {
				// Prefixes are used only in production mode
				String path = PathNormalizer
						.removeVariantPrefixFromPath(bundlePath);
				rd = resourceBundleHandler.getResourceBundleReader(path);
				if (liveProcessBundles.contains(path)) {
					rd = processInLive(rd);
				}
			}

			IOUtils.copy(rd, writer);
			writer.flush();
		} catch (IOException e) {
			throw new BundlingProcessException(
					"Unexpected IOException writing bundle[" + bundlePath + "]",
					e);
		} finally {
			IOUtils.close(rd);
		}
	}

	/**
	 * Process the bundle content in live
	 * 
	 * @param reader the reader
	 * @return the processed bundle content
	 * @throws IOException if an IOException occured
	 */
	private StringReader processInLive(Reader reader) throws IOException {

		String requestURL = ThreadLocalJawrContext.getRequestURL();
		StringWriter swriter = new StringWriter();
		IOUtils.copy(reader, swriter, true);
		String updatedContent = swriter.getBuffer().toString();
		if (requestURL != null) {

			updatedContent = updatedContent.replaceAll(
					JawrConstant.JAWR_BUNDLE_PATH_PLACEHOLDER_PATTERN,
					requestURL);
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
	public void streamBundleTo(String bundlePath, OutputStream out)
			throws ResourceNotFoundException {

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
					GZIPOutputStream gzOut = new GZIPOutputStream(bos);
					byte[] byteData = strWriter.getBuffer().toString()
							.getBytes(config.getResourceCharset().name());
					gzOut.write(byteData, 0, byteData.length);
					gzOut.close();
					ByteArrayInputStream bis = new ByteArrayInputStream(
							bos.toByteArray());
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
			throw new BundlingProcessException(
					"Unexpected IOException writing bundle [" + path + "]", e);
		} finally {
			IOUtils.close(data);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.ResourceCollector#getConfig()
	 */
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
	public void initAllBundles() {

		if (config.getUseBundleMapping()) {
			bundleMapping = resourceBundleHandler.getJawrBundleMapping();
		}

		// Run through every bundle
		boolean mappingFileExists = resourceBundleHandler
				.isExistingMappingFile();

		// TODO Clean up usage of processBundleFlag... Why it is in
		// GlobalProcessingContext?
		boolean processBundleFlag = !config.getUseBundleMapping()
				|| !mappingFileExists;

		if (resourceTypePreprocessor != null) {
			GlobalPreprocessingContext ctx = new GlobalPreprocessingContext(
					config, resourceHandler, processBundleFlag);
			resourceTypePreprocessor.processBundles(ctx, bundles);
		}

		for (Iterator<JoinableResourceBundle> itCol = bundles.iterator(); itCol
				.hasNext();) {
			JoinableResourceBundle bundle = itCol.next();

			boolean processBundle = processBundleFlag;
			if (!ThreadLocalJawrContext.isBundleProcessingAtBuildTime()
					&& null != bundle.getAlternateProductionURL()) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("No bundle generated for '"
							+ bundle.getId()
							+ "' because a production URL is defined for this bundle.");
				}
				processBundle = false;
			}
			if (bundle instanceof CompositeResourceBundle)
				joinAndStoreCompositeResourcebundle(
						(CompositeResourceBundle) bundle, processBundle);
			else
				joinAndStoreBundle(bundle, processBundle);

			if (config.getUseBundleMapping() && !mappingFileExists) {
				JoinableResourceBundlePropertySerializer.serializeInProperties(
						bundle, resourceBundleHandler.getResourceType(),
						bundleMapping);
			}
		}

		// Launch global postprocessing
		if (resourceTypePostprocessor != null) {
			GlobalPostProcessingContext ctx = new GlobalPostProcessingContext(
					config, this, resourceHandler, processBundleFlag);

			resourceTypePostprocessor.processBundles(ctx, bundles);
		}

		if (config.getUseBundleMapping() && !mappingFileExists) {
			resourceBundleHandler.storeJawrBundleMapping(bundleMapping);

			if (resourceBundleHandler.getResourceType().equals(
					JawrConstant.CSS_TYPE)) {
				// Retrieve the image servlet mapping
				BinaryResourcesHandler binaryRsHandler = (BinaryResourcesHandler) config
						.getContext().getAttribute(
								JawrConstant.BINARY_CONTEXT_ATTRIBUTE);
				if (binaryRsHandler != null) {
					// Here we update the image mapping if we are using the
					// build time bundle processor
					JawrConfig binaryJawrConfig = binaryRsHandler.getConfig();

					// If we use the full image bundle mapping and the jawr
					// working directory is not located inside the web
					// application
					// We store the image bundle maping which now contains the
					// mapping for CSS images
					String jawrWorkingDirectory = binaryJawrConfig
							.getJawrWorkingDirectory();
					if (binaryJawrConfig.getUseBundleMapping()
							&& (jawrWorkingDirectory == null || !jawrWorkingDirectory
									.startsWith(JawrConstant.URL_SEPARATOR))) {

						// Store the bundle mapping
						Properties props = new Properties();
						props.putAll(binaryRsHandler.getBinaryPathMap());
						binaryRsHandler.getRsBundleHandler()
								.storeJawrBundleMapping(props);

					}
				}
			}
		}
	}

	/**
	 * Joins the members of a composite bundle in all its variants, storing in a
	 * separate file for each variant.
	 * 
	 * @param composite the composite resource bundle
	 * @param processBundle the flag indicating if we should process the bundle
	 *            or not
	 */
	private void joinAndStoreCompositeResourcebundle(
			CompositeResourceBundle composite, boolean processBundle) {

		BundleProcessingStatus status = new BundleProcessingStatus(
				BundleProcessingStatus.FILE_PROCESSING_TYPE, composite,
				resourceHandler, config);

		// Collect all variant names from child bundles
		Map<String, VariantSet> compositeBundleVariants = new HashMap<String, VariantSet>();
		for (Iterator<JoinableResourceBundle> it = composite.getChildBundles()
				.iterator(); it.hasNext();) {
			JoinableResourceBundle childbundle = it.next();
			if (null != childbundle.getVariants())
				compositeBundleVariants = VariantUtils.concatVariants(
						compositeBundleVariants, childbundle.getVariants());
		}
		composite.setVariants(compositeBundleVariants);
		status.setSearchingPostProcessorVariants(true);
		joinAndPostProcessBundle(composite, status, processBundle);

		Map<String, VariantSet> postProcessVariants = status
				.getPostProcessVariants();
		if (!postProcessVariants.isEmpty()) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Post process variants found for bundle "
						+ composite.getId() + ":" + postProcessVariants);
			}
			Map<String, VariantSet> newVariants = VariantUtils.concatVariants(
					composite.getVariants(), postProcessVariants);
			composite.setVariants(newVariants);
			status.setSearchingPostProcessorVariants(false);
			joinAndPostProcessBundle(composite, status, processBundle);
		}

	}

	/**
	 * Joins and post process the variant composite bundle
	 * 
	 * @param composite the composite bundle
	 * @param status the status
	 * @param compositeBundleVariants the variants
	 * @param processBundle the flag indicating if we must process the bundle or
	 *            not.
	 */
	private void joinAndPostProcessBundle(CompositeResourceBundle composite,
			BundleProcessingStatus status, boolean processBundle) {
		JoinableResourceBundleContent store;

		List<Map<String, String>> allVariants = VariantUtils
				.getAllVariants(composite.getVariants());
		// Add the default bundle variant (the non variant one)
		allVariants.add(null);
		// Process all variants
		for (Iterator<Map<String, String>> vars = allVariants.iterator(); vars
				.hasNext();) {

			Map<String, String> variants = vars.next();
			status.setBundleVariants(variants);
			store = new JoinableResourceBundleContent();
			for (Iterator<JoinableResourceBundle> it = composite
					.getChildBundles().iterator(); it.hasNext();) {
				JoinableResourceBundle childbundle = (JoinableResourceBundle) it
						.next();

				if(!childbundle.getInclusionPattern().isIncludeOnlyOnDebug()){
					JoinableResourceBundleContent childContent = joinAndPostprocessBundle(
							childbundle, variants, status, processBundle);
					// Do unitary postprocessing.
					status.setProcessingType(BundleProcessingStatus.FILE_PROCESSING_TYPE);
					StringBuffer content = executeUnitaryPostProcessing(composite,
							status, childContent.getContent(),
							this.unitaryCompositePostProcessor);
					childContent.setContent(content);
					store.append(childContent);
				}
			}

			// Post process composite bundle as needed
			store = postProcessJoinedCompositeBundle(composite,
					store.getContent(), status);

			if (processBundle) {

				String variantKey = VariantUtils.getVariantKey(variants);
				String name = VariantUtils.getVariantBundleName(
						composite.getId(), variantKey, false);
				storeBundle(name, store);
				initBundleDataHashcode(composite, store, variantKey);
			}
		}
	}

	/**
	 * Postprocess the composite bundle only if a composite bundle post
	 * processor is defined
	 * 
	 * @param composite the composite bundle
	 * @param content the content
	 * @param status the status
	 * @return the content
	 */
	private JoinableResourceBundleContent postProcessJoinedCompositeBundle(
			CompositeResourceBundle composite, StringBuffer content,
			BundleProcessingStatus status) {

		JoinableResourceBundleContent store = new JoinableResourceBundleContent();
		StringBuffer processedContent = null;
		status.setProcessingType(BundleProcessingStatus.BUNDLE_PROCESSING_TYPE);
		ResourceBundlePostProcessor bundlePostProcessor = composite
				.getBundlePostProcessor();
		if (null != bundlePostProcessor) {

			processedContent = bundlePostProcessor.postProcessBundle(status,
					content);
		} else if (null != this.compositePostProcessor) {

			processedContent = this.compositePostProcessor.postProcessBundle(
					status, content);
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
	 * @param bundle the bundle
	 * @param store the data to store
	 */
	private void initBundleDataHashcode(JoinableResourceBundle bundle,
			JoinableResourceBundleContent store, String variant) {

		String bundleHashcode = bundleHashcodeGenerator.generateHashCode(
				config, store.getContent().toString());
		bundle.setBundleDataHashCode(variant, bundleHashcode);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getTypeBundleHashcode(java.lang.String)
	 */
	public BundleHashcodeType getBundleHashcodeType(String requestedPath) {

		BundleHashcodeType typeBundleHashcode = BundleHashcodeType.UNKNOW_BUNDLE;

		String[] pathInfos = PathNormalizer
				.extractBundleInfoFromPath(requestedPath);

		if (pathInfos != null) {
			String bundlePrefix = pathInfos[0];

			String bundleId = pathInfos[1];
			String variantKey = pathInfos[2];
			String hashcode = pathInfos[3];

			JoinableResourceBundle bundle = resolveBundleForPath(bundleId);
			if (bundle != null) {
				String bundleHashcode = bundle
						.getBundleDataHashCode(variantKey);
				if (hashcode == null
						&& bundleHashcode == null
						|| hashcode != null
						&& hashcode.equals(bundleHashcode)
						&& ((bundlePrefix == null && bundle.getBundlePrefix() == null) || (bundlePrefix != null && bundlePrefix
								.equals(bundle.getBundlePrefix())))) {
					typeBundleHashcode = BundleHashcodeType.VALID_HASHCODE;
				}else{
					typeBundleHashcode = BundleHashcodeType.INVALID_HASHCODE;
				}
			}
		}
		return typeBundleHashcode;
	}

	/**
	 * Joins the members of a bundle and stores it
	 * 
	 * @param bundle the bundle
	 * @param the flag indicating if we should process the bundle or not
	 */
	private void joinAndStoreBundle(JoinableResourceBundle bundle,
			boolean processBundle) {

		if (processBundle) {

			BundleProcessingStatus status = new BundleProcessingStatus(
					BundleProcessingStatus.FILE_PROCESSING_TYPE, bundle,
					resourceHandler, config);
			JoinableResourceBundleContent store = null;

			// Process the bundle
			status.setSearchingPostProcessorVariants(true);
			joinAndPostProcessBundle(bundle, status, processBundle);

			Map<String, VariantSet> postProcessVariants = status
					.getPostProcessVariants();
			if (!postProcessVariants.isEmpty()) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Post process variants found for bundle "
							+ bundle.getId() + ":" + postProcessVariants);
				}
				Map<String, VariantSet> newVariants = VariantUtils
						.concatVariants(bundle.getVariants(),
								postProcessVariants);
				bundle.setVariants(newVariants);
				status.setSearchingPostProcessorVariants(false);
				joinAndPostProcessBundle(bundle, status, processBundle);
			}

			// Store the collected resources as a single file, both in text and
			// gzip
			// formats.
			store = joinAndPostprocessBundle(bundle, null, status,
					processBundle);
			storeBundle(bundle.getId(), store);
			// Set the data hascode in the bundle, in case the prefix needs to
			// be generated
			initBundleDataHashcode(bundle, store, null);

		}
	}

	/**
	 * Store the bundle
	 * 
	 * @param bundleId the bundle Id to store
	 * @param store the bundle
	 */
	private void storeBundle(String bundleId,
			JoinableResourceBundleContent store) {

		if (bundleMustBeProcessedInLive(store.getContent().toString())) {
			liveProcessBundles.add(bundleId);
		}
		resourceBundleHandler.storeBundle(bundleId, store);
	}

	/**
	 * Checks if the bundle must be processed in live
	 * 
	 * @param the bundle content
	 * @return true if the bundle must be processed in live
	 */
	private boolean bundleMustBeProcessedInLive(String content) {
		return content.indexOf(JawrConstant.JAWR_BUNDLE_PATH_PLACEHOLDER) != -1;
	}

	/**
	 * Join and post process the bundle taking in account all its variants.
	 * 
	 * @param bundle the bundle
	 * @param status the bundle processing status
	 * @param processBundle the flag indicating if we must process the bundle or
	 *            not
	 */
	private void joinAndPostProcessBundle(JoinableResourceBundle bundle,
			BundleProcessingStatus status, boolean processBundle) {

		JoinableResourceBundleContent store;
		List<Map<String, String>> allVariants = VariantUtils
				.getAllVariants(bundle.getVariants());
		// Add the default bundle variant (the non variant one)
		allVariants.add(null);

		for (Iterator<Map<String, String>> it = allVariants.iterator(); it
				.hasNext();) {
			Map<String, String> variantMap = it.next();
			status.setBundleVariants(variantMap);
			String variantKey = VariantUtils.getVariantKey(variantMap);
			String name = VariantUtils.getVariantBundleName(bundle.getId(),
					variantKey, false);
			store = joinAndPostprocessBundle(bundle, variantMap, status,
					processBundle);
			storeBundle(name, store);
			initBundleDataHashcode(bundle, store, variantKey);
		}
	}

	/**
	 * Reads all the members of a bundle and executes all associated
	 * postprocessors.
	 * 
	 * @param bundle the bundle
	 * @param variants the variant map
	 * @param the bundling processing status
	 * @param the flag indicating if we should process the bundle or not
	 * @return the resource bundle content, where all postprocessors have been
	 *         executed
	 */
	private JoinableResourceBundleContent joinAndPostprocessBundle(
			JoinableResourceBundle bundle, Map<String, String> variants,
			BundleProcessingStatus status, boolean processBundle) {

		JoinableResourceBundleContent bundleContent = new JoinableResourceBundleContent();

		// // Don't bother with the bundle if it is excluded because of the
		// // inclusion pattern
		// // or if we don't process the bundle at start up
		// if ((bundle.getInclusionPattern().isExcludeOnDebug() && config
		// .isDebugModeOn())
		// || (bundle.getInclusionPattern().isIncludeOnlyOnDebug() && !config
		// .isDebugModeOn()) || !processBundle)
		// return bundleContent;

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
					LOGGER.debug("Adding file [" + path + "] to bundle "
							+ bundle.getId());

				// Get a reader on the resource, with appropiate encoding
				Reader rd = null;

				try {
					rd = resourceHandler.getResource(path, true);
				} catch (ResourceNotFoundException e) {
					// If a mapped file does not exist, a warning is issued and
					// process continues normally.
					LOGGER.warn("A mapped resource was not found: [" + path
							+ "]. Please check your configuration");
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
					buffer.append(config.getLineSeparator());
				}

				// Do unitary postprocessing.
				status.setProcessingType(BundleProcessingStatus.FILE_PROCESSING_TYPE);
				bundleData.append(executeUnitaryPostProcessing(bundle, status,
						buffer, this.unitaryPostProcessor));
			}

			// Post process bundle as needed
			store = executeBundlePostProcessing(bundle, status, bundleData);

		} catch (IOException e) {
			throw new BundlingProcessException(
					"Unexpected IOException generating collected file ["
							+ bundle.getId() + "].", e);
		}

		bundleContent.setContent(store);
		return bundleContent;
	}

	/**
	 * Executes the unitary resource post processing
	 * 
	 * @param bundle the bundle
	 * @param status the bundle processing status
	 * @param content the content to process
	 * @return the processed content
	 */
	private StringBuffer executeUnitaryPostProcessing(
			JoinableResourceBundle bundle, BundleProcessingStatus status,
			StringBuffer content,
			ResourceBundlePostProcessor defaultPostProcessor) {

		StringBuffer bundleData = new StringBuffer();
		status.setProcessingType(BundleProcessingStatus.FILE_PROCESSING_TYPE);
		if (null != bundle.getUnitaryPostProcessor()) {
			StringBuffer resourceData = bundle.getUnitaryPostProcessor()
					.postProcessBundle(status, content);

			bundleData.append(resourceData);
		} else if (null != defaultPostProcessor) {
			if (LOGGER.isDebugEnabled())
				LOGGER.debug("POSTPROCESSING UNIT:" + status.getLastPathAdded());
			StringBuffer resourceData = defaultPostProcessor.postProcessBundle(
					status, content);
			bundleData.append(resourceData);
		} else {
			bundleData = content;
		}

		return bundleData;
	}

	/**
	 * Execute the bundle post processing
	 * 
	 * @param bundle the bundle
	 * @param status the status
	 * @param bundleData the bundle data
	 * @return the processed content
	 */
	private StringBuffer executeBundlePostProcessing(
			JoinableResourceBundle bundle, BundleProcessingStatus status,
			StringBuffer bundleData) {

		StringBuffer store;
		status.setProcessingType(BundleProcessingStatus.BUNDLE_PROCESSING_TYPE);
		status.setLastPathAdded(bundle.getId());
		if (null != bundle.getBundlePostProcessor())
			store = bundle.getBundlePostProcessor().postProcessBundle(status,
					bundleData);
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
	public JoinableResourceBundle resolveBundleForPath(String path) {

		JoinableResourceBundle theBundle = null;
		for (Iterator<JoinableResourceBundle> it = bundles.iterator(); it
				.hasNext();) {
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
	public ClientSideHandlerGenerator getClientSideHandler() {
		return this.clientSideHandlerGenerator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.handler.ResourceBundlesHandler#
	 * getBundleTextDirPath()
	 */
	public String getBundleTextDirPath() {
		return this.resourceBundleHandler.getBundleTextDirPath();
	}

}
