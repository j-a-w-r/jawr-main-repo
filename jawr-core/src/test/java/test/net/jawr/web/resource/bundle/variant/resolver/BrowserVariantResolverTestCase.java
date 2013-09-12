/**
 * 
 */
package test.net.jawr.web.resource.bundle.variant.resolver;

import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;
import net.jawr.web.resource.bundle.variant.resolver.BrowserResolver;

import org.mockito.Mockito;

/**
 * @author ibrahim
 *
 */
public class BrowserVariantResolverTestCase extends TestCase {

	BrowserResolver resolver = new BrowserResolver();
	
	public void testResolveVariant(){
		
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		when(request.getHeader("User-Agent")).thenReturn("Firefox 6");
	    String variant = resolver.resolveVariant(request);
		assertEquals("firefox", variant);
		
		when(request.getHeader("User-Agent")).thenReturn("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
	    variant = resolver.resolveVariant(request); 
		assertEquals("ie6", variant);
		
		when(request.getHeader("User-Agent")).thenReturn("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.0)");
	    variant = resolver.resolveVariant(request); 
		assertEquals("ie7", variant);
		
		when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.A.B.C Safari/525.13");
	    variant = resolver.resolveVariant(request); 
		assertEquals("webkit", variant);

		when(request.getHeader("User-Agent")).thenReturn("Opera/9.25 (X11; Linux i686; U; fr-ca)");
	    variant = resolver.resolveVariant(request); 
		assertEquals("opera", variant);
		
		when(request.getHeader("User-Agent")).thenReturn(null);
	    variant = resolver.resolveVariant(request);
		assertNull(variant);
		
		when(request.getHeader("User-Agent")).thenReturn("Googlebot/2.X (http://www.googlebot.com/bot.html)");
	    variant = resolver.resolveVariant(request);
		assertNull(variant);
	}
	
}
