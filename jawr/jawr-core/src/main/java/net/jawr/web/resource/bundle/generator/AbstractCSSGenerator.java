/**
 * Copyright 2008-2016 Jordi Hernández Sellés, Ibrahim Chaehoi
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

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.css.CssImageUrlRewriter;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;

/**
 * Abstract implementation of ResourceGenerator with a default return value for
 * the getMappingPrefix method.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public abstract class AbstractCSSGenerator extends AbstractCachedGenerator
		implements SpecificCDNDebugPathResourceGenerator, CssResourceGenerator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.BaseResourceGenerator#
	 * getDebugModeRequestPath()
	 */
	public String getDebugModeRequestPath() {
		return ResourceGenerator.CSS_DEBUGPATH;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.ResourceGenerator#
	 * getDebugModeBuildTimeGenerationPath(java.lang.String)
	 */
	public String getDebugModeBuildTimeGenerationPath(String path) {
		return path.replaceFirst(GeneratorRegistry.PREFIX_SEPARATOR, JawrConstant.URL_SEPARATOR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.TextResourceGenerator#
	 * createResource(net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	@Override
	public Reader createResource(GeneratorContext context) {

		Reader rd = super.createResource(context);
		if (!context.isProcessingBundle()) {
			rd = generateResourceForDebug(rd, context);
		}

		return rd;
	}

	/**
	 * Returns the resource in debug mode. Here an extra step is used to rewrite
	 * the URL in debug mode
	 * 
	 * @param reader
	 *            the reader
	 * @param context
	 *            the generator context
	 * @return the reader
	 */
	protected Reader generateResourceForDebug(Reader rd, GeneratorContext context) {

		// Rewrite the image URL
		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(rd, writer);
			String content = rewriteUrl(context, writer.toString());
			rd = new StringReader(content);
		} catch (IOException e) {
			throw new BundlingProcessException(e);
		}

		return rd;
	}

	/**
	 * Rewrite the URL for debug mode
	 * 
	 * @param context
	 *            the generator context
	 * @param content
	 *            the content
	 * @return the rewritten content
	 * @throws IOException
	 *             if IOException occurs
	 */
	protected String rewriteUrl(GeneratorContext context, String content) throws IOException {
		
		JawrConfig jawrConfig = context.getConfig();
		CssImageUrlRewriter rewriter = new CssImageUrlRewriter(jawrConfig);
		String bundlePath = PathNormalizer.joinPaths(jawrConfig.getServletMapping(), ResourceGenerator.CSS_DEBUGPATH);

		StringBuffer result = rewriter.rewriteUrl(context.getPath(), bundlePath, content);
		return result.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.CssResourceGenerator#
	 * isHandlingCssImage()
	 */
	public boolean isHandlingCssImage() {
		return false;
	}

}
