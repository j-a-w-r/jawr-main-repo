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

import java.util.Properties;

import javax.servlet.ServletContext;

import junit.framework.TestCase;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.impl.CSSCombineMediaPostProcessor;
import net.jawr.web.util.LineSeparator;
import net.jawr.web.util.StringUtils;
import test.net.jawr.web.resource.bundle.MockJoinableResourceBundle;
import test.net.jawr.web.servlet.mock.MockServletContext;

/**
 * @author Ibrahim Chaehoi
 * 
 */
public class CssCombineMediaPostProcessorTestCase extends TestCase {

	JoinableResourceBundle bundle;
	JawrConfig config;
	BundleProcessingStatus status;
	CSSCombineMediaPostProcessor processor;

	protected void setUp() throws Exception {
		super.setUp();

		bundle = buildFakeBundle("/css/bundle.css", "myBundle");
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

		String expectedResult = "@media print {"+LineSeparator.CRLF
				+ ".style { background-image:url(../../images/logo.png); }}"+LineSeparator.CRLF + LineSeparator.CRLF;
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("Content was not rewritten properly", expectedResult,
				result);
	}

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

		String expectedResult = "@media screen {"+LineSeparator.CRLF
				+ ".style { background-image:url(../../images/logo.png); }}"+LineSeparator.CRLF + LineSeparator.CRLF;
		status.setLastPathAdded(filePath);

		String result = processor.postProcessBundle(status, data).toString();
		assertEquals("Content was not rewritten properly", expectedResult,
				result);
	}
	
	private JoinableResourceBundle buildFakeBundle(final String id,
			final String name) {

		return new MockJoinableResourceBundle() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * test.net.jawr.web.resource.bundle.MockJoinableResourceBundle#
			 * getId()
			 */
			public String getId() {
				return id;
			}

			public String getName() {
				return name;
			}
		};

	}

}
