/**
 * Copyright 2010-2016 Ibrahim Chaehoi
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

import javax.servlet.http.HttpServletRequest;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.variant.VariantResolver;
import net.jawr.web.resource.bundle.variant.VariantSet;

/**
 * The class defines the abstract class for skin variant resolver, which is used
 * to determine the current skin from the HTTP request.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public abstract class AbstractCssSkinVariantResolver implements VariantResolver {

	/** the default skin */
	protected String defaultSkin;

	/** the cookie name for the skin */
	protected String skinCookieName;

	/**
	 * Constructor
	 */
	public AbstractCssSkinVariantResolver() {

	}

	/**
	 * Constructor
	 * 
	 * @param defaultSkin
	 *            the default skin
	 * @param skinCookieName
	 *            the cookie name for the skin
	 */
	public AbstractCssSkinVariantResolver(String defaultSkin, String skinCookieName) {

		this.defaultSkin = defaultSkin;
		this.skinCookieName = skinCookieName;
	}

	/**
	 * @param defaultSkin
	 *            the defaultSkin to set
	 */
	public void setDefaultSkin(String defaultSkin) {
		this.defaultSkin = defaultSkin;
	}

	/**
	 * @param skinCookieName
	 *            the skinCookieName to set
	 */
	public void setSkinCookieName(String skinCookieName) {
		this.skinCookieName = skinCookieName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.variant.VariantResolver#resolveVariant(javax
	 * .servlet.http.HttpServletRequest)
	 */
	@Override
	public abstract String resolveVariant(HttpServletRequest request);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.variant.VariantResolver#getVariantType()
	 */
	@Override
	public final String getVariantType() {

		return JawrConstant.SKIN_VARIANT_TYPE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.variant.VariantResolver#getAvailableVariant(
	 * java.lang.String, java.util.Collection)
	 */
	@Override
	public String getAvailableVariant(String variant, VariantSet variantSet) {

		String result = variantSet.getDefaultVariant();
		if (variantSet.contains(variant)) {
			result = variant;
		}
		return result;
	}

}
