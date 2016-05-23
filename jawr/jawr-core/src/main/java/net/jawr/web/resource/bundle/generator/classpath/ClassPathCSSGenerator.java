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
package net.jawr.web.resource.bundle.generator.classpath;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Set;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.JoinableResourceBundleImpl;
import net.jawr.web.resource.bundle.generator.AbstractCSSGenerator;
import net.jawr.web.resource.bundle.generator.CachedGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.ResourceGenerator;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolverFactory;
import net.jawr.web.resource.bundle.mappings.FilePathMapping;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.impl.CSSURLPathRewriterPostProcessor;
import net.jawr.web.resource.handler.reader.ResourceBrowser;
import net.jawr.web.util.StringUtils;

/**
 * This class defines the generator for the CSS defined in the classpath.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
@CachedGenerator(name = "Classpath CSS", cacheDirectory = "cssClasspath", mappingFileName = "cssClasspathMapping.txt")
public class ClassPathCSSGenerator extends AbstractCSSGenerator
		implements ResourceBrowser {

	/** the class path generator helper */
	private static final String CLASSPATH_GENERATOR_HELPER_PREFIX = "";

	/** The resolver */
	protected ResourceGeneratorResolver resolver;

	/** The classpath generator helper */
	private ClassPathGeneratorHelper helper;

	/**
	 * The flag indicating if the generator is handling the Css Image ressources
	 */
	private boolean isHandlingCssImage;

	/**
	 * Constructor
	 */
	public ClassPathCSSGenerator() {
		helper = new ClassPathGeneratorHelper(getClassPathGeneratorHelperPrefix());
		resolver = createResolver(getGeneratorPrefix());
	}

	/**
	 * create the resource generator resolver
	 *
	 * @param generatorPrefix
	 *            the generator prefix
	 * @return the resource generator resolver
	 */
	protected ResourceGeneratorResolver createResolver(String generatorPrefix) {
		return ResourceGeneratorResolverFactory.createPrefixResolver(generatorPrefix);
	}

	/**
	 * Returns the class path generator helper
	 * 
	 * @return the class path generator helper
	 */
	protected String getClassPathGeneratorHelperPrefix() {
		return CLASSPATH_GENERATOR_HELPER_PREFIX;
	}

	/**
	 * Returns the generator prefix
	 * 
	 * @return the generator prefix
	 */
	protected String getGeneratorPrefix() {
		return GeneratorRegistry.CLASSPATH_RESOURCE_BUNDLE_PREFIX;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.AbstractCachedGenerator#isCacheValid()
	 */
	@Override
	protected boolean isCacheValid() {
		
		boolean isValid = false;
		String servletMapping = config.getServletMapping();
		boolean isHandlingCssCPImage = config.isCssClasspathImageHandledByClasspathCss();
		if(StringUtils.equals(servletMapping, cacheProperties.getProperty("jawr.servlet.mapping"))
				&& StringUtils.equals(cacheProperties.getProperty(JawrConfig.JAWR_CSS_CLASSPATH_HANDLE_IMAGE), Boolean.toString(isHandlingCssCPImage))){
			isValid = false;
		}
		
		return isValid;
	}
	
	

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.AbstractCachedGenerator#resetCache()
	 */
	@Override
	protected void resetCache() {
		super.resetCache();
		cacheProperties.put("jawr.servlet.mapping", config.getServletMapping());
		cacheProperties.put(JawrConfig.JAWR_CSS_CLASSPATH_HANDLE_IMAGE, Boolean.toString(isHandlingCssImage));
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.AbstractCachedGenerator#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		this.isHandlingCssImage = config.isCssClasspathImageHandledByClasspathCss();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.BaseResourceGenerator#
	 * getPathMatcher ()
	 */
	public ResourceGeneratorResolver getResolver() {

		return resolver;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.CssResourceGenerator#
	 * isHandlingCssImage()
	 */
	public boolean isHandlingCssImage() {
		return isHandlingCssImage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.AbstractCachedGenerator#
	 * generateResource(net.jawr.web.resource.bundle.generator.GeneratorContext,
	 * java.lang.String)
	 */
	@Override
	protected Reader generateResource(String path, GeneratorContext context) {

		Reader reader = helper.createResource(context);
		
		String filePath = helper.getFilePath(path);
		if(filePath != null){
			long lastModified = rsHandler.getLastModified(filePath);
			FilePathMapping fMapping = new FilePathMapping(filePath, lastModified);
			addLinkedResources(path, context, Arrays.asList(fMapping));
		}
		
		return reader;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.AbstractCSSGenerator#rewriteUrl(
	 * net.jawr.web.resource.bundle.generator.GeneratorContext,
	 * java.lang.String)
	 */
	@Override
	protected String rewriteUrl(GeneratorContext context, String content) throws IOException {

		// Here we create a new context where the bundle name is the Jawr
		// generator CSS path
		// The version of the CSS classpath for debug mode will be different
		// compare to the standard one
		JoinableResourceBundle tempBundle = new JoinableResourceBundleImpl(ResourceGenerator.CSS_DEBUGPATH, null, null,
				null, null, null, context.getConfig().getGeneratorRegistry());
		BundleProcessingStatus tempStatus = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE,
				tempBundle, context.getResourceReaderHandler(), context.getConfig());

		CSSURLPathRewriterPostProcessor postProcessor = new CSSURLPathRewriterPostProcessor();
		String resourcePath = context.getPath();
		tempStatus.setLastPathAdded(getGeneratorPrefix() + GeneratorRegistry.PREFIX_SEPARATOR + resourcePath);
		StringBuffer resourceData = postProcessor.postProcessBundle(tempStatus, new StringBuffer(content));
		return resourceData.toString();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.AbstractCSSGenerator#
	 * generateResourceForDebug
	 * (net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	@Override
	protected Reader generateResourceForDebug(Reader rd, GeneratorContext context) {

		// The following section is executed in DEBUG mode to retrieve the
		// classpath CSS from the temporary folder,
		// if the user defines that the image servlet should be used to retrieve
		// the CSS images.
		// It's not executed at the initialization process to be able to read
		// data from classpath.
		if (context.getConfig().isCssClasspathImageHandledByClasspathCss()) {
			rd = super.generateResourceForDebug(rd, context);
		}

		return rd;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.handler.reader.ResourceBrowser#getResourceNames(
	 * java.lang.String)
	 */
	@Override
	public Set<String> getResourceNames(String path) {
		return helper.getResourceNames(resolver.getResourcePath(path));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.handler.reader.ResourceBrowser#isDirectory(java.
	 * lang.String)
	 */
	@Override
	public boolean isDirectory(String path) {
		return helper.isDirectory(path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.handler.reader.ResourceBrowser#getFilePath(java.
	 * lang.String)
	 */
	@Override
	public String getFilePath(String resourcePath) {
		return helper.getFilePath(resolver.getResourcePath(resourcePath));
	}

}
