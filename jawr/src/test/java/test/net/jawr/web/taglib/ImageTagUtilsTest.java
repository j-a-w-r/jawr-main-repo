/**
 * 
 */
package test.net.jawr.web.taglib;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import junit.framework.TestCase;
import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.ImageResourcesHandler;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.handler.reader.ResourceReader;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.taglib.ImageTagUtils;

/**
 * @author ibrahim Chaehoi
 * 
 */
public class ImageTagUtilsTest extends TestCase {

	private JawrConfig config;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	public void setUp(){
		config = new JawrConfig("img", new Properties());
		config.setImageHashAlgorithm("MD5");
		GeneratorRegistry generatorRegistry = new GeneratorRegistry(JawrConstant.IMG_TYPE);
		config.setGeneratorRegistry(generatorRegistry);
		generatorRegistry.setConfig(config);
	}
	
	public void testImageUrl() {

		testImageUrlWithContextPathOverride("http://mycomp.com/basicwebapp", config, "/basicwebapp");
	}
	
	public void testImageUrlWithServletMapping() {

		config.setServletMapping("/jawrImg/");
		testImageUrlWithContextPathOverride("http://mycomp.com/basicwebapp", config, "/basicwebapp");
	}

	public void testImageUrlWithContextPathOverride() {

		String contextPathOverride = "http://mycdn";
		config.setContextPathOverride(contextPathOverride+"/");
		config.setContextPathSslOverride("https://mycdn/");
		testImageUrlWithContextPathOverride("http://mycomp.com/basicwebapp", config, contextPathOverride);

	}

	public void testImageUrlWithContextPathSslOverride() {

		String contextPathOverride = "https://mycdn";
		config.setContextPathSslOverride(contextPathOverride+"/");
		config.setContextPathOverride("http://mycdn/");
		testImageUrlWithContextPathOverride("https://mycomp.com/basicwebapp", config, contextPathOverride);

	}
	
	private void testImageUrlWithContextPathOverride(String requestUrl, JawrConfig config,
			String contextPath) {
		HttpServletRequest request = new MockHttpServletRequest(requestUrl,
				"/basicwebapp/", "/basicwebapp/content/myPage.jsp");
		HttpServletResponse response = new MockHttpResponse();
		ResourceReaderHandler rsHandler = new MockResourceHandler();
		ImageResourcesHandler imgRsHandler = new ImageResourcesHandler(config, rsHandler, null);
		String servletMapping = null;
		if(config.getServletMapping() != null && config.getServletMapping().trim().length() > 0){
			servletMapping = "/"+config.getServletMapping();
		}else{
			servletMapping = "";
		}
		
		String result = ImageTagUtils.getImageUrl("/img/logo/myLogo.png",
				imgRsHandler, request, response);
		String expectedResult = contextPath+servletMapping+"/cb90c55a38064627dca337dfa5fc5be120/img/logo/myLogo.png";
		assertEquals(expectedResult, result);

		result = ImageTagUtils.getImageUrl("../img/logo/myLogo.png",
				imgRsHandler, request, response);
		expectedResult = contextPath+servletMapping+"/cb90c55a38064627dca337dfa5fc5be120/img/logo/myLogo.png";
		assertEquals(expectedResult, result);

		result = ImageTagUtils.getImageUrl("./img/logo/myLogo.png",
				imgRsHandler, request, response);
		expectedResult = contextPath+servletMapping+"/cb90c55a38064627dca337dfa5fc5be120/content/img/logo/myLogo.png";
		assertEquals(expectedResult, result);

		result = ImageTagUtils.getImageUrl("img/logo/myLogo.png", imgRsHandler,
				request, response);
		expectedResult = contextPath+servletMapping+"/cb90c55a38064627dca337dfa5fc5be120/content/img/logo/myLogo.png";
		assertEquals(expectedResult, result);
	}

	
	private static class MockResourceHandler implements ResourceReaderHandler {

		public InputStream getResourceAsStream(String resourceName)
				throws ResourceNotFoundException {

			return new ByteArrayInputStream("dummy content".getBytes());
		}

		public void addResourceReaderToEnd(ResourceReader rd) {
			
		}

		public void addResourceReaderToStart(ResourceReader rd) {
			
		}

		public Reader getResource(String resourceName)
				throws ResourceNotFoundException {
			return null;
		}

		public Reader getResource(String resourceName, boolean processingBundle)
				throws ResourceNotFoundException {
			return null;
		}

		public InputStream getResourceAsStream(String resourceName,
				boolean processingBundle) throws ResourceNotFoundException {
			return null;
		}

		public Set<String> getResourceNames(String dirPath) {
			return null;
		}

		public String getWorkingDirectory() {
			return null;
		}

		public boolean isDirectory(String resourcePath) {
			return false;
		}

		public void setWorkingDirectory(String workingDir) {
			
		}

		@Override
		public Reader getResource(String resourceName,
				boolean processingBundle, List<Class<?>> excludedReader)
				throws ResourceNotFoundException {
			return null;
		}
	}

	private static class MockHttpServletRequest implements HttpServletRequest {

		private String contextPath;
		private String requestUri;
		private StringBuffer requestUrl;

		public MockHttpServletRequest(String requestUrl, String contextPath, String requestUri) {
			this.contextPath = contextPath;
			this.requestUri = requestUri;
			this.requestUrl = new StringBuffer(requestUrl);
		}

		public String getAuthType() {
			return null;
		}

		public String getContextPath() {
			return contextPath;
		}

		public Cookie[] getCookies() {
			return null;
		}

		public long getDateHeader(String name) {
			return 0;
		}

		public String getHeader(String name) {
			return null;
		}

		public Enumeration<String> getHeaderNames() {
			return null;
		}

		public Enumeration<String> getHeaders(String name) {
			return null;
		}

		public int getIntHeader(String name) {
			return 0;
		}

		public String getMethod() {
			return null;
		}

		public String getPathInfo() {
			return null;
		}

		public String getPathTranslated() {
			return null;
		}

		public String getQueryString() {
			return null;
		}

		public String getRemoteUser() {
			return null;
		}

		public String getRequestURI() {
			return requestUri;
		}

		public StringBuffer getRequestURL() {
			return requestUrl;
		}

		public String getRequestedSessionId() {
			return null;
		}

		public String getServletPath() {
			return null;
		}

		public HttpSession getSession() {
			return null;
		}

		public HttpSession getSession(boolean create) {
			return null;
		}

		public Principal getUserPrincipal() {
			return null;
		}

		public boolean isRequestedSessionIdFromCookie() {

			return false;
		}

		public boolean isRequestedSessionIdFromURL() {

			return false;
		}

		public boolean isRequestedSessionIdFromUrl() {

			return false;
		}

		public boolean isRequestedSessionIdValid() {

			return false;
		}

		public boolean isUserInRole(String role) {

			return false;
		}

		public Object getAttribute(String name) {

			return null;
		}

		public Enumeration<String> getAttributeNames() {

			return null;
		}

		public String getCharacterEncoding() {

			return null;
		}

		public int getContentLength() {

			return 0;
		}

		public String getContentType() {

			return null;
		}

		public ServletInputStream getInputStream() throws IOException {

			return null;
		}

		public Locale getLocale() {

			return null;
		}

		public Enumeration<Locale> getLocales() {

			return null;
		}

		public String getParameter(String name) {

			return null;
		}

		public Map<String, String> getParameterMap() {

			return null;
		}

		public Enumeration<String> getParameterNames() {

			return null;
		}

		public String[] getParameterValues(String name) {

			return null;
		}

		public String getProtocol() {

			return null;
		}

		public BufferedReader getReader() throws IOException {

			return null;
		}

		public String getRealPath(String path) {

			return null;
		}

		public String getRemoteAddr() {

			return null;
		}

		public String getRemoteHost() {

			return null;
		}

		public RequestDispatcher getRequestDispatcher(String path) {

			return null;
		}

		public String getScheme() {

			String url = requestUrl.toString();
			return url.substring(0,url.indexOf(":"));
		}

		public String getServerName() {

			return null;
		}

		public int getServerPort() {

			return 0;
		}

		public boolean isSecure() {

			return false;
		}

		public void removeAttribute(String name) {

		}

		public void setAttribute(String name, Object o) {

		}

		public void setCharacterEncoding(String arg0)
				throws UnsupportedEncodingException {

		}

	}

	private static class MockHttpResponse implements HttpServletResponse {

		public void addCookie(Cookie cookie) {

		}

		public void addDateHeader(String name, long date) {

		}

		public void addHeader(String name, String value) {

		}

		public void addIntHeader(String name, int value) {

		}

		public boolean containsHeader(String name) {

			return false;
		}

		public String encodeRedirectURL(String url) {

			return null;
		}

		public String encodeRedirectUrl(String url) {

			return null;
		}

		public String encodeURL(String url) {
			return url;
		}

		public String encodeUrl(String url) {
			return url;
		}

		public void sendError(int sc) throws IOException {

		}

		public void sendError(int sc, String msg) throws IOException {

		}

		public void sendRedirect(String location) throws IOException {

		}

		public void setDateHeader(String name, long date) {

		}

		public void setHeader(String name, String value) {

		}

		public void setIntHeader(String name, int value) {

		}

		public void setStatus(int sc) {

		}

		public void setStatus(int sc, String sm) {

		}

		public void flushBuffer() throws IOException {

		}

		public int getBufferSize() {

			return 0;
		}

		public String getCharacterEncoding() {

			return null;
		}

		public Locale getLocale() {

			return null;
		}

		public ServletOutputStream getOutputStream() throws IOException {

			return null;
		}

		public PrintWriter getWriter() throws IOException {

			return null;
		}

		public boolean isCommitted() {

			return false;
		}

		public void reset() {

		}

		public void resetBuffer() {

		}

		public void setBufferSize(int size) {

		}

		public void setContentLength(int len) {

		}

		public void setContentType(String type) {

		}

		public void setLocale(Locale loc) {

		}

	}
}
