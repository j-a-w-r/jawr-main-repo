/**
 * Copyright 2008-2010 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.handler;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides methods to generate a javascript equivalent of a resourcehandler, used to 
 * generate links in a non dynamic page where taglibs cannot be used. 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public interface ClientSideHandlerGenerator {

	/** The script template */
	public static final String SCRIPT_TEMPLATE = "/net/jawr/web/resource/bundle/handler/handler.js";
	
	/** The script template for debug mode */
	public static final String DEBUG_SCRIPT_TEMPLATE = "/net/jawr/web/resource/bundle/handler/debughandler.js";
	
	/**
	 * Builds a javascript script that can be used to include bundles in non dynamic html pages. 
	 * @param request the request
	 * @return the content of the javascript
	 */
	public StringBuffer getClientSideHandlerScript(HttpServletRequest request);
	
	/**
	 * Returns the part of the script that creates all instances of ResourceBundle javascript objects
	 * for a given resourcehandler.  
	 * @param variantMap the variant map
	 * @param useGzip the flag indicating if we must gip or not the content
	 * @return the part of the script that creates all instances of ResourceBundle javascript objects
	 */
	public StringBuffer getClientSideBundles(Map<String, String> variantMap, boolean useGzip);

}
