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
package net.jawr.web.resource.bundle.variant.resolver;

import javax.servlet.http.HttpServletRequest;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.variant.VariantResolver;
import net.jawr.web.resource.bundle.variant.VariantSet;

/**
 * This class defines the URL scheme resolver.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class ConnectionTypeResolver implements VariantResolver {

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.variant.VariantResolver#getVariantType()
	 */
	public String getVariantType() {
		return JawrConstant.CONNECTION_TYPE_VARIANT_TYPE;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.variant.VariantResolver#getAvailableVariant(java.lang.String, net.jawr.web.resource.bundle.variant.VariantSet)
	 */
	public String getAvailableVariant(String variant, VariantSet variantSet) {
		
		String connectionType = variantSet.getDefaultVariant();
		if(variantSet.contains(variant)){
			connectionType = variant;
		}
		
		return connectionType;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.variant.VariantResolver#resolveVariant(javax.servlet.http.HttpServletRequest)
	 */
	public String resolveVariant(HttpServletRequest request) {
		
		String connectionType = "";
		if(request.getScheme().equals(JawrConstant.HTTPS)){
			connectionType = JawrConstant.SSL;
		}
		return connectionType;
	}

}
