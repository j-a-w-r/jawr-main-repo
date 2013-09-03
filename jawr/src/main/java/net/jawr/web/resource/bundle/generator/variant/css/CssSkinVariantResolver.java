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
package net.jawr.web.resource.bundle.generator.variant.css;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * The skin variant resolver is used to determine the current skin from the cookie
 * set in the request.
 *  
 * @author Ibrahim Chaehoi
 *
 */
public class CssSkinVariantResolver extends AbstractCssSkinVariantResolver {

	private Logger log = Logger.getLogger(CssSkinVariantResolver.class);
	
	/**
	 * Constructor
	 */
	public CssSkinVariantResolver() {
		
	}
	
	/**
	 * Constructor
	 * @param defaultSkin the default skin
	 * @param skinCookieName the cookie name for the skin
	 */
	public CssSkinVariantResolver(String defaultSkin, String skinCookieName) {
		
		super(defaultSkin, skinCookieName);
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.variant.VariantResolver#resolveVariant(javax.servlet.http.HttpServletRequest)
	 */
	public String resolveVariant(HttpServletRequest request) {
		
		Cookie[] cookies = request.getCookies();
		String skin = defaultSkin;
		if(cookies != null){
			int nbCookies = cookies.length;
			for (int i = 0; i < nbCookies; i++) {
				Cookie cookie = cookies[i];
				if(cookie.getName().equals(skinCookieName)){
					skin = cookie.getValue();
				}
			}
		}
		log.debug("Resolved skin "+skin);
		
		return skin;
	}
}
