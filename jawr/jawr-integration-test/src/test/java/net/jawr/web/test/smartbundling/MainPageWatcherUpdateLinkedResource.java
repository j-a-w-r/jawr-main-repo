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
package net.jawr.web.test.smartbundling;

import java.io.FileNotFoundException;
import java.io.IOException;

import net.jawr.web.test.JawrTestConfigFiles;

/**
 * 
 * @author Ibrahim Chaehoi
 */
@JawrTestConfigFiles(webXml = "net/jawr/web/smartbundling/config/web.xml", jawrConfig = "net/jawr/web/smartbundling/config/jawr-watch-linked-resources.properties")
public class MainPageWatcherUpdateLinkedResource extends MainPageUpdateLinkedResourceTest {

	
	protected static String getTempFolder() {
		return "jawr-integration-smartbundling-test-watch-2";
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.test.smartbundling.MainPageTest#updateContent()
	 */
	@Override
	protected void updateContent() throws Exception, InterruptedException, FileNotFoundException, IOException {

		updateResources();
		
		// Wait a little bit
		Thread.sleep(3000);
		 
	}

}
