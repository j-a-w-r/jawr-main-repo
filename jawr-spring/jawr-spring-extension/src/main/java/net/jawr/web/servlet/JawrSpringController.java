/**
 * Copyright 2013-2016 Jordi Hernández Sellés, Ibrahim Chaehoi 
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

package net.jawr.web.servlet;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jawr.web.JawrConstant;
import net.jawr.web.context.ThreadLocalJawrContext;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.util.UrlPathHelper;

/**
 * A Spring Controller implementation which uses a JawrRequestHandler instance
 * to provide with Jawr functionality within a Spring DispatcherServlet
 * instance.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class JawrSpringController implements Controller, ServletContextAware,
		InitializingBean, ServletContextListener, DisposableBean {

	/** The logger */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(JawrSpringController.class);

	/** The request handler */
	private JawrRequestHandler requestHandler;

	/** The initialization parameters */
	private Map<String, Object> initParams;

	// Init params
	/** The type */
	private String type;

	/** The configuration properties source class */
	private String configPropertiesSourceClass;

	/** The mapping */
	private String mapping;

	/** The controller mappingg */
	private String controllerMapping;

	/** The URL path helper */
	private final UrlPathHelper helper = new UrlPathHelper();

	// Config
	/** The configuration */
	private Properties configuration;

	/** The configuration file location */
	private String configLocation;

	/** The servlet context */
	private ServletContext context;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.context.ServletContextAware#setServletContext
	 * (javax.servlet.ServletContext)
	 */
	public void setServletContext(ServletContext context) {
		this.context = context;
	}

	/**
	 * Sets the configuration file location
	 * 
	 * @param configLocation
	 *            the configuration file location
	 */
	public void setConfigLocation(String configLocation) {
		this.configLocation = configLocation;
	}

	/**
	 * Returns the type of resource handled by the controller
	 * 
	 * @return the type of resource handled by the controller
	 */
	public String getType() {
		return type;
	}

	/**
	 * Returns the init parameters
	 * 
	 * @return the init parameters
	 */
	public Map<String, Object> getInitParams() {
		return initParams;
	}

	/**
	 * Sets the type
	 * 
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Sets the configPropertiesSourceClass
	 * 
	 * @param configPropertiesSourceClass
	 *            the configPropertiesSourceClass to set
	 */
	public void setConfigPropertiesSourceClass(
			String configPropertiesSourceClass) {
		this.configPropertiesSourceClass = configPropertiesSourceClass;
	}

	/**
	 * Sets the mapping
	 * 
	 * @param mapping
	 *            the mapping to set
	 */
	public void setMapping(String mapping) {
		this.mapping = mapping;
	}

	/**
	 * Sets the configuration
	 * 
	 * @param configuration
	 *            the configuration to set
	 */
	public void setConfiguration(Properties configuration) {
		this.configuration = configuration;
	}

	/**
	 * Sets the controller mapping
	 * 
	 * @param controllerMapping
	 *            the controllerMapping to set
	 */
	public void setControllerMapping(String controllerMapping) {
		if (controllerMapping.endsWith("/")) {
			this.controllerMapping = controllerMapping.substring(0,
					controllerMapping.length() - 1);
		} else {
			this.controllerMapping = controllerMapping;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.web.servlet.mvc.Controller#handleRequest(javax.servlet
	 * .http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		String requestedPath = (StringUtils.isEmpty(mapping)) ? helper
				.getPathWithinApplication(request) : helper
				.getPathWithinServletMapping(request);

		if (StringUtils.isNotEmpty(controllerMapping)) {
			requestedPath = requestedPath.substring(controllerMapping.length());
		}

		requestHandler.processRequest(requestedPath, request, response);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		initParams = new HashMap<String, Object>(3);
		initParams.put("type", type);
		initParams.put("configPropertiesSourceClass",
				configPropertiesSourceClass);
		initParams.put("configLocation", configLocation);

		if (null == configuration && null == configLocation
				&& null == configPropertiesSourceClass)
			throw new ServletException(
					"Neither configuration nor configLocation nor configPropertiesSourceClass init params were set."
							+ " You must set at least the configuration or the configLocation param or the configPropertiesSourceClass. Please check your web.xml file");

		String fullMapping = "";
		if (StringUtils.isNotEmpty(mapping))
			fullMapping = mapping;

		if (StringUtils.isNotEmpty(controllerMapping))
			fullMapping = PathNormalizer.joinPaths(fullMapping,
					controllerMapping);

		initParams.put(JawrConstant.SERVLET_MAPPING_PROPERTY_NAME, fullMapping);
		if (mapping != null) {
			initParams.put(JawrConstant.SPRING_SERVLET_MAPPING_PROPERTY_NAME,
					PathNormalizer.asDirPath(mapping));
		}

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Initializing Jawr Controller's JawrRequestHandler");

		if (JawrConstant.BINARY_TYPE.equals(type)) {
			requestHandler = new JawrBinaryResourceRequestHandler(context, initParams,
					configuration);
		} else {
			requestHandler = new JawrRequestHandler(context, initParams,
					configuration);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.servlet.ServletContextListener#contextInitialized(javax.servlet
	 * .ServletContextEvent)
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.
	 * ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		ThreadLocalJawrContext.reset();
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	@Override
	public void destroy() throws Exception {
		requestHandler.destroy();
	}
}
