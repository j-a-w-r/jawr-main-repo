/**
 * Copyright 2010-2016 Ibrahim Chaehoi
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
package net.jawr.web.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.Locale;
import java.util.Properties;

import javax.management.remote.JMXServiceURL;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.jmx.ConnectorServer;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.webapp.WebAppClassLoader;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jawr.web.test.utils.FileUtils;

/**
 * The Jawr integration server
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class JawrIntegrationServer {

	/** The logger */
	private static Logger LOGGER = LoggerFactory
			.getLogger(JawrIntegrationServer.class);

	/** The properties file name */
	private static final String PROP_FILE_NAME = "jawr-integration-server.properties";

	/** The port property name */
	private static final String PORT_PROPERTY_NAME = "port";

	/** The webapp property name */
	private static final String WEBAPP_PROPERTY_NAME = "webapp-name";

	/** The default webapp name */
	private static final String DEFAULT_WEBAPP_NAME = "jawr-integration-test";

	/** The port */
	protected static String DEFAULT_PORT = "8080";

	/** The property indicating if we must use the empty context path */
	private static String USE_DEFAULT_CONTEXT_PATH_PROPERTY_NAME = "use-empty-context-path";

	/** The default value for the use of default context path */
	private static String DONT_USE_DEFAULT_CONTEXT_PATH = "false";

	/** The application URL */
	public static final String SERVER_URL = "http://localhost:" + DEFAULT_PORT;

	/** The target root directory */
	private static final String TARGET_ROOT_DIR = "target/";

	/**
	 * The flag indicating if we have configured the web application for all the
	 * tests of the current test case class
	 */
	private static boolean webAppConfigInitialized = false;

	/** The Jawr integration HTTP Server */
	private static JawrIntegrationServer instance = new JawrIntegrationServer();

	/** The Jetty server */
	private Server server;

	/** The web app context path */
	private int serverPort;

	/** The web app dir */
	private String webAppRootDir;

	/** The web application context */
	private WebAppContext jettyWebAppContext;

	JawrIntegrationServer() {

		Properties prop = new Properties();
		InputStream inStream = getClass().getClassLoader().getResourceAsStream(
				PROP_FILE_NAME);
		if (inStream != null) {
			try {
				prop.load(inStream);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			IOUtils.closeQuietly(inStream);
		}

		serverPort = Integer.parseInt(prop.getProperty(PORT_PROPERTY_NAME,
				DEFAULT_PORT));
		server = new Server(serverPort);
		// Setup JMX
		MBeanContainer mbContainer=new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
		server.addEventListener(mbContainer);
		server.addBean(mbContainer);
		
		try {
			
			String host = "localhost";
			int jmxPort = 1099;
			String urlPath = String.format("/jndi/rmi://%s:%s/jmxrmi", host, jmxPort);
	        JMXServiceURL serviceURL = new JMXServiceURL("rmi", host, jmxPort, urlPath);
	        ConnectorServer connectorServer = new ConnectorServer(serviceURL, "org.eclipse.jetty.jmx:name=rmiconnectorserver");
			connectorServer.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		server.setStopAtShutdown(true);
		String webappName = prop.getProperty(WEBAPP_PROPERTY_NAME,
				DEFAULT_WEBAPP_NAME);
		try {
			webAppRootDir = new File(TARGET_ROOT_DIR + webappName)
					.getCanonicalFile().getAbsolutePath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		boolean useEmptyContextPath = Boolean.parseBoolean(prop.getProperty(
				USE_DEFAULT_CONTEXT_PATH_PROPERTY_NAME,
				DONT_USE_DEFAULT_CONTEXT_PATH));
		String webAppCtx = "";
		if (!useEmptyContextPath) {
			webAppCtx = "/" + webappName;
		}

		jettyWebAppContext = new WebAppContext(webAppRootDir, webAppCtx);
		jettyWebAppContext.setConfigurationClasses(new String[] {
				"org.eclipse.jetty.webapp.WebInfConfiguration",
				"org.eclipse.jetty.webapp.WebXmlConfiguration",
				"org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
				"org.eclipse.jetty.annotations.AnnotationConfiguration" });

		ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();
		contextHandlerCollection
				.setHandlers(new Handler[] { jettyWebAppContext });
		server.setHandler(contextHandlerCollection);
	}

	public static JawrIntegrationServer getInstance() {
		return instance;
	}

	/**
	 * Returns the Jetty webapp context
	 * 
	 * @return the Jetty webapp context
	 */
	public WebAppContext getJettyWebAppContext() {
		return jettyWebAppContext;
	}

	/**
	 * Returns the webapp context path
	 * 
	 * @return the webapp context path
	 */
	public String getServerUrlPrefix() {
		return "http://localhost:" + serverPort;
	}

	/**
	 * Returns the webapp context path
	 * 
	 * @return the webapp context path
	 */
	public String getContextPath() {
		return jettyWebAppContext.getContextPath();
	}

	/**
	 * Returns the webapp root dir
	 * 
	 * @return the webapp root dir
	 */
	public String getWebAppRootDir() {
		return webAppRootDir;
	}

	/**
	 * Initialize the application server before the test
	 * 
	 * @throws IOException
	 */
	public void initBeforeTestCase() throws IOException {

		LOGGER.info("Init Jawr integration server before testcase");
		webAppConfigInitialized = false;
		// Set default locale to en_US
		Locale.setDefault(new Locale("en", "US"));
	}

	public void setup() throws Exception {

		LOGGER.info("Jawr integration server "
				+ (webAppConfigInitialized ? "is already started"
						: "will start now."));
		if (!webAppConfigInitialized) {
			// Starts the web application
			startWebApplication();

			webAppConfigInitialized = true;
		}
	}

	/**
	 * Starts the web application. The web application root directory will be
	 * define in target/jawr-integration-test, the directory used for the war
	 * generation.
	 * 
	 * @throws Exception
	 *             if an exception occurs
	 */
	public void startWebApplication() throws Exception {

		// Create a new class loader to take in account the changes of the jawr
		// config file in the WEB-INF/classes
		WebAppClassLoader webAppClassLoader = new WebAppClassLoader(
				jettyWebAppContext);
		jettyWebAppContext.setClassLoader(webAppClassLoader);
		// Fix issue with web app context reloading on Windows where deleting temporary directory fails because of locked files
		jettyWebAppContext.setPersistTempDirectory(true);
		
		File tempDirectory = jettyWebAppContext.getTempDirectory();
		// Clean generator cache directory
		File generatorCacheDir = new File(tempDirectory, "generatorCache");
		if(generatorCacheDir.exists()){
			FileUtils.deleteDirectory(generatorCacheDir);
		}
		
		if (server.isStopped()) {
			LOGGER.info("Start jetty server....");
			server.start();
		}
		if (jettyWebAppContext.isStopped()) {
			LOGGER.info("Start jetty webApp context....");
			jettyWebAppContext.start();
		}
	}

	/**
	 * Resets the test configuration.
	 * 
	 * @throws Exception
	 *             if an exception occurs
	 */
	public void resetTestConfiguration() throws Exception {

		webAppConfigInitialized = false;
		// Stop the web application context at the end of the tests associated
		// to the current class.
		LOGGER.info("Stop jetty webApp context....");
		jettyWebAppContext.stop();
	}

}
