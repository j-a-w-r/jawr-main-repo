/**
 * Copyright 2010 Ibrahim Chaehoi
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
package net.jawr.web.resource.handler.reader.grails;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.InvalidPathException;
import net.jawr.web.resource.bundle.factory.util.RegexUtil;
import net.jawr.web.resource.handler.reader.ServletContextResourceReader;
import net.jawr.web.util.FileUtils;

import org.apache.log4j.Logger;

/**
 * This class defines the resource reader for Grails application which is based on the servletContextReader.
 * 
 * @author Ibrahim Chaehoi
 */
public class GrailsServletContextResourceReader extends
	ServletContextResourceReader {

	/** The logger */
	private static final Logger LOGGER = Logger.getLogger(GrailsServletContextResourceReader.class);
	
	/** The pattern of the plugin resource path */
	private static Pattern PLUGIN_RESOURCE_PATTERN = Pattern.compile("^(/plugins/([a-zA-Z0-9_\\-]*))");
	
	/** The plugin path map */
	private Map<String, String> pluginPathMap;
	
	/** The flag indicating if the grails war is deployed */
	private boolean warDeployed;
	
	/**
	 * Constructor
	 * @param context the context
	 * @param config the configuration
	 */
	@SuppressWarnings("unchecked")
	public GrailsServletContextResourceReader(ServletContext context,
			JawrConfig config) {
		super(context, config);
		pluginPathMap = (Map<String, String>) context.getAttribute(JawrConstant.JAWR_GRAILS_PLUGIN_PATHS);
		if(pluginPathMap == null){
			throw new BundlingProcessException("No grails plugin paths map defined in the servlet context");
		}
		
		// Determine wether this is run-app or run-war style of runtime. 
		warDeployed = ((Boolean) context.getAttribute(JawrConstant.GRAILS_WAR_DEPLOYED)).booleanValue();
	}

	/**
	 * Handle the mapping of the resource path to the right one.
	 * This can be the case for plugin resources.
	 * It will returns the file system path or the real path 
	 * or the same path if the path has not been remapped
	 * 
	 * @param path the path
	 * @return the real path or the same path if the path has not been remapped
	 */
	public String getRealResourcePath(String path){
		String realPath = path;
		Matcher matcher = PLUGIN_RESOURCE_PATTERN.matcher(path);
		StringBuffer sb = new StringBuffer();
		
		if(matcher.find()){
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("Plugin path found : "+path);
			}
			
			String pluginName = matcher.group(2);
			LOGGER.debug("pluginName : "+pluginName);
			String pluginPath = (String) pluginPathMap.get(pluginName);
			if(pluginPath != null){
				matcher.appendReplacement(sb, RegexUtil.adaptReplacementToMatcher(pluginPath));
				matcher.appendTail(sb);
				realPath = sb.toString();
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("Plugin path '"+path+"' mapped to '"+realPath+"'");
				}
			}else{
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("No Plugin path found for '"+pluginName);
				}
			}
		}
		
		return realPath;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.ServletContextResourceReader#getResource(java.lang.String, boolean)
	 */
	@SuppressWarnings("resource")
	public Reader getResource(String resourceName, boolean processingBundle) {
		
		Reader rd = null;
		String realPath = getRealResourcePath(resourceName);
		if(isFileSystemPath(resourceName, realPath)){ // The resource has been remapped
			try {
				rd = new FileReader(realPath);
			} catch (FileNotFoundException e) {
				LOGGER.debug("The resource "+realPath+" has not been found");
			}
		}
		if(rd == null){
			rd = super.getResource(realPath, processingBundle);
		}
		
		return rd;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.ServletContextResourceReader#getResource(java.lang.String)
	 */
	@SuppressWarnings("resource")
	public Reader getResource(String resourceName) {
		Reader rd = null;
		String realPath = getRealResourcePath(resourceName);
		if(isFileSystemPath(resourceName, realPath)){ // The resource has been remapped to a file system path
			try {
				rd = new FileReader(realPath);
			} catch (FileNotFoundException e) {
				LOGGER.debug("The resource "+realPath+" has not been found");
			}
		}
		if(rd == null){
			rd = super.getResource(realPath);
		}
		
		return rd;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.ServletContextResourceReader#getResourceAsStream(java.lang.String, boolean)
	 */
	@SuppressWarnings("resource")
	public InputStream getResourceAsStream(String resourceName,
			boolean processingBundle) {
		
		InputStream is = null;
		String realPath = getRealResourcePath(resourceName);
		if(isFileSystemPath(resourceName, realPath)){ // The resource has been remapped to a file system path
			try {
				is = new FileInputStream(realPath);
			} catch (FileNotFoundException e) {
				LOGGER.debug("The resource "+realPath+" has not been found");
			}
		}
		if(is == null){
			is = super.getResourceAsStream(realPath, processingBundle);
		}
		return is;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.ServletContextResourceReader#getResourceAsStream(java.lang.String)
	 */
	@SuppressWarnings("resource")
	public InputStream getResourceAsStream(String resourceName) {
		InputStream is = null;
		String realPath = getRealResourcePath(resourceName);
		if(isFileSystemPath(resourceName, realPath)){ // The resource has been remapped to a file system path
				try {
				is = new FileInputStream(realPath);
			} catch (FileNotFoundException e) {
				LOGGER.debug("The resource "+realPath+" has not been found");
			}
		}
		if(is == null){
			is = super.getResourceAsStream(realPath);
		}
		return is;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.ServletContextResourceReader#getResourceNames(java.lang.String)
	 */
	public Set<String> getResourceNames(String path) {
		
		Set<String> resourceNames = new HashSet<String>();
		String realPath = getRealResourcePath(path);
		if(isFileSystemPath(path, realPath)){ // The resource has been remapped to a file system path
			try{
				resourceNames = FileUtils.getResourceNames(new File(realPath));
			}catch(InvalidPathException e){
				LOGGER.warn("The path "+path+" which has been mapped to "+realPath+" doesn't exist");
			}
		}else{
			resourceNames = super.getResourceNames(realPath);
		}
		
		return resourceNames;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.reader.ServletContextResourceReader#isDirectory(java.lang.String)
	 */
	public boolean isDirectory(String path) {
		
		boolean isDirectory = false;
		String realPath = getRealResourcePath(path);
		if(isFileSystemPath(path, realPath)){ // The resource has been remapped to a file system path
			isDirectory = new File(realPath).isDirectory();
		}else{
			isDirectory = super.isDirectory(realPath);
		}
		return isDirectory;
	}

	/**
	 * Checks if the path given in parameter are file system path or not
	 * @param path the original path
	 * @param realPath the real path
	 * @return true if the path given in parameter are file system path or not
	 */
	private boolean isFileSystemPath(String path, String realPath) {
		return !realPath.equals(path) &&  !warDeployed;
	}

}
