/**
 * Copyright 2008-2013 Jordi Hernández Sellés, Ibrahim Chaehoi
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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.generator.AbstractJavascriptGenerator;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.generator.ResourceGenerator;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver;
import net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolverFactory;

import org.directwebremoting.Container;
import org.directwebremoting.extend.ContainerUtil;
import org.directwebremoting.extend.CreatorManager;
import org.directwebremoting.impl.DefaultCreatorManager;
import org.directwebremoting.impl.StartupUtil;
import org.directwebremoting.servlet.CachingHandler;
import org.directwebremoting.servlet.InterfaceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The DWR 3 ResourceGenerator for JAWR
 * 
 * @author Jordi Hernández Sellés
 * @author Nicolas Bourdeau
 * @author Ibrahim Chaehoi
 */
public class DWR3BeanGenerator extends AbstractJavascriptGenerator implements
		ResourceGenerator {

	private static final Logger log = LoggerFactory
			.getLogger(DWR3BeanGenerator.class);

	private static final String DWR_BUNDLE_PREFIX = "dwr";

	// Mapping keys
	private static final String ALL_INTERFACES_KEY = "_**";

	private static final String ENGINE_KEY = "_engine";
	private static final String UTIL_KEY = "_util";
	private static final String DTOS_KEY = "_dtos";
	private static final String WEBWORK_KEY = "_actionutil";
	private static final String BAYEUX_KEY = "_bayeux";
	private static final String GI_KEY = "_gi";

	// Path to DWR javascript files
	private static final String ENGINE_PATH = "url:/engine.js";
	private static final String UTIL_PATH = "url:/util.js";
	private static final String GI_PATH = "url:/gi.js";
	private static final String WEBWORK_PATH = "url:/webwork/DWRActionUtil.js";
	private static final String BAYEUX_PATH = "url:/dwr-bayeux.js";
	private static final String DTOS_PATH = "url:/dtoall.js";

	private static final String INTERFACE_HANDLER_URL = "interfaceHandlerUrl";

	// Convenience map to avoid many if-elses later
	private static final Map<String, String> dwrLibraries = new HashMap<String, String>(
			3);

	static {
		dwrLibraries.put(ENGINE_KEY, ENGINE_PATH);
		dwrLibraries.put(UTIL_KEY, UTIL_PATH);
		dwrLibraries.put(DTOS_KEY, DTOS_PATH);
		dwrLibraries.put(WEBWORK_KEY, WEBWORK_PATH);
		dwrLibraries.put(BAYEUX_KEY, BAYEUX_PATH);
		dwrLibraries.put(GI_KEY, GI_PATH);
	}

	/** The resolver */
	private ResourceGeneratorResolver resolver;

	public DWR3BeanGenerator() {
		resolver = ResourceGeneratorResolverFactory
				.createPrefixResolver(DWR_BUNDLE_PREFIX);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.BaseResourceGenerator#getPathMatcher
	 * ()
	 */
	public ResourceGeneratorResolver getResolver() {

		return resolver;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.TextResourceGenerator#createResource
	 * (net.jawr.web.resource.bundle.generator.GeneratorContext)
	 */
	public Reader createResource(GeneratorContext context) {
		String data = null;
		try {
			if (dwrLibraries.containsKey(context.getPath())) {
				// Static script
				data = getStaticScript(dwrLibraries.get(context.getPath()),
						context);
			} else if (ALL_INTERFACES_KEY.equals(context.getPath())) {
				// All interfaces
				data = getAllPublishedInterfaces(context);
			} else {
				// This must be a specific interface ...
				StringBuilder sb = new StringBuilder();
				StringTokenizer tk = new StringTokenizer(context.getPath(), "|");
				while (tk.hasMoreTokens()) {
					sb.append(getInterfaceScript(tk.nextToken(), context));
				}
				data = sb.toString();
			}

		} catch (IOException e) {
			throw new RuntimeException(
					"An IO error occured while getting content from DWR", e);
		}
		return new StringReader(data);
	}

	/**
	 * Get a static script from DWR (scripts that are not Interfaces nor DTOs)
	 * 
	 * @param path
	 *            The Script URL (i.e.: /engine.js)
	 * @param context
	 *            The GeneratorContext
	 * @return The script as a String
	 * @throws IOException
	 */
	private String getStaticScript(final String path,
			final GeneratorContext context) throws IOException {
		CachingHandler cachingHandler = (CachingHandler) getContainer(context)
				.getBean(path);
		String dwrMapping = context.getConfig().getDwrMapping();
		if(!dwrMapping.startsWith(JawrConstant.URL_SEPARATOR)){
			dwrMapping = JawrConstant.URL_SEPARATOR+dwrMapping;
		}
		return cachingHandler.generateCachableContent(context
				.getServletContext().getContextPath(), dwrMapping, "");
	}

	/**
	 * Get the DWR Container. If multiple DWR containers exist in the
	 * ServletContext, the first found is returned.
	 * 
	 * @param context
	 *            The GeneratorContext
	 * @return The DWR Container
	 */
	private Container getContainer(final GeneratorContext context) {
		List<Container> containers = StartupUtil
				.getAllPublishedContainers(context.getServletContext());
		// TODO: here we assume there is only one DWR Servlet / Container ... we
		// should find a way to math the container with the DWR mapping config
		for (Container container : containers) {
			return container;
		}
		throw new RuntimeException("FATAL: unable to find DWR Container!");
	}

	/**
	 * Get a specific interface script
	 * 
	 * @param scriptName
	 *            The name of the script
	 * @param context
	 *            The GeneratorContext
	 * @return The script as a String
	 * @throws IOException
	 */
	private String getInterfaceScript(String scriptName,
			final GeneratorContext context) throws IOException {
		Container container = getContainer(context);
		CreatorManager ctManager = (CreatorManager) container
				.getBean(CreatorManager.class.getName());
		if (ctManager.getCreator(scriptName, false) != null) {
			InterfaceHandler handler = (InterfaceHandler) ContainerUtil
					.getHandlerForUrlProperty(container, INTERFACE_HANDLER_URL);
			return handler.generateInterfaceScript(context.getServletContext()
					.getContextPath(), context.getConfig().getDwrMapping(),
					scriptName);
		} else
			throw new IllegalArgumentException("The DWR bean named '"
					+ scriptName
					+ "' was not found in any DWR configuration instance.");
	}

	/**
	 * Get all interfaces in one script
	 * 
	 * @param context
	 *            The GeneratorContext
	 * @return All interfaces scripts as a single string
	 * @throws IOException
	 */
	private String getAllPublishedInterfaces(final GeneratorContext context)
			throws IOException {

		StringBuilder sb = new StringBuilder();

		Container container = getContainer(context);

		// The creatormanager holds the list of beans
		CreatorManager ctManager = (CreatorManager) container
				.getBean(CreatorManager.class.getName());
		if (null != ctManager) {
			Collection<String> creators;
			if (ctManager instanceof DefaultCreatorManager) {
				DefaultCreatorManager creatorManager = (DefaultCreatorManager) ctManager;
				boolean currentDebugValue = creatorManager.isDebug();
				creatorManager.setDebug(true); // DWR will throw a
												// ScurityException if not in
												// debug mode ...
				creators = ctManager.getCreatorNames(false);
				creatorManager.setDebug(currentDebugValue);
			} else {
				log.warn("Getting creator names from an unknown CreatorManager. This may fail ...");
				creators = ctManager.getCreatorNames(false);
			}
			for (String name : creators) {
				if (log.isDebugEnabled())
					log.debug("_** mapping: generating found interface named: "
							+ name);
				InterfaceHandler handler = (InterfaceHandler) ContainerUtil
						.getHandlerForUrlProperty(container,
								INTERFACE_HANDLER_URL);
				sb.append(handler.generateInterfaceScript(context
						.getServletContext().getContextPath(), context
						.getConfig().getDwrMapping(), name));
			}
		}

		return sb.toString();
	}

}