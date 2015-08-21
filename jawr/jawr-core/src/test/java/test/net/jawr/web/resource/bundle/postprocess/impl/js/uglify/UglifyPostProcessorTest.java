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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Properties;

import javax.servlet.ServletContext;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.impl.js.uglify.UglifyPostProcessor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import test.net.jawr.web.FileUtils;

/**
 * The test case class for UglifyPostProcessor
 * 
 * @author Ibrahim Chaehoi
 */
@RunWith(MockitoJUnitRunner.class)
public class UglifyPostProcessorTest {

	/** The post processor */
	private UglifyPostProcessor processor;

	@Mock
	private JoinableResourceBundle bundle;

	@Mock
	private JawrConfig config;

	@Mock
	private ServletContext context;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		processor = new UglifyPostProcessor();
		when(
				config.getJavascriptEngineName()).thenReturn(
				JawrConstant.JS_ENGINE_DEFAULT);
		when(config.getContext()).thenReturn(context);
		when(config.getConfigProperties()).thenReturn(new Properties());
		when(bundle.getId()).thenReturn("/myJsBundle.js");
	}

	@Test
	public void testPostProcessSimple() {

		String script = "//comment\n        \talert('áéñí')";
		Charset charset = Charset.forName("UTF-8");
		StringBuffer sb = new StringBuffer();
		try {
			sb.append(script.getBytes(charset.name()));
		} catch (UnsupportedEncodingException ignore) {
			fail("UnsupportedEncodingException that will never be thrown");
		}

		// getBundle("/myBundle.js")
		BundleProcessingStatus status = new BundleProcessingStatus(
				BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle, null,
				config);
		StringBuffer ret = processor.postProcessBundle(status,
				new StringBuffer(script));

		// Not really testing JSMin, that is supposed to work.
		assertEquals("alert(\"áéñí\");", ret.toString());
	}

	@Test
	public void testPostProcessSimpleJS() throws Exception {

		String src = FileUtils
				.readClassPathFile("postprocessor/js/uglify/simpleJS.js");
		StringBuffer sb = new StringBuffer(src);

		BundleProcessingStatus status = new BundleProcessingStatus(
				BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle, null,
				config);
		StringBuffer ret = processor.postProcessBundle(status, sb);

		String expected = FileUtils
				.readClassPathFile("postprocessor/js/uglify/simpleJS_expected.js");
		assertEquals(expected, ret.toString());
	}

	@Test
	public void testPostProcessWithCompressOptions() throws Exception {

		String src = FileUtils
				.readClassPathFile("postprocessor/js/uglify/simpleJS.js");
		StringBuffer sb = new StringBuffer(src);
		when(config.getProperty(JawrConstant.UGLIFY_POSTPROCESSOR_OPTIONS, "{}")).thenReturn("{ compress : { unsafe : true}}");
		BundleProcessingStatus status = new BundleProcessingStatus(
				BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle, null,
				config);
		StringBuffer ret = processor.postProcessBundle(status, sb);

		String expected = FileUtils
				.readClassPathFile("postprocessor/js/uglify/simpleJS_WithCompressOptions_expected.js");
		assertEquals(expected, ret.toString());

	}

	@Test
	public void testPostProcessWithOutputOptions() throws Exception {

		String src = FileUtils
				.readClassPathFile("postprocessor/js/uglify/simpleJS.js");
		StringBuffer sb = new StringBuffer(src);
		when(config.getProperty(JawrConstant.UGLIFY_POSTPROCESSOR_OPTIONS, "{}")).thenReturn("{output : {comments : /@preserve/ }}");
		BundleProcessingStatus status = new BundleProcessingStatus(
				BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle, null,
				config);
		StringBuffer ret = processor.postProcessBundle(status, sb);

		String expected = FileUtils
				.readClassPathFile("postprocessor/js/uglify/simpleJS_WithOutputOptions_expected.js");
		assertEquals(expected, ret.toString());
	}

}
