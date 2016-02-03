/**
 * Copyright 2010-2016 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.postprocess.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.BinaryResourcesHandler;
import net.jawr.web.resource.FileNameUtils;
import net.jawr.web.resource.bundle.CheckSumUtils;
import net.jawr.web.resource.bundle.css.CssImageUrlRewriter;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.mappings.FilePathMappingUtils;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.servlet.util.MIMETypesSupport;
import net.jawr.web.util.StringUtils;

/**
 * This class defines the URL rewriter for the Css post processor
 * 
 * @author Ibrahim Chaehoi
 */
public class PostProcessorCssImageUrlRewriter extends CssImageUrlRewriter {

	/** Logger */
	private static Logger LOGGER = LoggerFactory
			.getLogger(PostProcessorCssImageUrlRewriter.class);

	/** The bundle processing status */
	protected BundleProcessingStatus status;

	/**
	 * Constructor
	 * 
	 * @param status
	 *            the bundle processing status
	 */
	public PostProcessorCssImageUrlRewriter(BundleProcessingStatus status) {
		super(status.getJawrConfig());
		this.status = status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.css.CssImageUrlRewriter#getRewrittenImagePath
	 * (java.lang.String, java.lang.String, java.lang.String)
	 */
	protected String getRewrittenImagePath(String originalCssPath,
			String newCssPath, String url) throws IOException {

		JawrConfig jawrConfig = status.getJawrConfig();

		BinaryResourcesHandler binaryRsHandler = (BinaryResourcesHandler) jawrConfig
				.getContext().getAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE);
		String binaryServletPath = "";

		if (binaryRsHandler != null) {
			binaryServletPath = PathNormalizer.asPath(binaryRsHandler.getConfig()
					.getServletMapping());
		}

		String imgUrl = null;

		// Retrieve the current CSS file from which the CSS image is referenced
		String currentCss = originalCssPath;
		boolean generatedImg = false;
		if (binaryRsHandler != null) {
			GeneratorRegistry imgRsGeneratorRegistry = binaryRsHandler.getConfig()
					.getGeneratorRegistry();
			generatedImg = imgRsGeneratorRegistry.isGeneratedBinaryResource(url);
		}

		boolean cssGeneratorIsHandleCssImage = isCssGeneratorHandlingCssImage(
				currentCss, status);

		String rootPath = currentCss;

		// If the CSS image is taken from the classpath, add the classpath cache
		// prefix
		if (generatedImg || cssGeneratorIsHandleCssImage) {

			String tempUrl = url;

			// If it's a classpath CSS, the url of the CSS image is defined
			// relatively to it.
			if (cssGeneratorIsHandleCssImage && !generatedImg) {
				tempUrl = PathNormalizer.concatWebPath(rootPath, url);
			}

			// generate image cache URL
			imgUrl = rewriteURL(status, tempUrl, binaryServletPath, newCssPath,
					binaryRsHandler);
		} else {

			if (jawrConfig.getGeneratorRegistry().isPathGenerated(rootPath)) {
				rootPath = rootPath.substring(rootPath
						.indexOf(GeneratorRegistry.PREFIX_SEPARATOR) + 1);
			}

			// Generate the image URL from the current CSS path
			imgUrl = PathNormalizer.concatWebPath(rootPath, url);
			imgUrl = rewriteURL(status, imgUrl, binaryServletPath, newCssPath,
					binaryRsHandler);
		}

		// This following condition should never be true.
		// If it does, it means that the image path is wrongly defined.
		if (imgUrl == null) {
			LOGGER.error("The CSS image path for '"
					+ url
					+ "' defined in '"
					+ currentCss
					+ "' is out of the application context. Please check your CSS file.");
		}

		return imgUrl;
	}

	/**
	 * Checks if the Css generator associated to the Css resource path handle
	 * also the Css image resources.
	 * 
	 * @param currentCss
	 *            the CSS resource path
	 * @param status
	 *            the status
	 * @return true if the Css generator associated to the Css resource path
	 *         handle also the Css image resources.
	 */
	private boolean isCssGeneratorHandlingCssImage(String currentCss,
			BundleProcessingStatus status) {
		return status.getJawrConfig().getGeneratorRegistry()
				.isHandlingCssImage(currentCss);
	}

	/**
	 * Rewrites the image URL
	 * 
	 * @param status
	 *            the bundle processing status
	 * @param url
	 *            the image URL
	 * @param binaryServletPath
	 *            the binary servlet path
	 * @param newCssPath
	 *            the new Css path
	 * @param binaryRsHandler
	 *            the image resource handler
	 * @return the rewritten image URL
	 * @throws IOException
	 *             if an IOException occurs
	 */
	protected String rewriteURL(BundleProcessingStatus status, String url,
			String binaryServletPath, String newCssPath,
			BinaryResourcesHandler binaryRsHandler) throws IOException {

		String imgUrl = url;
		if (isBinaryResource(imgUrl)) {
			imgUrl = addCacheBuster(status, url, binaryRsHandler);
			// Add image servlet path in the URL, if it's defined
			if (StringUtils.isNotEmpty(binaryServletPath)) {
				imgUrl = binaryServletPath + JawrConstant.URL_SEPARATOR + imgUrl;
			}
		}

		imgUrl = PathNormalizer.asPath(imgUrl);
		return PathNormalizer.getRelativeWebPath(
				PathNormalizer.getParentPath(newCssPath), imgUrl);
	}

	/**
	 * Checks if the resource is an binary resource
	 * 
	 * @param resourcePath
	 *            the resourcePath
	 * @return true if the resource is an binary resource
	 */
	protected boolean isBinaryResource(String resourcePath) {
		String extension = FileNameUtils.getExtension(resourcePath);
		if (extension != null) {
			extension = extension.toLowerCase();
		}
		return MIMETypesSupport.getSupportedProperties(this).containsKey(
				extension);
	}

	/**
	 * Adds the cache buster to the CSS image
	 * 
	 * @param status
	 *            the bundle processing status
	 * @param url
	 *            the URL of the image
	 * @param binaryRsHandler
	 *            the binary resource handler
	 * @return the url of the CSS image with a cache buster
	 * @throws IOException
	 *             if an IO exception occurs
	 */
	@SuppressWarnings("unchecked")
	private String addCacheBuster(BundleProcessingStatus status, String url,
			BinaryResourcesHandler binaryRsHandler) throws IOException {

		if(binaryRsHandler != null){
			FilePathMappingUtils.addLinkedFilePathMapping(status.getCurrentBundle(), url, binaryRsHandler.getRsReaderHandler());
		}
		
		// Try to retrieve the cache busted URL from the bundle processing cache
		Map<String, String> imageMapping = (Map<String, String>) status
				.getData(JawrConstant.POST_PROCESSING_CTX_JAWR_BINARY_MAPPING);
		String newUrl = null;
		if (imageMapping != null) {
			newUrl = imageMapping.get(url);
			if (newUrl != null) {
				return newUrl;
			}
		}
		
		// Try to retrieve the from the image resource handler cache
		if (binaryRsHandler != null) {
			newUrl = binaryRsHandler.getCacheUrl(url);
			if (newUrl != null) {
				return newUrl;
			}
			// Retrieve the new URL with the cache prefix
			try {
				newUrl = CheckSumUtils.getCacheBustedUrl(url,
						binaryRsHandler.getRsReaderHandler(),
						binaryRsHandler.getConfig());
			} catch (ResourceNotFoundException e) {
				LOGGER.info("Impossible to define the checksum for the resource '"
						+ url + "'. ");
				return url;
			} catch (IOException e) {
				LOGGER.info("Impossible to define the checksum for the resource '"
						+ url + "'.");
				return url;
			}

			binaryRsHandler.addMapping(url, newUrl);

		} else {
			newUrl = url;
		}

		// Set the result in a cache, so we will not search for it the next time
		if (imageMapping == null) {
			imageMapping = new HashMap<String, String>();
			status.putData(JawrConstant.POST_PROCESSING_CTX_JAWR_BINARY_MAPPING,
					imageMapping);
		}
		imageMapping.put(url, newUrl);

		return newUrl;
	}

}
