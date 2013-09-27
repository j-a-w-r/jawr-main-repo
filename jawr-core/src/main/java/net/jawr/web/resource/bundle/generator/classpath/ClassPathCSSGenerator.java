/**
 * Copyright 2008-2012 Jordi Hernández Sellés, Ibrahim Chaehoi
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.JoinableResourceBundleImpl;
import net.jawr.web.resource.bundle.generator.AbstractCSSGenerator;
import net.jawr.web.resource.bundle.generator.ConfigurationAwareResourceGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.ResourceGenerator;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolverFactory;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.impl.CSSURLPathRewriterPostProcessor;
import net.jawr.web.resource.handler.reader.WorkingDirectoryLocationAware;

/**
 * This class defines the generator for the CSS defined in the classpath.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class ClassPathCSSGenerator extends AbstractCSSGenerator implements ConfigurationAwareResourceGenerator, WorkingDirectoryLocationAware {
	
	/** The name of the directory which contain the CSS defined in classpath for the DEBUG mode */
	private static final String TEMP_CSS_CLASSPATH_SUBDIR = "cssClasspath";

	/** The resolver */
	private ResourceGeneratorResolver resolver;
	
	/** The classpath generator helper */
	private ClassPathGeneratorHelper helper;
	
	/** The working directory */
	private String workingDir;
	
	/** The flag indicating if the generator is handling the Css Image ressources */
	private boolean isHandlingCssImage;
	
	/**
	 * Constructor 
	 */
	public ClassPathCSSGenerator() {
		helper = new ClassPathGeneratorHelper();
		resolver = ResourceGeneratorResolverFactory.createPrefixResolver(GeneratorRegistry.CLASSPATH_RESOURCE_BUNDLE_PREFIX);
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.ConfigurationAwareResourceGenerator#setConfig(net.jawr.web.config.JawrConfig)
	 */
	public void setConfig(JawrConfig config) {
		this.isHandlingCssImage = config.isCssClasspathImageHandledByClasspathCss();
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.BaseResourceGenerator#getPathMatcher()
	 */
	public ResourceGeneratorResolver getResolver() {
		
		return resolver;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.CssResourceGenerator#isHandlingCssImage()
	 */
	public boolean isHandlingCssImage() {
		return isHandlingCssImage;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.TemporaryResourceLocationAware#setTemporaryDirectory(java.lang.String)
	 */
	public void setWorkingDirectory(String workingDir) {
		this.workingDir = workingDir;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.ResourceGenerator#createResource(net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	@Override
	protected Reader generateResourceForBundle(GeneratorContext context) {
		
		Reader reader = helper.createResource(context);
		if(reader != null){
			reader = createTempResource(context, reader);
		}
		
		return reader;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.AbstractCSSGenerator#generateResourceForDebug(net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	@SuppressWarnings("resource")
	@Override
	protected Reader generateResourceForDebug(GeneratorContext context) {
		
		Reader rd = null;
		// The following section is executed in DEBUG mode to retrieve the classpath CSS from the temporary folder, 
		// if the user defines that the image servlet should be used to retrieve the CSS images.
		// It's not executed at the initialization process to be able to read data from classpath.
		if(context.getConfig().isCssClasspathImageHandledByClasspathCss()){

			FileInputStream fis = null;
			try {
				fis = new FileInputStream(new File(workingDir+"/"+TEMP_CSS_CLASSPATH_SUBDIR, context.getPath()));
			} catch (FileNotFoundException e) {
				throw new BundlingProcessException("An error occured while creating temporary resource for "+context.getPath(), e);
			}
	        if(fis != null){
	        	FileChannel inchannel = fis.getChannel();
	        	rd = Channels.newReader(inchannel,context.getConfig().getResourceCharset().newDecoder(),-1);
	        }
		}else{
			
			rd = helper.createResource(context);
		}
	    
		return rd;
		
	}

//	/* (non-Javadoc)
//	 * @see net.jawr.web.resource.bundle.generator.AbstractCSSGenerator#createResource(net.jawr.web.resource.bundle.generator.GeneratorContext)
//	 */
//	public Reader createResource(GeneratorContext context) {
//		
//		Reader reader = null;
//		
//		if(FileNameUtils.isExtension(context.getPath(), JawrConstant.CSS_TYPE)){
//				
//			// The following section is executed in DEBUG mode to retrieve the classpath CSS from the temporary folder, 
//			// if the user defines that the image servlet should be used to retrieve the CSS images.
//			// It's not executed at the initialization process to be able to read data from classpath.
//			if(!context.isProcessingBundle() && context.getConfig().isCssClasspathImageHandledByClasspathCss()){
//	
//				Reader rd = null;
//				FileInputStream fis;
//				try {
//					fis = new FileInputStream(new File(workingDir+"/"+TEMP_CSS_CLASSPATH_SUBDIR, context.getPath()));
//				} catch (FileNotFoundException e) {
//					throw new BundlingProcessException("An error occured while creating temporary resource for "+context.getPath(), e);
//				}
//		        if(fis != null){
//		        	FileChannel inchannel = fis.getChannel();
//		        	rd = Channels.newReader(inchannel,context.getConfig().getResourceCharset().newDecoder(),-1);
//		        }
//				
//		        return rd;
//			}
//			
//			if(reader == null){
//				reader = helper.createResource(context);
//				if(reader != null){
//					reader = createTempResource(context, reader);
//				}
//			}
//		}
//		
//		
//		return reader;
//	}


	/**
	 * Creates the temporary resource which will be processed and used to retrieve the content for the path.
	 * @param generatorContext the context
	 * @param rd the reader
	 * @return the reader to the temporary processed resource
	 */
	private Reader createTempResource(GeneratorContext generatorContext, Reader rd) {
		
		Reader result = null;
		
		// Here we create a new context where the bundle name is the Jawr generator CSS path
		// The version of the CSS classpath for debug mode will be different compare to the standard one
		JoinableResourceBundle tempBundle = new JoinableResourceBundleImpl(ResourceGenerator.CSS_DEBUGPATH, null, null, null, null, generatorContext.getConfig().getGeneratorRegistry());
		BundleProcessingStatus tempStatus = new BundleProcessingStatus(BundleProcessingStatus.FILE_PROCESSING_TYPE, tempBundle, generatorContext.getResourceReaderHandler(), generatorContext.getConfig());
		
		CSSURLPathRewriterPostProcessor postProcessor = new CSSURLPathRewriterPostProcessor();
		String resourcePath = generatorContext.getPath();
		tempStatus.setLastPathAdded(JawrConstant.CLASSPATH_RESOURCE_PREFIX+resourcePath);
		FileWriter fWriter = null;
		try {
			StringWriter writer = new StringWriter();
			IOUtils.copy(rd, writer, true);
			result = new StringReader(writer.getBuffer().toString());
			StringBuffer resourceData = postProcessor.postProcessBundle(tempStatus, writer.getBuffer());
			
			String tempCssClasspathDir = workingDir+"/"+TEMP_CSS_CLASSPATH_SUBDIR;
			File cssTempFile = new File(tempCssClasspathDir, resourcePath);
			File tempCssDir = cssTempFile.getParentFile(); 
			if(!tempCssDir.exists()){
				if(!tempCssDir.mkdirs()){
					throw new BundlingProcessException("An error occured while creating temporary resource for "+resourcePath+".\n" +
							"Enable to create temporary directory '"+tempCssClasspathDir+"'");
				}
			}
			
			fWriter = new FileWriter(cssTempFile);
			IOUtils.copy(new StringReader(resourceData.toString()), fWriter, true);
		} catch (IOException e) {
			throw new BundlingProcessException("An error occured while creating temporary resource for "+resourcePath, e);
		}
		
		return result;
	}

	
	
}
