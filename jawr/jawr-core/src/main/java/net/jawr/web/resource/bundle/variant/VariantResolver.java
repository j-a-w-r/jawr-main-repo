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
package net.jawr.web.resource.bundle.variant;

import javax.servlet.http.HttpServletRequest;

/**
 * The variant request resolver determines from the HttpServletRequest what is
 * the variant bundle to send to the user.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public interface VariantResolver {

	/**
	 * Resolves the variant to use for the user associated to a request.
	 * 
	 * @param request
	 *            the request
	 * @return A string identifying the variant.
	 */
	String resolveVariant(HttpServletRequest request);

	/**
	 * Resolves the variant to use for the user associated to a request.
	 * 
	 * @return A string identifying the variant.
	 */
	String getVariantType();

	/**
	 * Returns the value to use for the variant from the list of variants
	 * available
	 * 
	 * @param variant
	 *            the value of the variant
	 * @param variantSet
	 *            the available variantSet
	 * @return the value to use for the variant
	 */
	String getAvailableVariant(String variant, VariantSet variantSet);

}
