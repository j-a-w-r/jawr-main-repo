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
package test.net.jawr.web.resource.bundle.postprocess.impl;

import static test.net.jawr.web.TestUtils.assertContentEquals;

import java.util.Properties;

import javax.servlet.ServletContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.impl.CSSCombineMediaPostProcessor;
import net.jawr.web.util.StringUtils;
import test.net.jawr.web.servlet.mock.MockServletContext;

/**
 * @author Ibrahim Chaehoi
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class CssCombineMediaPostProcessorTestCase {

	@Mock
	JoinableResourceBundle bundle;
	JawrConfig config;
	BundleProcessingStatus status;
	CSSCombineMediaPostProcessor processor;
	
	@Before
	public void setUp() throws Exception {

		when(bundle.getId()).thenReturn("/css/bundle.css");
		when(bundle.getName()).thenReturn("myBundle");
		
		Properties props = new Properties();
		config = new JawrConfig("css", props);
		ServletContext servletContext = new MockServletContext();
		config.setContext(servletContext);
		config.setServletMapping("/css");
		config.setCharsetName("UTF-8");
		status = new BundleProcessingStatus(
				BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle, null,
				config);
		addGeneratorRegistryToConfig(config, "css");
		status.setLastPathAdded("/css/someCSS.css");
		processor = new CSSCombineMediaPostProcessor();
	}
	
	private GeneratorRegistry addGeneratorRegistryToConfig(JawrConfig config, String type) {
		GeneratorRegistry generatorRegistry = new GeneratorRegistry(type){

			private static final long serialVersionUID = 1L;

			public boolean isHandlingCssImage(String cssResourcePath) {
				
				boolean result = false;
				if(cssResourcePath.startsWith("jar:")){
					result = true;
				}
				return result;
			}
		};
		generatorRegistry.setConfig(config);
		config.setGeneratorRegistry(generatorRegistry);
		return generatorRegistry;
	}

	@Test
	public void testBasicMediaCssRewriting() {

		// Set the properties
		config.getConfigProperties().put("jawr.css.bundle." + bundle.getName() + ".media", "print");
		status = new BundleProcessingStatus(
				BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle, null,
				config);

		// Css data
		StringBuffer data = new StringBuffer(
				".style { background-image:url(../../images/logo.png); }");

		// Css path
		String filePath = "style/default/assets/someCSS.css";

		String expectedResult = "@media print {"+StringUtils.LF
				+ ".style { background-image:url(../../images/logo.png); }}"+StringUtils.LF + StringUtils.LF;
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertContentEquals("Content was not rewritten properly", expectedResult,
				result);
	}

	@Test
	public void testBasicWithoutMediaDefinedCssRewriting() {

		// Set the properties
		status = new BundleProcessingStatus(
				BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle, null,
				config);

		// Css data
		StringBuffer data = new StringBuffer(
				".style { background-image:url(../../images/logo.png); }");

		// Css path
		String filePath = "style/default/assets/someCSS.css";

		String expectedResult = "@media screen {"+StringUtils.LF
				+ ".style { background-image:url(../../images/logo.png); }}"+StringUtils.LF + StringUtils.LF;
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertContentEquals("Content was not rewritten properly", expectedResult,
				result);
	}

}
