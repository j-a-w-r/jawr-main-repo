/**
 * Copyright 2009-2014 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.jawr.web.JawrConstant;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.JoinableResourceBundleImpl;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolverFactory;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.iterator.BundlePath;
import net.jawr.web.resource.bundle.iterator.ConditionalCommentCallbackHandler;
import net.jawr.web.resource.bundle.iterator.ListPathsIteratorImpl;
import net.jawr.web.resource.bundle.iterator.ResourceBundlePathsIterator;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.impl.CSSURLPathRewriterPostProcessor;
import net.jawr.web.resource.bundle.renderer.ConditionalCommentRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines a generator which will bundle all the CSS defines in
 * parameter. To use it you need to define your mapping like :
 * 
 * jawr.css.bundle.myBundle.id=/my-ie-bundle.css
 * jawr.css.bundle.myBundle.mappings=ieCssGen:/my-ie-bundle.css
 * 
 * @author Ibrahim Chaehoi
 * 
 */
public class IECssBundleGenerator extends AbstractCSSGenerator {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(IECssBundleGenerator.class);

	/** The resolver */
	private ResourceGeneratorResolver resolver;

	/**
	 * Constructor
	 */
	public IECssBundleGenerator() {
		resolver = ResourceGeneratorResolverFactory
				.createPrefixResolver(GeneratorRegistry.IE_CSS_GENERATOR_PREFIX);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.BaseResourceGenerator#getPathMatcher
	 * ()
	 */
	public ResourceGeneratorResolver getResolver() {

		return resolver;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.ResourceGenerator#createResource
	 * (net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	public Reader createResource(GeneratorContext context) {

		return generateResourceForBundle(context);
	}

	@Override
	protected Reader generateResourceForBundle(GeneratorContext context) {

		ResourceBundlesHandler bundlesHandler = (ResourceBundlesHandler) context
				.getServletContext().getAttribute(
						JawrConstant.CSS_CONTEXT_ATTRIBUTE);

		String contextPath = context.getPath();
		String bundlePath = getBundlePath(contextPath);

		Map<String, String> variants = getVariantMap(bundlesHandler,
				contextPath, bundlePath);

		String result = generateContent(context, bundlesHandler, bundlePath,
				variants);

		return new StringReader(result.toString());
	}

	/**
	 * Generates the Css content for the bundle path
	 * 
	 * @param context
	 *            the generator context
	 * @param bundlesHandler
	 *            the bundles handler
	 * @param bundlePath
	 *            the bundle path
	 * @param variants
	 *            the variants
	 * @return the generated CSS content
	 */
	private String generateContent(GeneratorContext context,
			ResourceBundlesHandler bundlesHandler, String bundlePath,
			Map<String, String> variants) {

		// Here we create a new context where the bundle name is the Jawr
		// generator CSS path
		String cssGeneratorBundlePath = PathNormalizer.concatWebPath(context
				.getConfig().getServletMapping(),
				ResourceGenerator.CSS_DEBUGPATH);

		JoinableResourceBundle tempBundle = new JoinableResourceBundleImpl(
				cssGeneratorBundlePath, null, null, "css", null, null, context
						.getConfig().getGeneratorRegistry());

		BundleProcessingStatus tempStatus = new BundleProcessingStatus(
				BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, tempBundle,
				context.getResourceReaderHandler(), context.getConfig());

		CSSURLPathRewriterPostProcessor postProcessor = new CSSURLPathRewriterPostProcessor();

		ResourceBundlePathsIterator it = null;

		StringWriter resultWriter = new StringWriter();
		StringBuffer result = resultWriter.getBuffer();
		ConditionalCommentCallbackHandler callbackHandler = new ConditionalCommentRenderer(
				resultWriter);
		if (bundlesHandler.isGlobalResourceBundle(bundlePath)) {
			it = new ListPathsIteratorImpl(new BundlePath(null, bundlePath));
			it = bundlesHandler.getGlobalResourceBundlePaths(bundlePath,
					callbackHandler, variants);
		} else {
			it = bundlesHandler.getBundlePaths(bundlePath, callbackHandler,
					variants);
		}

		while (it.hasNext()) {
			BundlePath resourcePath = it.nextPath();
			if (resourcePath != null) {

				tempStatus.setLastPathAdded(resourcePath.getPath());
				try {
					Reader cssReader = context.getResourceReaderHandler()
							.getResource(resourcePath.getPath(), true);
					StringWriter writer = new StringWriter();
					IOUtils.copy(cssReader, writer, true);
					StringBuffer resourceData = postProcessor
							.postProcessBundle(tempStatus, writer.getBuffer());
					result.append("/** CSS resource : " + resourcePath.getPath()
							+ " **/\n");
					result.append(resourceData);
					if (it.hasNext()) {
						result.append("\n\n");
					}

				} catch (ResourceNotFoundException e) {
					LOGGER.debug("The resource '" + resourcePath.getPath()
							+ "' was not found");
				} catch (IOException e) {
					throw new BundlingProcessException(e);
				}
			}
		}

		return result.toString();
	}

	/**
	 * Returns the variant map from the context path
	 * 
	 * @param bundlesHandler
	 *            the bundles handler
	 * @param contextPath
	 *            the context path
	 * @param bundlePath
	 *            the bundle path
	 * @return the variant map for the current context
	 */
	private Map<String, String> getVariantMap(
			ResourceBundlesHandler bundlesHandler, String contextPath,
			String bundlePath) {

		JoinableResourceBundle bundle = bundlesHandler
				.resolveBundleForPath(bundlePath);
		Set<String> variantTypes = bundle.getVariants().keySet();

		String variantKey = getVariantKey(contextPath);
		String[] variantValues = new String[0];
		if (variantKey.length() > 0) {
			if (variantKey.length() == 1) {
				variantValues = new String[] { "", "" };
			} else {
				variantValues = variantKey.split(String
						.valueOf(JawrConstant.VARIANT_SEPARATOR_CHAR));
			}
		}

		Map<String, String> variants = new HashMap<String, String>();
		if (variantTypes.size() != variantValues.length) {
			throw new BundlingProcessException(
					"For the resource '"
							+ contextPath
							+ "', the number variant types for the bundle don't match the variant values.");
		}
		int i = 0;
		for (Iterator<String> iterator = variantTypes.iterator(); iterator
				.hasNext();) {
			String variantType = iterator.next();
			variants.put(variantType, variantValues[i++]);
		}
		return variants;
	}

	/**
	 * Returns the IE Css bundle path from the context path
	 * 
	 * @param contextPath
	 *            the context path
	 * @return the IE Css bundle path
	 */
	private String getBundlePath(String contextPath) {
		String bundlePath = contextPath;
		int idx = -1;
		if (bundlePath.startsWith(JawrConstant.URL_SEPARATOR)) {
			idx = bundlePath.indexOf(JawrConstant.URL_SEPARATOR, 1);
		} else {
			idx = bundlePath.indexOf(JawrConstant.URL_SEPARATOR, 1);
		}

		if (idx != -1) {
			bundlePath = bundlePath.substring(idx);
		}
		return bundlePath;
	}

	/**
	 * Returns the variant key from the context path.
	 * 
	 * @param contextPath
	 *            the path
	 * @return the variant key
	 */
	private String getVariantKey(String contextPath) {

		// Remove first slash
		String resultPath = contextPath.substring(1);
		String variantKey = "";
		// eval the existence of a suffix
		String prefix = resultPath.substring(0,
				resultPath.indexOf(JawrConstant.URL_SEPARATOR));

		// The prefix also contains variant information after a '.'
		if (prefix.indexOf('.') != -1) {
			variantKey = prefix.substring(prefix.indexOf('.') + 1);
		}

		return variantKey.trim();
	}

}
