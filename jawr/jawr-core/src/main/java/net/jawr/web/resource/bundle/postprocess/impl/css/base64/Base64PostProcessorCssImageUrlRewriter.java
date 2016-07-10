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
package net.jawr.web.resource.bundle.postprocess.impl.css.base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jawr.web.JawrConstant;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.BinaryResourcesHandler;
import net.jawr.web.resource.FileNameUtils;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.factory.util.RegexUtil;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.impl.PostProcessorCssImageUrlRewriter;
import net.jawr.web.servlet.util.MIMETypesSupport;
import net.jawr.web.util.Base64Encoder;
import net.jawr.web.util.StringUtils;

/**
 * This class defines the image URL rewriter for the base64 image post
 * processor. This postprocessor will apply the standard URL rewriting process
 * if the image URL is annotated as "jawr:base64-skip" or if the brwoser is IE7,
 * because the MHTML is not properly supported with IE7 on Windows Vista and
 * Windows 7 Please check the following link for more info.
 * 
 * http://www.phpied.com/data-uris-mhtml-ie7-win7-vista-blues/#vista
 * 
 * @author Ibrahim Chaehoi
 */
public class Base64PostProcessorCssImageUrlRewriter extends PostProcessorCssImageUrlRewriter {

	/** The logger */
	private final Logger LOGGER = LoggerFactory.getLogger(Base64PostProcessorCssImageUrlRewriter.class);

	/** The data prefix */
	private static final String DATA_PREFIX = "data:";

	/** The mhtml prefix */
	private static final String MHTML_PREFIX = "mhtml:";

	/**
	 * The annotation to skip or force the base64 encoding (jawr:base64 or
	 * jawr:base64-skip )
	 */
	private static final Pattern ANNOTATION_BASE64_PATTERN = Pattern.compile("jawr(?:\\s)*:(?:\\s)*(base64)(-skip)?");

	/**
	 * The annotation for sprite image (sprite:sprite)
	 */
	private static final Pattern ANNOTATION_SPRITE_PATTERN = Pattern.compile("sprite(?:\\s)*:(?:\\s)*?");

	/** The annotation group in the URL pattern */
	private static final int ANNOTATION_GROUP = 9;

	/** The url pattern */
	private static final Pattern URL_WITH_ANNOTATION_PATTERN = Pattern.compile(
			"((" + URL_REGEXP + "\\s*)+)" + "([^;]*);?" + "\\s*(/\\*\\*(?:.|[\\n\\r])*?\\*/)?", // Any
																								// number
																								// of
			// whitespaces and then
			// an annotation

			Pattern.CASE_INSENSITIVE); // works with 'URL('

	/** The default max file size */
	private static final int MAX_LENGTH_FILE = 30000;

	/** The current browser */
	private final String browser;

	/** The maximum image file size authorized to be encoded in base64 */
	private int maxFileSize;

	/** The map of encoded resources */
	private Map<String, Base64EncodedResource> encodedResources = null;

	/** The flag which determine if we must encode by default or not */
	private final boolean encodeByDefault;

	/** The flag indicating if we must encode the sprites or not */
	private final boolean encodeSprite;

	/** The flag indicating if we must skip the base64 encoding */
	private boolean skipBase64Encoding;

	/**
	 * Constructor
	 * 
	 * @param status
	 *            the bundle processing status
	 */
	@SuppressWarnings("unchecked")
	public Base64PostProcessorCssImageUrlRewriter(BundleProcessingStatus status) {
		super(status);
		this.browser = status.getVariant(JawrConstant.BROWSER_VARIANT_TYPE);

		encodedResources = (Map<String, Base64EncodedResource>) status.getData(JawrConstant.BASE64_ENCODED_RESOURCES);
		maxFileSize = MAX_LENGTH_FILE;
		Properties configProperties = status.getJawrConfig().getConfigProperties();
		String maxLengthProperty = (String) configProperties.get(JawrConstant.BASE64_MAX_IMG_FILE_SIZE);

		if (StringUtils.isNotEmpty(maxLengthProperty)) {
			maxFileSize = Integer.parseInt(maxLengthProperty);
		}

		String strEncodeByDefault = (String) configProperties.get(JawrConstant.BASE64_ENCODE_BY_DEFAULT);
		encodeByDefault = getBooleanValue(strEncodeByDefault, true);

		String strEncodeSprite = (String) configProperties.get(JawrConstant.BASE64_ENCODE_SPRITE);
		encodeSprite = getBooleanValue(strEncodeSprite, false);

		LOGGER.debug("max file length: " + maxFileSize);
	}

	/**
	 * Returns the boolean value of the string passed in parameter or the
	 * default value if the string is null
	 * 
	 * @param strVal
	 *            the string value
	 * @param defaultValue
	 *            the default value
	 * @return the boolean value of the string passed in parameter or the
	 *         default value if the string is null
	 */
	private boolean getBooleanValue(String strVal, boolean defaultValue) {

		boolean result = defaultValue;
		if (strVal != null) {
			result = Boolean.parseBoolean(strVal);
		}

		return result;
	}

	/**
	 * Rewrites the image URL
	 * 
	 * @param originalCssPath
	 *            the original CSS path
	 * @param newCssPath
	 *            the new CSS path
	 * @param originalCssContent
	 *            the original CSS content
	 * @return the new CSS content with image path rewritten
	 * @throws IOException
	 */
	@Override
	public StringBuffer rewriteUrl(String originalCssPath, String newCssPath, String originalCssContent)
			throws IOException {

		// Rewrite each css image url path
		Matcher matcher = URL_WITH_ANNOTATION_PATTERN.matcher(originalCssContent);
		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {

			String annotation = matcher.group(ANNOTATION_GROUP);
			if (StringUtils.isNotEmpty(annotation)) {
				Matcher annotationMatcher = ANNOTATION_BASE64_PATTERN.matcher(annotation);
				if (annotationMatcher.find()) {
					skipBase64Encoding = annotationMatcher.group(2) != null;
				} else {
					annotationMatcher = ANNOTATION_SPRITE_PATTERN.matcher(annotation);
					if (annotationMatcher.find()) { // Encode sprite depending
													// on jawr configuration
						skipBase64Encoding = !encodeSprite;
					}
				}
			} else {
				// If no annotation use the default encoding mode
				skipBase64Encoding = !encodeByDefault;
			}

			StringBuffer sbUrl = new StringBuffer();
			Matcher urlMatcher = URL_PATTERN.matcher(matcher.group());
			while (urlMatcher.find()) {

				String url = urlMatcher.group();

				// Skip sprite encoding if it is configured so
				if (!encodeSprite && url
						.contains(GeneratorRegistry.SPRITE_GENERATOR_PREFIX + GeneratorRegistry.PREFIX_SEPARATOR)) {
					skipBase64Encoding = true;
				}

				if (LOGGER.isDebugEnabled() && skipBase64Encoding) {
					LOGGER.debug("Skip encoding image resource : " + url);
				}

				url = getUrlPath(url, originalCssPath, newCssPath);

				urlMatcher.appendReplacement(sbUrl, RegexUtil.adaptReplacementToMatcher(url));
			}
			urlMatcher.appendTail(sbUrl);
			matcher.appendReplacement(sb, RegexUtil.adaptReplacementToMatcher(sbUrl.toString()));

		}
		matcher.appendTail(sb);

		return sb;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.postprocess.impl.
	 * PostProcessorCssImageUrlRewriter#rewriteURL(net.jawr.web.resource.bundle.
	 * postprocess.BundleProcessingStatus, java.lang.String, java.lang.String,
	 * java.lang.String, net.jawr.web.resource.BinaryResourcesHandler)
	 */
	@Override
	protected String rewriteURL(String url, String imgServletPath, String newCssPath,
			BinaryResourcesHandler binaryRsHandler) throws IOException {

		String imgUrl = url;

		if (skipBase64Encoding) { // Skip base64 encoding if it has ben
									// deactivated
			imgUrl = super.rewriteURL(imgUrl, imgServletPath, newCssPath, binaryRsHandler);
		} else {

			LOGGER.info("Encoding resource: " + url);
			try {
				InputStream is = binaryRsHandler.getRsReaderHandler().getResourceAsStream(url);

				String fileExtension = FileNameUtils.getExtension(url);
				String fileMimeType = (String) MIMETypesSupport.getSupportedProperties(this).get(fileExtension);
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				IOUtils.copy(is, out, true);

				int size = out.size();
				if (size > maxFileSize) { // Check file size

					LOGGER.warn("File content length of '" + url + "' exceeds maximum file length: " + size + " > "
							+ maxFileSize);
				} else {

					byte[] data = out.toByteArray();
					StringBuffer s = new StringBuffer(encodeInBase64(data));

					Base64EncodedResource encodedImage = new Base64EncodedResource();
					encodedImage.setId(url.hashCode());
					encodedImage.setType(fileMimeType);
					encodedImage.setBase64Encoding(s);

					encodedResources.put(encodedImage.getId(), encodedImage);

					// For IE under IE8, use MHTML
					if (JawrConstant.BROWSER_IE6.equals(browser) || JawrConstant.BROWSER_IE7.equals(browser)) {

						/**
						 * For Internet Explorer 6 and 7, the url must be mhtml:
						 * followed by an absolute url. However, this URL is not
						 * known at post process time. So we make add a place
						 * holder which will be resolved at runtime.
						 */
						imgUrl = MHTML_PREFIX + JawrConstant.JAWR_BUNDLE_PATH_PLACEHOLDER + "!" + encodedImage.getId();

					} else {
						imgUrl = DATA_PREFIX + fileMimeType + ";base64," + s;
					}
				}
			} catch (IOException e) {
				LOGGER.error("Unable to rewrite image URL", e);

			} catch (ResourceNotFoundException e) {
				LOGGER.error("The resource '" + e.getRequestedPath() + "' has not been found.");
			} catch (Throwable e) {
				LOGGER.error("Unable to rewrite image URL", e);
			}
		}

		return imgUrl;
	}

	/**
	 * Encodes the data in base64
	 * 
	 * @param data
	 *            the byte array of data to encode
	 * @return the base64 encoded string of the data
	 */
	private String encodeInBase64(byte[] data) {
		return new String(Base64Encoder.encode(data));
	}
}
