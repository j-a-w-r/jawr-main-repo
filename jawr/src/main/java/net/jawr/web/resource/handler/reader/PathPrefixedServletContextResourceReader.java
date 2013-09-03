/**
 * Copyright 2009 Ibrahim Chaehoi
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

import javax.servlet.ServletContext;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.util.StringUtils;


/**
 * This class defines a resource handler which is based on the servlet context (so which retrieves the resource 
 * from the web application),  and which use a path prefix to access the resource.
 * 
 * For example : 
 * if the pathPrefix is set to /css/section1/, to access the resource /css/section1/subsectionA/flower.css,
 * we must use => /subsectionA/flower.css. The path prefix will be automatically added.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class PathPrefixedServletContextResourceReader extends
		ServletContextResourceReader {

	/** The path prefix to append to any requested resource */
	private String pathPrefix;
	
	/**
	 * Constructor 
	 * @param context the context
	 * @param config the Jawr config
	 */
	public PathPrefixedServletContextResourceReader(ServletContext context, JawrConfig config, String pathPrefix) {
		super(context, config);
		if(StringUtils.isNotEmpty(pathPrefix)){
			this.pathPrefix = PathNormalizer.asDirPath(pathPrefix);
		}
	}
	
	/**
	 * Returns the full path for the specified resource
	 * @param path the resource path
	 * @return the full path for the specified resource
	 */
	public String getFullPath(String path){
		
		if(StringUtils.isEmpty(pathPrefix)){
			return path;
		}
		
		return PathNormalizer.asPath(pathPrefix+path);
	}
	
}
