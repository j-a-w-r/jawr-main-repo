/**
 * Copyright 2015 Ibrahim Chaehoi
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

package net.jawr.web.test.postprocessor.autoprefixer;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import net.jawr.web.test.JawrTestConfigFiles;

import org.junit.Test;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.html.HtmlLink;

/**
 * Integration Test for Autoprefixer postprocessor
 *  
 * @author Ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/postprocessor/css/autoprefixer/config/web.xml", jawrConfig = "net/jawr/web/postprocessor/css/autoprefixer/config/jawr-with-autoprefixer-options.properties")
public class PageAutoPrefixerPostProcessorWithOptionsTest extends PageAutoPrefixerPostProcessorTest {

	@Test
	public void testPageLoad() throws Exception {

		final List<String> expectedAlerts = Collections
				.singletonList("A little message retrieved from the message bundle : Hello $ world!");
		assertEquals(expectedAlerts, collectedAlerts);
		
		assertContentEquals("/net/jawr/web/postprocessor/css/autoprefixer/resources/index-jsp-autoprefixer-with-options-result-expected.txt", page);
	}
	
	@Test
	public void checkGeneratedCssLinks() {
		// Test generated Css link
		final List<HtmlLink> styleSheets = getHtmlLinkTags();
		assertEquals(1, styleSheets.size());
		final HtmlLink css = styleSheets.get(0);
		assertEquals(
				getUrlPrefix()+"/N46287811/fwk/core/component.css",
				css.getHrefAttribute());

	}

	@Test
	public void testCssBundleContent() throws Exception {

		final List<HtmlLink> styleSheets = getHtmlLinkTags();
		final HtmlLink css = styleSheets.get(0);
		final TextPage page = getCssPage(css);
		assertContentEquals("/net/jawr/web/postprocessor/css/autoprefixer/resources/component-autoprefixer-with-options-expected.css", page);
	}
}
