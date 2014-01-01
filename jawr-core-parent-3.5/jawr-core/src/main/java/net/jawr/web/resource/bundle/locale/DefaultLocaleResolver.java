/**
 * Copyright 2008-2010 Jordi Hernández Sellés, Ibrahim Chaehoi
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

import java.io.Serializable;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

/**
 * Default implementation of the LocaleResolver interface. 
 * Uses request.getLocale() to determine the user's Locale. 
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class DefaultLocaleResolver implements LocaleResolver, Serializable {

	/** The serial version UID */
	private static final long serialVersionUID = -6079547159706255940L;

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.locale.LocaleResolver#resolveLocaleCode(javax.servlet.http.HttpServletRequest)
	 */
	public String resolveLocaleCode(final HttpServletRequest request) {
		
		String localCode = null;
		if(request.getLocale() != Locale.getDefault()){
			localCode = request.getLocale().toString();
		}
		
		return localCode;
	}
}
