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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This class defines the variant set for a variant type. A variant set defines
 * for a type of variant what are the available variant values, and which one is
 * used by default.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class VariantSet implements Set<String> {

	/** The variant set type */
	private String type;

	/** The default variant */
	private String defaultVariant;

	/** The variant set */
	private Set<String> variants;

	/**
	 * Constructor
	 * 
	 * @param type
	 *            the variant type
	 * @param defaultVariant
	 *            the default variant
	 * @param variants
	 *            the variant set
	 */
	public VariantSet(String type, String defaultVariant, String[] variants) {

		this(type, defaultVariant, Arrays.asList(variants));
	}

	/**
	 * Constructor
	 * 
	 * @param type
	 *            the variant type
	 * @param defaultVariant
	 *            the default variant
	 * @param variants
	 *            the variant set
	 */
	public VariantSet(String type, String defaultVariant, Collection<String> variants) {

		if (!variants.contains(defaultVariant)) {
			throw new IllegalArgumentException("For the variant type '" + type + "', the default variant '"
					+ defaultVariant + "' doesn't exist in the variant set " + variants + ".");
		}

		this.type = type;
		this.defaultVariant = defaultVariant;
		this.variants = new HashSet<>(variants);
	}

	/**
	 * Constructor
	 * 
	 * @param type
	 *            the variant type
	 * @param defaultVariant
	 *            the default variant
	 * @param variants
	 *            the variant set
	 */
	public VariantSet(String type, String defaultVariant, Set<String> variants) {

		if (!variants.contains(defaultVariant)) {
			throw new IllegalArgumentException("For the variant type '" + type + "', the default variant '"
					+ defaultVariant + "' doesn't exist in the variant set " + variants + ".");
		}

		this.type = type;
		this.defaultVariant = defaultVariant;
		this.variants = variants;
	}

	/**
	 * Returns the variant type
	 * 
	 * @return the variant type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Returns the default variant
	 * 
	 * @return the defaultVariant
	 */
	public String getDefaultVariant() {
		return defaultVariant;
	}

	/**
	 * Returns the variant set
	 * 
	 * @return the variants
	 */
	public Set<String> getVariants() {
		return variants;
	}

	/**
	 * Returns true of the variantSet passed in parameter has the same default
	 * value
	 * 
	 * @param obj
	 *            the variantSet to test
	 * @return true of the variantSet passed in parameter has the same default
	 *         value
	 */
	public boolean hasSameDefaultVariant(VariantSet obj) {

		return (this.defaultVariant == null && obj.defaultVariant == null)
				|| (this.defaultVariant != null && this.defaultVariant.equals(obj.defaultVariant));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#add(E)
	 */
	@Override
	public boolean add(String arg0) {
		return variants.add(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends String> arg0) {
		return variants.addAll(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#clear()
	 */
	@Override
	public void clear() {
		variants.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object o) {
		return variants.contains(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(Collection<?> coll) {
		return variants.containsAll(coll);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return variants.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#iterator()
	 */
	@Override
	public Iterator<String> iterator() {
		return variants.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object o) {
		return variants.remove(o);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> coll) {
		return variants.removeAll(coll);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(Collection<?> coll) {
		return variants.retainAll(coll);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#size()
	 */
	@Override
	public int size() {
		return variants.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#toArray()
	 */
	@Override
	public Object[] toArray() {
		return variants.toArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Set#toArray(T[])
	 */
	@Override
	public <T> T[] toArray(T[] a) {
		return variants.toArray(a);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((defaultVariant == null) ? 0 : defaultVariant.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((variants == null) ? 0 : variants.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VariantSet other = (VariantSet) obj;
		if (defaultVariant == null) {
			if (other.defaultVariant != null)
				return false;
		} else if (!defaultVariant.equals(other.defaultVariant))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (variants == null) {
			if (other.variants != null)
				return false;
		} else if (!variants.equals(other.variants))
			return false;
		return true;
	}

}
