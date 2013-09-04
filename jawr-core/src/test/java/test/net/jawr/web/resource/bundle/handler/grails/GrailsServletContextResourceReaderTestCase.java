/**
 * Copyright 2010 Ibrahim Chaehoi
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
package test.net.jawr.web.resource.bundle.handler.grails;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import junit.framework.TestCase;
import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.handler.reader.grails.GrailsServletContextResourceReader;
import test.net.jawr.web.servlet.mock.MockServletContext;

/**
 * @author ibrahim Chaehoi
 *
 */
public class GrailsServletContextResourceReaderTestCase extends TestCase {

	private ServletContext servletContext;
	private GrailsServletContextResourceReader rsReader;
	private Map<String, String> pluginPaths;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	public void setUp(){
		servletContext = new MockServletContext();
		pluginPaths = new HashMap<String, String>();
		pluginPaths.put("test","C:/plugins/test/webapp");
		servletContext.setAttribute(JawrConstant.JAWR_GRAILS_PLUGIN_PATHS, pluginPaths);
		servletContext.setAttribute(JawrConstant.GRAILS_WAR_DEPLOYED, Boolean.FALSE);
	}
	
	public void testGrailsPluginPathResolverNotFound(){
		JawrConfig config = new JawrConfig("js",new Properties());
		servletContext.removeAttribute(JawrConstant.JAWR_GRAILS_PLUGIN_PATHS);
		try{
			rsReader = new GrailsServletContextResourceReader(servletContext, config);
			fail("An exception should be thrown");	
		}catch(Exception e){
			
		}
	}
	
	public void testGetRealResourcePath(){
		JawrConfig config = new JawrConfig("js",new Properties());
		rsReader = new GrailsServletContextResourceReader(servletContext, config);
		
		String realPath = rsReader.getRealResourcePath("/plugins/test/css/myStyle.css");
		assertEquals("C:/plugins/test/webapp/css/myStyle.css", realPath);
	}
	
	public void testGetRealResourcePathWithGeneratorPrefix(){
		JawrConfig config = new JawrConfig("js",new Properties());
		rsReader = new GrailsServletContextResourceReader(servletContext, config);
		String realPath = rsReader.getRealResourcePath("ext:/plugins/test/css/myStyle.css");
		assertEquals("ext:/plugins/test/css/myStyle.css", realPath);
	}
	
}
