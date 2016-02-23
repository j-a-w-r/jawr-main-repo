/**
 * Copyright 2016 Danny Trunk
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

package net.jawr.web.autoconfigure;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Jawr.
 *
 * @author Danny Trunk
 * @since 3.9
 *
 */
@ConfigurationProperties
public class JawrProperties {
	private Map<String, String> jawr = new HashMap<String, String>();

	public Map<String, String> getJawr() {
		return jawr;
	}

	public void setJawr(Map<String, String> jawr) {
		this.jawr = jawr;
	}
}