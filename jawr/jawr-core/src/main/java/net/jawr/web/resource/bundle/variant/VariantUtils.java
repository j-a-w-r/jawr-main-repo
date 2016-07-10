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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import net.jawr.web.JawrConstant;
import net.jawr.web.exception.BundlingProcessException;
import net.jawr.web.util.StringUtils;

/**
 * Utility class for variants
 * 
 * @author Ibrahim Chaehoi
 */
public class VariantUtils {

	/**
	 * Returns all variant keys from the map of variant set
	 * 
	 * @param variantSets
	 *            the map of variant set
	 * @return all variant keys
	 */
	public static List<Map<String, String>> getAllVariants(Map<String, ? extends Collection<String>> variantSets) {

		List<Map<String, String>> variants = new ArrayList<>();
		if (variantSets != null) {
			for (Entry<String, Collection<String>> variantEntry : new TreeMap<>(variantSets).entrySet()) {
				String variantType = variantEntry.getKey();
				Collection<String> variantList = variantEntry.getValue();
				if (variants.isEmpty()) {
					variants = getVariants(null, variantType, variantList);
				} else {

					List<Map<String, String>> tmpResult = new ArrayList<>();
					for (Map<String, String> curVariant : variants) {
						tmpResult.addAll(getVariants(curVariant, variantType, variantList));
					}
					variants = tmpResult;
				}
			}
		}

		return variants;
	}

	/**
	 * Returns the list of variant maps, which are initialized with the current
	 * map values and each element of the list contains an element of the
	 * variant list with the variant type as key
	 * 
	 * @param curVariant
	 *            the current variant map
	 * @param variantType
	 *            the variant type
	 * @param variantList
	 *            the variant list
	 * @return the list of variant maps
	 */
	private static List<Map<String, String>> getVariants(Map<String, String> curVariant, String variantType,
			Collection<String> variantList) {

		List<Map<String, String>> variants = new ArrayList<>();
		for (String variant : variantList) {
			Map<String, String> map = new HashMap<>();
			if (curVariant != null) {
				map.putAll(curVariant);
			}
			map.put(variantType, variant);
			variants.add(map);
		}
		return variants;
	}

	/**
	 * Returns all variant keys from the map of variant set
	 * 
	 * @param variants
	 *            the map of variant set
	 * @return all variant keys
	 */
	public static List<String> getAllVariantKeys(Map<String, ? extends Collection<String>> variants) {

		List<String> variantKeys = new ArrayList<>();
		for (Collection<String> variantList : new TreeMap<>(variants).values()) {
			if (variantKeys.isEmpty()) {
				variantKeys = getVariantKeys(null, variantList);
			} else {

				List<String> tmpResult = new ArrayList<>();
				for (String curVariantKey : variantKeys) {
					tmpResult.addAll(getVariantKeys(curVariantKey + JawrConstant.VARIANT_SEPARATOR_CHAR, variantList));
				}
				variantKeys = tmpResult;
			}
		}

		if (variantKeys.isEmpty()) {
			variantKeys.add(null);
		}
		return variantKeys;
	}

	/**
	 * Returns all variant keys from the map of variant set
	 * 
	 * @param variants
	 *            the map of variant set
	 * @param fixedVariants
	 *            the fixed variant map
	 * @return all variant keys
	 */
	public static List<String> getAllVariantKeysFromFixedVariants(Map<String, VariantSet> variants,
			Map<String, String> fixedVariants) {

		Map<String, VariantSet> tempVariants = new HashMap<>(variants);

		if (fixedVariants != null) {

			for (Entry<String, String> entry : fixedVariants.entrySet()) {
				VariantSet variantSet = tempVariants.get(entry.getKey());
				if (variantSet != null) {
					String variantValue = variantSet.getDefaultVariant();
					if (variantSet.contains(entry.getValue())) {
						variantValue = entry.getValue();
					}

					VariantSet newVariantSet = new VariantSet(variantSet.getType(), variantValue,
							Arrays.asList(variantValue));
					tempVariants.put(variantSet.getType(), newVariantSet);
				}
			}
		}

		return getAllVariantKeys(tempVariants);
	}

	/**
	 * Returns the variant keys
	 * 
	 * @param variantKeyPrefix
	 *            the variant key prefix
	 * @param variants
	 *            The variants
	 */
	private static List<String> getVariantKeys(String variantKeyPrefix, Collection<String> variants) {

		List<String> variantKeys = new ArrayList<>();
		for (String variant : variants) {
			if (variant == null) {
				variant = "";
			}
			if (variantKeyPrefix == null) {
				variantKeys.add(variant);
			} else {
				variantKeys.add(variantKeyPrefix + variant);
			}
		}

		return variantKeys;
	}

	/**
	 * Returns the variant key from the variants given in parameter
	 * 
	 * @param variants
	 *            the variants
	 * @return the variant key
	 */
	public static String getVariantKey(Map<String, String> variants) {

		String variantKey = "";
		if (variants != null) {
			variantKey = getVariantKey(variants, variants.keySet());
		}

		return variantKey;
	}

	/**
	 * Resolves a registered path from a locale key, using the same algorithm
	 * used to locate ResourceBundles.
	 * 
	 * @param curVariants
	 *            the current variant map
	 * @param variantTypes
	 *            the list of variant types
	 * @return the variant key to use
	 */
	public static String getVariantKey(Map<String, String> curVariants, Set<String> variantTypes) {

		String variantKey = "";
		if (curVariants != null && variantTypes != null) {

			Map<String, String> tempVariants = new TreeMap<>(curVariants);
			StringBuilder variantKeyBuf = new StringBuilder();
			for (Entry<String, String> entry : tempVariants.entrySet()) {
				if (variantTypes.contains(entry.getKey())) {
					String value = entry.getValue();
					if (value == null) {
						value = "";
					}
					variantKeyBuf.append(value + JawrConstant.VARIANT_SEPARATOR_CHAR);
				}
			}

			variantKey = variantKeyBuf.toString();
			if (StringUtils.isNotEmpty(variantKey)
					&& variantKey.charAt(variantKey.length() - 1) == JawrConstant.VARIANT_SEPARATOR_CHAR) {
				variantKey = variantKey.substring(0, variantKey.length() - 1);
			}
		}

		return variantKey;
	}

	/**
	 * Get the bundle name taking in account the variant key
	 * 
	 * @param bundleName
	 *            the bundle name
	 * @param variantKey
	 *            the variant key
	 * @param iGeneratedResource
	 *            the flag indicating if it's a generated resource
	 * @return the variant bundle name
	 */
	public static String getVariantBundleName(String bundleName, String variantKey, boolean iGeneratedResource) {

		String newName = bundleName;
		if (StringUtils.isNotEmpty(variantKey)) {
			int idxSeparator = bundleName.lastIndexOf('.');
			if (!iGeneratedResource && idxSeparator != -1) {
				newName = bundleName.substring(0, idxSeparator);
				newName += JawrConstant.VARIANT_SEPARATOR_CHAR + variantKey;
				newName += bundleName.substring(idxSeparator);
			} else {
				newName += JawrConstant.VARIANT_SEPARATOR_CHAR + variantKey;
			}
		}

		return newName;
	}

	/**
	 * Returns the bundle name from the variants given in parameter
	 * 
	 * @param bundleName
	 *            the original bundle name
	 * @param variants
	 *            the map of variant
	 * @param isGeneratedResource
	 *            the flag indicating if it's a generated resource or not
	 * @return the variant bundle name
	 */
	public static String getVariantBundleName(String bundleName, Map<String, String> variants,
			boolean isGeneratedResource) {

		String variantKey = getVariantKey(variants);
		return getVariantBundleName(bundleName, variantKey, isGeneratedResource);
	}

	/**
	 * Concatenates 2 map of variant sets.
	 * 
	 * @param variantSet1
	 *            the first map
	 * @param variantSet2
	 *            the second map
	 * @return the concatenated variant map
	 */
	public static Map<String, VariantSet> concatVariants(Map<String, VariantSet> variantSet1,
			Map<String, VariantSet> variantSet2) {

		Map<String, VariantSet> result = new HashMap<>();
		if (!isEmpty(variantSet1) && isEmpty(variantSet2)) {
			result.putAll(variantSet1);
		} else if (isEmpty(variantSet1) && !isEmpty(variantSet2)) {
			result.putAll(variantSet2);
		} else if (!isEmpty(variantSet1) && !isEmpty(variantSet2)) {

			Set<String> keySet = new HashSet<>();
			keySet.addAll(variantSet1.keySet());
			keySet.addAll(variantSet2.keySet());
			for (String variantType : keySet) {
				VariantSet variants1 = variantSet1.get(variantType);
				VariantSet variants2 = variantSet2.get(variantType);
				Set<String> variants = new HashSet<>();
				String defaultVariant = null;

				if (variants1 != null && variants2 != null && !variants1.hasSameDefaultVariant(variants2)) {
					throw new BundlingProcessException("For the variant type '" + variantType
							+ "', the variant sets defined in your bundles don't have the same default value.");
				}

				if (variants1 != null) {
					variants.addAll(variants1);
					defaultVariant = variants1.getDefaultVariant();
				}
				if (variants2 != null) {
					variants.addAll(variants2);
					defaultVariant = variants2.getDefaultVariant();
				}

				VariantSet variantSet = new VariantSet(variantType, defaultVariant, variants);
				result.put(variantType, variantSet);
			}
		}

		return result;
	}

	/**
	 * Returns true if the map is null or empty
	 * 
	 * @param map
	 *            the map
	 * @return true if the map is null or empty
	 */
	private static boolean isEmpty(Map<?, ?> map) {
		return map == null || map.isEmpty();
	}

}
