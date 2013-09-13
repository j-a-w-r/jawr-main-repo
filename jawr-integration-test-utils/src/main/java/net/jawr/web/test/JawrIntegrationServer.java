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
package net.jawr.web.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppClassLoader;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * @author Ibrahim Chaehoi
 *
 */
public class JawrIntegrationServer {

	/** The logger */
	private static Logger LOGGER = Logger.getLogger(JawrIntegrationServer.class);

	/** The web application context path */
	public static final String CONTEXT_PATH = "/jawr-integration-test";

	/** The port */
	protected static int PORT = 8080;
	
	/** The application URL */
	public static final String SERVER_URL = "http://localhost:"+PORT;

	/** The web application directory */
	private static final String WEBAPP_DIR = "target/jawr-integration-test";

	/** The flag indicating if we have configured the web application for all the tests of the current test case class */
	private static boolean webAppConfigInitialized = false;

	/** The Jetty server */
	private Server SERVER;
	
	/** The web application context */
	private WebAppContext WEB_APP_CONTEXT;
	
	/** The path of the web.xml in the web application (The file will be overwritten by the current test configuration) */
	private static String WEB_APP_WEB_XML_PATH = "";
	
	/** The path of the jawr.properties in the web application (The file will be overwritten by the current test configuration) */
	private static String WEB_APP_JAWR_CONFIG_PATH = "";

	/** The path of the current web.xml which is used for the configuration */
	private static String WEB_XML_SRC_PATH = "";
	
	/** The path of the current jawr.properties which is used for the configuration */
	private static String JAWR_CONFIG_SRC_PATH = "";
	
	private static JawrIntegrationServer instance = new JawrIntegrationServer();
	
	public JawrIntegrationServer() {
		SERVER = new Server(PORT);
		SERVER.setStopAtShutdown(true);
		WEB_APP_CONTEXT = new WebAppContext(WEBAPP_DIR, "/jawr-integration-test");
		WEB_APP_CONTEXT.setConfigurationClasses(new String[] {
				"org.mortbay.jetty.webapp.WebInfConfiguration",
				"org.mortbay.jetty.webapp.WebXmlConfiguration", });
	}
	
	public static JawrIntegrationServer getInstance(){
		return instance;
	}
	
	public void initBeforeTestCase() throws IOException{
		
		LOGGER.info("Init Jawr integration server before testcase");
		webAppConfigInitialized = false;
		String webAppRootDir = new File(WEBAPP_DIR).getCanonicalFile().getAbsolutePath();
		WEB_APP_WEB_XML_PATH =  webAppRootDir+"/WEB-INF/web.xml";
		WEB_APP_JAWR_CONFIG_PATH = webAppRootDir+"/WEB-INF/classes/jawr.properties";
		// Set default locale to en_US
		Locale.setDefault(new Locale("en","US"));
	}
	
	public void setup(JawrTestConfigFiles annotationConfig) throws Exception {

		LOGGER.info("Jawr integration server "+(webAppConfigInitialized?"is already started" : "will start now."));
		if (!webAppConfigInitialized) {
			initializeWebAppConfig(annotationConfig);
		}
	}
	
	/**
	 * Starts the web application.
	 * The web application root directory will be define in target/jawr-integration-test, the directory used for the war generation.
	 * 
	 * @throws Exception if an exception occurs
	 */
	public void startWebApplication() throws Exception {
//		if(SERVER == null){
//			SERVER = new Server(PORT);
//			SERVER.setStopAtShutdown(true);
//			WEB_APP_CONTEXT = new WebAppContext(WEBAPP_DIR, "/jawr-integration-test");
//			WEB_APP_CONTEXT.setConfigurationClasses(new String[] {
//					"org.mortbay.jetty.webapp.WebInfConfiguration",
//					"org.mortbay.jetty.webapp.WebXmlConfiguration", });
//		}
		
		// Create a new class loader to take in account the changes of the jawr config file in the WEB-INF/classes
		WebAppClassLoader webAppClassLoader = new WebAppClassLoader(WEB_APP_CONTEXT);
		WEB_APP_CONTEXT.setClassLoader(webAppClassLoader);
		
		SERVER.setHandler(WEB_APP_CONTEXT);
		
		if(SERVER.isStopped()){
			LOGGER.info("Start jetty server....");
				SERVER.start();
		}
		if(WEB_APP_CONTEXT.isStopped()){
			LOGGER.info("Start jetty webApp context....");
			WEB_APP_CONTEXT.start();
		}
		
	}
	
	/**
	 * Initialize the web application configuration for the tests
	 * @throws Exception if an exception occurs.
	 */
	public void initializeWebAppConfig(JawrTestConfigFiles annotationConfig) throws Exception {
		
		String currentJawrConfigPath = annotationConfig.jawrConfig();
		if(!JAWR_CONFIG_SRC_PATH.equals(currentJawrConfigPath)){
			
			OutputStream outFile = new FileOutputStream(new File(WEB_APP_JAWR_CONFIG_PATH));
			IOUtils.copy(getClass().getClassLoader().getResourceAsStream(
					currentJawrConfigPath), outFile);
			IOUtils.closeQuietly(outFile);
			JAWR_CONFIG_SRC_PATH = currentJawrConfigPath;
		}
		
		String currentWebXmlPath = annotationConfig.webXml();
		if(!WEB_XML_SRC_PATH.equals(currentWebXmlPath)){
			
			OutputStream outFile = new FileOutputStream(new File(WEB_APP_WEB_XML_PATH));
			IOUtils.copy(getClass().getClassLoader().getResourceAsStream(
					currentWebXmlPath), outFile);
			IOUtils.closeQuietly(outFile);
			
			WEB_XML_SRC_PATH = currentWebXmlPath;
		}
		
		// Starts the web application 
		startWebApplication();
		
		webAppConfigInitialized = true;
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
		WEB_APP_CONTEXT.stop();
		//SERVER.stop();
		//WEB_APP_CONTEXT.destroy();
		//SERVER.destroy();
//		WEB_APP_CONTEXT = null;
//		SERVER = null;
	}
}
