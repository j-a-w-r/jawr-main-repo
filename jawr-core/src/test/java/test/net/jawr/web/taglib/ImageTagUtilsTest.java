/**
 * 
 */
package test.net.jawr.web.taglib;

import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.BinaryResourcesHandler;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.taglib.ImageTagUtils;

import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;

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
		config.setBinaryHashAlgorithm("MD5");
		GeneratorRegistry generatorRegistry = new GeneratorRegistry(JawrConstant.BINARY_TYPE);
		config.setGeneratorRegistry(generatorRegistry);
		generatorRegistry.setConfig(config);
	}
	
	public void testImageUrl() {

		testImageUrlWithContextPathOverride("http://mycomp.com/basicwebapp", config, "/basicwebapp/");
	}
	
	public void testImageUrlWithServletMapping() {

		config.setServletMapping("/jawrImg/");
		testImageUrlWithContextPathOverride("http://mycomp.com/basicwebapp", config, "/basicwebapp");
	}

	public void testImageUrlWithContextPathOverride() {

		String contextPathOverride = "http://mycdn/";
		config.setContextPathOverride(contextPathOverride);
		config.setContextPathSslOverride("https://mycdn/");
		testImageUrlWithContextPathOverride("http://mycomp.com/basicwebapp", config, contextPathOverride);

	}
	
	public void testImageUrlWithEmptyContextPathOverrideAndEmptyImgServletMapping() {

		String contextPathOverride = "";
		config.setServletMapping("");
		config.setContextPathOverride(contextPathOverride);
		testImageUrlWithContextPathOverride("http://mycomp.com/basicwebapp", config, contextPathOverride);

	}

	public void testImageUrlWithContextPathSslOverride() {

		String contextPathOverride = "https://mycdn/";
		config.setContextPathSslOverride(contextPathOverride);
		config.setContextPathOverride("http://mycdn/");
		testImageUrlWithContextPathOverride("https://mycomp.com/basicwebapp", config, contextPathOverride);

	}
	
	private void testImageUrlWithContextPathOverride(String requestUrl, JawrConfig config,
			String contextPath) {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		when(request.getRequestURL()).thenReturn(new StringBuffer(requestUrl));
		when(request.getContextPath()).thenReturn("/basicwebapp/");
		when(request.getRequestURI()).thenReturn("/basicwebapp/content/myPage.jsp");
		
		String scheme = requestUrl.substring(0,requestUrl.indexOf(":"));
		when(request.getScheme()).thenReturn(scheme);
	    
		
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		when(response.encodeURL(Mockito.anyString())).then(AdditionalAnswers.returnsFirstArg());
		ResourceReaderHandler rsHandler = Mockito.mock(ResourceReaderHandler.class);
		
		BinaryResourcesHandler imgRsHandler = new BinaryResourcesHandler(config, rsHandler, null);
		String servletMapping = null;
		if(config.getServletMapping() != null && config.getServletMapping().trim().length() > 0){
			servletMapping = "/"+config.getServletMapping()+"/";
		}else{
			servletMapping = "";
		}
		
		try {
			when(rsHandler.getResourceAsStream(Mockito.anyString())).thenReturn(new ByteArrayInputStream("dummy content".getBytes()));
		} catch (ResourceNotFoundException e) {
			// Do nothing. 
			// This can't happen
		}
		
		String result = ImageTagUtils.getImageUrl("/img/logo/myLogo.png",
				imgRsHandler, request, response);
		String expectedResult = contextPath+servletMapping+"cb90c55a38064627dca337dfa5fc5be120/img/logo/myLogo.png";
		assertEquals(expectedResult, result);

		try {
			when(rsHandler.getResourceAsStream(Mockito.anyString())).thenReturn(new ByteArrayInputStream("dummy content".getBytes()));
		} catch (ResourceNotFoundException e) {
			// Do nothing. 
			// This can't happen
		}
		
		result = ImageTagUtils.getImageUrl("../img/logo/myLogo.png",
				imgRsHandler, request, response);
		expectedResult = contextPath+servletMapping+"cb90c55a38064627dca337dfa5fc5be120/img/logo/myLogo.png";
		assertEquals(expectedResult, result);

		try {
			when(rsHandler.getResourceAsStream(Mockito.anyString())).thenReturn(new ByteArrayInputStream("dummy content".getBytes()));
		} catch (ResourceNotFoundException e) {
			// Do nothing. 
			// This can't happen
		}
		
		result = ImageTagUtils.getImageUrl("./img/logo/myLogo.png",
				imgRsHandler, request, response);
		expectedResult = contextPath+servletMapping+"cb90c55a38064627dca337dfa5fc5be120/content/img/logo/myLogo.png";
		assertEquals(expectedResult, result);

		try {
			when(rsHandler.getResourceAsStream(Mockito.anyString())).thenReturn(new ByteArrayInputStream("dummy content".getBytes()));
		} catch (ResourceNotFoundException e) {
			// Do nothing. 
			// This can't happen
		}
		
		result = ImageTagUtils.getImageUrl("img/logo/myLogo.png", imgRsHandler,
				request, response);
		expectedResult = contextPath+servletMapping+"cb90c55a38064627dca337dfa5fc5be120/content/img/logo/myLogo.png";
		assertEquals(expectedResult, result);
	}

}
