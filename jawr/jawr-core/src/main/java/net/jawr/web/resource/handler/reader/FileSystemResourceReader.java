/**
 * Copyright 2009-2011 Ibrahim Chaehoi
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
package net.jawr.web.resource.handler.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Set;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.util.FileUtils;
import net.jawr.web.util.StringUtils;

/**
 * This class defines the resource reader which is based on a file system and which can handle
 * text and stream resources.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class FileSystemResourceReader implements TextResourceReader, StreamResourceReader, ResourceBrowser {

	/** The base directory */
	private String baseDir;
	
	/** The charset */
	private Charset charset;
	
	/**
	 * Constructor
	 * 
	 * @param baseDir the base directory
	 * @param charset the charset
	 */
	public FileSystemResourceReader(JawrConfig config) {
		this(config.getProperty(JawrConstant.JAWR_BASECONTEXT_DIRECTORY), config);
	}
	
	/**
	 * Constructor
	 * 
	 * @param baseDir the base directory
	 * @param charset the charset
	 */
	public FileSystemResourceReader(String baseDir, JawrConfig config) {
		
		this.baseDir = baseDir;
		if(StringUtils.isEmpty(this.baseDir)){
			throw new BundlingProcessException("The 'jawr.basecontext.directory' is not set. Please provide a value or remove it if it's not used.");
		}
		if (this.baseDir != null && this.baseDir.startsWith(JawrConstant.FILE_URI_PREFIX)) {
			this.baseDir = this.baseDir.substring(JawrConstant.FILE_URI_PREFIX.length());
		}
		
		File baseDirFile = new File(this.baseDir);
		if(!baseDirFile.exists()){
			throw new BundlingProcessException("The base context directory '"+this.baseDir+" doesn't exists. Please check your configuration.");
		}else if(!baseDirFile.isDirectory()){
			throw new BundlingProcessException("The base context directory '"+this.baseDir+" is not a directory. Please check your configuration.");
		}
		this.charset = config.getResourceCharset();
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.ResourceReader#getResource(java.lang.String)
	 */
	public Reader getResource(String resourceName) {
		
		return getResource(resourceName, false);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.ResourceReader#getResource(java.lang.String, boolean)
	 */
	public Reader getResource(String resourceName, boolean processingBundle) {
		
		Reader rd = null;
		FileInputStream fis = (FileInputStream) getResourceAsStream(resourceName);
        if(fis != null){
        	FileChannel inchannel = fis.getChannel();
        	rd = Channels.newReader(inchannel,charset.newDecoder (),-1);
        }
		
        return rd;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.ResourceReader#getResourceAsStream(java.lang.String)
	 */
	public InputStream getResourceAsStream(String resourceName) {
		
		return getResourceAsStream(resourceName, false);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.stream.StreamResourceReader#getResourceAsStream(java.lang.String, boolean)
	 */
	public InputStream getResourceAsStream(String resourceName,
			boolean processingBundle) {
		
		InputStream is = null;
		try {
			File resource = new File(baseDir, resourceName);
			is = new FileInputStream( resource );
		} catch (FileNotFoundException e) {
			// Nothing to do
		}
		
		return is; 
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.ResourceBrowser#getResourceNames(java.lang.String)
	 */
	public Set<String> getResourceNames(String path) {
		path = path.replace('/', File.separatorChar);
		File resource = new File(baseDir, path);
        return FileUtils.getResourceNames(resource);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.ResourceBrowser#isDirectory(java.lang.String)
	 */
	public boolean isDirectory(String dirPath) {
		String path = dirPath.replace('/', File.separatorChar);
		return new File(baseDir, path).isDirectory();
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.ResourceBrowser#getFilePath(java.lang.String)
	 */
	@Override
	public String getFilePath(String resourcePath) {
		String path = resourcePath.replace('/', File.separatorChar);
		return new File(baseDir, path).getAbsolutePath();
	}

}
