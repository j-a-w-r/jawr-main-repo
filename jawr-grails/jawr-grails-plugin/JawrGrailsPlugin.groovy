import org.codehaus.groovy.grails.commons.*
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils as GPU
import grails.util.*

import net.jawr.web.JawrGrailsConstant
import net.jawr.web.resource.bundle.locale.ResourceBundleMessagesGenerator
import net.jawr.web.servlet.JawrGrailsServlet
import net.jawr.web.servlet.JawrRequestHandler

class JawrGrailsPlugin {

	// the plugin version
	def version = "3.7-SNAPSHOT"
	// the version or versions of Grails the plugin is designed for
	def grailsVersion = "2.4 > *"
	// resources that are excluded from plugin packaging
	def pluginExcludes = ["grails-app/views/error.gsp"]

	def authorEmail = "icefox@dev.java.net"
	def title = "adds Jawr (https://jawr.dev.java.net) functionality to Grails applications."
	def description = '''\
    			Jawr is a tunable packaging solution for Javascript and CSS which allows for rapid development of resources 
    			in separate module files. You can work with a large set of split javascript files in development mode, then
    			Jawr bundles all together into one or several files in any way you define. By using a tag library, Jawr allows 
    			you to use the same, unchanged GSP pages for development and production. Jawr also minifies and compresses the 
    			files, resulting in reduced page load times. 
    			'''
	def documentation = "https://jawr.dev.java.net"


	// License: one of 'APACHE', 'GPL2', 'GPL3'
	def license = "APACHE"

	// Details of company behind the plugin (if there is one)
	//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

	// Any additional developers beyond the author specified above.
	//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

	// Location of the plugin's issue tracker.
	def issueManagement = [ system: "JIRA", url: "https://java.net/jira/browse/JAWR/" ]

	// Online location of the plugin's browseable source code.
	def scm = [ url: "https://github.com/ic3fox/jawr-grails/" ]

	// Reference to the application context used for refreshing the config
	def appContext;

	// The Grails resource locator
	def grailsResourceLocator;

	// Reference to the hashcode of Jawr config properties to know when to refresh the requestHandlers
	private int currentConfigHash;

	// Define the config as watched to reload the configuration when it changes.
	def watchedResources =  "file:./grails-app/conf/Config.groovy";

	def doWithWebDescriptor = { webXml ->
		def conf =  Holders.config;
		def servlets = webXml.servlet
		def servletMappings = webXml.'servlet-mapping'

		// Init the Javascript handler
		if(conf.jawr.js){
			def mapping = conf.jawr.js.mapping ? conf.jawr.js.mapping + "*" : "*.js";
			servlets[servlets.size()-1] + {
				'servlet' {
					'servlet-name' ("jawrJavascriptServlet")
					'servlet-class' ("net.jawr.web.servlet.JawrGrailsServlet")
					'load-on-startup' (2)
				}
			}
			servletMappings[servletMappings.size()-1] + {
				'servlet-mapping'
				{
					'servlet-name' ("jawrJavascriptServlet")
					'url-pattern' (mapping)
				}
			}
		}

		// Init the CSS handler
		if(conf.jawr.css){
			def mapping = conf.jawr.css.mapping ? conf.jawr.css.mapping + "*" : "*.css";
			servlets[servlets.size()-1] + {
				'servlet' {
					'servlet-name' ("jawrCSSServlet")
					'servlet-class' ("net.jawr.web.servlet.JawrGrailsServlet")
					'init-param' {
						'param-name' ('type')
						'param-value' ('css') }
					'load-on-startup' (3)
				}
			}
			servletMappings[servletMappings.size()-1] + {
				'servlet-mapping'
				{
					'servlet-name' ("jawrCSSServlet")
					'url-pattern' (mapping)
				}
			}
		}

		// Init the Binary resource handler
		if(conf.jawr.binary || conf.jawr.css.image.classpath.use.servlet){

			servlets[servlets.size()-1] + {
				'servlet' {
					'servlet-name' ("jawrBinaryServlet")
					'servlet-class' ("net.jawr.web.servlet.JawrGrailsServlet")
					'init-param' {
						'param-name' ('type')
						'param-value' ('binary') }
					'load-on-startup' (1)
				}
			}

			def binaryMappings
			def mapping
			if(conf.jawr.binary.mapping){
				binaryMappings = conf.jawr.binary.mapping.split(';')
				if(binaryMappings.length == 1){
					mapping = [conf.jawr.binary.mapping + "*"]
				}else{
					mapping = binaryMappings
				}
			}else{
				mapping = ["*.png", "*.gif", "*.jpg", "*.jpeg", "*.ico", "*.ttf", "*.eot", "*.woff"]
			}

			mapping.each{  itMapping ->
				servletMappings[servletMappings.size()-1] + {
					'servlet-mapping'
					{
						'servlet-name' ("jawrBinaryServlet")
						'url-pattern' (itMapping.trim())
					}
				}
			}
		}

	}

	def doWithSpring = {

	}

	def doWithDynamicMethods = { ctx ->
	
	}

	/**
	 * Initializes the Jawr requestHandler instances which will attend of javascript and CSS requests. 
	 */
	def doWithApplicationContext = { applicationContext ->
		def conf =  Holders.config 

		if(conf.jawr) {

			// Set the plugin path
			def pluginPaths = [:]
			def pluginMsgPaths = [:]
			def pluginManager = Holders.pluginManager
			def pluginSettings = GPU.getPluginBuildSettings()
			if(pluginManager!=null) {
				for(plugin in pluginManager.userPlugins) {
					if(application.warDeployed){
						pluginPaths.put(plugin.name, plugin.pluginPath);
						pluginMsgPaths.put(plugin.fileSystemName, plugin.pluginPath);
						pluginMsgPaths.put(plugin.name, plugin.pluginPath);
					}else{

						def dir = pluginSettings.getPluginDirForName(GrailsNameUtils.getScriptName(plugin.name))
						def webappDir = dir ? new File("${dir.file.absolutePath}/web-app") : null
						if (webappDir?.exists()){
							pluginPaths.put(plugin.name, webappDir.canonicalPath);
						}
						def i18nDir =  dir ? new File("${dir.file.absolutePath}/grails-app/i18n") : null
						if (i18nDir?.exists()){
							pluginMsgPaths.put(plugin.fileSystemName, dir.file.canonicalPath);
							pluginMsgPaths.put(plugin.name, dir.file.canonicalPath);
						}
					}
				}
			}

			applicationContext.servletContext.setAttribute(JawrGrailsConstant.JAWR_GRAILS_PLUGIN_PATHS, pluginPaths);
			applicationContext.servletContext.setAttribute(JawrGrailsConstant.JAWR_GRAILS_PLUGIN_MSG_PATHS, pluginMsgPaths);
			
			Properties jawrProps = filterJawrProps(conf);
			currentConfigHash = jawrProps.hashCode();
			applicationContext.servletContext.setAttribute(JawrGrailsConstant.JAWR_GRAILS_CONFIG_HASH,currentConfigHash);
			applicationContext.servletContext.setAttribute(JawrGrailsConstant.GRAILS_WAR_DEPLOYED,application.warDeployed);


			// Init the Javascript handler
			if(conf.jawr.js){
				// Pass properties
				def map = [type:"js",handlerName:'JavascriptJawrRequestHandler',(JawrGrailsConstant.JAWR_GRAILS_CONFIG_PROPERTIES_KEY):jawrProps]
				if(conf.jawr.js.mapping)
					map.put('mapping',conf.jawr.js.mapping);
				applicationContext.servletContext.setAttribute(JawrGrailsConstant.JAWR_GRAILS_JS_CONFIG ,map);
			}
			// Init the CSS handler
			if(conf.jawr.css){
				def map = [type:"css",handlerName:'CSSJawrRequestHandler',(JawrGrailsConstant.JAWR_GRAILS_CONFIG_PROPERTIES_KEY):jawrProps]
				if(conf.jawr.css.mapping)
					map.put('mapping',conf.jawr.css.mapping);
				applicationContext.servletContext.setAttribute(JawrGrailsConstant.JAWR_GRAILS_CSS_CONFIG ,map);
			}
			// Init the Image handler
			if(conf.jawr.binary || conf.jawr.css.image.classpath.use.servlet){
				def map = [type:"binary",handlerName:'BinaryJawrRequestHandler',(JawrGrailsConstant.JAWR_GRAILS_CONFIG_PROPERTIES_KEY):jawrProps]
				if(conf.jawr.binary.mapping && conf.jawr.binary.mapping.indexOf('*') == -1)
					map.put('mapping',conf.jawr.binary.mapping);

				applicationContext.servletContext.setAttribute(JawrGrailsConstant.JAWR_GRAILS_BINARY_CONFIG ,map);
			}
		}
		appContext = applicationContext;
	}

	/**
	 *  Gets a grails config object and extracts all Jawr properties from it, returning a Properties object 
	 *  with all of them. 
	 */
	def filterJawrProps = { config ->
		Properties props = config.toProperties();
		Properties rets = new Properties();
		props.each{ pair ->
			if(pair.key.startsWith('jawr'))
				rets.put(pair.key,pair.value);
		}
		rets.put('jawr.charset.name',config.grails.views.gsp.encoding);
		// Sets the servlet context reader to the grails one
		rets.put('jawr.servlet.context.reader.class', 'net.jawr.web.resource.handler.reader.grails.GrailsServletContextResourceReader');
		return rets;
	}

	/**
	 * Handles a configuration reloading event, refreshing jawr if needed. 
	 */
	def onChange = { event ->
		if(Holders.config.jawr) {
			// Check if Jawr properties have changed in this refresh
			Properties jawrProps = filterJawrProps(Holders.config);
			if(currentConfigHash != jawrProps.hashCode()){
				//doWithApplicationContext(appContext);
				// Reload IMG handler
				JawrRequestHandler requestHandler = (JawrRequestHandler) appContext.servletContext.getAttribute(JawrGrailsConstant.JAWR_GRAILS_BINARY_REQUEST_HANDLER);
				if(requestHandler != null){
					requestHandler.configChanged(jawrProps);
				}

				// Reload CSS handler
				requestHandler = (JawrRequestHandler) appContext.servletContext.getAttribute(JawrGrailsConstant.JAWR_GRAILS_CSS_REQUEST_HANDLER);
				if(requestHandler != null){
					requestHandler.configChanged(jawrProps);
				}

				// Reload JS handler
				requestHandler = (JawrRequestHandler) appContext.servletContext.getAttribute(JawrGrailsConstant.JAWR_GRAILS_JS_REQUEST_HANDLER);
				if(requestHandler != null){
					requestHandler.configChanged(jawrProps);
				}
			}
		}
	}

	def onConfigChange = { event ->
		// TODO Implement code that is executed when the project configuration changes.
		// The event is the same as for 'onChange'.
	}

	def onShutdown = { event ->
		// TODO Implement code that is executed when the application shuts down (optional)
	}
}
