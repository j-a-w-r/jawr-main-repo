/**
 * Copyright 2009-2013  Andreas Andreou, Ibrahim Chaehoi
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
package net.jawr.maven;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import net.jawr.web.bundle.processor.BundleProcessor;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;

/**
 * The Jawr Mojo which will perform the bundle.
 * 
 * The original author of this Mojo is Andreas Andreou.
 * 
 * @author Andreas Andreou
 * @author Ibrahim Chaehoi
 * 
 * @goal bundle
 */
public class JawrMojo extends AbstractMojo {

	/** The servlet name separator */
	private static final String SERVLET_NAME_SEPARATOR = ",";

	/**
	 * The path to the root of the web application where the resources are loaded from.
	 * 
	 * @parameter default-value="${project.build.directory}/${project.build.finalName}"
	 */
	private String rootPath;
	
	/**
	 * The path to the temporary directory for Jawr where the temporary resources are generated.
	 * 
	 * @parameter default-value="${project.build.directory}/jawr/temp"
	 */
	private String tempDirPath;
	
	/**
	 * The path to the destination directory where the files are stored at the end of the process.
	 * 
	 * @parameter default-value="${project.build.directory}/jawr/bundles"
	 */
	private String destDirPath;

	/**
	 * The list of comma separated servlet names to initialize.
	 * 
	 * @parameter default-value=""
	 */
	private String servletsToInitialize;

	/**
	 * The list of comma separated path to script config files to use.
	 * 
	 * @parameter default-value=""
	 */
	private String springConfigFiles;

	/**
	 * The flag indicating if we  should generate the CDN files or not
	 * 
	 * @parameter default-value="true"
	 */
	private boolean generateCDNFiles;
	
	/**
	 * The flag indicating if we want to keep the jawr URL mapping or if we rewrite it to remove resource hashcode.
	 * 
	 * @parameter default-value="false"
	 */
	private boolean keepUrlMapping;
	
	/**
	 * The servlet API version.
	 * 
	 * @parameter default-value="2.3"
	 */
	private String servletAPIversion = "2.3";
	
	
	/**
	 * Sets the servlet API version
	 * @param servletAPIversion the servlet API version to set (ex : "2.3", "2.5")
	 */
	public void setServletAPIversion(String servletAPIversion) {
		this.servletAPIversion = servletAPIversion;
	}

	/**
	 * Sets the list of servlet to initialize
	 * @param servletsToInitialize the servletsToInitialize to set
	 */
	public void setServletsToInitialize(String servletsToInitialize) {
		this.servletsToInitialize = servletsToInitialize;
	}

	/**
	 * Sets the list of spring config files to use
	 * @param springConfigFiles the spring config files to set
	 */
	public void setSpringConfigFiles(String springConfigFiles) {
		this.springConfigFiles = springConfigFiles;
	}

	/**
	 * @param generateCDNFiles the generateCDNFiles to set
	 */
	public void setGenerateCDNFiles(boolean generateCDNFiles) {
		this.generateCDNFiles = generateCDNFiles;
	}

	/**
	 * Sets the flag indicating if we keep or not the URL mapping
	 * @param keepUrlMapping the keepUrlMapping to set
	 */
	public void setKeepUrlMapping(boolean keepUrlMapping) {
		this.keepUrlMapping = keepUrlMapping;
	}

	/**
	 * @return the rootPath
	 */
	public String getRootPath() {
		return rootPath;
	}

	/**
	 * @param rootPath the rootPath to set
	 */
	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	/**
	 * @return the tempDirPath
	 */
	public String getTempDirPath() {
		return tempDirPath;
	}

	/**
	 * @param tempDirPath the tempDirPath to set
	 */
	public void setTempDirPath(String tempDirPath) {
		this.tempDirPath = tempDirPath;
	}

	/**
	 * @return the destDirPath
	 */
	public String getDestDirPath() {
		return destDirPath;
	}

	/**
	 * @param destDirPath the destDirPath to set
	 */
	public void setDestDirPath(String destDirPath) {
		this.destDirPath = destDirPath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			createBundles();
		} catch (Exception ex) {
			Logger logger = Logger.getLogger(JawrMojo.class.getName());
			if(ex instanceof ServletException){
				logger.log(Level.SEVERE, null, ((ServletException)ex).getRootCause());
			}else{
				logger.log(Level.SEVERE, null, ex);
			}
			throw new MojoFailureException("JAWR Maven plugin failed: " + ex.getLocalizedMessage());
		}
	}

	/**
	 * Create the bundles.
	 * 
	 * @throws Exception if an exception occurs
	 */
	public void createBundles() throws Exception {

		File tempDir = new File(tempDirPath);
		if(!tempDir.exists()){
			tempDir.mkdirs();
		}else{
			FileUtils.cleanDirectory(tempDir);
		}
		File destDir = new File(destDirPath);
		if(!destDir.exists()){
			destDir.mkdirs();
		}else{
			FileUtils.cleanDirectory(destDir);
		}
		
		List<String> servlets = new ArrayList<String>();
		if(servletsToInitialize != null){
			
			String[] servletNames = servletsToInitialize.split(SERVLET_NAME_SEPARATOR);
			for (int i = 0; i < servletNames.length; i++) {
				servlets.add(servletNames[i].trim());
			}
		}
		
		BundleProcessor bundleProcessor = new BundleProcessor();
		bundleProcessor.process(rootPath, tempDirPath, destDirPath, springConfigFiles, servlets, generateCDNFiles, keepUrlMapping, servletAPIversion);
	}

}
