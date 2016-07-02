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

import org.junit.Test;

import net.jawr.web.resource.bundle.css.CssDebugUrlRewriter;
import test.net.jawr.web.FileUtils;

/**
 * Test case class for the CssDeburUrlRewriter
 * 
 * @author ibrahim Chaehoi
 */
public class CssDebugUrlRewriterTestCase {

	@Test
	public void rewriteDebugUrl() throws Exception {
		String content = FileUtils.readClassPathFile("cssUrlRewriter/one.css");
		String result = CssDebugUrlRewriter.rewriteGeneratedBinaryResourceDebugUrl("/css/one.css", content, null);
		String expectedContent = FileUtils.readClassPathFile("cssUrlRewriter/expected-debug-one.css");
		assertEquals(expectedContent, result.toString());

	}

	@Test
	public void rewriteDebugUrlWithBinaryMapping() throws Exception {
		String content = FileUtils.readClassPathFile("cssUrlRewriter/one.css");
		String result = CssDebugUrlRewriter.rewriteGeneratedBinaryResourceDebugUrl("/css/one.css", content,
				"/jawr/bin/");
		String expectedContent = FileUtils
				.readClassPathFile("cssUrlRewriter/expected-debug-one-with-binary-mapping.css");
		assertEquals(expectedContent, result.toString());

	}
}
