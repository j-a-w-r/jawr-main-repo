package test.net.jawr.web.resource.bundle.postprocess.impl;

import static net.jawr.web.resource.bundle.postprocess.impl.AutoPrefixerPostProcessor.AUTOPREFIXER_DEFAULT_OPTIONS;
import static net.jawr.web.resource.bundle.postprocess.impl.AutoPrefixerPostProcessor.AUTOPREFIXER_SCRIPT_DEFAULT_LOCATION;
import static net.jawr.web.resource.bundle.postprocess.impl.AutoPrefixerPostProcessor.AUTOPREFIXER_SCRIPT_LOCATION;
import static net.jawr.web.resource.bundle.postprocess.impl.AutoPrefixerPostProcessor.AUTOPREFIXER_SCRIPT_OPTIONS;

import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.when;

import java.util.Properties;

import javax.servlet.ServletContext;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.postprocess.BundleProcessingStatus;
import net.jawr.web.resource.bundle.postprocess.impl.AutoPrefixerPostProcessor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import test.net.jawr.web.FileUtils;

@RunWith(MockitoJUnitRunner.class)
public class AutoPrefixerTest {

	@Mock
	private JawrConfig config;

	private AutoPrefixerPostProcessor processor;

	@Mock
	private ServletContext context;

	@Mock
	private JoinableResourceBundle bundle;

	@Before
	public void setUp() throws Exception {

		processor = new AutoPrefixerPostProcessor();
		when(config.getContext()).thenReturn(context);

		when(
				config.getProperty(AUTOPREFIXER_SCRIPT_OPTIONS,
						AUTOPREFIXER_DEFAULT_OPTIONS)).thenReturn(
				AUTOPREFIXER_DEFAULT_OPTIONS);
		when(
				config.getProperty(AUTOPREFIXER_SCRIPT_LOCATION,
						AUTOPREFIXER_SCRIPT_DEFAULT_LOCATION)).thenReturn(
				AUTOPREFIXER_SCRIPT_DEFAULT_LOCATION);
		when(config.getConfigProperties()).thenReturn(new Properties());
		when(bundle.getId()).thenReturn("/myCssBundle.css");

	}

	@Test
	public void testSimplePostProcessing() throws Exception {

		String src = FileUtils
				.readClassPathFile("postprocessor/css/autoprefixer/simple.css");
		StringBuffer sb = new StringBuffer(src);

		BundleProcessingStatus status = new BundleProcessingStatus(
				BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle, null,
				config);
		StringBuffer ret = processor.postProcessBundle(status, sb);

		String expected = FileUtils
				.readClassPathFile("postprocessor/css/autoprefixer/simple_expected.css");
		assertEquals(expected, ret.toString());
	}

	@Test
	public void testPostProcessingWithBrowserOptions() throws Exception {

		when(
				config.getProperty(AUTOPREFIXER_SCRIPT_OPTIONS,
						AUTOPREFIXER_DEFAULT_OPTIONS)).thenReturn(
				"{browsers : ['Opera 12']}");

		String src = FileUtils
				.readClassPathFile("postprocessor/css/autoprefixer/simple.css");
		StringBuffer sb = new StringBuffer(src);

		BundleProcessingStatus status = new BundleProcessingStatus(
				BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle, null,
				config);
		StringBuffer ret = processor.postProcessBundle(status, sb);

		String expected = FileUtils
				.readClassPathFile("postprocessor/css/autoprefixer/simple_opera_expected.css");
		assertEquals(expected, ret.toString());
	}

	@Test
	public void testPostProcessingWithSafeMode() throws Exception {

		when(
				config.getProperty(AUTOPREFIXER_SCRIPT_OPTIONS,
						AUTOPREFIXER_DEFAULT_OPTIONS)).thenReturn(
				"{safe : true}");

		StringBuffer sb = new StringBuffer("a {");

		BundleProcessingStatus status = new BundleProcessingStatus(
				BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle, null,
				config);
		StringBuffer ret = processor.postProcessBundle(status, sb);

		String expected = "a {}";
		assertEquals(expected, ret.toString());
	}

	@Test
	public void testPostProcessingDontRemoveOldPrefix() throws Exception {

		when(config.getProperty(AUTOPREFIXER_SCRIPT_OPTIONS, "{}")).thenReturn(
				"{remove: false}");

		String src = "a { -moz-border-radius: 5px; border-radius: 5px }";
		StringBuffer sb = new StringBuffer(src);

		BundleProcessingStatus status = new BundleProcessingStatus(
				BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle, null,
				config);
		StringBuffer ret = processor.postProcessBundle(status, sb);

		String expected = src;
		assertEquals(expected, ret.toString());
	}

	@Test
	public void testPostProcessingWithSpecifiedAutoprefixerScript()
			throws Exception {

		when(
				config.getProperty(AUTOPREFIXER_SCRIPT_LOCATION,
						AUTOPREFIXER_SCRIPT_DEFAULT_LOCATION)).thenReturn(
				"postprocessor/css/autoprefixer/autoprefixer-5.1.11.js");

		String src = ":placeholder-shown { color: #999 }";
		StringBuffer sb = new StringBuffer(src);

		BundleProcessingStatus status = new BundleProcessingStatus(
				BundleProcessingStatus.BUNDLE_PROCESSING_TYPE, bundle, null,
				config);
		StringBuffer ret = processor.postProcessBundle(status, sb);

		String expected = "::-webkit-input-placeholder { color: #999 }\n"
				+ "::-moz-placeholder { color: #999 }\n"
				+":-ms-input-placeholder { color: #999 }\n"
				+":placeholder-shown { color: #999 }";
		assertEquals(expected, ret.toString());
	}
}
