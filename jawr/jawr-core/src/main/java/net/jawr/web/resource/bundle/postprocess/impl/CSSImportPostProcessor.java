/**
 * Copyright 2009-2015 Ibrahim Chaehoi
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
import java.io.Reader;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.BinaryResourcesHandler;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.css.CssImageUrlRewriter;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.factory.util.RegexUtil;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.ResourceGenerator;
import net.jawr.web.resource.bundle.generator.resolver.SuffixedPathResolver;
import net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.PostProcessFactoryConstant;
import net.jawr.web.util.StringUtils;

/**
 * This class defines the Post processor which handle the inclusion of the CSS define with @import statement
 * 
 * @author Ibrahim Chaehoi
 * 
 */
public class CSSImportPostProcessor extends
		AbstractChainedResourceBundlePostProcessor {

	/**The Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(CSSImportPostProcessor.class);
	
	/** The url pattern */
	private static final Pattern IMPORT_PATTERN = Pattern.compile(	"@import\\s*url\\(\\s*" // 'url(' and any number of whitespaces 
																+ "[\"']?([^\"')]*)[\"']?" // any sequence of characters, except an unescaped ')'
																+ "\\s*\\)\\s*(\\w+)?\\s*;?",  // Any number of whitespaces, then ')'
																Pattern.CASE_INSENSITIVE); // works with 'URL('
	
	/**
	 * Constructor
	 * @param id the Id of the post processor
	 */
	public CSSImportPostProcessor() {
		super(PostProcessFactoryConstant.CSS_IMPORT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.postprocess.AbstractChainedResourceBundlePostProcessor#doPostProcessBundle(net.jawr.web.resource.bundle.postprocess
	 * .BundleProcessingStatus, java.lang.StringBuffer)
	 */
	protected StringBuffer doPostProcessBundle(BundleProcessingStatus status,
			StringBuffer bundleData) throws IOException {

		String data = bundleData.toString();
		
		// Rewrite each css url path
		Matcher matcher = IMPORT_PATTERN.matcher(data);
		StringBuffer sb = new StringBuffer();
		while(matcher.find()) {
		
			String content = getCssPathContent(matcher.group(1), matcher.group(2), status);
			matcher.appendReplacement(sb, RegexUtil.adaptReplacementToMatcher(content));
		}
		matcher.appendTail(sb);
		return sb;
		
	}

	/**
	 * Retrieve the content of the css to import
	 * @param cssPathToImport the path of the css to import
	 * @param media the media
	 * @param status the bundle processing status
	 * @return the content of the css to import
	 * @throws IOException if an IOException occurs
	 */
	private String getCssPathContent(String cssPathToImport, String media, BundleProcessingStatus status) throws IOException {
		
		String currentCssPath = status.getLastPathAdded();
		
		String path = cssPathToImport;

		JawrConfig jawrConfig = status.getJawrConfig();
		
		if(cssPathToImport.startsWith("http://") || cssPathToImport.startsWith("https://") || cssPathToImport.startsWith("//")){
			LOGGER.warn("In the CSS '"+currentCssPath+"', the HTTP URL '"+cssPathToImport+"' is not handled by Jawr.");
			return "";
		}
		
		if(jawrConfig.getGeneratorRegistry().isPathGenerated(path)){
			
			ResourceGenerator generator =  jawrConfig.getGeneratorRegistry().getResourceGenerator(path);
			if(generator != null && generator.getResolver() instanceof SuffixedPathResolver){
				path = PathNormalizer.concatWebPath(currentCssPath, cssPathToImport);
			}
		}else if(!cssPathToImport.startsWith("/")) { // relative URL
			path = PathNormalizer.concatWebPath(currentCssPath, cssPathToImport);
		}
		
		Reader reader = null;
		
		try {
			reader = status.getRsReader().getResource(path, true);
		} catch (ResourceNotFoundException e) {
			throw new IOException("Css to import '"+path+"' was not found", e);
		}
		
		StringWriter content = new StringWriter();
		IOUtils.copy(reader, content, true);
		
		BinaryResourcesHandler binaryRsHandler = (BinaryResourcesHandler) jawrConfig.getContext().getAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE);
		if(binaryRsHandler != null){
			jawrConfig = binaryRsHandler.getConfig();
		}
		// Rewrite image URL
		CssImportedUrlRewriter urlRewriter = new CssImportedUrlRewriter(jawrConfig);
		StringBuffer result = new StringBuffer();
		boolean isMediaAttributeSet = StringUtils.isNotEmpty(media);
		if(isMediaAttributeSet){
			result.append("@media "+media+" {\n");
		}
		result.append(urlRewriter.rewriteUrl(path, currentCssPath, content.getBuffer().toString()));
		if(isMediaAttributeSet){
			result.append("\n}\n");
		}
		return result.toString();
	}

	/**
	 * This class rewrite the image URL for the imported CSS.
	 * 
	 * @author Ibrahim Chaehoi
	 */
	private static class CssImportedUrlRewriter extends CssImageUrlRewriter {
		
		/** The generator registry */
		private GeneratorRegistry generatorRegistry;
		
		/**
		 * Constructor
		 * @param generatorRegistry the generator registry
		 */
		public CssImportedUrlRewriter(JawrConfig jawrConfig) {

			super(jawrConfig);
			this.generatorRegistry = jawrConfig.getGeneratorRegistry();
		}

		
		/* (non-Javadoc)
		 * @see net.jawr.web.resource.bundle.css.CssImageUrlRewriter#getRewrittenImagePath(java.lang.String, java.lang.String, java.lang.String)
		 */
		protected String getRewrittenImagePath(String originalCssPath,
				String newCssPath, String url) throws IOException {
			
			String currentPath = originalCssPath;
			
			String imgPath = PathNormalizer.concatWebPath(currentPath, url);
			if(!generatorRegistry.isGeneratedBinaryResource(imgPath) && !generatorRegistry.isHandlingCssImage(originalCssPath)){
				imgPath = PathNormalizer.getRelativeWebPath(PathNormalizer
						.getParentPath(newCssPath), imgPath);
			}
			
			return imgPath;
		}
	}
}
