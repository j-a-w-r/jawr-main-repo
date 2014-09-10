/**
 * Copyright 2007-2012  Jordi Hernández Sellés, Ibrahim Chaehoi
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

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jawr.web.JawrConstant;
import net.jawr.web.cache.CacheManagerFactory;
import net.jawr.web.config.ConfigPropertyResolver;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.config.jmx.JawrApplicationConfigManager;
import net.jawr.web.config.jmx.JawrConfigManager;
import net.jawr.web.config.jmx.JmxUtils;
import net.jawr.web.context.ThreadLocalJawrContext;
import net.jawr.web.exception.BundleDependencyException;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.exception.DuplicateBundlePathException;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.BinaryResourcesHandler;
import net.jawr.web.resource.FileNameUtils;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.factory.PropertiesBasedBundlesHandlerFactory;
import net.jawr.web.resource.bundle.factory.PropsConfigPropertiesSource;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.factory.util.ConfigChangeListener;
import net.jawr.web.resource.bundle.factory.util.ConfigChangeListenerThread;
import net.jawr.web.resource.bundle.factory.util.ConfigPropertiesSource;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.factory.util.PropsFilePropertiesSource;
import net.jawr.web.resource.bundle.factory.util.ServletContextAware;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.handler.ClientSideHandlerScriptRequestHandler;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.BundleRenderer;
import net.jawr.web.resource.handler.bundle.ResourceBundleHandler;
import net.jawr.web.resource.handler.bundle.ServletContextResourceBundleHandler;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.resource.handler.reader.ServletContextResourceReaderHandler;
import net.jawr.web.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Request handling class. Any jawr enabled servlet delegates to this class to
 * handle requests.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class JawrRequestHandler implements ConfigChangeListener, Serializable {

	/** The serial version UID */
	private static final long serialVersionUID = 5762937687546882131L;

	/** The logger */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(JawrRequestHandler.class);

	/** The cache control header parameter name */
	protected static final String CACHE_CONTROL_HEADER = "Cache-Control";

	/** The cache control parameter value */
	protected static final String CACHE_CONTROL_VALUE = "public, max-age=315360000, post-check=315360000, pre-check=315360000";

	/** The last-modified header parameter name */
	protected static final String LAST_MODIFIED_HEADER = "Last-Modified";

	/** The If-modified-since header parameter name */
	protected static final String IF_MODIFIED_SINCE_HEADER = "If-Modified-Since";

	/** The If-non-match-since header parameter name */
	protected static final String IF_NONE_MATCH_HEADER = "If-None-Match";

	/** The last-modified value */
	protected static final String LAST_MODIFIED_VALUE = "Sun, 06 Nov 2005 12:00:00 GMT";

	/** The ETag header parameter name */
	protected static final String ETAG_HEADER = "ETag";

	/** The ETag parameter value */
	protected static final String ETAG_VALUE = "2740050219";

	/** The expires header parameter name */
	protected static final String EXPIRES_HEADER = "Expires";

	/** The configuration property name for the reload interval */
	protected static final String CONFIG_RELOAD_INTERVAL = "jawr.config.reload.interval";

	/** The generation parameter */
	public static final String GENERATION_PARAM = "generationConfigParam";

	/** The client side request handler */
	public static final String CLIENTSIDE_HANDLER_REQ_PATH = "/jawr_loader.js";

	/** The generated image pattern */
	private static final Pattern GENERATED_BINARY_RESOURCE_PATTERN = Pattern
			.compile("(url\\(([\"' ]*))(([a-zA-Z]+)(?! (http|data)):(/)?)([^\\)\"']*)([\"']?\\))");

	/** The resource bundles handler */
	protected ResourceBundlesHandler bundlesHandler;

	/** The resource reader handler */
	protected ResourceReaderHandler rsReaderHandler;

	/** The content type */
	protected String contentType;

	/** The resource type */
	protected String resourceType;

	/** The servlet context */
	protected ServletContext servletContext;

	/** The maps for the init-parameters */
	protected Map<String, Object> initParameters;

	/** The Thread which listen the configuration changes */
	protected ConfigChangeListenerThread configChangeListenerThread;

	/** The generator registry */
	protected GeneratorRegistry generatorRegistry;

	/** The jawr config */
	protected JawrConfig jawrConfig;

	/** The configuration properties source */
	protected ConfigPropertiesSource propertiesSource;

	/** The configuration property resolver */
	protected ConfigPropertyResolver configPropResolver;

	/**
	 * The configuration properties which overrides the one defined with the
	 * propertiesSource
	 */
	protected Properties overrideProperties;

	/** The client-side script request handler */
	protected ClientSideHandlerScriptRequestHandler clientSideScriptRequestHandler;

	/** The handler for the illegal bundle request */
	protected IllegalBundleRequestHandler illegalBundleRequestHandler;

	/**
	 * Reads the properties file and initializes all configuration using the
	 * ServletConfig object. If applicable, a ConfigChangeListenerThread will be
	 * started to listen to changes in the properties configuration.
	 * 
	 * @param servletContext
	 *            ServletContext
	 * @param servletConfig
	 *            ServletConfig
	 * @throws ServletException
	 *             if an exception occurs
	 */
	@SuppressWarnings("unchecked")
	public JawrRequestHandler(ServletContext context, ServletConfig config)
			throws ServletException {
		this.initParameters = new HashMap<String, Object>();
		Enumeration<String> params = config.getInitParameterNames();
		while (params.hasMoreElements()) {
			String param = (String) params.nextElement();
			initParameters.put(param, config.getInitParameter(param));
		}
		initParameters.put("handlerName", config.getServletName());

		if (LOGGER.isInfoEnabled())
			LOGGER.info("Initializing jawr config for servlet named "
					+ config.getServletName());

		initRequestHandler(context, null);

	}

	/**
	 * Alternate constructor that does not need a ServletConfig object.
	 * Parameters normally read from it are read from the initParams Map, and
	 * the configProps are used instead of reading a .properties file.
	 * 
	 * @param servletContext
	 *            ServletContext
	 * @param servletConfig
	 *            ServletConfig
	 * @throws ServletException
	 *             if an exception occurs
	 */
	public JawrRequestHandler(ServletContext context,
			Map<String, Object> initParams, Properties configProps)
			throws ServletException {

		this.initParameters = initParams;
		initRequestHandler(context, configProps);
	}

	/**
	 * Initialize the request handler
	 * 
	 * @param context
	 *            the servlet context
	 * @param configProps
	 *            the configuration properties
	 * @throws ServletException
	 *             if an exception occurs
	 */
	private void initRequestHandler(ServletContext context,
			Properties configProps) throws ServletException {

		long initialTime = System.currentTimeMillis();
		if (LOGGER.isInfoEnabled())
			LOGGER.info("Initializing jawr config for request handler named "
					+ getInitParameter("handlerName"));

		this.servletContext = context;
		this.overrideProperties = configProps;
		resourceType = getInitParameter("type");
		resourceType = null == resourceType ? "js" : resourceType;

		// Initialize the config properties source that will provide with all
		// configuration options.
		ConfigPropertiesSource propsSrc = initConfigPropertiesSource(context,
				configProps);

		// Read properties from properties source
		Properties props = propsSrc.getConfigProperties();
		// override the properties if needed
		if (this.overrideProperties != null) {
			props.putAll(overrideProperties);
		}

		// hang onto the propertiesSource for manual reloads
		this.propertiesSource = propsSrc;

		// Initialize the ConfigPropertyResolver
		initConfigPropertyResolver(context);

		initializeJawrContext(props);

		// Initialize the properties reloading checker daemon if specified
		if (!ThreadLocalJawrContext.isBundleProcessingAtBuildTime()
				&& null != props.getProperty(CONFIG_RELOAD_INTERVAL)) {
			int interval = Integer.valueOf(
					props.getProperty(CONFIG_RELOAD_INTERVAL)).intValue();
			LOGGER.warn("Jawr started with configuration auto reloading on. "
					+ "Be aware that a daemon thread will be checking for changes to configuration every "
					+ interval + " seconds.");

			this.configChangeListenerThread = new ConfigChangeListenerThread(
					propsSrc, this.overrideProperties, this, interval);
			configChangeListenerThread.start();
		}

		if (LOGGER.isInfoEnabled()) {
			long totaltime = System.currentTimeMillis() - initialTime;
			LOGGER.info("Init method succesful. jawr started in "
					+ (totaltime / 1000) + " seconds....");
		}

		// Reset ThreadLocalJawrContext
		ThreadLocalJawrContext.reset();
	}

	/**
	 * Initialize the Jawr context (config, cache manager, application config
	 * manager...)
	 * 
	 * @param props
	 *            the Jawr properties
	 * 
	 * @throws ServletException
	 *             if an exception occurs
	 */
	protected void initializeJawrContext(Properties props)
			throws ServletException {

		// Initialize config
		initializeJawrConfig(props);

		// initialize the cache manager
		initializeApplicationCacheManager();

		// initialize the Application config manager
		JawrApplicationConfigManager appConfigMgr = initApplicationConfigManager();

		JmxUtils.initJMXBean(appConfigMgr, servletContext, resourceType,
				props.getProperty(JawrConstant.JAWR_JMX_MBEAN_PREFIX));
	}

	/**
	 * Resets the cache manager
	 */
	private void initializeApplicationCacheManager() {

		CacheManagerFactory.resetCacheManager(jawrConfig, resourceType);
	}

	/**
	 * Initialize the application config manager
	 * 
	 * @return the application config manager
	 */
	private JawrApplicationConfigManager initApplicationConfigManager() {

		JawrApplicationConfigManager appConfigMgr = (JawrApplicationConfigManager) servletContext
				.getAttribute(JawrConstant.JAWR_APPLICATION_CONFIG_MANAGER);
		if (appConfigMgr == null) {
			appConfigMgr = new JawrApplicationConfigManager();
			servletContext.setAttribute(
					JawrConstant.JAWR_APPLICATION_CONFIG_MANAGER, appConfigMgr);
		}

		// Create the config manager for the current Request Handler
		JawrConfigManager configMgr = new JawrConfigManager(this,
				jawrConfig.getConfigProperties());

		// Initialize the jawrApplicationConfigManager
		if (resourceType.equals(JawrConstant.JS_TYPE)) {
			appConfigMgr.setJsMBean(configMgr);
		} else if (resourceType.equals(JawrConstant.CSS_TYPE)) {
			appConfigMgr.setCssMBean(configMgr);
		} else {
			appConfigMgr.setImgMBean(configMgr);
		}
		return appConfigMgr;
	}

	/**
	 * Initialize the config property resolver
	 * 
	 * @param context
	 *            the servlet context
	 */
	private void initConfigPropertyResolver(ServletContext context) {
		String configPropertyResolverClass = getInitParameter("configPropertyResolverClass");
		// Load a custom class to set configPropertyResolver
		configPropResolver = null;
		if (null != configPropertyResolverClass) {
			configPropResolver = (ConfigPropertyResolver) ClassLoaderResourceUtils
					.buildObjectInstance(configPropertyResolverClass);
			if (configPropResolver instanceof ServletContextAware) {
				((ServletContextAware) configPropResolver)
						.setServletContext(context);
			}
		}
	}

	/**
	 * Initialize the config properties source that will provide with all
	 * configuration options.
	 * 
	 * @param context
	 *            the servlet context
	 * @param configProps
	 *            the config properties
	 * @return the config properties source
	 * @throws ServletException
	 *             if an exception occurs
	 */
	private ConfigPropertiesSource initConfigPropertiesSource(
			ServletContext context, Properties configProps)
			throws ServletException {

		String configLocation = getInitParameter("configLocation");
		String configPropsSourceClass = getInitParameter("configPropertiesSourceClass");
		if (null == configProps && null == configLocation
				&& null == configPropsSourceClass)
			throw new ServletException(
					"Neither configLocation nor configPropertiesSourceClass init params were set."
							+ " You must set at least the configLocation param. Please check your web.xml file");

		// Initialize the config properties source that will provide with all
		// configuration options.
		ConfigPropertiesSource propsSrc = null;

		// Load a custom class to set config properties
		if (null != configPropsSourceClass) {
			propsSrc = (ConfigPropertiesSource) ClassLoaderResourceUtils
					.buildObjectInstance(configPropsSourceClass);
			if (propsSrc instanceof ServletContextAware) {
				((ServletContextAware) propsSrc).setServletContext(context);
			}
		} else if (configLocation == null && configProps != null) {

			// configuration retrieved from the in memory configuration
			// properties
			propsSrc = new PropsConfigPropertiesSource(configProps);

		} else {
			// Default config properties source, reads from a .properties file
			// in the classpath.
			propsSrc = new PropsFilePropertiesSource();
		}

		// If a custom properties source is a subclass of
		// PropsFilePropertiesSource, we hand it the configLocation param.
		// This affects the standard one as well.
		if (propsSrc instanceof PropsFilePropertiesSource)
			((PropsFilePropertiesSource) propsSrc)
					.setConfigLocation(configLocation);
		return propsSrc;
	}

	/**
	 * Returns the init parameter value from the parameter name
	 * 
	 * @param paramName
	 *            the parameter name
	 * @return the init parameter value
	 */
	private String getInitParameter(String paramName) {
		return (String) initParameters.get(paramName);
	}

	/**
	 * Initialize the Jawr config
	 * 
	 * @param props
	 *            the properties
	 * @throws ServletException
	 *             if an exception occurs
	 */
	protected void initializeJawrConfig(Properties props)
			throws ServletException {

		// init registry
		generatorRegistry = new GeneratorRegistry(resourceType);

		// Initialize config
		if (null != jawrConfig) {
			jawrConfig.invalidate();
		}

		createJawrConfig(props);

		jawrConfig.setContext(servletContext);
		jawrConfig.setGeneratorRegistry(generatorRegistry);

		// Set the content type to be used for every request.
		contentType = "text/";
		contentType += "js".equals(resourceType) ? "javascript" : "css";
		contentType += "; charset=" + jawrConfig.getResourceCharset().name();

		// Set mapping, to be used by the tag lib to define URLs that point to
		// this servlet.
		String mapping = (String) initParameters.get("mapping");
		if (null != mapping)
			jawrConfig.setServletMapping(mapping);

		if (jawrConfig.isCssClasspathImageHandledByClasspathCss()
				&& resourceType.equals("css")) {
			BinaryResourcesHandler binaryRsHandler = (BinaryResourcesHandler) servletContext
					.getAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE);
			if (binaryRsHandler == null) {
				LOGGER.error("You are using the CSS classpath image feature, but the JAWR Binary servlet is not yet initialized.\n"
						+ "The JAWR Binary servlet must be initialized before the JAWR CSS servlet.\n"
						+ "Please check you web application configuration.");
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Configuration read. Current config:");
			LOGGER.debug(jawrConfig.toString());
		}

		// Initialize the IllegalBundleRequest handler
		initIllegalBundleRequestHandler();

		// Create a resource handler to read files from the WAR archive or
		// exploded dir.
		rsReaderHandler = initResourceReaderHandler();
		ResourceBundleHandler rsBundleHandler = initResourceBundleHandler();
		PropertiesBasedBundlesHandlerFactory factory = new PropertiesBasedBundlesHandlerFactory(
				props, resourceType, rsReaderHandler, rsBundleHandler,
				jawrConfig);
		try {
			bundlesHandler = factory.buildResourceBundlesHandler();
		} catch (DuplicateBundlePathException e) {
			throw new ServletException(e);
		} catch (BundleDependencyException e) {
			throw new ServletException(e);
		}

		if (resourceType.equals(JawrConstant.JS_TYPE))
			servletContext.setAttribute(JawrConstant.JS_CONTEXT_ATTRIBUTE,
					bundlesHandler);
		else
			servletContext.setAttribute(JawrConstant.CSS_CONTEXT_ATTRIBUTE,
					bundlesHandler);

		this.clientSideScriptRequestHandler = new ClientSideHandlerScriptRequestHandler(
				bundlesHandler, jawrConfig);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("content type set to: " + contentType);
		}

		// Warn when in debug mode
		if (jawrConfig.isDebugModeOn()) {
			LOGGER.warn("Jawr initialized in DEVELOPMENT MODE. Do NOT use this mode in production or integration servers. ");
		}
	}

	/**
	 * Initialize the illegal bundle request handler
	 */
	protected void initIllegalBundleRequestHandler() {
		String illegalBundleRequestandlerClassName = jawrConfig
				.getProperty(JawrConstant.ILLEGAL_BUNDLE_REQUEST_HANDLER);
		if (illegalBundleRequestandlerClassName != null) {
			illegalBundleRequestHandler = (IllegalBundleRequestHandler) ClassLoaderResourceUtils
					.buildObjectInstance(illegalBundleRequestandlerClassName);
		} else {
			illegalBundleRequestHandler = new IllegalBundleRequestHandlerImpl();
		}
	}

	/**
	 * Initialize the resource reader handler
	 * 
	 * @return the resource reader handler
	 */
	protected ResourceReaderHandler initResourceReaderHandler() {
		ResourceReaderHandler rsHandler = null;
		if (servletContext != null) {
			try {
				rsHandler = new ServletContextResourceReaderHandler(
						servletContext, jawrConfig, generatorRegistry);
			} catch (IOException e) {
				throw new BundlingProcessException(e);
			}
		}

		return rsHandler;
	}

	/**
	 * Initialize the resource bundle handler
	 * 
	 * @return the resource bundle handler
	 */
	protected ResourceBundleHandler initResourceBundleHandler() {
		ResourceBundleHandler rsHandler = null;
		if (jawrConfig.getUseBundleMapping()
				&& StringUtils.isNotEmpty(jawrConfig.getJawrWorkingDirectory())) {
			rsHandler = new ServletContextResourceBundleHandler(servletContext,
					jawrConfig.getJawrWorkingDirectory(),
					jawrConfig.getResourceCharset(),
					jawrConfig.getGeneratorRegistry(), resourceType);
		} else {
			rsHandler = new ServletContextResourceBundleHandler(servletContext,
					jawrConfig.getResourceCharset(),
					jawrConfig.getGeneratorRegistry(), resourceType);
		}
		return rsHandler;
	}

	/**
	 * Create the Jawr config from the properties
	 * 
	 * @param props
	 *            the properties
	 */
	protected JawrConfig createJawrConfig(Properties props) {
		jawrConfig = new JawrConfig(resourceType, props, configPropResolver);

		// Override properties which are incompatible with the build time bundle
		// processing
		if (ThreadLocalJawrContext.isBundleProcessingAtBuildTime()) {
			jawrConfig.setUseBundleMapping(true);

			// Use the standard working directory
			jawrConfig.setJawrWorkingDirectory(null);
		}

		return jawrConfig;
	}

	/**
	 * Handles a resource request by getting the requested path from the request
	 * object and invoking processRequest.
	 * 
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @throws ServletException
	 *             if a servlet exception occurs
	 * @throws IOException
	 *             if an IO exception occurs.
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {

			String requestedPath = "".equals(jawrConfig.getServletMapping()) ? request
					.getServletPath() : request.getPathInfo();
			processRequest(requestedPath, request, response);

		} catch (Exception e) {

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("ServletException : ", e);
			}

			throw new ServletException(e);
		}
	}

	/**
	 * Handles a resource request.
	 * <ul>
	 * <li>If the request contains an If-Modified-Since header, the 304 status
	 * is set and no data is written to the response</li>
	 * <li>If the requested path begins with the gzip prefix, a gzipped version
	 * of the resource is served, with the corresponding content-encoding
	 * header.</li>
	 * <li>Otherwise, the resource is written as text to the response.</li>
	 * <li>If the resource is not found, the response satus is set to 404 and no
	 * response is written.</li>
	 * </ul>
	 * 
	 * @param requestedPath
	 *            the requested path
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @throws ServletException
	 *             if a servlet exception occurs
	 * @throws IOException
	 *             if an IO exception occurs
	 */
	public void processRequest(String requestedPath,
			HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {
			// Initialize the Thread local for the Jawr context
			ThreadLocalJawrContext
					.setJawrConfigMgrObjectName(JmxUtils.getJawrConfigMBeanObjectName(
							request.getContextPath(),
							resourceType,
							jawrConfig
									.getProperty(JawrConstant.JAWR_JMX_MBEAN_PREFIX)));

			ThreadLocalJawrContext.setRequest(request.getRequestURL()
					.toString());

			RendererRequestUtils.setRequestDebuggable(request, jawrConfig);

			// manual reload request
			if (this.jawrConfig.getRefreshKey().length() > 0
					&& null != request.getParameter("refreshKey")
					&& this.jawrConfig.getRefreshKey().equals(
							request.getParameter("refreshKey"))) {
				this.configChanged(propertiesSource.getConfigProperties());
			}

			if (LOGGER.isDebugEnabled())
				LOGGER.debug("Request received for path:" + requestedPath);

			String contentType = getContentType(requestedPath, request);

			if (handleSpecificRequest(requestedPath, contentType, request,
					response)) {
				return;
			}

			// Handle the strict mode
			boolean validBundle = isValidBundle(requestedPath);
			processRequest(requestedPath, request, response, contentType,
					validBundle);
		} finally {

			// Reset the Thread local for the Jawr context
			ThreadLocalJawrContext.reset();
		}
	}

	/**
	 * Handle the specific requests
	 * 
	 * @param requestedPath
	 *            the requested path
	 * @param contentType
	 *            the content type
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @return true if the request has been processed
	 * @throws ServletException
	 *             if a servlet exception occurs
	 * @throws IOException
	 *             if a IO exception occurs
	 */
	protected boolean handleSpecificRequest(String requestedPath,
			String contentType, HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		boolean processed = false;
		if (CLIENTSIDE_HANDLER_REQ_PATH.equals(requestedPath)) {
			this.clientSideScriptRequestHandler.handleClientSideHandlerRequest(
					request, response);
			processed = true;
		} else {

			// CSS images would be requested through this handler in case
			// servletMapping is used
			if (JawrConstant.CSS_TYPE.equals(resourceType)
					&& !JawrConstant.CSS_TYPE
							.equals(getExtension(requestedPath))
			) {

				if (null == bundlesHandler.resolveBundleForPath(requestedPath)) {
					if (LOGGER.isDebugEnabled())
						LOGGER.debug("Path '"
								+ requestedPath
								+ "' does not belong to a bundle. Forwarding request to the server. ");
					request.getRequestDispatcher(requestedPath).forward(
							request, response);
					processed = true;
				}
			}
		}

		return processed;
	}

	/**
	 * Returns true if the bundle is a valid bundle
	 * 
	 * @param requestedPath
	 *            the requested path
	 * @return true if the bundle is a valid bundle
	 */
	protected boolean isValidBundle(String requestedPath) {
		boolean validBundle = true;
		if (!jawrConfig.isDebugModeOn() && jawrConfig.isStrictMode()) {
			validBundle = bundlesHandler
					.containsValidBundleHashcode(requestedPath);
		}
		return validBundle;
	}

	/**
	 * Process the request
	 * 
	 * @param requestedPath
	 *            the requested path
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @param validBundle
	 *            the flag indicating if the requested bundle is a valid bundle
	 * @throws IOException
	 *             if an IOException occurs
	 */
	protected void processRequest(String requestedPath,
			HttpServletRequest request, HttpServletResponse response,
			String contentType, boolean validBundle) throws IOException {

		boolean writeResponseHeader = false;

		if (this.jawrConfig.isDebugModeOn()
				&& null != request.getParameter(GENERATION_PARAM))
			requestedPath = request.getParameter(GENERATION_PARAM);

		// If debug mode is off, check for If-Modified-Since and If-none-match
		// headers and set response caching headers.
		if (!this.jawrConfig.isDebugModeOn()) {
			// If a browser checks for changes, always respond 'no changes'.
			if (validBundle
					&& (null != request.getHeader(IF_MODIFIED_SINCE_HEADER) || null != request
							.getHeader(IF_NONE_MATCH_HEADER))) {
				response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("Returning 'not modified' header. ");
				return;
			}

			if (validBundle) {
				// Add caching headers
				setResponseHeaders(response);
			} else {

				writeResponseHeader = illegalBundleRequestHandler
						.writeResponseHeader(requestedPath, request, response);
				if (!writeResponseHeader) {
					// Add caching headers
					setResponseHeaders(response);
				}
			}
		}

		try {
			if (validBundle
					|| illegalBundleRequestHandler.canWriteContent(
							requestedPath, request)) {
				// By setting content type, the response writer will use
				// appropriate encoding
				response.setContentType(contentType);
				writeContent(requestedPath, request, response);
				if (LOGGER.isDebugEnabled())
					LOGGER.debug("request succesfully attended");
			} else {
				if (!writeResponseHeader) {
					logBundleNotFound(requestedPath);
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
				}
			}
		} catch (EOFException eofex) {
			LOGGER.debug("Browser cut off response", eofex);
		} catch (ResourceNotFoundException e) {
			logBundleNotFound(requestedPath);
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
	}

	/**
	 * Logs that the requested bundle was not found
	 * 
	 * @param requestedPath
	 */
	private void logBundleNotFound(String requestedPath) {
		if (LOGGER.isInfoEnabled())
			LOGGER.info("Received a request for a non existing bundle: "
					+ requestedPath);
	}

	/**
	 * Returns the content type of the requested path
	 * 
	 * @param requestedPath
	 *            the requested path
	 * @param request
	 *            the request
	 * @return the content type of the requested path
	 */
	protected String getContentType(String requestedPath,
			HttpServletRequest request) {
		return contentType;
	}

	/**
	 * Writes the content to the ouput stream
	 * 
	 * @param requestedPath
	 *            the requested path
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @throws IOException
	 *             if an IOException occurs
	 * @throws ResourceNotFoundException
	 *             if the resource is not found
	 */
	protected void writeContent(String requestedPath,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ResourceNotFoundException {

		// Send gzipped resource if user agent supports it.
		int idx = requestedPath.indexOf(BundleRenderer.GZIP_PATH_PREFIX);
		if (idx != -1) {
			
			requestedPath = "/"
					+ requestedPath.substring(
							idx+BundleRenderer.GZIP_PATH_PREFIX.length(),
							requestedPath.length());
			if (isValidRequestedPath(requestedPath)) {
				response.setHeader("Content-Encoding", "gzip");
				bundlesHandler.streamBundleTo(requestedPath,
						response.getOutputStream());
			} else {
				throw new ResourceNotFoundException(requestedPath);
			}

		} else {

			// In debug mode, we take in account the image generated from a
			// StreamGenerator like classpath Image generator
			// The following code will rewrite the URL path for the generated
			// images,
			// because in debug mode, we are retrieving the CSS ressources
			// directly from the webapp
			// and if the CSS contains generated images, we should rewrite the
			// URL.
			BinaryResourcesHandler imgRsHandler = (BinaryResourcesHandler) servletContext
					.getAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE);
			if (imgRsHandler != null && this.jawrConfig.isDebugModeOn()
					&& resourceType.equals(JawrConstant.CSS_TYPE)) {

				handleGeneratedCssInDebugMode(requestedPath, request, response,
						imgRsHandler);
			} else {

				if (isValidRequestedPath(requestedPath)) {
					Writer out = response.getWriter();
					bundlesHandler.writeBundleTo(requestedPath, out);
				} else {
					throw new ResourceNotFoundException(requestedPath);
				}
			}
		}
	}

	/**
	 * Handle the generated CSS content in debug mode.
	 * 
	 * @param requestedPath
	 *            the request path
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @param binaryRsHandler
	 *            the image resource handler
	 * @throws ResourceNotFoundException
	 *             if the resource is not found
	 * @throws IOException
	 *             if an IO exception occurs
	 */
	private void handleGeneratedCssInDebugMode(String requestedPath,
			HttpServletRequest request, HttpServletResponse response,
			BinaryResourcesHandler binaryRsHandler)
			throws ResourceNotFoundException, IOException {

		// Write the content of the CSS in the Stringwriter
		Writer writer = new StringWriter();
		Reader rd = rsReaderHandler.getResource(requestedPath);
		if (rd == null) {
			throw new ResourceNotFoundException(requestedPath);
		}

		IOUtils.copy(rd, writer);
		String content = writer.toString();

		String imageServletMapping = binaryRsHandler.getConfig()
				.getServletMapping();

		if (imageServletMapping == null) {
			imageServletMapping = "";
		}

		String requestPath = getRequestPath(request);

		// Define the replacement pattern for the generated binary resource (like
		// jar:img/myImg.png)
		String relativeRootUrlPath = PathNormalizer
				.getRootRelativePath(requestPath);
		String replacementPattern = PathNormalizer.normalizePath("$1"
				+ relativeRootUrlPath + imageServletMapping
				+ "/$4_cbDebug/$7$8");

		Matcher matcher = GENERATED_BINARY_RESOURCE_PATTERN.matcher(content);

		// Rewrite the images define in the classpath, to point to the image
		// servlet
		StringBuffer result = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(result, replacementPattern);
		}
		matcher.appendTail(result);
		Writer out = response.getWriter();
		out.write(result.toString());
	}

	/**
	 * Checks if the path is valid and can be accessed.
	 * 
	 * @param requestedPath
	 *            the requested path
	 * @return true if the path is valid and can be accessed.
	 */
	protected boolean isValidRequestedPath(String requestedPath) {

		boolean result = true;
		if (!this.jawrConfig.isDebugModeOn()
				&& requestedPath.startsWith(JawrConstant.WEB_INF_DIR_PREFIX)
				|| requestedPath.startsWith(JawrConstant.META_INF_DIR_PREFIX)) {
			result = false;
		} else {
			// If it's not a generated path check the extension file
			if (this.jawrConfig.isDebugModeOn()
					&& !generatorRegistry.isPathGenerated(requestedPath)) {
				String extension = FileNameUtils.getExtension(requestedPath);
				if (!extension.toLowerCase().equals(resourceType)) {
					result = false;
				}
			}
		}

		return result;
	}

	/**
	 * Returns the extension for the requested path
	 * 
	 * @param requestedPath
	 *            the requested path
	 * @return the extension for the requested path
	 */
	protected String getExtension(String requestedPath) {

		return FileNameUtils.getExtension(requestedPath);
	}

	/**
	 * Returns the request path
	 * 
	 * @param request
	 *            the request
	 * @return the request path
	 */
	private String getRequestPath(HttpServletRequest request) {

		String finalUrl = null;
		String servletPath = request.getServletPath();
		if ("".equals(jawrConfig.getServletMapping())) {
			finalUrl = PathNormalizer.asPath(servletPath);
		} else {
			finalUrl = PathNormalizer.asPath(servletPath
					+ request.getPathInfo());
		}
		return finalUrl;
	}

	/**
	 * Adds aggresive caching headers to the response in order to prevent
	 * browsers requesting the same file twice.
	 * 
	 * @param resp
	 *            the response
	 */
	protected void setResponseHeaders(HttpServletResponse resp) {
		// Force resource caching as best as possible
		resp.setHeader(CACHE_CONTROL_HEADER, CACHE_CONTROL_VALUE);
		resp.setHeader(LAST_MODIFIED_HEADER, LAST_MODIFIED_VALUE);
		resp.setHeader(ETAG_HEADER, ETAG_VALUE);
		Calendar cal = Calendar.getInstance();
		cal.roll(Calendar.YEAR, 10);
		resp.setDateHeader(EXPIRES_HEADER, cal.getTimeInMillis());
	}

	/**
	 * Analog to Servlet.destroy(), should be invoked whenever the app is
	 * redeployed.
	 */
	public void destroy() {
		// Stop the config change listener.
		if (null != this.configChangeListenerThread) {
			configChangeListenerThread.stopPolling();
		}
		ThreadLocalJawrContext.reset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.factory.util.ConfigChangeListener#configChanged
	 * (java.util.Properties)
	 */
	public synchronized void configChanged(Properties newConfig) {
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Reloading Jawr configuration");
		try {
			// Initialize the Thread local for the Jawr context
			ThreadLocalJawrContext
					.setJawrConfigMgrObjectName(JmxUtils.getMBeanObjectName(
							servletContext,
							resourceType,
							jawrConfig
									.getProperty(JawrConstant.JAWR_JMX_MBEAN_PREFIX)));

			Properties props = propertiesSource.getConfigProperties();
			// override the properties if needed
			if (this.overrideProperties != null) {
				props.putAll(overrideProperties);
			}
			props.putAll(newConfig);
			initializeJawrContext(props);
		} catch (Exception e) {
			throw new BundlingProcessException("Error reloading Jawr config: "
					+ e.getMessage(), e);
		} finally {

			// Reset the Thread local for the Jawr context
			ThreadLocalJawrContext.reset();
		}

		if (LOGGER.isDebugEnabled())
			LOGGER.debug("Jawr configuration succesfully reloaded. ");
	}
}
