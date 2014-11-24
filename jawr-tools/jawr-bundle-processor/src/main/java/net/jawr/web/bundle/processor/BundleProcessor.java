/**
 * Copyright 2009-2010 Ibrahim Chaehoi
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import net.jawr.web.JawrConstant;
import net.jawr.web.bundle.processor.renderer.BasicBundleRenderer;
import net.jawr.web.bundle.processor.renderer.RenderedLink;
import net.jawr.web.bundle.processor.spring.SpringControllerBundleProcessor;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.context.ThreadLocalJawrContext;
import net.jawr.web.resource.BinaryResourcesHandler;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.BundleRendererContext;
import net.jawr.web.resource.bundle.variant.VariantUtils;
import net.jawr.web.servlet.JawrRequestHandler;
import net.jawr.web.servlet.JawrServlet;
import net.jawr.web.servlet.mock.MockServletConfig;
import net.jawr.web.servlet.mock.MockServletContext;
import net.jawr.web.servlet.mock.MockServletRequest;
import net.jawr.web.servlet.mock.MockServletResponse;
import net.jawr.web.servlet.mock.MockServletSession;
import net.jawr.web.util.FileUtils;
import net.jawr.web.util.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The bundle processor is managing the bundle processing at build time.
 * 
 * @author Ibrahim Chaehoi
 */
public class BundleProcessor {

	/** The logger */
	private static Logger logger = LoggerFactory.getLogger(BundleProcessor.class);

	/** The default servlet API version */
	private static final String DEFAULT_SERVLET_API_VERSION_2_3 = "2.3";

	/** The jawr bundle processor context path */
	private static final String JAWR_BUNDLE_PROCESSOR_CONTEXT_PATH = "jawr-bundle-processor";

	/** The jar file extension */
	private static final String JAR_FILE_EXTENSION = ".jar";

	/** The /WEB-INF/lib directory path */
	private static final String WEB_INF_LIB_DIR_PATH = "/WEB-INF/lib/";

	/** The /WEB-INF/classes directory path */
	private static final String WEB_INF_CLASSES_DIR_PATH = "/WEB-INF/classes/";

	/** The path to the web.xml file from the web application root directory */
	private static final String WEB_XML_FILE_PATH = "WEB-INF/web.xml";

	/** The name of the param-name tag */
	private static final String PARAM_NAME_TAG_NAME = "param-name";

	/** The name of the param-value tag */
	private static final String PARAM_VALUE_TAG_NAME = "param-value";

	/** The name of the servlet tag */
	private static final String SERVLET_TAG_NAME = "servlet";

	/** The name of the listener tag */
	private static final String CONTEXT_TAG_NAME = "context-param";

	/** The name of the servlet-class tag */
	private static final String SERVLET_CLASS_TAG_NAME = "servlet-class";

	/** The name of the servlet-name tag */
	private static final String SERVLET_NAME_TAG_NAME = "servlet-name";

	/** The name of the param-value tag */
	private static final String INIT_PARAM_TAG_NAME = "init-param";

	/** The name of the load on startup tag */
	private static final String LOAD_ON_STARTUP_TAG_NAME = "load-on-startup";

	/** The init type parameter */
	private static final String TYPE_INIT_PARAMETER = "type";

	/** The CDN directory name */
	private static final String CDN_DIR_NAME = "/CDN";

	/** The parameter name of the spring context config location */
	public static final String CONFIG_LOCATION_PARAM = "contextConfigLocation";

	// The following constants are related to the jawr-apache-httpd.conf file

	/** The path to the template Jawr apache HTTPD conf */
	private static final String TEMPLATE_JAWR_APACHE_HTTPD_CONF_PATH = "/net/jawr/web/bundle/resource/template-jawr-apache-httpd.conf";

	/** The file name of the jawr-apache-httpd.conf file */
	private static final String JAWR_APACHE_HTTPD_CONF_FILE = "jawr-apache-httpd.conf";

	/**
	 * The statement which define that we should check the JS servlet mapping is
	 * defined, before processing the next line
	 */
	private static final String CHECKS_JAWR_JS_SERVLET_MAPPING_EXISTS = "## if <jawr.js.servlet.mapping>";

	/**
	 * The statement which define that we should check the CSS servlet mapping
	 * is defined, before processing the next line
	 */
	private static final String CHECK_JAWR_CSS_SERVLET_MAPPING_EXISTS = "## if <jawr.css.servlet.mapping>";

	/** The pattern for the jawr image servlet mapping in the template file */
	private static final String JAWR_IMG_SERVLET_MAPPING_PATTERN = "<jawr\\.img\\.servlet\\.mapping>";

	/** The pattern for the jawr CSS servlet mapping in the template file */
	private static final String JAWR_CSS_SERVLET_MAPPING_PATTERN = "<jawr\\.css\\.servlet\\.mapping>";

	/** The pattern for the jawr JS servlet mapping in the template file */
	private static final String JAWR_JS_SERVLET_MAPPING_PATTERN = "<jawr\\.js\\.servlet\\.mapping>";

	/** The root directory which will contains the resource on the CDN */
	private static final String APP_ROOT_DIR_PATTERN = "<app\\.root\\.dir>";

	/** The default web application URL */
	private static final String DEFAULT_WEBAPP_URL = "{WEBAPP_URL}";


	/**
	 * Launch the bundle processing
	 * 
	 * @param baseDirPath
	 *            the base directory path
	 * @param tmpDirPath
	 *            the temp directory path
	 * @param destDirPath
	 *            the destination directory path
	 * @param generateCdnFiles
	 *            the flag indicating if we should generate the CDN files or not
	 * @throws Exception
	 *             if an exception occurs
	 */
	public void process(String baseDirPath, String tmpDirPath,
			String destDirPath, boolean generateCdnFiles) throws Exception {
		process(baseDirPath, tmpDirPath, destDirPath, generateCdnFiles,
				DEFAULT_SERVLET_API_VERSION_2_3);
	}

	/**
	 * Launch the bundle processing
	 * 
	 * @param baseDirPath
	 *            the base directory path
	 * @param tmpDirPath
	 *            the temp directory path
	 * @param destDirPath
	 *            the destination directory path
	 * @param generateCdnFiles
	 *            the flag indicating if we should generate the CDN files or not
	 * @throws Exception
	 *             if an exception occurs
	 */
	public void process(String baseDirPath, String tmpDirPath,
			String destDirPath, boolean generateCdnFiles,
			String servletApiVersion) throws Exception {

		process(baseDirPath, tmpDirPath, destDirPath, null,
				new ArrayList<String>(), generateCdnFiles, false,
				servletApiVersion);
	}

	/**
	 * Launch the bundle processing
	 * 
	 * @param baseDirPath
	 *            the base directory path
	 * @param tmpDirPath
	 *            the temp directory path
	 * @param destDirPath
	 *            the destination directory path
	 * @param springConfigFiles
	 *            the spring config file to initialize
	 * @param propertyPlaceHolderFile
	 *            the path to the property place holder file
	 * @param servletNames
	 *            the list of the name of servlets to initialized
	 * @param generateCdnFiles
	 *            the flag indicating if we should generate the CDN files or not
	 * @param keepUrlMapping
	 *            the flag indicating if we should keep the URL mapping or not.
	 * @param servletApiVersion
	 *            the servlet API version (ex: "2.3" or "2.5")
	 * @throws Exception
	 *             if an exception occurs
	 */
	public void process(String baseDirPath, String tmpDirPath,
			String destDirPath, String springConfigFiles,
			List<String> servletsToInitialize, boolean generateCdnFiles,
			boolean keepUrlMapping, String servletApiVersion) throws Exception {

		// Creates the web app class loader
		ClassLoader webAppClassLoader = initClassLoader(baseDirPath);

		// Retrieve the parameters from baseDir+"/WEB-INF/web.xml"
		Document doc = getWebXmlDocument(baseDirPath);

		ServletContext servletContext = initServletContext(doc, baseDirPath,
				tmpDirPath, springConfigFiles, servletApiVersion);

		List<ServletDefinition> servletDefinitions = getWebXmlServletDefinitions(
				doc, servletContext, servletsToInitialize, webAppClassLoader);

		// Initialize the servlets and retrieve the jawr servlet definitions
		List<ServletDefinition> jawrServletDefinitions = initServlets(servletDefinitions);
		if (jawrServletDefinitions.isEmpty()) {

			logger.debug("No Jawr Servlet defined in web.xml");
			if (servletContext.getInitParameter(CONFIG_LOCATION_PARAM) != null) {
				logger.debug("Spring config location defined. Try loading spring context");
				jawrServletDefinitions = initJawrSpringControllers(servletContext);
			}
		}

		// Copy the temporary directory in the dest directory
		FileUtils.copyDirectory(new File(tmpDirPath), new File(destDirPath));

		if (generateCdnFiles) {
			// Process the Jawr servlet to generate the bundles
			String cdnDestDirPath = destDirPath + CDN_DIR_NAME;
			processJawrServlets(cdnDestDirPath, jawrServletDefinitions,
					keepUrlMapping);
		}

	}

	/**
	 * Returns the XML document of the web.xml file
	 * 
	 * @param webXmlPath
	 *            the web.xml path
	 * @return the Xml document of the web.xml file
	 * 
	 * @throws ParserConfigurationException
	 *             if a parser configuration exception occurs
	 * @throws FactoryConfigurationError
	 *             if a factory configuration exception occurs
	 * @throws SAXException
	 *             if a SAX exception occurs
	 * @throws IOException
	 *             if an IO exception occurs
	 */
	protected Document getWebXmlDocument(String baseDir)
			throws ParserConfigurationException, FactoryConfigurationError,
			SAXException, IOException {
		File webXml = new File(baseDir, WEB_XML_FILE_PATH);
		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		docBuilder.setEntityResolver(new EntityResolver() {

			public InputSource resolveEntity(String publicId, String systemId)
					throws SAXException, IOException {

				return null;
			}
		});

		Document doc = docBuilder.parse(webXml);
		return doc;
	}

	/**
	 * Initialize the classloader
	 * 
	 * @param baseDirPath
	 *            the base directory path
	 * @return the class loader
	 * @throws MalformedURLException
	 */
	protected ClassLoader initClassLoader(String baseDirPath)
			throws MalformedURLException {
		File webAppClasses = new File(baseDirPath + WEB_INF_CLASSES_DIR_PATH);
		File[] webAppLibs = new File(baseDirPath + WEB_INF_LIB_DIR_PATH)
				.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.endsWith(JAR_FILE_EXTENSION);
					}
				});

		int length = webAppLibs != null ? webAppLibs.length + 1 : 1;
		URL[] urls = new URL[length];

		urls[0] = webAppClasses.toURI().toURL();

		for (int i = 1; i < length; i++) {
			urls[i] = webAppLibs[i - 1].toURI().toURL();
		}

		ClassLoader webAppClassLoader = new JawrBundleProcessorCustomClassLoader(
				urls, getClass().getClassLoader());

		Thread.currentThread().setContextClassLoader(webAppClassLoader);
		return webAppClassLoader;
	}

	/**
	 * Initalize the servlet context
	 * 
	 * @param webXmlDoc
	 *            the web.xml document
	 * @param baseDirPath
	 *            the base drectory path
	 * @param tmpDirPath
	 *            the temp directory path
	 * @param springConfigFiles
	 *            the list of spring config files
	 * @return the servlet context
	 */
	protected ServletContext initServletContext(Document webXmlDoc,
			String baseDirPath, String tmpDirPath, String springConfigFiles,
			String servletAPIversion) {

		// Parse the context parameters
		MockServletContext servletContext = new MockServletContext(
				servletAPIversion, baseDirPath, tmpDirPath);
		Map<String, Object> servletContextInitParams = new HashMap<String, Object>();
		NodeList contextParamsNodes = webXmlDoc
				.getElementsByTagName(CONTEXT_TAG_NAME);
		for (int i = 0; i < contextParamsNodes.getLength(); i++) {
			Node node = contextParamsNodes.item(i);
			initializeInitParams(node, servletContextInitParams);
		}

		// Override spring config file if needed
		if (StringUtils.isNotEmpty(springConfigFiles)) {
			servletContextInitParams.put(CONFIG_LOCATION_PARAM,
					springConfigFiles);
		}

		servletContext.setInitParameters(servletContextInitParams);
		return servletContext;
	}

	/**
	 * Returns the list of servlet definition, which must be initialize
	 * 
	 * @param webXmlDoc
	 *            the web.xml document
	 * @param servletContext
	 *            the servlet context
	 * @param servletsToInitialize
	 *            the list of servlet to initialize
	 * @param webAppClassLoader
	 *            the web application class loader
	 * @return the list of servlet definition, which must be initialize
	 * @throws ClassNotFoundException
	 *             if a class is not found
	 */
	protected List<ServletDefinition> getWebXmlServletDefinitions(
			Document webXmlDoc, ServletContext servletContext,
			List<String> servletsToInitialize, ClassLoader webAppClassLoader)
			throws ClassNotFoundException {

		// Parse the servlet configuration
		NodeList servletNodes = webXmlDoc
				.getElementsByTagName(SERVLET_TAG_NAME);

		List<ServletDefinition> servletDefinitions = new ArrayList<ServletDefinition>();

		for (int i = 0; i < servletNodes.getLength(); i++) {

			String servletName = null;
			Class<?> servletClass = null;
			MockServletConfig config = new MockServletConfig(servletContext);
			int order = i;

			Node servletNode = servletNodes.item(i);
			Map<String, Object> initParameters = new HashMap<String, Object>();
			NodeList childNodes = servletNode.getChildNodes();
			for (int j = 0; j < childNodes.getLength(); j++) {
				Node servletChildNode = childNodes.item(j);
				if (servletChildNode.getNodeName()
						.equals(SERVLET_NAME_TAG_NAME)) {

					servletName = getTextValue(servletChildNode);
					config.setServletName(servletName);

				} else if (servletChildNode.getNodeName().equals(
						SERVLET_CLASS_TAG_NAME)) {

					String servletClassName = getTextValue(servletChildNode);
					servletClass = webAppClassLoader
							.loadClass(servletClassName);

				} else if (servletChildNode.getNodeName().equals(
						INIT_PARAM_TAG_NAME)) {

					initializeInitParams(servletChildNode, initParameters);
				} else if (servletChildNode.getNodeName().equals(
						LOAD_ON_STARTUP_TAG_NAME)) {

					order = Integer.parseInt(getTextValue(servletChildNode));
				}
			}

			// Initialize the servlet config with the init parameters
			config.setInitParameters(initParameters);

			// If the servlet name is part of the list of servlet to initialized
			// Set the flag accordingly
			if (servletsToInitialize.contains(servletName)
					|| JawrServlet.class.isAssignableFrom(servletClass)) {
				ServletDefinition servletDef = new ServletDefinition(
						servletClass, config, order);
				servletDefinitions.add(servletDef);
			}
			// Handle Spring MVC servlet definition
			if (servletContext.getInitParameter(CONFIG_LOCATION_PARAM) == null
					&& servletClass
							.getName()
							.equals("org.springframework.web.servlet.DispatcherServlet")) {
				((MockServletContext) servletContext).putInitParameter(
						CONFIG_LOCATION_PARAM, "/WEB-INF/" + servletName
								+ "-servlet.xml");
			}

		}
		return servletDefinitions;
	}

	/**
	 * Initialize the Jawr spring controller
	 * 
	 * @param servletContext
	 *            the servlet context
	 * @return the Jawr spring controller
	 * @throws ServletException
	 *             if a servlet exception occurs
	 */
	protected List<ServletDefinition> initJawrSpringControllers(
			ServletContext servletContext) throws ServletException {

		SpringControllerBundleProcessor springBundleProcessor = new SpringControllerBundleProcessor();
		return springBundleProcessor.initJawrSpringServlets(servletContext);
	}

	/**
	 * Initialize the servlets and returns only the list of Jawr servlets
	 * 
	 * @param servletDefinitions
	 *            the list of servlet definition
	 * @throws Exception
	 *             if an exception occurs
	 */
	protected List<ServletDefinition> initServlets(
			List<ServletDefinition> servletDefinitions) throws Exception {

		// Sort the list taking in account the load-on-startup attribute
		Collections.sort(servletDefinitions);

		// Sets the Jawr context at "bundle processing at build time"
		ThreadLocalJawrContext.setBundleProcessingAtBuildTime(true);

		List<ServletDefinition> jawrServletDefinitions = new ArrayList<ServletDefinition>();
		for (Iterator<ServletDefinition> iterator = servletDefinitions
				.iterator(); iterator.hasNext();) {
			ServletDefinition servletDefinition = (ServletDefinition) iterator
					.next();
			servletDefinition.initServlet();
			if (servletDefinition.isJawrServletDefinition()) {
				jawrServletDefinitions.add(servletDefinition);
			}
		}

		return jawrServletDefinitions;
	}

	/**
	 * Initialize the init parameters define in the servlet config
	 * 
	 * @param initParameters
	 *            the map of initialization parameters
	 */
	protected void initializeInitParams(Node initParamNode,
			Map<String, Object> initParameters) {

		String paramName = null;
		String paramValue = null;

		NodeList childNodes = initParamNode.getChildNodes();
		for (int j = 0; j < childNodes.getLength(); j++) {
			Node childNode = childNodes.item(j);
			String nodeName = childNode.getNodeName();
			if (nodeName.equals(PARAM_NAME_TAG_NAME)) {
				paramName = getTextValue(childNode);
			} else if (nodeName.equals(PARAM_VALUE_TAG_NAME)) {
				paramValue = getTextValue(childNode);
			}
		}

		initParameters.put(paramName, paramValue);
	}

	/**
	 * Returns the text value
	 * 
	 * @param node
	 *            the node
	 * @return the text value
	 */
	private String getTextValue(Node node) {
		return node.getFirstChild().getNodeValue().trim();
	}

	/**
	 * Process the Jawr Servlets
	 * 
	 * @param destDirPath
	 *            the destination directory path
	 * @param jawrServletDefinitions
	 *            the destination directory
	 * @throws Exception
	 *             if an exception occurs.
	 */
	protected void processJawrServlets(String destDirPath,
			List<ServletDefinition> jawrServletDefinitions,
			boolean keepUrlMapping) throws Exception {

		String appRootDir = "";
		String jsServletMapping = "";
		String cssServletMapping = "";
		String binaryServletMapping = "";

		for (Iterator<ServletDefinition> iterator = jawrServletDefinitions
				.iterator(); iterator.hasNext();) {

			ServletDefinition servletDef = (ServletDefinition) iterator.next();
			ServletConfig servletConfig = servletDef.getServletConfig();

			// Force the production mode, and remove config listener parameters
			Map<?, ?> initParameters = ((MockServletConfig) servletConfig)
					.getInitParameters();
			initParameters.remove("jawr.config.reload.interval");

			String jawrServletMapping = servletConfig
					.getInitParameter(JawrConstant.SERVLET_MAPPING_PROPERTY_NAME);
			String servletMapping = servletConfig
					.getInitParameter(JawrConstant.SPRING_SERVLET_MAPPING_PROPERTY_NAME);
			if (servletMapping == null) {
				servletMapping = jawrServletMapping;
			}

			ResourceBundlesHandler bundleHandler = null;
			BinaryResourcesHandler binaryRsHandler = null;

			// Retrieve the bundle Handler
			ServletContext servletContext = servletConfig.getServletContext();
			String type = servletConfig.getInitParameter(TYPE_INIT_PARAMETER);
			if (type == null || type.equals(JawrConstant.JS_TYPE)) {
				bundleHandler = (ResourceBundlesHandler) servletContext
						.getAttribute(JawrConstant.JS_CONTEXT_ATTRIBUTE);
				String contextPathOverride = bundleHandler.getConfig()
						.getContextPathOverride();
				if (StringUtils.isNotEmpty(contextPathOverride)) {
					int idx = contextPathOverride.indexOf("//");
					if (idx != -1) {
						idx = contextPathOverride.indexOf("/", idx + 2);
						if (idx != -1) {
							appRootDir = PathNormalizer
									.asPath(contextPathOverride.substring(idx));
						}
					}
				}

				if (jawrServletMapping != null) {
					jsServletMapping = PathNormalizer
							.asPath(jawrServletMapping);
				}

			} else if (type.equals(JawrConstant.CSS_TYPE)) {
				bundleHandler = (ResourceBundlesHandler) servletContext
						.getAttribute(JawrConstant.CSS_CONTEXT_ATTRIBUTE);
				if (jawrServletMapping != null) {
					cssServletMapping = PathNormalizer
							.asPath(jawrServletMapping);
				}
			} else if (type.equals(JawrConstant.BINARY_TYPE)) {
				binaryRsHandler = (BinaryResourcesHandler) servletContext
						.getAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE);
				if (jawrServletMapping != null) {
					binaryServletMapping = PathNormalizer
							.asPath(jawrServletMapping);
				}
			}

			if (bundleHandler != null) {
				createBundles(servletDef.getServlet(), bundleHandler,
						destDirPath, servletMapping, keepUrlMapping);
			} else if (binaryRsHandler != null) {
				createBinaryBundle(servletDef.getServlet(), binaryRsHandler,
						destDirPath, servletConfig, keepUrlMapping);
			}
		}

		// Create the apache rewrite config file.
		createApacheRewriteConfigFile(destDirPath, appRootDir,
				jsServletMapping, cssServletMapping, binaryServletMapping);

	}

	/**
	 * Create the apache rewrite configuration file
	 * 
	 * @param cdnDestDirPath
	 *            the CDN destination directory
	 * @param appRootDir
	 *            the application root dir path in the CDN
	 * @param jsServletMapping
	 *            the JS servlet mapping
	 * @param cssServletMapping
	 *            the CSS servlet mapping
	 * @param imgServletMapping
	 *            the image servlet mapping
	 * @throws IOException
	 *             if an IOException occurs.
	 */
	protected void createApacheRewriteConfigFile(String cdnDestDirPath,
			String appRootDir, String jsServletMapping,
			String cssServletMapping, String imgServletMapping)
			throws IOException {

		BufferedReader templateFileReader = null;
		FileWriter fileWriter = null;
		try {

			templateFileReader = new BufferedReader(new InputStreamReader(this
					.getClass().getResourceAsStream(
							TEMPLATE_JAWR_APACHE_HTTPD_CONF_PATH)));
			fileWriter = new FileWriter(cdnDestDirPath + File.separator
					+ JAWR_APACHE_HTTPD_CONF_FILE);
			String line = null;

			boolean processNextString = true;
			while ((line = templateFileReader.readLine()) != null) {

				// If the line starts with the condition to check the existence
				// of the JS servlet mapping,
				// sets the processNextString flag accordingly
				if (line.startsWith(CHECKS_JAWR_JS_SERVLET_MAPPING_EXISTS)) {
					if (StringUtils.isEmpty(jsServletMapping)) {
						processNextString = false;
					}
					// If the line starts with the condition to check the
					// existence of the servlet mapping,
					// sets the processNextString flag accordingly
				} else if (line
						.startsWith(CHECK_JAWR_CSS_SERVLET_MAPPING_EXISTS)) {
					if (StringUtils.isEmpty(cssServletMapping)) {
						processNextString = false;
					}
					// If the processNextString flag is set to false, skip the
					// current line, and process the next one
				} else if (processNextString == false) {
					processNextString = true;
				} else {

					// Make the replacement
					line = line.replaceAll(APP_ROOT_DIR_PATTERN, appRootDir);
					line = line.replaceAll(JAWR_JS_SERVLET_MAPPING_PATTERN,
							jsServletMapping);
					line = line.replaceAll(JAWR_CSS_SERVLET_MAPPING_PATTERN,
							cssServletMapping);
					line = line.replaceAll(JAWR_IMG_SERVLET_MAPPING_PATTERN,
							imgServletMapping);
					fileWriter.write(line + "\n");
				}
			}
		} finally {

			IOUtils.close(templateFileReader);
			IOUtils.close(fileWriter);

		}
	}

	/**
	 * Creates the bundles in the destination directory
	 * 
	 * @param servlet
	 *            the servlet
	 * @param bundleHandler
	 *            the bundles handler
	 * @param destDirPath
	 *            the destination directory path
	 * @param servletMapping
	 *            the mapping of the servlet
	 * @param keepUrlMapping
	 *            the flag indicating if we must keep the URL mapping
	 * @throws IOException
	 *             if an IO exception occurs
	 * @throws ServletException
	 *             if a servlet exception occurs
	 */
	protected void createBundles(HttpServlet servlet,
			ResourceBundlesHandler bundleHandler, String destDirPath,
			String servletMapping, boolean keepUrlMapping) throws IOException,
			ServletException {

		List<JoinableResourceBundle> bundles = bundleHandler
				.getContextBundles();

		Iterator<JoinableResourceBundle> bundleIterator = bundles.iterator();
		MockServletResponse response = new MockServletResponse();
		MockServletRequest request = new MockServletRequest(
				JAWR_BUNDLE_PROCESSOR_CONTEXT_PATH);
		MockServletSession session = new MockServletSession(
				servlet.getServletContext());
		request.setSession(session);

		String resourceType = servlet.getServletConfig().getInitParameter(
				TYPE_INIT_PARAMETER);
		if (resourceType == null) {
			resourceType = JawrConstant.JS_TYPE;
		}

		// For the list of bundle defines, create the file associated
		while (bundleIterator.hasNext()) {
			JoinableResourceBundle bundle = (JoinableResourceBundle) bundleIterator
					.next();

			// Check if there is a resource file, which could be in conflict
			// with the bundle name
			URL url = servlet.getServletContext().getResource(bundle.getId());
			if (url != null) {
				logger.error("It is not recommended to use a bundle name which could be in conflict with a resource.\n"
						+ "Please rename your bundle '"
						+ bundle.getId()
						+ "' to avoid any issue");

			}

			List<Map<String, String>> allVariants = VariantUtils
					.getAllVariants(bundle.getVariants());

			if (allVariants == null) {
				allVariants = new ArrayList<Map<String, String>>();
			}

			if (allVariants.isEmpty()) {
				allVariants.add(new HashMap<String, String>());
			}
			// Creates the bundle file for each local variant
			for (Iterator<Map<String, String>> it = allVariants.iterator(); it
					.hasNext();) {
				Map<String, String> variantMap = (Map<String, String>) it
						.next();

				List<RenderedLink> linksToBundle = createLinkToBundle(
						bundleHandler, bundle.getId(), resourceType, variantMap);
				for (Iterator<RenderedLink> iteratorLinks = linksToBundle
						.iterator(); iteratorLinks.hasNext();) {
					RenderedLink renderedLink = iteratorLinks.next();
					String path = renderedLink.getLink();

					// Force the debug mode of the config to match what was used
					// in the generated link
					JawrConfig config = bundleHandler.getConfig();
					config.setDebugModeOn(renderedLink.isDebugMode());

					String finalBundlePath = null;
					if (keepUrlMapping) {
						finalBundlePath = path;
					} else {
						finalBundlePath = getFinalBundlePath(path, config,
								variantMap);
					}

					// Sets the request URL
					setRequestUrl(request, variantMap, path, config);

					// We can't use path for generated resources because it's
					// not a valid file path ( /jawr_generator.js?xxx.... )
					if (!(path.indexOf("?") != -1) || !keepUrlMapping) {
						File bundleFile = new File(destDirPath, finalBundlePath);
						createBundleFile(servlet, response, request, path,
								bundleFile, servletMapping);
					}
				}
			}
		}
	}

	/**
	 * Set the request URL
	 * 
	 * @param request
	 *            the request
	 * @param variantMap
	 *            the variantMap
	 * @param path
	 *            the path
	 * @param config
	 *            the Jawr config
	 */
	protected void setRequestUrl(MockServletRequest request,
			Map<String, String> variantMap, String path, JawrConfig config) {

		String domainURL = JawrConstant.HTTP_URL_PREFIX + DEFAULT_WEBAPP_URL;

		if (JawrConstant.SSL.equals(variantMap
				.get(JawrConstant.CONNECTION_TYPE_VARIANT_TYPE))) {
			// Use the contextPathSslOverride property if it's an absolute URL
			if (StringUtils.isNotEmpty(config.getContextPathSslOverride())
					&& config.getContextPathSslOverride().startsWith(
							JawrConstant.HTTPS_URL_PREFIX)) {
				domainURL = config.getContextPathSslOverride();
			} else {
				domainURL = JawrConstant.HTTPS_URL_PREFIX + DEFAULT_WEBAPP_URL;
			}
		} else {
			// Use the contextPathOverride property if it's an absolute URL
			if (StringUtils.isNotEmpty(config.getContextPathOverride())
					&& config.getContextPathOverride().startsWith(
							JawrConstant.HTTP_URL_PREFIX)) {
				domainURL = config.getContextPathOverride();
			} else {
				domainURL = JawrConstant.HTTP_URL_PREFIX + DEFAULT_WEBAPP_URL;
			}
		}

		request.setRequestUrl(PathNormalizer.joinDomainToPath(domainURL, path));
	}

	/**
	 * Retrieves the final path, where the servlet mapping and the cache prefix
	 * have been removed, and take also in account the jawr generator URLs.
	 * 
	 * <pre>
	 * 	"/N1785986402/js/bundle/msg.js" -> "/js/bundle/msg.js"
	 *  "/jawr_generator.js?generationConfigParam=messages%3Amessages%40fr" -> "/jawr_generator/js/messages/messages_fr.js"
	 *  "/cssJawrPath/1414653084/folder/core/component.css" -> "folder/core/component.css"
	 * </pre>
	 * 
	 * @param path
	 *            the path
	 * @param jawrConfig
	 *            the jawr config
	 * @param localVariantKey
	 *            The local variant key
	 * @return the final path
	 */
	public String getFinalBundlePath(String path, JawrConfig jawrConfig,
			Map<String, String> variantMap) {

		String finalPath = path;
		int jawrGenerationParamIdx = finalPath
				.indexOf(JawrRequestHandler.GENERATION_PARAM);
		if (jawrGenerationParamIdx != -1) {

			try {
				finalPath = URLDecoder.decode(path, "UTF-8");
			} catch (UnsupportedEncodingException neverHappens) {
				/* URLEncoder:how not to use checked exceptions... */
				throw new RuntimeException(
						"Something went unexpectedly wrong while decoding a URL for a generator. ",
						neverHappens);
			}

			// Remove servlet mapping if it exists.
			finalPath = removeServletMappingFromPath(finalPath,
					jawrConfig.getServletMapping());

			finalPath = jawrConfig.getGeneratorRegistry()
					.getDebugModeBuildTimeGenerationPath(finalPath);

		} else {

			// Remove servlet mapping if it exists.
			finalPath = removeServletMappingFromPath(finalPath,
					jawrConfig.getServletMapping());
			if (finalPath.startsWith("/")) {
				finalPath = finalPath.substring(1);
			}

			// remove cache prefix, when not in debug mode
			if (!jawrConfig.isDebugModeOn()) {

				int idx = finalPath.indexOf("/");
				finalPath = finalPath.substring(idx + 1);
			}

			// For localized bundle add the local info in the file name
			// For example, with local variant = 'en'
			// /bundle/myBundle.js -> /bundle/myBundle_en.js
			finalPath = VariantUtils
					.getVariantBundleName(finalPath, variantMap, false);
		}

		return finalPath;
	}

	/**
	 * Retrieves the image final path, where the servlet mapping and the cache
	 * prefix have been removed
	 * 
	 * @param path
	 *            the path
	 * @param jawrConfig
	 *            the jawr config
	 * @return the final path
	 */
	public String getImageFinalPath(String path, JawrConfig jawrConfig) {

		String finalPath = path;

		// Remove servlet mapping if it exists.
		finalPath = removeServletMappingFromPath(finalPath,
				jawrConfig.getServletMapping());
		if (finalPath.startsWith("/")) {
			finalPath = finalPath.substring(1);
		}

		// remove cache prefix
		int idx = finalPath.indexOf("/");
		finalPath = finalPath.substring(idx + 1);

		return finalPath;
	}

	/**
	 * Remove the servlet mapping from the path
	 * 
	 * @param path
	 *            the path
	 * @param mapping
	 *            the servlet mapping
	 * @return the path without the servlet mapping
	 */
	protected String removeServletMappingFromPath(String path, String mapping) {
		if (mapping != null && mapping.length() > 0) {
			int idx = path.indexOf(mapping);
			if (idx > -1) {
				path = path.substring(idx + mapping.length());
			}

			path = PathNormalizer.asPath(path);
		}
		return path;
	}

	/**
	 * Create the image bundle
	 * 
	 * @param servlet
	 *            the servlet
	 * @param binaryRsHandler
	 *            the binary resource handler
	 * @param destDirPath
	 *            the destination directory path
	 * @param servletMapping
	 *            the mapping
	 * @param keepUrlMapping
	 *            = the flag indicating if we must keep the url mapping
	 * @throws IOException
	 *             if an IOExceptin occurs
	 * @throws ServletException
	 *             if an exception occurs
	 */
	protected void createBinaryBundle(HttpServlet servlet,
			BinaryResourcesHandler binaryRsHandler, String destDirPath,
			ServletConfig servletConfig, boolean keepUrlMapping)
			throws IOException, ServletException {
		Map<String, String> bundleImgMap = binaryRsHandler.getBinaryPathMap();

		Iterator<String> bundleIterator = bundleImgMap.values().iterator();
		MockServletResponse response = new MockServletResponse();
		MockServletRequest request = new MockServletRequest(
				JAWR_BUNDLE_PROCESSOR_CONTEXT_PATH);

		String jawrServletMapping = servletConfig
				.getInitParameter(JawrConstant.SERVLET_MAPPING_PROPERTY_NAME);
		if (jawrServletMapping == null) {
			jawrServletMapping = "";
		}

		String servletMapping = servletConfig
				.getInitParameter(JawrConstant.SPRING_SERVLET_MAPPING_PROPERTY_NAME);
		if (servletMapping == null) {
			servletMapping = jawrServletMapping;
		}

		// For the list of bundle defines, create the file associated
		while (bundleIterator.hasNext()) {
			String path = (String) bundleIterator.next();

			String binaryFinalPath = null;

			if (keepUrlMapping) {
				binaryFinalPath = path;
			} else {
				binaryFinalPath = getImageFinalPath(path,
						binaryRsHandler.getConfig());
			}

			File destFile = new File(destDirPath, binaryFinalPath);
						
			Map<String, String> variantMap = new HashMap<String, String>();
			setRequestUrl(request, variantMap , path, binaryRsHandler.getConfig());

			// Update the bundle mapping
			path = PathNormalizer.concatWebPath(
					PathNormalizer.asDirPath(jawrServletMapping), path);
			createBundleFile(servlet, response, request, path, destFile,
					servletMapping);
		}
	}

	/**
	 * Create the bundle file
	 * 
	 * @param servlet
	 *            the servlet
	 * @param response
	 *            the response
	 * @param request
	 *            the request
	 * @param path
	 *            the path
	 * @param destFile
	 *            the destination file
	 * @param mapping
	 *            the mapping
	 * @throws IOException
	 *             if an IO exception occurs
	 * @throws ServletException
	 *             if an exception occurs
	 */
	protected void createBundleFile(HttpServlet servlet,
			MockServletResponse response, MockServletRequest request,
			String path, File destFile, String mapping) throws IOException,
			ServletException {

		request.setRequestPath(mapping, path);

		// Create the parent directory of the destination file
		if (!destFile.getParentFile().exists()) {
			boolean dirsCreated = destFile.getParentFile().mkdirs();
			if (!dirsCreated) {
				throw new IOException("The directory '"
						+ destFile.getParentFile().getCanonicalPath()
						+ "' can't be created.");
			}
		}

		// Set the response mock to write in the destination file
		try {
			response.setOutputStream(new FileOutputStream(destFile));
			servlet.service(request, response);
		} finally {
			response.close();
		}

		if (destFile.length() == 0) {
			logger.warn("No content retrieved for file '"
					+ destFile.getAbsolutePath()
					+ "', which is associated to the path : " + path);
			System.out.println("No content retrieved for file '"
					+ destFile.getAbsolutePath()
					+ "', which is associated to the path : " + path);
		}
	}

	/**
	 * Returns the link to the bundle
	 * 
	 * @param handler
	 *            the resource bundles handler
	 * @param path
	 *            the path
	 * @param variantKey
	 *            the local variant key
	 * @return the link to the bundle
	 * @throws IOException
	 *             if an IO exception occurs
	 */
	protected List<RenderedLink> createLinkToBundle(
			ResourceBundlesHandler handler, String path, String resourceType,
			Map<String, String> variantMap) throws IOException {

		ArrayList<RenderedLink> linksToBundle = new ArrayList<RenderedLink>();

		BasicBundleRenderer bundleRenderer = new BasicBundleRenderer(handler,
				resourceType);
		StringWriter sw = new StringWriter();

		// The gzip compression will be made by the CDN server
		// So we force it to false.
		boolean useGzip = false;

		// The generation of bundle is the same in SSL and non SSL mode
		boolean isSslRequest = false;

		// First deals with the production mode
		handler.getConfig().setDebugModeOn(false);
		handler.getConfig().setGzipResourcesModeOn(useGzip);

		BundleRendererContext ctx = new BundleRendererContext("", variantMap,
				useGzip, isSslRequest);
		bundleRenderer.renderBundleLinks(path, ctx, sw);

		// Then take in account the debug mode
		handler.getConfig().setDebugModeOn(true);
		ctx = new BundleRendererContext("", variantMap, useGzip, isSslRequest);
		bundleRenderer.renderBundleLinks(path, ctx, sw);

		List<RenderedLink> renderedLinks = bundleRenderer.getRenderedLinks();
		// Remove context override path if it's defined.
		String contextPathOverride = handler.getConfig()
				.getContextPathOverride();
		for (Iterator<RenderedLink> iterator = renderedLinks.iterator(); iterator
				.hasNext();) {
			RenderedLink renderedLink = iterator.next();
			String renderedLinkPath = renderedLink.getLink();
			// Remove the context path override
			if (StringUtils.isNotEmpty(contextPathOverride)
					&& renderedLinkPath.startsWith(contextPathOverride)) {
				renderedLinkPath = renderedLinkPath
						.substring(contextPathOverride.length());
			}
			renderedLink.setLink(PathNormalizer.asPath(renderedLinkPath));
			linksToBundle.add(renderedLink);
		}

		return linksToBundle;
	}

	/**
	 * This is the custom class loader for Jawr Bundle processor
	 * 
	 * @author Ibrahim Chaehoi
	 * 
	 */
	protected static class JawrBundleProcessorCustomClassLoader extends
			URLClassLoader {

		/**
		 * Constructor
		 * 
		 * @param urls
		 *            the URL location for the class loading
		 * @param parent
		 *            the parent classloader
		 */
		public JawrBundleProcessorCustomClassLoader(URL[] urls,
				ClassLoader parent) {
			super(urls, parent);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.net.URLClassLoader#findResource(java.lang.String)
		 */
		public URL findResource(String name) {
			URL url = super.findResource(name);
			if (url == null && name.startsWith("/")) {
				url = super.findResource(name.substring(1));
			}
			return url;
		}
	}

}
