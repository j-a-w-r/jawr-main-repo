/**
 * Copyright 2013 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator.dwr;

import javax.servlet.http.HttpServletRequest;

import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.handler.ClientSideHandlerGeneratorImpl;
import net.jawr.web.resource.bundle.renderer.DWRParamWriter;

/**
 * Implementation of DWR ClientSideHandlerGenerator
 * 
 * @author Ibrahim Chaehoi
 */
public class DwrClientSideHandlerGeneratorImpl extends ClientSideHandlerGeneratorImpl {
	
	/**
	 * Constructor
	 */
	public DwrClientSideHandlerGeneratorImpl() {
		
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.handler.ClientSideHandlerGeneratorImpl#getHeaderSection(javax.servlet.http.HttpServletRequest)
	 */
	protected StringBuffer getHeaderSection(HttpServletRequest request){
	
		StringBuffer sb = super.getHeaderSection(request);
		if(null != this.config.getDwrMapping()){
			sb.append(DWRParamWriter.buildDWRJSParams(request.getContextPath(),PathNormalizer.joinPaths(request.getContextPath(),  
																						 			this.config.getDwrMapping())));
			sb.append("if(!window.DWR)window.DWR={};\nDWR.loader = JAWR.loader;\n");
		}
		return sb;
	}
	
}
