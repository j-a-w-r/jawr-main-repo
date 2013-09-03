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
package net.jawr.web.resource.bundle.hashcode;

import net.jawr.web.config.JawrConfig;

/**
 * The default bundle hashcode generator
 * @author Ibrahim Chaehoi
 *
 */
public class BundleStringHashcodeGenerator implements BundleHashcodeGenerator {

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.hash.IBundleHashcodeGenerator#generateHashCode(java.lang.String)
	 */
	public String generateHashCode(JawrConfig config, String content) {
		
		String generatedHashCode = null;
		int bundleDataHashCode = content.hashCode();
		// Since this number is used as part of urls, the -sign is converted to 'N'
		if (bundleDataHashCode < 0) {
			generatedHashCode = "N" + bundleDataHashCode * -1;
		} else{
			generatedHashCode = Integer.toString(bundleDataHashCode);
		}
		return generatedHashCode;
	}

}
