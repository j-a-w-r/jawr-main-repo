/**
 * Copyright 2008-2013 Ibrahim Chaehoi
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
package test.net.jawr.web.resource.bundle.handler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.exception.BundleDependencyException;
import net.jawr.web.exception.DuplicateBundlePathException;
import net.jawr.web.resource.bundle.handler.ClientSideHandlerGeneratorImpl;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.handler.bundle.ResourceBundleHandler;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;

import org.junit.Assert;
import org.junit.Test;

import test.net.jawr.web.resource.bundle.PredefinedBundlesHandlerUtil;
import test.net.jawr.web.servlet.mock.MockServletContext;

/**
 * ClientSideHandlerGeneratorImpl TestCase
 * 
 * @author Ibrahim Chaehoi
 */
public class ClientSideHandlerGeneratorImplTest extends ResourceHandlerBasedTest {
	
	private static final String ROOT_TESTDIR = "/bundleLinkRenderer/";
	private static final String JS_BASEDIR = "js";
	
	private JawrConfig jawrConfig;
	
	private ClientSideHandlerGeneratorImpl generator = null;
	private ResourceBundlesHandler jsHandler = null;
	
	
	/**
	 * Constructor
	 */
	public ClientSideHandlerGeneratorImplTest() {
		
		Charset charsetUtf = Charset.forName("UTF-8"); 
		
	    ResourceReaderHandler rsHandler = createResourceReaderHandler(ROOT_TESTDIR,"js",charsetUtf);
	    ResourceBundleHandler rsBundleHandler = createResourceBundleHandler(ROOT_TESTDIR,charsetUtf);
	    jawrConfig = new JawrConfig("js", new Properties());
	    jawrConfig.setGzipResourcesModeOn(true);
	    jawrConfig.setCharsetName("UTF-8");
	    jawrConfig.setServletMapping("/srvMapping");
	    ServletContext servletCtx = new MockServletContext();
	    jawrConfig.setContext(servletCtx);
	    
	    try {
	    	jsHandler = PredefinedBundlesHandlerUtil.buildSimpleBundles(rsHandler,rsBundleHandler,JS_BASEDIR,"js", jawrConfig);
	    } catch (DuplicateBundlePathException e) {
			// 
			throw new RuntimeException(e);
		} catch (BundleDependencyException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void setUp() throws Exception {
		generator = new ClientSideHandlerGeneratorImpl();
		generator.init(jawrConfig, jsHandler.getGlobalBundles(), jsHandler.getContextBundles());
	}

	@Test
	public void testGetClientSideHandlerScript() throws Exception{
	
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getContextPath()).thenReturn("/sample/");
		when(request.getScheme()).thenReturn("http");
		when(request.getLocale()).thenReturn(new Locale("en", "US"));
		HttpSession session = mock(HttpSession.class);
		when(request.getSession()).thenReturn(session);
		ServletContext ctx = mock(ServletContext.class);
		when(session.getServletContext()).thenReturn(ctx);
		
		StringBuffer result = generator.getClientSideHandlerScript(request);
		Assert.assertNotNull(result);
		// TODO : fix ordering issue for test case
		//Assert.assertEquals(FileUtils.readClassPathFile("generator/clientside/expected-result.js"), result.toString());
	}
	
	
	
}
