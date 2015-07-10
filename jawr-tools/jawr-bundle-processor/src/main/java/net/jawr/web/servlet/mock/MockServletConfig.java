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
package net.jawr.web.servlet.mock;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * The mock class for the servlet config
 * @author Ibrahim Chaehoi
 */
public class MockServletConfig implements ServletConfig {

	/** The servlet context */
	private ServletContext context;
	
	/** The initialization parameters */
	private Map<String, Object> initParameters = new HashMap<String, Object>();
	
	private String servletName;
	
	/**
	 * Constructor 
	 */
	public MockServletConfig(ServletContext context){
		
		this.context = context;
	}
	
	/**
	 * Constructor 
	 */
	public MockServletConfig(String servletName, ServletContext context, Map<String, Object> initParameters){
		
		this.servletName = servletName;
		this.context = context;
		this.initParameters = initParameters;
	}
	
	/**
	 * @param servletName the servletName to set
	 */
	public void setServletName(String servletName) {
		this.servletName = servletName;
	}

	/**
	 * Returns the initialization parameters map
	 * @return the initialization parameters map
	 */
	public Map<String, Object> getInitParameters() {
		return this.initParameters;
	}

	/**
	 * Sets the initialization parameters map
	 * @param initParameters the initParameters to set
	 */
	public void setInitParameters(Map<String, Object> initParameters) {
		this.initParameters = initParameters;
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.ServletConfig#getInitParameter(java.lang.String)
	 */
	public String getInitParameter(String name) {
		
		return (String) initParameters.get(name);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletConfig#getInitParameterNames()
	 */
	public Enumeration<String> getInitParameterNames() {
		
		return Collections.enumeration(initParameters.keySet());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletConfig#getServletContext()
	 */
	public ServletContext getServletContext() {
		return context;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletConfig#getServletName()
	 */
	public String getServletName() {
		return servletName;
	}

}
