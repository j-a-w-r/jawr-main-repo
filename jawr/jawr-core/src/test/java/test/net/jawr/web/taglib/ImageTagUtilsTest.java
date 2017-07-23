/**
 * 
 */
package test.net.jawr.web.taglib;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.ResourceNotFoundException;
import net.jawr.web.resource.BinaryResourcesHandler;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import net.jawr.web.taglib.ImageTagUtils;
import test.net.jawr.web.servlet.mock.MockServletContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author ibrahim Chaehoi
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ImageTagUtilsTest {

	private JawrConfig config;
	
	@Mock
	private ResourceBundlesHandler rsBundlesHandler;
	
	@Before
	public void setUp(){
		config = new JawrConfig(JawrConstant.BINARY_TYPE, new Properties());
		config.setBinaryHashAlgorithm("MD5");
		ServletContext context = new MockServletContext();
		config.setContext(context);
		context.setAttribute(JawrConstant.CSS_CONTEXT_ATTRIBUTE, rsBundlesHandler);
		JawrConfig cssConfig = new JawrConfig(JawrConstant.CSS_TYPE, new Properties());
		when(rsBundlesHandler.getConfig()).thenReturn(cssConfig);
		GeneratorRegistry generatorRegistry = new GeneratorRegistry(JawrConstant.BINARY_TYPE);
		config.setGeneratorRegistry(generatorRegistry);
		generatorRegistry.setConfig(config);
	}
	
	@Test
	public void testImageUrl() {

		testImageUrlWithContextPathOverride("http://mycomp.com/basicwebapp", config, "/basicwebapp/");
	}
	
	@Test
	public void testImageUrlWithServletMapping() {

		config.setServletMapping("/jawrImg/");
		testImageUrlWithContextPathOverride("http://mycomp.com/basicwebapp", config, "/basicwebapp");
	}

	@Test
	public void testImageUrlWithContextPathOverride() {

		String contextPathOverride = "http://mycdn/";
		config.setContextPathOverride(contextPathOverride);
		config.setContextPathSslOverride("https://mycdn/");
		testImageUrlWithContextPathOverride("http://mycomp.com/basicwebapp", config, contextPathOverride);

	}
	
	@Test
	public void testImageUrlWithEmptyContextPathOverrideAndEmptyImgServletMapping() {

		String contextPathOverride = "";
		config.setServletMapping("");
		config.setContextPathOverride(contextPathOverride);
		testImageUrlWithContextPathOverride("http://mycomp.com/basicwebapp", config, contextPathOverride);

	}

	@Test
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
