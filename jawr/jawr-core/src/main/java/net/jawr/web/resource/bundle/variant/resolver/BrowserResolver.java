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
package net.jawr.web.resource.bundle.variant.resolver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.variant.VariantResolver;
import net.jawr.web.resource.bundle.variant.VariantSet;

/**
 * This class defines the browser variant.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class BrowserResolver implements VariantResolver {

	/** The IE Pattern */
	private static final Pattern IE_PATTERN = Pattern.compile("MSIE (\\d+)");

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.variant.VariantResolver#getVariantType()
	 */
	@Override
	public String getVariantType() {
		return JawrConstant.BROWSER_VARIANT_TYPE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.variant.VariantResolver#getAvailableVariant(
	 * java.lang.String, net.jawr.web.resource.bundle.variant.VariantSet)
	 */
	@Override
	public String getAvailableVariant(String variant, VariantSet variantSet) {

		String browser = variantSet.getDefaultVariant();
		if (variantSet.contains(variant)) {
			browser = variant;
		}

		return browser;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.variant.VariantResolver#resolveVariant(javax
	 * .servlet.http.HttpServletRequest)
	 */
	@Override
	public String resolveVariant(HttpServletRequest request) {

		String browser = null;
		String userAgent = request.getHeader("User-Agent");
		if (userAgent != null) {
			Matcher matcher = IE_PATTERN.matcher(userAgent);
			if (matcher.find()) {
				browser = "ie" + matcher.group(1);
			} else if (userAgent.contains("AppleWebKit")) {
				browser = "webkit";
			} else if (userAgent.contains("Firefox")) {
				browser = "firefox";
			} else if (userAgent.contains("Opera")) {
				browser = "opera";
			}
		}

		return browser;
	}

}
