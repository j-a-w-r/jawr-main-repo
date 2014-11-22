/**
 * Copyright 2010  Ibrahim Chaehoi
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
package net.jawr.web.bundle.processor;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import net.jawr.web.servlet.JawrServlet;

/**
 * This class is used internally to handle the definition of a servlet.
 * 
 * @author Ibrahim Chaehoi
 */
public class ServletDefinition implements Comparable<ServletDefinition> {

	/** The servlet instance */
	private HttpServlet servlet;

	/** The servlet class */
	private Class<?> servletClass;

	/** The servlet config */
	private ServletConfig servletConfig;

	/** The initialization order of the servlet */
	private int order;

	/**
	 * Constructor 
	 */
	public ServletDefinition(){
		
	}
	
	/**
	 * Constructor.
	 * 
	 * @param servletClass the servlet class
	 * @param servletConfig the servlet config
	 * @param order the order
	 */
	public ServletDefinition(Class<?> servletClass, ServletConfig servletConfig, int order) {
		super();
		this.servletClass = servletClass;
		this.servletConfig = servletConfig;
		this.order = order;
	}

	/**
	 * Constructor.
	 * 
	 * @param servlet the servlet
	 * @param servletConfig the servlet config
	 */
	public ServletDefinition(HttpServlet servlet, ServletConfig servletConfig) {
		super();
		this.servlet = servlet;
		this.servletConfig = servletConfig;
	}
	
	/**
	 * @return the servlet
	 */
	public HttpServlet getServlet() {
		return servlet;
	}

	/**
	 * Returns the servlet config
	 * 
	 * @return the servletConfig
	 */
	public ServletConfig getServletConfig() {
		return servletConfig;
	}

	/**
	 * Create a new instance of the servlet and initialize it.
	 * 
	 * @param servletClass the servlet class
	 * @param servletConfig the servlet config
	 * @throws ServletException if a servlet exception occurs.
	 */
	public HttpServlet initServlet() throws Exception {

		servlet = (HttpServlet) servletClass.newInstance();
		servlet.init(servletConfig);
		return servlet;
	}

	/**
	 * Returns true if the servlet definition is a definition for a Jawr servlet
	 * 
	 * @return true if the servlet definition is a definition for a Jawr servlet
	 */
	public boolean isJawrServletDefinition() {
		return JawrServlet.class.isAssignableFrom(servletClass);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(T)
	 */
	public int compareTo(ServletDefinition arg0) {

		return order - arg0.order;
	}
}
