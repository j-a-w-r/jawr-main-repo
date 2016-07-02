/**
 * Copyright 2010-2016 Ibrahim Chaehoi
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
package test.net.jawr.web.resource.bundle.css;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Properties;

import javax.servlet.ServletContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.BinaryResourcesHandler;
import net.jawr.web.resource.bundle.css.CssImageUrlRewriter;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.handler.reader.ResourceReaderHandler;
import test.net.jawr.web.FileUtils;

/**
 * the test case for CSS Url rewriter
 * 
 * @author ibrahim Chaehoi
 */
@RunWith(MockitoJUnitRunner.class)
public class CssUrlRewriterTestCase {

	@Mock
	private ServletContext servletCtx;

	@Mock
	private BinaryResourcesHandler binRsHandler;

	@Mock
	private ResourceReaderHandler rsHandler;

	private CssImageUrlRewriter rewriter;

	private JawrConfig config;

	private JawrConfig binConfig;

	@Mock
	private GeneratorRegistry binGeneratorRegistry;

	@Before
	public void before() {
		Properties props = new Properties();
		config = new JawrConfig(JawrConstant.CSS_TYPE, props);
		config.setContext(servletCtx);
		binConfig = new JawrConfig(JawrConstant.BINARY_TYPE, props);
		binConfig.setContext(servletCtx);
		binConfig.setGeneratorRegistry(binGeneratorRegistry);
		binGeneratorRegistry.setConfig(binConfig);
		when(binRsHandler.getConfig()).thenReturn(binConfig);
		when(servletCtx.getAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE)).thenReturn(binRsHandler);
		when(binGeneratorRegistry.isGeneratedBinaryResource(Matchers.startsWith("sprite:"))).thenReturn(true);
		when(binGeneratorRegistry.isGeneratedBinaryResource(Matchers.startsWith("jar:"))).thenReturn(true);
	}

	@Test
	public void testRewriteWithSimpleCss() throws Exception {

		// Simulate no binary resource handler
		when(servletCtx.getAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE)).thenReturn(null);

		rewriter = new CssImageUrlRewriter(config);
		String content = FileUtils.readClassPathFile("cssUrlRewriter/simple-one.css");
		StringBuffer result = rewriter.rewriteUrl("/css/one.css", "/jawr/css/one.css", content);
		String expectedContent = FileUtils.readClassPathFile("cssUrlRewriter/expected-simple-one.css");
		assertEquals(expectedContent, result.toString());
	}

	@Test
	public void testRewriteWithNewCssMappingAndBinaryResourceHandler() throws Exception {

		rewriter = new CssImageUrlRewriter(config);
		String content = FileUtils.readClassPathFile("cssUrlRewriter/one.css");
		StringBuffer result = rewriter.rewriteUrl("/css/one.css", "/jawr/css/one.css", content);
		String expectedContent = FileUtils.readClassPathFile("cssUrlRewriter/expected-one.css");
		assertEquals(expectedContent, result.toString());
	}

	@Test
	public void testRewriteWithBinaryServletMapping() throws Exception {

		rewriter = new CssImageUrlRewriter(config);
		binConfig.setServletMapping("/jawr/bin/");
		String content = FileUtils.readClassPathFile("cssUrlRewriter/one.css");
		StringBuffer result = rewriter.rewriteUrl("/css/one.css", "/jawr/css/one.css", content);
		String expectedContent = FileUtils.readClassPathFile("cssUrlRewriter/expected-one-with-binary-mapping.css");
		assertEquals(expectedContent, result.toString());
	}
}
