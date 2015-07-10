/**
 * Copyright 2009-2013 Ibrahim Chaehoi
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
package net.jawr.web.servlet.mock;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.util.StringUtils;

/**
 * Mock implementation of the HttpServletRequest interface.
 * Supports the Servlet 2.5 API level.
 * 
 * This class is used by the Jawr build time class handler,
 * to fake http servlet request to the Jawr servlet.
 * 
 * @author Ibrahim Chaehoi
 */
public class MockServletRequest implements HttpServletRequest {

	/** The requested path */
	private String requestPath;

	/** The requested URI */
	private String requestURI;

	/** The requested URL */
	private String requestURL;

	/** The servlet path */
	private String servletPath;

	/** The context path */
	private String contextPath;

	/** The path info */
	private String pathInfo;

	/** The session */
	private HttpSession session;
	
	/** The request parameters */
	private Map<String, String> parameters = new HashMap<String, String>();
	
	/**
	 * Constructor 
	 */
	public MockServletRequest(String contextPath) {
		this.contextPath = contextPath;
	}

	/**
	 * Sets the requested path
	 * @param path the requested path
	 */
	public void setRequestPath(String mapping, String path) {
		this.requestPath = path;
		int paramStartIdx = requestPath.indexOf("?");
		if(StringUtils.isEmpty(mapping)){
			if(paramStartIdx != -1){
				this.servletPath = requestPath.substring(0, paramStartIdx);
			}else{
				this.servletPath = requestPath;
			}
		}else{
			this.servletPath = PathNormalizer.asPath(mapping);
			String pathInfo = removeServletMappingFromPath(path, mapping);
			this.pathInfo = pathInfo;
		}
		
		// Sets the request URI
		if(paramStartIdx == -1){
			this.requestURI = requestPath;
		}else{
			this.requestURI = requestPath.substring(0, paramStartIdx);
		}
		
		initParameters();
		
	}

	/**
	 * Remove the servlet mapping from the path
	 * @param path the path
	 * @param mapping the servlet mapping
	 * @return the path without the servlet mapping
	 */
	private String removeServletMappingFromPath(String path, String mapping) {
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
	 * Initialize the parameters from the request path
	 * This is a naive implementation which serves only the purpose of bundle generation.
	 * It don't even handle multiple parameters value...
	 */
	private void initParameters()  {
		
		int idx = requestPath.indexOf("?");
		if(idx != -1){
			
			String strParams = null;
			try {
				strParams = URLDecoder.decode(requestPath.substring(idx+1), "UTF-8");
			} catch (UnsupportedEncodingException neverHappens) {
				/*URLEncoder:how not to use checked exceptions...*/
				throw new RuntimeException("Something went unexpectedly wrong while decoding a URL for a generator. ",
											neverHappens);
			}
			
			String[] params = strParams.split("&");
			for (int i = 0; i < params.length; i++) {
				String[] param = params[i].split("=");
				parameters.put(param[0], param[1]);
			}
		}else{ // No parameters
			parameters.clear();
		}
	}

	/**
	 * Sets the session
	 * @param session the session to set
	 */
	public void setSession(HttpSession session) {
		this.session = session;
	}

	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getAuthType()
	 */
	public String getAuthType() {
		throw new RuntimeException("Unsupported operation");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getContextPath()
	 */
	public String getContextPath() {
		//return "/";
		//return "";
		return contextPath;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getMethod()
	 */
	public String getMethod() {
		
		return "GET";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathInfo()
	 */
	public String getPathInfo() {
		
		return pathInfo;
	}

	/**
	 * Sets the pqth info
	 * @param pathInfo the pathInfo to set
	 */
	public void setPathInfo(String pathInfo) {
		this.pathInfo = pathInfo;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getCookies()
	 */
	public Cookie[] getCookies() {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
	 */
	public long getDateHeader(String name) {

		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
	 */
	public String getHeader(String name) {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
	 */
	public Enumeration<String> getHeaderNames() {

		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
	 */
	public Enumeration<String> getHeaders(String name) {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
	 */
	public int getIntHeader(String name) {
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
	 */
	public String getPathTranslated() {

		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getQueryString()
	 */
	public String getQueryString() {

		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
	 */
	public String getRemoteUser() {

		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURI()
	 */
	public String getRequestURI() {

		return requestURI;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURL()
	 */
	public StringBuffer getRequestURL() {

		StringBuffer result = null;
		if(requestURL != null){
			result = new StringBuffer(requestURL);
		}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
	 */
	public String getRequestedSessionId() {
		return session.getId();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getServletPath()
	 */
	public String getServletPath() {
		return servletPath;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession()
	 */
	public HttpSession getSession() {
		return session;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
	 */
	public HttpSession getSession(boolean create) {
		return session;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
	 */
	public Principal getUserPrincipal() {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
	 */
	public boolean isRequestedSessionIdFromCookie() {
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
	 */
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
	 */
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
	 */
	public boolean isRequestedSessionIdValid() {
		
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
	 */
	public boolean isUserInRole(String role) {
		
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String name) {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttributeNames()
	 */
	public Enumeration<String> getAttributeNames() {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentLength()
	 */
	public int getContentLength() {
		
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentType()
	 */
	public String getContentType() {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getInputStream()
	 */
	public ServletInputStream getInputStream() throws IOException {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocale()
	 */
	public Locale getLocale() {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocales()
	 */
	public Enumeration<Locale> getLocales() {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
	 */
	public String getParameter(String name) {
		
		return (String) parameters.get(name);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterMap()
	 */
	public Map<String, String> getParameterMap() {
		
		return parameters;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterNames()
	 */
	public Enumeration<String> getParameterNames() {
		
		return Collections.enumeration(parameters.keySet());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
	 */
	public String[] getParameterValues(String name) {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getProtocol()
	 */
	public String getProtocol() {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getReader()
	 */
	public BufferedReader getReader() throws IOException {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
	 */
	public String getRealPath(String path) {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteAddr()
	 */
	public String getRemoteAddr() {
		
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteHost()
	 */
	public String getRemoteHost() {

		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
	 */
	public RequestDispatcher getRequestDispatcher(String path) {

		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getScheme()
	 */
	public String getScheme() {

		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerName()
	 */
	public String getServerName() {

		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerPort()
	 */
	public int getServerPort() {

		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#isSecure()
	 */
	public boolean isSecure() {

		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name) {

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String name, Object o) {

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemotePort()
	 */
	@Override
	public int getRemotePort() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalName()
	 */
	@Override
	public String getLocalName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalAddr()
	 */
	@Override
	public String getLocalAddr() {
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalPort()
	 */
	@Override
	public int getLocalPort() {
		return 0;
	}
	
	/**
	 * Sets the URL
	 * @param url the URL
	 */
	public void setRequestUrl(String url) {
		requestURL = url;
	}


}
