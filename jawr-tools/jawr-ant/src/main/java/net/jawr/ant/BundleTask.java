/**
 * Copyright 2009-2013 Ibrahim Chaehoi
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
package net.jawr.ant;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import net.jawr.web.bundle.processor.BundleProcessor;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Delete;

/**
 * The ANT task to launch the bundle processing.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class BundleTask extends Task {

	/** The default servlet API version */
	private static final String DEFAULT_SERVLET_API_VERSION = "2.3";

	/** The servlet name separator */
	private static final String SERVLET_NAME_SEPARATOR = ",";

	/**
	 * The path to the root of the web application where the resources are loaded from.
	 */
	private String rootPath;
	
	/**
	 * The path to the temporary directory for Jawr where the temporary resources are generated.
	 */
	private String tempDirPath;
	
	/**
	 * The path to the root of the web application where the the new files are generated.
	 */
	private String destDirPath;

	/**
	 * The list of comma separated servlet names to initialize.
	 */
	private String servletsToInitialize;

	/**
	 * The list of comma separated path to spring config files.
	 */
	private String springConfigFiles;

	/**
	 * The flag indicating if we  should generate the CDN files or not
	 */
	private boolean generateCDNFiles = true;
	
	/**
	 * The flag indicating if we want to keep the jawr URL mapping or if we rewrite it to remove resource hashcode.  
	 */
	private boolean keepUrlMapping = false;
	
	/**
	 * The servlet API version. Default value : 2.3.  
	 */
	private String servletAPIversion = DEFAULT_SERVLET_API_VERSION;
	
	/**
	 * Sets the root directory path
	 * @param rootPath the path to set
	 */
	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	/**
	 * Sets the temporary directory path
	 * @param tempDirPath the path to set
	 */
	public void setTempDirPath(String tempDirPath) {
		this.tempDirPath = tempDirPath;
	}

	/**
	 * Sets the destination directory path 
	 * @param destDirPath the path to set
	 */
	public void setDestDirPath(String destDirPath) {
		this.destDirPath = destDirPath;
	}
	
	/**
	 * Sets the servlets to initialize
	 * 
	 * @param servletsToInitialize the servletsToInitialize to set
	 */
	public void setServletsToInitialize(String servletsToInitialize) {
		this.servletsToInitialize = servletsToInitialize;
	}

	/**
	 * Sets the spring config files to use
	 * @param springConfigFiles the spring config files to use
	 */
	public void setSpringConfigFiles(String springConfigFiles) {
		this.springConfigFiles = springConfigFiles;
	}

	/**
	 * Sets the flag indicating if we must generate CDN files or not
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
	 * Sets the servlet API version
	 * @param servletAPIversion the servlet API version to set
	 */
	public void setServletAPIversion(String servletAPIversion) {
		this.servletAPIversion = servletAPIversion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	public void execute() {
		
		try {
			createBundles();
		} catch (Exception ex) {
			Logger logger = Logger.getLogger(getClass().getName());
			if(ex instanceof ServletException){
				logger.log(Level.SEVERE, null, ((ServletException)ex).getRootCause());
			}else{
				logger.log(Level.SEVERE, null, ex);
			}
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
			cleanDirectory(tempDir);
		}
		File destDir = new File(destDirPath);
		if(!destDir.exists()){
			destDir.mkdirs();
		}else{
			cleanDirectory(destDir);
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

	/**
     * Clean a directory without deleting it.
     */
    public void cleanDirectory( final File directory )
        throws IOException
    {
        if ( !directory.exists() )
        {
            final String message = directory + " does not exist";
            throw new IllegalArgumentException( message );
        }

        if ( !directory.isDirectory() )
        {
            final String message = directory + " is not a directory";
            throw new IllegalArgumentException( message );
        }

        Delete deleteTask = new Delete();
        deleteTask.setProject(getProject());
		deleteTask.setDir(directory);
		deleteTask.execute();
		
		if(!directory.exists()){
			directory.mkdirs();
		}
    }
  
}
