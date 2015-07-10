/**
 * Copyright 2008-2011 Jordi Hernández Sellés
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
package net.jawr.web.resource.bundle.generator.dwr;

import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;

import org.directwebremoting.util.VersionUtil;

/**
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class DWRGeneratorFactory {

	private static final String V3_GENERATOR_CLASS = "net.jawr.web.resource.bundle.generator.dwr.DWR3BeanGenerator";
	private static boolean isV2 = true;
	private static boolean isVersionDetermined = false;
	
	public static Class<?> getDWRGeneratorClass() {
		
		if(!isVersionDetermined) {
			String versionLabel = VersionUtil.getVersion();
			versionLabel = versionLabel.substring(0,versionLabel.indexOf('.'));
			isV2 = (2 == Integer.valueOf(versionLabel).intValue());
			isVersionDetermined = true;
		}
		if(isV2)
			return DWRBeanGenerator.class;
		else {
			return ClassLoaderResourceUtils.getClass(V3_GENERATOR_CLASS);
		}
	}
	
}
