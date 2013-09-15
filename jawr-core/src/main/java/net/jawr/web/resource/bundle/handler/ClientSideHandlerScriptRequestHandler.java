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
package net.jawr.web.resource.bundle.handler;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.resource.bundle.IOUtils;
import net.jawr.web.resource.bundle.variant.VariantUtils;
import net.jawr.web.servlet.RendererRequestUtils;

/**
 * Handles requests for the client side script used in non dynamic html pages. 
 * 
 * @author Jordi Hern�ndez Sell�s
 * @author Ibrahim Chaehoi
 */
public class ClientSideHandlerScriptRequestHandler {
	
	/** The start time */
	private static final long START_TIME = System.currentTimeMillis();
	
	/**
     * HTTP etag header
     */
    public static final String HEADER_ETAG = "ETag";

    /**
     * HTTP etag equivalent of HEADER_IF_MODIFIED
     */
    public static final String HEADER_IF_NONE = "If-None-Match";

    /**
     * HTTP header for when a file was last modified
     */
    public static final String HEADER_LAST_MODIFIED = "Last-Modified";

    /**
     * HTTP header to request only modified data
     */
    public static final String HEADER_IF_MODIFIED = "If-Modified-Since";

    /**
     * The name of the user agent HTTP header
     */
    public static final String HEADER_USER_AGENT = "User-Agent";

	/** The resource bundle handler */
	private ResourceBundlesHandler rsHandler;
	
	/** The Jawr config */
	private JawrConfig config;
	
	/** The handler cache */
	private Map<String, Handler> handlerCache;
	
	/**
	 * Placeholder for a script stringbuffer and its hashcode, meant to 
	 * avoid constant recalculation of the hash value. 
	 * 
	 * @author Jordi Hern�ndez Sell�s
	 */
	private static class Handler {
		String hash;
		StringBuffer data;
		Handler(StringBuffer data, String hash){
			this.data = data;
			this.hash = hash;
		}
	}
	
	/**
	 * Constructor
	 * @param rsHandler the resource bundle handler
	 * @param config the jawr config
	 */
	public ClientSideHandlerScriptRequestHandler(
			ResourceBundlesHandler rsHandler, JawrConfig config) {
		super();
		this.rsHandler = rsHandler;
		this.config = config;
		this.handlerCache = new ConcurrentHashMap<String, ClientSideHandlerScriptRequestHandler.Handler>();
	}
	

	/**
	 * Generates a locale dependent script used to include bundles in non dynamic html pages. 
	 * Uses a cache of said scripts to avoid constant regeneration. 
	 * It also keeps track of eTags and if-modified-since headers to take advantage of 
	 * client side caching. 
	 * 
	 * @param request the request
	 * @param response the response
	 */
	public void handleClientSideHandlerRequest(HttpServletRequest request, HttpServletResponse response){
		Handler handler;
		
		Map<String, String> variants = config.getGeneratorRegistry().resolveVariants(request);
		String variantKey = VariantUtils.getVariantKey(variants);
		
		if(handlerCache.containsKey(variantKey)){
			handler = (Handler) handlerCache.get(variantKey);
		}
		else {	
			StringBuffer sb = rsHandler.getClientSideHandler().getClientSideHandlerScript(request);
			handler = new Handler(sb, Integer.toString(sb.hashCode()));
			handlerCache.put(variantKey, handler);
		}

		// Decide wether to set a 304 response		
		if(useNotModifiedHeader(request,handler.hash)){
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
			return;
		}
		response.setHeader(HEADER_ETAG, handler.hash);
		response.setDateHeader(HEADER_LAST_MODIFIED,START_TIME);
		
		if(RendererRequestUtils.isRequestGzippable(request, this.config)) {			
			try {
				response.setHeader("Content-Encoding", "gzip");
				GZIPOutputStream gzOut = new GZIPOutputStream(response.getOutputStream());
				byte[] data = handler.data.toString().getBytes(this.config.getResourceCharset().name());
				gzOut.write(data, 0, data.length);
				gzOut.close();
			} catch (IOException e) {
				throw new BundlingProcessException("Unexpected IOException writing ClientSideHandlerScript",e);
			}
		}
		else {
			StringReader rd = new StringReader(handler.data.toString());
			try {
				Writer writer = response.getWriter();
                IOUtils.copy(rd, writer, true);
			} catch (IOException e) {
				throw new BundlingProcessException("Unexpected IOException writing ClientSideHandlerScript",e);
			}
		}
	}
	
	/**
	 * Determines wether a response should get a 304 response and empty body, according to 
	 * etags and if-modified-since headers. 
	 * 
	 * @param request
	 * @param scriptEtag
	 * @return
	 */
	private boolean useNotModifiedHeader(HttpServletRequest request,String scriptEtag) {
		long modifiedHeader = -1;
		try
        {
            modifiedHeader = request.getDateHeader(HEADER_IF_MODIFIED);
			if(modifiedHeader != -1)
				modifiedHeader -= modifiedHeader % 1000;
        }
        catch (RuntimeException ex){}
        String eTag = request.getHeader(HEADER_IF_NONE);
        if(modifiedHeader == -1) {
        	return scriptEtag.equals(eTag);
        }
        else if(null == eTag){
        	return modifiedHeader <= START_TIME;
        }
        else {
        	return scriptEtag.equals(eTag) && modifiedHeader <= START_TIME;
        }
    }
}
