/**
 * Copyright 2014 Ibrahim Chaehoi
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
package test.net.jawr.web.resource.bundle.postprocess.impl.js.uglify;

import static org.junit.Assert.assertEquals;

import static org.mockito.Matchers.anyString;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.minification.CompressionResult;
import net.jawr.web.resource.bundle.postprocess.impl.js.uglify.UglifyJS;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import test.net.jawr.web.FileUtils;
import test.net.jawr.web.util.js.rhino.JSEngineUtils;

/**
 * Test class for Uglyfy compressor
 * 
 * @author ibrahim chaehoi
 */
@RunWith(Parameterized.class)
public class UglifyTest {

	private static Logger LOGGER = LoggerFactory.getLogger(UglifyTest.class);

	@Parameter
	public String jsEngineName;

	@Parameters
	public static List<Object[]> jsEnginesToTestWith() {
		return Arrays.asList(new Object[][] {
				{ JawrConstant.DEFAULT_JS_ENGINE }, { "nashorn" } });
	}

	@Mock
	private JawrConfig config;

	@Mock
	private ServletContext context;

	private UglifyJS uglify;

	@Before
	public void init() {

		initMocks(this);
		when(config.getContext()).thenReturn(context);
		when(config.getJavascriptEngineName(Matchers.anyString())).thenReturn(
				JawrConstant.DEFAULT_JS_ENGINE);
		when(context.getResourceAsStream(anyString())).thenReturn(null);
	}

	/**
	 * Checks if the JS engine is available
	 * 
	 * @return true if JS engine is available
	 */
	private boolean isJsEngineAvailable() {
		return JSEngineUtils.isJsEngineAvailable(jsEngineName, LOGGER);
	}

	@Test
	public void testUglifySimple() throws Exception {
		if (isJsEngineAvailable()) {

			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("*** Uglifying simple JS ****");
			}
		
			uglify = new UglifyJS(config, null, "{}");
			String src = FileUtils
					.readClassPathFile("postprocessor/js/uglify/simpleJS.js");
			CompressionResult result = uglify.compress(src);
			assertEquals(
					FileUtils
							.readClassPathFile("postprocessor/js/uglify/simpleJS_expected.js"),
					result.getCode());
		}
	}

	@Test
	public void testUglifyBootstrap() throws Exception {
		if (isJsEngineAvailable()) {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("*** Uglifying bootstrap ****");
			}
			uglify = new UglifyJS(config, null, "{}");
			String src = FileUtils
					.readClassPathFile("postprocessor/js/uglify/bootstrap.js");
			CompressionResult result = uglify.compress(src);
			assertEquals(
					FileUtils
							.readClassPathFile("postprocessor/js/uglify/bootstrap_expected.js"),
					result.getCode());
		}
	}

	@Test
	public void testUglifyWithCompressOptions() throws Exception {
		if (isJsEngineAvailable()) {

			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("*** Uglifying with compress options ****");
			}
			
			uglify = new UglifyJS(config, null,
					"{ compress : { unsafe : true}}");
			String src = FileUtils
					.readClassPathFile("postprocessor/js/uglify/simpleJS.js");
			CompressionResult result = uglify.compress(src);
			assertEquals(
					FileUtils
							.readClassPathFile("postprocessor/js/uglify/simpleJS_WithCompressOptions_expected.js"),
					result.getCode());
		}
	}

	@Test
	public void testUglifyWithOutputOptions() throws Exception {
		if (isJsEngineAvailable()) {

			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("*** Uglifying with output options ****");
			}
			
			uglify = new UglifyJS(config, null,
					"{output : {comments : /@preserve/ }}");
			String src = FileUtils
					.readClassPathFile("postprocessor/js/uglify/simpleJS.js");
			CompressionResult result = uglify.compress(src);
			assertEquals(
					FileUtils
							.readClassPathFile("postprocessor/js/uglify/simpleJS_WithOutputOptions_expected.js"),
					result.getCode());
		}
	}
}
