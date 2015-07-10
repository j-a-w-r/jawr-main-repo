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
package net.jawr.web.resource.bundle.locale;

import javax.servlet.http.HttpServletRequest;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.variant.VariantResolver;
import net.jawr.web.resource.bundle.variant.VariantSet;

/**
 * This class defines the variant resolver wrapper for locale resolver
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class LocaleVariantResolverWrapper implements VariantResolver , LocaleResolver {

	/** The locale resolver */
	private final LocaleResolver localeResolver;
	
	/**
	 * Constructor
	 * @param localeResolver the locale resolver
	 */
	public LocaleVariantResolverWrapper(LocaleResolver localeResolver) {
		this.localeResolver = localeResolver;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.variant.VariantResolver#getVariantType()
	 */
	public String getVariantType() {
		return JawrConstant.LOCALE_VARIANT_TYPE;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.variant.VariantResolver#resolveVariant(javax.servlet.http.HttpServletRequest)
	 */
	public String resolveVariant(final HttpServletRequest request) {
	
		return localeResolver.resolveLocaleCode(request);
	}

	/**
	 * @param request
	 * @return
	 * @see net.jawr.web.resource.bundle.locale.LocaleResolver#resolveLocaleCode(javax.servlet.http.HttpServletRequest)
	 */
	public String resolveLocaleCode(HttpServletRequest request) {
		return localeResolver.resolveLocaleCode(request);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.variant.VariantResolver#getAvailableVariant(java.lang.String, java.util.List)
	 */
	public String getAvailableVariant(String variant, VariantSet variantSet) {
		String availableVariant = null;
		if (variantSet.contains(variant)) {
			availableVariant = variant;
		} else {
			String subVar = variant;
			while (subVar.indexOf('_') != -1) {
				subVar = subVar.substring(0, subVar.lastIndexOf('_'));
				if (variantSet.contains(subVar)) {
					availableVariant = subVar;
				}
			}
		}
		if(availableVariant == null){
			availableVariant = variantSet.getDefaultVariant();
		}
		
		return availableVariant;
	}

}
