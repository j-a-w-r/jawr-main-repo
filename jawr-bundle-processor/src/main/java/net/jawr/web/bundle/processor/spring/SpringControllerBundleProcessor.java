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
package net.jawr.web.bundle.processor.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import net.jawr.web.bundle.processor.ServletDefinition;
import net.jawr.web.servlet.JawrSpringController;
import net.jawr.web.servlet.mock.MockServletConfig;
import net.jawr.web.servlet.mock.spring.MockJawrSpringServlet;

import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

/**
 * This class manage the initialization of JawrSpringController.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class SpringControllerBundleProcessor {

	/** The name of the spring servlet */
	private static final String SPRING_DISPATCHER_SERVLET = "SpringDispatcherServlet";

	/**
	 * Initialize the servlets which will handle the request to the JawrSpringController 
	 * @param servletContext the servlet context
	 * @return the list of servlet definition for the JawrSpringControllers
	 * @throws ServletException if a servlet exception occurs
	 */
	@SuppressWarnings("rawtypes")
	public List<ServletDefinition> initJawrSpringServlets(ServletContext servletContext) throws ServletException{
		
		List<ServletDefinition> jawrServletDefinitions = new ArrayList<ServletDefinition>();
		ContextLoader contextLoader = new ContextLoader();
		WebApplicationContext applicationCtx = contextLoader.initWebApplicationContext(servletContext);
		Map<?, ?> jawrControllersMap = applicationCtx.getBeansOfType(JawrSpringController.class);
		
		Iterator<?> entrySetIterator = jawrControllersMap.entrySet().iterator();
		while(entrySetIterator.hasNext()){
			
			JawrSpringController jawrController = (JawrSpringController) ((Map.Entry) entrySetIterator.next()).getValue();
			Map<String, Object> initParams = new HashMap<String, Object>();
			initParams.putAll(jawrController.getInitParams());
			ServletConfig servletConfig = new MockServletConfig(SPRING_DISPATCHER_SERVLET,servletContext, initParams);
			MockJawrSpringServlet servlet = new MockJawrSpringServlet(jawrController, servletConfig);
			ServletDefinition servletDefinition = new ServletDefinition(servlet, servletConfig) ;
			jawrServletDefinitions.add(servletDefinition);
		}
		
		contextLoader.closeWebApplicationContext(servletContext);
		return jawrServletDefinitions;
	}
}
