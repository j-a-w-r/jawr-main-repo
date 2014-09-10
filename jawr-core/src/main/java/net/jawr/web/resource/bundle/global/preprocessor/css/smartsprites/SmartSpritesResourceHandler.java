/**
 * Copyright 2009-2012 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.global.preprocessor.css.smartsprites;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import net.jawr.web.JawrConstant;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.FileNameUtils;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

import org.carrot2.labs.smartsprites.message.MessageLog;
import org.carrot2.labs.smartsprites.resource.ResourceHandler;

/**
 * This class defines the resource handler for smartSprites
 * 
 * @author Ibrahim Chaehoi
 */
public class SmartSpritesResourceHandler implements ResourceHandler {
	
	/** The resource handler for CSS resources */
	private ResourceReaderHandler rsHandler;

	/** The resource handler for image resources */
	private ResourceReaderHandler imgRsHandler;
	
	/** The Css generator registry */
	private GeneratorRegistry cssGeneratorRegistry;
	
	/** The binary resource generator registry */
	private GeneratorRegistry binaryGeneratorRegistry;
	
	/** The charset */
	private final String charset;

	/** The working directory */
	private final String workingDir;
	
	/** The webapp context path */
	private String contextPath = null;
	
	/**
	 * Constructor
	 * 
	 * @param rsHandler the CSS resource handler
	 * @param imgRsHandler the image resource handler
	 * @param imgGeneratorRegistry the image generator registry
	 * @param charset the charset
	 * @param messageLog the message log
	 */
	public SmartSpritesResourceHandler(
			ResourceReaderHandler rsHandler,
			ResourceReaderHandler imgRsHandler, 
			GeneratorRegistry cssGeneratorRegistry,
			GeneratorRegistry imgGeneratorRegistry,
			String charset, MessageLog messageLog) {
		this.rsHandler = rsHandler;
		this.imgRsHandler = imgRsHandler;
		this.cssGeneratorRegistry = cssGeneratorRegistry;
		this.binaryGeneratorRegistry = imgGeneratorRegistry;
		this.charset = charset;
		this.workingDir = rsHandler.getWorkingDirectory()+JawrConstant.CSS_SMARTSPRITES_TMP_DIR;
	}

	/**
	 * Sets the context path
	 * @param contextPath the contextPath to set
	 */
	public void setContextPath(String contextPath) {
		
		if(contextPath != null && !contextPath.endsWith("/")){
			contextPath += "/";
		}
		this.contextPath = contextPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.carrot2.labs.smartsprites.resource.ResourceHandler#getReader(java.lang.String)
	 */
	public Reader getResourceAsReader(String resourceName)
			throws IOException {

		try {
			return rsHandler.getResource(resourceName, true);
		} catch (ResourceNotFoundException e) {
			throw new IOException("The resource '"+resourceName+"' was not found.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.carrot2.labs.smartsprites.resource.ResourceHandler#getResourceAsStream(java.lang.String)
	 */
	public InputStream getResourceAsInputStream(String resourceName)
			throws IOException {
		
		try {
			return imgRsHandler.getResourceAsStream(resourceName);
		} catch (ResourceNotFoundException e) {
			throw new IOException("The resource '"+resourceName+"' was not found.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.carrot2.labs.smartsprites.resource.ResourceHandler#getResourcePath(java.lang.String, java.lang.String)
	 */
	public String getResourcePath(String basePath, String relativePath) {

		String result = null;
		if (binaryGeneratorRegistry.isGeneratedBinaryResource(relativePath)) {
			result = relativePath;
		} else if(contextPath != null && relativePath.startsWith(contextPath)){
			result = relativePath.substring(contextPath.length()-1);
		}else{
			result = PathNormalizer.concatWebPath(basePath, relativePath);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.carrot2.labs.smartsprites.resource.ResourceHandler#getResourceAsOutputStream(java.lang.String)
	 */
	public OutputStream getResourceAsOutputStream(String resourceName)
			throws IOException {

		// Create directories if needed
		String generatedFilePath = resourceName.substring(workingDir.length());
		if(!FileNameUtils.isExtension(generatedFilePath, JawrConstant.CSS_TYPE) && binaryGeneratorRegistry.isGeneratedBinaryResource(generatedFilePath)){
			// for generated image put it  in the generated Image directory
			generatedFilePath = workingDir+JawrConstant.SPRITE_GENERATED_IMG_DIR+generatedFilePath.replace(':','/');
		}else if(cssGeneratorRegistry.isPathGenerated(generatedFilePath)) { // Rename resource for For generated CSS
			generatedFilePath = workingDir+JawrConstant.SPRITE_GENERATED_CSS_DIR+generatedFilePath.replace(':','/');
		}else{
			generatedFilePath = resourceName;
		}
		
		final File parentFile = new File(generatedFilePath).getParentFile();
		if (!parentFile.exists()) {
			if (!parentFile.mkdirs()) {
				throw new IOException("Unable to create the directory : "
						+ parentFile.getPath());
			}
		}
		
		File file = new File(generatedFilePath);
		try {
			file = file.getCanonicalFile();
		} catch (final IOException e) {
			file = file.getAbsoluteFile();
		}

		return new FileOutputStream(file);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.carrot2.labs.smartsprites.resource.ResourceHandler#getResourceAsWriter(java.lang.String)
	 */
	public Writer getResourceAsWriter(String path) throws IOException {
		try {
			return new OutputStreamWriter(getResourceAsOutputStream(path),
					charset);
		} catch (UnsupportedEncodingException e) {
			// Should not happen as we're checking the charset in constructor
			throw new BundlingProcessException(e);
		}
	}
}
