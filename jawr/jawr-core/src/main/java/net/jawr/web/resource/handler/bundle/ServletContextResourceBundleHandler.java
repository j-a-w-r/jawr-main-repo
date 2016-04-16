/**
 * Copyright 2007-2012 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.handler.bundle;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.servlet.ServletContext;

import net.jawr.web.JawrConstant;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;

/**
 * This class defines the resource bundle handler which is based on the servlet context.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 *
 */
public class ServletContextResourceBundleHandler extends
		AbstractResourceBundleHandler {

	/** The servlet context */
	private ServletContext context;
	
	/**
	 * Constructor
	 * @param context the servlet context
	 * @param charset the charset
	 * @param generatorRegistry the generator registry
	 * @param resourceType the resource type
	 */
	public ServletContextResourceBundleHandler(ServletContext context, Charset charset,GeneratorRegistry generatorRegistry, String resourceType) {
		super(new File((File) context.getAttribute(JawrConstant.SERVLET_CONTEXT_TEMPDIR), JawrConstant.JAWR_WRK_DIR),charset,generatorRegistry, resourceType);		
		this.context  = context;
		this.charset = charset;
	}

	/**
	 * Constructor
	 * @param context the servlet context
	 * @param workingDirectory the working directory
	 * @param charset the charset
	 * @param generatorRegistry the generator registry
	 * @param resourceType the resource type
	 */
	public ServletContextResourceBundleHandler(ServletContext context, String workingDirectory, Charset charset,GeneratorRegistry generatorRegistry, String resourceType) {
		super(workingDirectory,charset,resourceType, false);		
		this.context  = context;
		this.charset = charset;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.bundle.ResourceBundleHandler#getResourceBundleAsStream(java.lang.String)
	 */
	public InputStream getResourceBundleAsStream(String bundleName)
			throws ResourceNotFoundException {
		
		InputStream is = context.getResourceAsStream(bundleName);
		if(is == null){
			throw new ResourceNotFoundException(bundleName);
		}
		
		return is;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.handler.bundle.AbstractResourceBundleHandler#doGetResourceAsStream(java.lang.String)
	 */
	protected InputStream doGetResourceAsStream(String resourceName) {
		return context.getResourceAsStream(resourceName);
	}

}
