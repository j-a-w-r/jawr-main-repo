/**
 * Copyright 2008 Jordi Hernández Sellés
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
package net.jawr.web.resource.bundle.renderer;

import org.directwebremoting.util.IdGenerator;

/**
 * This class adds a small script to pages, which is needed when integrating JAWR with DWR.
 * 
 * 
 * @author Jordi Hernández Sellés
 */
public class DWRParamWriter {
	private static final IdGenerator ID_GENERATOR = new IdGenerator();
	private static final int PAGE_ID_LENGTH = 16;
	private static boolean USE_DYNAMIC_SESSION_ID; // Should this evolve to an interceptor chain for the AbstractBundleLinkRenderer?
	
	
	
	/**
	 * Adds a script with DWR needed params, including a generated ID that DWR needs. 
	 * 
	 * @param contextPath
	 * @param dwrPath
	 * @return
	 */
	public static StringBuffer buildRequestSpecificParams(String contextPath,String dwrPath) {
		StringBuffer sb = new StringBuffer("<script type=\"text/javascript\">if(!JAWR){var JAWR = {};};");
		sb.append(buildDWRJSParams(contextPath, dwrPath));
        sb.append("</script>").append("\n");
		
		return sb;
	}
	
	public static StringBuffer buildDWRJSParams(String contextPath,String dwrPath) {
		StringBuffer sb = new StringBuffer(";");
		sb.append("JAWR.jawr_dwr_path='");
        sb.append(dwrPath).append("';");
        
        if(USE_DYNAMIC_SESSION_ID)
        	sb.append("JAWR.dwr_scriptSessionId='").append(ID_GENERATOR.generateId(PAGE_ID_LENGTH)).append("';");
		
        sb.append("JAWR.app_context_path='").append(contextPath).append("';");
        return sb;
	}



	public static void setUseDynamicSessionId(boolean useDynamicSessionId) {
		DWRParamWriter.USE_DYNAMIC_SESSION_ID = useDynamicSessionId;
	}

	
}
