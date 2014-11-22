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

import javax.servlet.ServletContext;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.minification.CompressionResult;
import net.jawr.web.resource.bundle.postprocess.impl.js.uglify.UglifyJS;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import test.net.jawr.web.FileUtils;

/**
 * Test class for Uglyfy compressor 
 * 
 * @author ibrahim chaehoi
 */
@RunWith(MockitoJUnitRunner.class)
public class UglifyTest {

	@Mock
	private JawrConfig config;
	
	@Mock
	private ServletContext context;
	
	private UglifyJS uglify;
	
	@Before
	public void init() {
		when(config.getContext()).thenReturn(context);
		when(context.getResourceAsStream(anyString())).thenReturn(null);
	}
	
	@Test
	public void testUglifySimple() throws Exception{
		
		uglify = new UglifyJS(config, null, "{}");
		String src = FileUtils.readClassPathFile("postprocessor/js/uglify/simpleJS.js");
		CompressionResult result = uglify.compress(src);
		assertEquals(FileUtils.readClassPathFile("postprocessor/js/uglify/simpleJS_expected.js"), result.getCode());
	}

	@Test
	public void testUglifyBootstrap() throws Exception{
		
		uglify = new UglifyJS(config, null, "{}");
		String src = FileUtils.readClassPathFile("postprocessor/js/uglify/bootstrap.js");
		CompressionResult result = uglify.compress(src);
		assertEquals(FileUtils.readClassPathFile("postprocessor/js/uglify/bootstrap_expected.js"), result.getCode());
	}
	
	@Test
	public void testUglifyWithCompressOptions() throws Exception{
		
		uglify = new UglifyJS(config, null, "{ compress : { unsafe : true}}");
		String src = FileUtils.readClassPathFile("postprocessor/js/uglify/simpleJS.js");
		CompressionResult result = uglify.compress(src);
		assertEquals(FileUtils.readClassPathFile("postprocessor/js/uglify/simpleJS_WithCompressOptions_expected.js"), result.getCode());
	}
	
	@Test
	public void testUglifyWithOutputOptions() throws Exception{
		
		uglify = new UglifyJS(config, null, "{output : {comments : /@preserve/ }}");
		String src = FileUtils.readClassPathFile("postprocessor/js/uglify/simpleJS.js");
		CompressionResult result = uglify.compress(src);
		assertEquals(FileUtils.readClassPathFile("postprocessor/js/uglify/simpleJS_WithOutputOptions_expected.js"), result.getCode());
	}
}
