/**
 * Copyright 2008-2011 Jordi Hernández Sellés
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
package net.jawr.web.resource.bundle.generator.dwr;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.generator.AbstractJavascriptGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolverFactory;
import net.jawr.web.resource.bundle.renderer.DWRParamWriter;

import org.apache.log4j.Logger;
import org.directwebremoting.Container;
import org.directwebremoting.extend.CreatorManager;
import org.directwebremoting.extend.DwrConstants;
import org.directwebremoting.extend.EnginePrivate;
import org.directwebremoting.extend.Remoter;
import org.directwebremoting.extend.ServerLoadMonitor;
import org.directwebremoting.impl.ContainerUtil;
import org.directwebremoting.impl.DefaultCreatorManager;

/**
 * Generator that creates resources from DWR beans. 
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class DWRBeanGenerator extends AbstractJavascriptGenerator {
	private static final Logger LOGGER = Logger.getLogger(DWRBeanGenerator.class.getName());

	/** The message bundle prefix */
	public static final String DWR_BUNDLE_PREFIX = "dwr";

	// Mapping keys
	private static final String ALL_INTERFACES_KEY = "_**";
	
	private static final String ENGINE_KEY = "_engine";
	private static final String UTIL_KEY = "_util";
	private static final String AUTH_KEY = "_auth";
	private static final String WEBWORK_KEY = "_actionutil";
	

	// Path to DWR javascript files
	private static final String ENGINE_PATH = "org/directwebremoting/engine.js";
	private static final String UTIL_PATH = "org/directwebremoting/util.js";
	private static final String AUTH_PATH = "org/directwebremoting/auth.js";
	private static final String WEBWORK_PATH = "org/directwebremoting/webwork/DWRActionUtil.js";
	
	// Convenience map to avoid many if-elses later
	private static final Map<String, String> DWR_LIBRARIES = new HashMap<String, String>(3);
	
	// Script replacement to refer to a javascript var that JAWR creates
	private static final String JS_PATH_REF = "'+JAWR.jawr_dwr_path+'";
	private static final String JS_CTX_PATH = "'+JAWR.app_context_path+'/";
	
	// Some names of init-params in DWR servlets
	private static final String DWR_MAPPING_PARAM = "jawr_mapping";
	private static final String DWR_OVERRIDEPATH_PARAM = "overridePath";
	
	// A patter to replace some expressions at the engine javascript
	private static final Pattern PARAMS_PATTERN = Pattern.compile("(\\$\\{allowGetForSafariButMakeForgeryEasier}|"  
											 + "\\$\\{pollWithXhr}|"  
											 + "\\$\\{scriptSessionId}|"  
											 + "\\$\\{sessionCookieName}|"  
											 + "\\$\\{scriptTagProtection}|"  
											 + "\\$\\{defaultPath})");
	
	// This is a script portion stored in a static method of DWR. We store it to remove it, so it is not replicated many times.  
	private final static String ENGINE_INIT;
	
	static {
		ENGINE_INIT = EnginePrivate.getEngineInitScript();
		DWR_LIBRARIES.put(UTIL_KEY, UTIL_PATH);
		DWR_LIBRARIES.put(AUTH_KEY, AUTH_PATH);
		DWR_LIBRARIES.put(WEBWORK_KEY, WEBWORK_PATH);
	}
	
	/** The resolver */
	private ResourceGeneratorResolver resolver;
	
	public DWRBeanGenerator() {
		resolver = ResourceGeneratorResolverFactory.createPrefixResolver(DWR_BUNDLE_PREFIX);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.BaseResourceGenerator#getPathMatcher()
	 */
	public ResourceGeneratorResolver getResolver() {
	
		return resolver;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.ResourceGenerator#createResource(java.lang.String, java.nio.charset.Charset)
	 */
	public Reader createResource(GeneratorContext context) {
		ServletContext servletContext = context.getServletContext();
		StringBuffer data = null;
		String path = context.getPath();
		if(ENGINE_KEY.equals(path)) {
			data = buildEngineScript(readDWRScript(ENGINE_PATH),servletContext);
		}
		else if(DWR_LIBRARIES.containsKey(path)){
			data = readDWRScript((String)DWR_LIBRARIES.get(path));
		}
		else if(ALL_INTERFACES_KEY.equals(path)) {
			data = new StringBuffer(ENGINE_INIT);
			data.append(getAllPublishedInterfaces(servletContext));
		}
		else {
			data = new StringBuffer(ENGINE_INIT);
			StringTokenizer tk = new StringTokenizer(path,"|");
			while(tk.hasMoreTokens()) {
				data.append(getInterfaceScript(tk.nextToken(),servletContext));
			}
		}
		
		return new StringReader(data.toString());
	}
	
	/**
	 * Performs replacement on the engine.js script from DWR. 
	 * Mainly copies what DWR does, only at startup. A couple params are actually
	 * replaced to references to javascript vars that jawr will create on the page. 
	 * 
	 * @param engineScript
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private StringBuffer buildEngineScript(StringBuffer engineScript,ServletContext servletContext) {
		List<Container> containers = ContainerUtil.getAllPublishedContainers(servletContext);
		String allowGetForSafariButMakeForgeryEasier = "";
		String scriptTagProtection = DwrConstants.SCRIPT_TAG_PROTECTION;
		String pollWithXhr = "";
		String sessionCookieName = "JSESSIONID";
		

		
		for(Iterator<Container> it = containers.iterator();it.hasNext();) {
			Container container = it.next();
			ServerLoadMonitor monitor = (ServerLoadMonitor) container.getBean(ServerLoadMonitor.class.getName());
			pollWithXhr = monitor.supportsStreaming() ? "false" : "true";
			
			if(null != container.getBean("allowGetForSafariButMakeForgeryEasier")){
				allowGetForSafariButMakeForgeryEasier = (String)container.getBean("allowGetForSafariButMakeForgeryEasier");
			}
			if(null != container.getBean("scriptTagProtection")){
				scriptTagProtection = (String)container.getBean("scriptTagProtection");
			}
			if(null != container.getBean("sessionCookieName")){
				sessionCookieName = (String)container.getBean("sessionCookieName");
			}		
		}
		StringBuffer sb = new StringBuffer();
		Matcher matcher = PARAMS_PATTERN.matcher(engineScript);
		while(matcher.find()) {
			String match = matcher.group();
			if("${allowGetForSafariButMakeForgeryEasier}".equals(match)){
				matcher.appendReplacement(sb, allowGetForSafariButMakeForgeryEasier);				
			}
			else if("${pollWithXhr}".equals(match)){
				matcher.appendReplacement(sb, pollWithXhr);				
			}
			else if("${sessionCookieName}".equals(match)){
				matcher.appendReplacement(sb, sessionCookieName);				
			}
			else if("${scriptTagProtection}".equals(match)){
				matcher.appendReplacement(sb, scriptTagProtection);				
			}
			else if("${scriptSessionId}".equals(match)){
				matcher.appendReplacement(sb, "\"+JAWR.dwr_scriptSessionId+\"");	
			}
			else if("${defaultPath}".equals(match)){

				matcher.appendReplacement(sb, "\"+JAWR.jawr_dwr_path+\"");
			}
		}
		DWRParamWriter.setUseDynamicSessionId(true);
		matcher.appendTail(sb);
		return sb;
	}
	
	/**
	 * Read a DWR utils script from the classpath. 
	 * @param classpath
	 * @return
	 */
	private StringBuffer readDWRScript(String classpath) {
		StringBuffer sb = null;
		try {
			InputStream is = ClassLoaderResourceUtils.getResourceAsStream(classpath, this);
			ReadableByteChannel chan = Channels.newChannel(is);
			Reader r = Channels.newReader(chan,"utf-8");
			StringWriter sw = new StringWriter();
            IOUtils.copy(r, sw, true);
			sb = sw.getBuffer();
			
		} catch (FileNotFoundException e) {
			throw new BundlingProcessException(e);
		} catch (IOException e) {
			throw new BundlingProcessException(e);
		}
		
		return sb;
	}
	
	/**
	 * Returns a script with a specified DWR interface
	 * @param basePath
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private StringBuffer getInterfaceScript(String scriptName,ServletContext servletContext) {
		StringBuffer sb = new StringBuffer(ENGINE_INIT);

		// List all containers to find all DWR interfaces
		List<Container> containers = ContainerUtil.getAllPublishedContainers(servletContext);
		boolean found = false;
		for(Iterator<Container> it = containers.iterator();it.hasNext() && !found;) {
			Container container = it.next();
			
			// The creatormanager holds the list of beans
			CreatorManager ctManager = (CreatorManager) container.getBean(CreatorManager.class.getName());
			if( null != ctManager ) {
				// The remoter builds interface scripts. 
				Remoter remoter = (Remoter) container.getBean(Remoter.class.getName());
				String path = getPathReplacementString(container);
				try {
					String script = remoter.generateInterfaceScript(scriptName, path);
					found = true;
					// Must remove the engine init script to avoid unneeded duplication
					script = removeEngineInit(script);
					sb.append(script);
				}
				catch(SecurityException ex){throw new BundlingProcessException(ex); }
			}
		}
		if(!found)
			throw new IllegalArgumentException("The DWR bean named '" + scriptName + "' was not found in any DWR configuration instance."); 
		
		return sb;
	}



	/**
	 * Gets the appropiate path replacement string for a DWR container
	 * @param container
	 * @return
	 */
	private String getPathReplacementString(Container container) {
		String path = JS_PATH_REF;
		if(null != container.getBean(DWR_OVERRIDEPATH_PARAM)) {
			path = (String) container.getBean(DWR_OVERRIDEPATH_PARAM);
		}
		else if(null != container.getBean(DWR_MAPPING_PARAM)) {
			path = JS_CTX_PATH + container.getBean(DWR_MAPPING_PARAM);
		}
		return path;
	}
	
	/**
	 * Returns a script with all the DWR interfaces available in the servletcontext
	 * @param basePath
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private StringBuffer getAllPublishedInterfaces(ServletContext servletContext) {
		
		StringBuffer sb = new StringBuffer();

		// List all containers to find all DWR interfaces
		List<Container> containers = ContainerUtil.getAllPublishedContainers(servletContext);
		for(Iterator<Container> it = containers.iterator();it.hasNext();) {
			Container container = (Container) it.next();
			
			// The creatormanager holds the list of beans
			CreatorManager ctManager = (CreatorManager) container.getBean(CreatorManager.class.getName());
			
			if(null != ctManager) {
				// The remoter builds interface scripts. 
				Remoter remoter = (Remoter) container.getBean(Remoter.class.getName());
				
				String path = getPathReplacementString(container);
				boolean debugMode = ctManager.isDebug();
				Collection creators = null;
				if(!(ctManager instanceof DefaultCreatorManager)) {
					if(!debugMode)
						LOGGER.warn("The current creatormanager is a custom implementation [" 
								+ ctManager.getClass().getName() 
								+ "]. Debug mode is off, so the mapping dwr:_** is likely to trigger a SecurityException." +
								" Attempting to get all published creators..." );
					creators = ctManager.getCreatorNames();
					
				}
				else {	
					DefaultCreatorManager dfCreator = (DefaultCreatorManager) ctManager;					
					try 
					{
						dfCreator.setDebug(true);
						creators = ctManager.getCreatorNames();
					}
					finally{
						// restore debug mode no matter what
						dfCreator.setDebug(debugMode);
					}
				}
				for(Iterator<String> names = creators.iterator();names.hasNext();) {
					String script = remoter.generateInterfaceScript(names.next(), path);
					// Must remove the engine init script to avoid unneeded duplication
					script = removeEngineInit(script);
					sb.append(script);
				}
			}
		}
		return sb;
	}
	
	/**
	 * Removes the engine init script so that it is not repeated unnecesarily. 
	 * @param script
	 * @return
	 */
	private String removeEngineInit(String script) {
		int start = script.indexOf(ENGINE_INIT);
		int end = start + ENGINE_INIT.length();
		StringBuffer rets = new StringBuffer();
		
		if(start > 0) {
			rets.append(script.substring(0, start)).append("\n");
		}
		rets.append(script.substring(end));
		
		
		return rets.toString();
	}

}
