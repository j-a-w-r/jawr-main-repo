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
package net.jawr.web.resource.bundle.locale;

import javax.servlet.http.HttpServletRequest;

/**
 * An adapter to let Jawr access the user's locale based on the current request.  
 * 
 * @author Jordi Hernández Sellés
 */
public interface LocaleResolver {
	
	/**
	 * Resolve the Locale to use for the user associated to a request. 
	 * @param request
	 * @return A string identifying the locale using the standard underscore-separated locale keys (en_US, etc). 
	 */
	String resolveLocaleCode(HttpServletRequest request); 

}
