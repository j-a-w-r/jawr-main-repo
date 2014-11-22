/**
 * Copyright 2007-2013 Jordi Hernández Sellés, Matt Ruby, Ibrahim Chaehoi
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.config.jmx.JawrApplicationConfigManager;
import net.jawr.web.context.ThreadLocalJawrContext;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.renderer.BundleRenderer;
import net.jawr.web.resource.bundle.renderer.BundleRendererContext;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;

/**
 * Utilities for tag rendering components, which help in handling request lifecycle aspects.
 * 
 * @author Jordi Hernández Sellés
 * @author Matt Ruby
 * @author Ibrahim Chaehoi
 * 
 */
public class RendererRequestUtils {
	
	/** The logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(RendererRequestUtils.class.getName());
	
	/** The bundle renderer context attribute name */
	private static final String BUNDLE_RENDERER_CONTEXT_ATTR_PREFIX = "net.jawr.web.resource.renderer.BUNDLE_RENDERER_CONTEXT";
	
	/** The IE user agent pattern */
	private static Pattern IE_USER_AGENT_PATTERN = Pattern.compile("MSIE (\\d+)");
	
	/**
	 * Returns the bundle renderer context.
	 * 
	 * @param request the request
	 * @param resourceType the resource type
	 * @return the bundle renderer context.
	 */
	public static BundleRendererContext getBundleRendererContext(HttpServletRequest request, BundleRenderer renderer) {
		String bundleRendererCtxAttributeName = BUNDLE_RENDERER_CONTEXT_ATTR_PREFIX+renderer.getResourceType();
		
		BundleRendererContext ctx = (BundleRendererContext) request.getAttribute(bundleRendererCtxAttributeName);
		if(ctx == null){
			ctx = new BundleRendererContext(request, renderer.getBundler().getConfig());
	        request.setAttribute(bundleRendererCtxAttributeName, ctx);
		}
		
		return ctx;
		
	}

	/**
	 * Sets the bundle renderer context.
	 * 
	 * @param request the request
	 * @param resourceType the resource type
	 * @param ctx the bundle renderer context to set.
	 */
	public static void setBundleRendererContext(ServletRequest request, String resourceType, BundleRendererContext ctx) {
		String globalBundleAddedAttributeName = BUNDLE_RENDERER_CONTEXT_ATTR_PREFIX+resourceType;
		request.setAttribute(globalBundleAddedAttributeName, ctx);
	}
	
	/**
	 * Determines wether gzip is suitable for the current request given the current config.
	 * 
	 * @param req
	 * @param jawrConfig
	 * @return
	 */
	public static boolean isRequestGzippable(HttpServletRequest req, JawrConfig jawrConfig) {
		boolean rets;
		// If gzip is completely off, return false.
		if (!jawrConfig.isGzipResourcesModeOn())
			rets = false;
		else if (req.getHeader("Accept-Encoding") != null && req.getHeader("Accept-Encoding").indexOf("gzip") != -1) {

			// If gzip for IE6 or less is off, the user agent is checked to avoid compression.
			if (!jawrConfig.isGzipResourcesForIESixOn() && isIE6orLess(req)) {
				rets = false;
				if (LOGGER.isDebugEnabled()){
					LOGGER.debug("Gzip enablement for IE executed, with result:" + rets);
				}
			} else
				rets = true;
		} else
			rets = false;
		return rets;
	}

	/**
	 * Checks if the user agent is IE
	 * @param req the request
	 * @return true if the user agent is IE
	 */
	public static boolean isIE(HttpServletRequest req) {
	
		String agent = req.getHeader("User-Agent");
		return null != agent && agent.indexOf("MSIE") != -1;
	}
	
	/**
	 * Checks if the user agent is IE6 or less
	 * @param req the request
	 * @return true if the user agent is IE6 or less
	 */
	public static boolean isIE6orLess(HttpServletRequest req) {
	
		return isIEVersionInferiorOrEqualTo(req, 6);
	}

	/**
	 * Checks if the user agent is IE7 or less
	 * @param req the request
	 * @return true if the user agent is IE7 or less
	 */
	public static boolean isIE7orLess(HttpServletRequest req) {
	
		return isIEVersionInferiorOrEqualTo(req, 7);
	}

	/**
	 * Checks if the user agent is IE and the version is equal or less than the one passed in parameter
	 * @param req the request
	 * @param the ie version to check
	 * @return true if the user agent is IE and the version is equal or less than the one passed in parameter
	 */
	private static boolean isIEVersionInferiorOrEqualTo(HttpServletRequest req, int ieVersion) {
		
		boolean result = false;
		String agent = req.getHeader("User-Agent");
		if (LOGGER.isDebugEnabled()){
			LOGGER.debug("User-Agent for this request:" + agent);
		}
		if(agent != null){

			Matcher matcher = IE_USER_AGENT_PATTERN.matcher(agent);
			if(matcher.find()){
				int version = Integer.parseInt(matcher.group(1));
				if(version <= ieVersion){
					result = true;
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Determines wether to override the debug settings. Sets the debugOverride status on ThreadLocalJawrContext
	 * 
	 * @param req the request
	 * @param jawrConfig the jawr config
	 * 
	 */
	public static void setRequestDebuggable(HttpServletRequest req, JawrConfig jawrConfig) {

		// make sure we have set an overrideKey
		// make sure the overrideKey exists in the request
		// lastly, make sure the keys match
		if (jawrConfig.getDebugOverrideKey().length() > 0 && null != req.getParameter(JawrConstant.OVERRIDE_KEY_PARAMETER_NAME)
				&& jawrConfig.getDebugOverrideKey().equals(req.getParameter(JawrConstant.OVERRIDE_KEY_PARAMETER_NAME))) {
			ThreadLocalJawrContext.setDebugOverriden(true);
		} else {
			ThreadLocalJawrContext.setDebugOverriden(false);
		}

		// Inherit the debuggable property of the session if the session is a debuggable one
		inheritSessionDebugProperty(req);

	}

	/**
	 * Sets a request debuggable if the session is a debuggable session.
	 * 
	 * @param req the request
	 */
	public static void inheritSessionDebugProperty(HttpServletRequest request) {

		HttpSession session = request.getSession(false);
		if (session != null) {
			String sessionId = session.getId();

			JawrApplicationConfigManager appConfigMgr = (JawrApplicationConfigManager) session.getServletContext().getAttribute(
					JawrConstant.JAWR_APPLICATION_CONFIG_MANAGER);

			// If the session ID is a debuggable session ID, activate debug mode for the request.
			if (appConfigMgr != null && appConfigMgr.isDebugSessionId(sessionId)) {
				ThreadLocalJawrContext.setDebugOverriden(true);
			}
		}
	}
	
	/**
	 * Returns true if the request URL is a SSL request (https://) 
	 * @param request the request
	 * @return true if the request URL is a SSL request
	 */
	public static boolean isSslRequest(HttpServletRequest request) {
		
		String scheme = request.getScheme();
		return JawrConstant.HTTPS.equals(scheme);
	}

	/**
	 * Renders the URL taking in account the context path, the jawr config 
	 * @param newUrl the URL
	 * @param jawrConfig the jawr config
	 * @param contextPath the context path
	 * @param sslRequest the flag indicating if it's an SSL request or not
	 * @return the new URL
	 */
	public static String getRenderedUrl(String url, JawrConfig jawrConfig,
			String contextPath, boolean sslRequest) {
		
		String contextPathOverride = getContextPathOverride(sslRequest, jawrConfig);
		// If the contextPathOverride is not null and we are in production mode,
		// or if we are in debug mode but we should use the contextPathOverride even in debug mode
		// then use the contextPathOverride
		
		String renderedUrl = url;
		if(contextPathOverride != null && 
				((jawrConfig.isDebugModeOn() && jawrConfig.isUseContextPathOverrideInDebugMode()) ||
				!jawrConfig.isDebugModeOn())) {
			
				String override = contextPathOverride;
				// Blank override, create url relative to path
				if ("".equals(override)){
					if(url.startsWith("/")) {
						renderedUrl = renderedUrl.substring(1);
					}
				} else{
					renderedUrl = PathNormalizer.joinPaths(override, renderedUrl);
				}
		} else{
			renderedUrl = PathNormalizer.joinPaths(contextPath, renderedUrl);
		}
		
		return renderedUrl;
	}

	/**
	 * Returns the context path depending on the request mode (SSL or not)
	 * 
	 * @param isSslRequest the flag indicating that the request is an SSL request
	 * @return the context path depending on the request mode
	 */
	private static String getContextPathOverride(boolean isSslRequest, JawrConfig config) {
		String contextPathOverride = null;
		if (isSslRequest) {
			contextPathOverride = config.getContextPathSslOverride();
		} else {
			contextPathOverride = config.getContextPathOverride();
		}
		return contextPathOverride;
	}
	
	/**
	 * Refresh the Jawr config if a manual reload has been ask using the refresh key parameter from the URL
	 * 
	 * @param request the request 
	 * @param jawrConfig the Jawr config
	 * 
	 * @return true if the config has been refreshed
	 */
	public static boolean refreshConfigIfNeeded(HttpServletRequest request,
			JawrConfig jawrConfig) {
	
		boolean refreshed = false;
		if (request.getAttribute(JawrConstant.JAWR_BUNDLE_REFRESH_CHECK) == null) {
		    
			request.setAttribute(JawrConstant.JAWR_BUNDLE_REFRESH_CHECK, Boolean.TRUE);

		    if (jawrConfig.getRefreshKey().length() > 0 && null != request.getParameter(JawrConstant.REFRESH_KEY_PARAM)
				&& jawrConfig.getRefreshKey().equals(request.getParameter(JawrConstant.REFRESH_KEY_PARAM))) {
			
				JawrApplicationConfigManager appConfigMgr = (JawrApplicationConfigManager) request.getSession().getServletContext().getAttribute(JawrConstant.JAWR_APPLICATION_CONFIG_MANAGER);
				if(appConfigMgr == null){
					throw new IllegalStateException("JawrApplicationConfigManager is not present in servlet context. Initialization of Jawr either failed or never occurred.");
				}
				appConfigMgr.refreshConfig();
				refreshed = true;
			}
		}
		return refreshed;
	}
	
}
