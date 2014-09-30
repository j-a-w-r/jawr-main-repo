/**
 * Copyright 2014 Ibrahim Chaehoi
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
package net.jawr.web;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebListener;

/**
 * The web application initializer
 * 
 * @author Ibrahim Chaehoi
 */
@WebListener
public class WebApplicationInitializer implements ServletContextListener {

	/**
	 * Constructor
	 */
	public WebApplicationInitializer() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.ServletContextListener#contextInitialized(javax.servlet
	 * .ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent evt) {
		ServletContext sc = evt.getServletContext();

		// Initialize Jawr JS servlet
		ServletRegistration.Dynamic sr = sc.addServlet("JavascriptServlet",
				"net.jawr.web.servlet.JawrServlet");
		sr.setInitParameter("configLocation", "/jawr.properties");
		sr.addMapping("*.js");
		sr.setLoadOnStartup(0);

		// Initialize Jawr CSS servlet
		sr = sc.addServlet("CssServlet", "net.jawr.web.servlet.JawrServlet");
		sr.setInitParameter("configLocation", "/jawr.properties");
		sr.setInitParameter("type", JawrConstant.CSS_TYPE);
		sr.addMapping("*.css");
		sr.setLoadOnStartup(1);

		// Initialize Jawr Binary servlet
		sr = sc.addServlet("BinaryServlet", "net.jawr.web.servlet.JawrServlet");
		sr.setInitParameter("configLocation", "/jawr.properties");
		sr.setInitParameter("type", JawrConstant.BINARY_TYPE);
		sr.addMapping("*.jpg", "*.png", "*.gif", "*.woff", "*.ttf", "*.svg", "*.eot");
		sr.setLoadOnStartup(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.
	 * ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent evt) {

	}

}
