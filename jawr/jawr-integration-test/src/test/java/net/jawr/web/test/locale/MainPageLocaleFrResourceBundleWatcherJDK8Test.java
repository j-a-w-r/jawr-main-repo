/**
 * Copyright 2016 Ibrahim Chaehoi
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
package net.jawr.web.test.locale;

import java.util.List;

import org.junit.Assume;

import com.gargoylesoftware.htmlunit.JavaScriptPage;
import com.gargoylesoftware.htmlunit.html.HtmlScript;

import static org.junit.Assert.assertEquals;

import net.jawr.web.test.JawrTestConfigFiles;
import test.net.jawr.web.TestUtils;

/**
 * 
 * @author Ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/standard/config/web.xml", jawrConfig = "net/jawr/web/locale/config/jawr-watch.properties")
public class MainPageLocaleFrResourceBundleWatcherJDK8Test extends MainPageLocaleFrResourceBundleWatcherTest {

	/* (non-Javadoc)
	 * @see net.jawr.web.test.locale.MainPageLocaleFrResourceBundleWatcherTest#checkBeforeRun()
	 */
	@Override
	protected void checkBeforeRun() {
		Assume.assumeTrue(TestUtils.getJavaVersion() >= 1.8f);
	}

	protected void checkUpdatedPageContent() throws Exception {
		assertContentEquals("/net/jawr/web/locale/resources/index-jsp-updated-result-fr-jdk8-expected.txt", page);
	}

	public void checkUpdatedGeneratedJsLinks() {
		// Test generated Script link
		final List<?> scripts = getJsScriptTags();
		assertEquals(1, scripts.size());
		final HtmlScript script = (HtmlScript) scripts.get(0);
		assertEquals(getUrlPrefix() + "/301777870.fr/js/bundle/msg.js", script.getSrcAttribute());
	}

	public void checkUpdatedJsBundleContent() throws Exception {

		final List<?> scripts = getJsScriptTags();
		final HtmlScript script = (HtmlScript) scripts.get(0);
		final JavaScriptPage page = getJavascriptPage(script);
		assertContentEquals("/net/jawr/web/locale/resources/msg-bundle-fr-updated-jdk8.js", page);
	}

}
