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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.jawr.web.JawrConstant;
import net.jawr.web.resource.bundle.generator.GeneratorContext;
import net.jawr.web.resource.bundle.variant.VariantResourceReaderStrategy;
import net.jawr.web.resource.bundle.variant.VariantSet;

/**
 * This class defines the resource provider strategy for CSS skin
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class CssSkinVariantResourceProviderStrategy implements VariantResourceReaderStrategy {

	/** The iterator on the variant map */
	private Iterator<Map<String, String>> variantMapStrategyIterator;

	/**
	 * Constructor
	 */
	public CssSkinVariantResourceProviderStrategy() {

	}

	/**
	 * Initialize the variant resource provider strategy
	 * 
	 * @param context
	 *            the generator context
	 * @param variantsSetMap
	 *            the variant set map for the current context path
	 */
	@Override
	public void initVariantProviderStrategy(GeneratorContext context, Map<String, VariantSet> variantsSetMap) {

		List<Map<String, String>> variantMapStrategies = new ArrayList<>();
		Map<String, String> ctxVariantMap = context.getVariantMap();
		VariantSet skinVariantSet = variantsSetMap.get(JawrConstant.SKIN_VARIANT_TYPE);
		String skinVariant = ctxVariantMap.get(JawrConstant.SKIN_VARIANT_TYPE);

		VariantSet localeVariantSet = variantsSetMap.get(JawrConstant.LOCALE_VARIANT_TYPE);
		String localeVariant = ctxVariantMap.get(JawrConstant.LOCALE_VARIANT_TYPE);

		variantMapStrategies.add(getVariantMap(skinVariant, localeVariant));
		if (localeVariantSet != null) {
			variantMapStrategies.add(getVariantMap(skinVariant, localeVariantSet.getDefaultVariant()));
		}
		variantMapStrategies.add(getVariantMap(skinVariantSet.getDefaultVariant(), localeVariant));
		if (localeVariantSet != null) {
			variantMapStrategies
					.add(getVariantMap(skinVariantSet.getDefaultVariant(), localeVariantSet.getDefaultVariant()));
		}

		variantMapStrategyIterator = variantMapStrategies.iterator();
	}

	/**
	 * Returns the variant map from the skin and the locale parameters
	 * 
	 * @param skinVariant
	 *            the skin variant
	 * @param localeVariant
	 *            the locale variant
	 * @return the variant map
	 */
	private Map<String, String> getVariantMap(String skinVariant, String localeVariant) {

		Map<String, String> variantMap = new HashMap<>();
		variantMap.put(JawrConstant.SKIN_VARIANT_TYPE, skinVariant);
		variantMap.put(JawrConstant.LOCALE_VARIANT_TYPE, localeVariant);
		return variantMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.variant.
	 * VariantResourceProviderStrategy#getNextVariantMapConbination()
	 */
	@Override
	public Map<String, String> nextVariantMapConbination() {

		Map<String, String> variantMap = null;
		if (variantMapStrategyIterator.hasNext()) {
			variantMap = variantMapStrategyIterator.next();
		}
		return variantMap;
	}

}
