/**
 * 
 */
package test.net.jawr.web.resource.bundle.variant.resolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import junit.framework.TestCase;
import net.jawr.web.resource.bundle.variant.resolver.BrowserResolver;

/**
 * @author ibrahim
 *
 */
public class BrowserVariantResolverTestCase extends TestCase {

	BrowserResolver resolver = new BrowserResolver();
	
	public void testResolveVariant(){
		
		String variant = resolver.resolveVariant(new MockHttpServletRequest("Firefox 6"));
		assertEquals("firefox", variant);
		
		variant = resolver.resolveVariant(new MockHttpServletRequest("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)")); 
		assertEquals("ie6", variant);
		
		variant = resolver.resolveVariant(new MockHttpServletRequest("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.0)")); 
		assertEquals("ie7", variant);
		
		variant = resolver.resolveVariant(new MockHttpServletRequest("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.A.B.C Safari/525.13")); 
		assertEquals("webkit", variant);

		variant = resolver.resolveVariant(new MockHttpServletRequest("Opera/9.25 (X11; Linux i686; U; fr-ca)")); 
		assertEquals("opera", variant);
		
		variant = resolver.resolveVariant(new MockHttpServletRequest(null));
		assertNull(variant);
		
		variant = resolver.resolveVariant(new MockHttpServletRequest("Googlebot/2.X (http://www.googlebot.com/bot.html)"));
		assertNull(variant);
	}
	
	private static class MockHttpServletRequest implements HttpServletRequest{

			private String userAgent;
			
			public MockHttpServletRequest(String userAgent) {
				this.userAgent = userAgent;
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

			public String getParameter(String name) {
				return null;
			}

			public Locale getLocale() {
				return null;
			}

			public Enumeration<Locale> getLocales() {
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

			public String getScheme() {
				return null;
			}

			public String getServerName() {
				return null;
			}

			public int getServerPort() {
				return 0;
			}

			public BufferedReader getReader() throws IOException {
				return null;
			}

			public String getRemoteAddr() {
				return null;
			}

			public String getRemoteHost() {
				return null;
			}

			public void setAttribute(String name, Object o) {
				
			}

			public void removeAttribute(String name) {
				
			}

			public boolean isSecure() {
				return false;
			}

			public RequestDispatcher getRequestDispatcher(String path) {
				return null;
			}

			public String getRealPath(String path) {
				return null;
			}

			public void setCharacterEncoding(String arg0)
					throws UnsupportedEncodingException {
				
			}

			public String getAuthType() {
				return null;
			}

			public Cookie[] getCookies() {
				return null;
			}

			public long getDateHeader(String name) {
				return 0;
			}

			public String getHeader(String name) {
				
				if(name.equals("User-Agent")){
					return userAgent;
				}
				return null;
			}

			public Enumeration<String> getHeaders(String name) {
				return null;
			}

			public Enumeration<String> getHeaderNames() {
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

			public String getContextPath() {
				return null;
			}

			public String getQueryString() {
				return null;
			}

			public String getRemoteUser() {
				return null;
			}

			public boolean isUserInRole(String role) {
				return false;
			}

			public Principal getUserPrincipal() {
				return null;
			}

			public String getRequestURI() {
				return null;
			}

			public StringBuffer getRequestURL() {
				return null;
			}

			public String getRequestedSessionId() {
				return null;
			}

			public String getServletPath() {
				return null;
			}

			public HttpSession getSession(boolean create) {
				return null;
			}

			public HttpSession getSession() {
				return null;
			}

			public boolean isRequestedSessionIdValid() {
				return false;
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
		
	}
}
