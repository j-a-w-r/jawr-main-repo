/**
 * Copyright 2012-2016  Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator.resolver;

import net.jawr.web.util.StringUtils;

/**
 * This class defines the resolver which are resolved using a prefix in the path
 * : (jar:/mypackage/myscript.js)
 * 
 * @author ibrahim Chaehoi
 */
public class PrefixedPathResolver implements ResourceGeneratorResolver {

	/** The generator prefix separator */
	public static final String PREFIX_SEPARATOR = ":";

	/** The prefix */
	private String prefix;

	/**
	 * Constructor
	 * 
	 * @param prefix
	 *            the path prefix
	 */
	public PrefixedPathResolver(String prefix) {
		this(prefix, PREFIX_SEPARATOR);
	}

	/**
	 * Constructor
	 * 
	 * @param prefix
	 *            the path prefix
	 * @param separator
	 *            the prefix separator
	 */
	public PrefixedPathResolver(String prefix, String separator) {
		this.prefix = prefix + separator;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.resolver.ResourceGeneratorResolver
	 * #getType()
	 */
	@Override
	public ResolverType getType() {

		return ResolverType.PREFIXED;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.matcher.
	 * ResourceGeneratorPathMatcher#matchPath(java.lang.String)
	 */
	@Override
	public boolean matchPath(String path) {

		boolean match = false;
		if (StringUtils.isNotEmpty(path)) {
			match = path.startsWith(prefix);
		}
		return match;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.matcher.
	 * ResourceGeneratorPathMatcher#isSameAs(net.jawr.web.resource.bundle.
	 * generator.matcher.ResourceGeneratorPathMatcher)
	 */
	@Override
	public boolean isSameAs(ResourceGeneratorResolver matcher) {

		return equals(matcher);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.matcher.
	 * ResourceGeneratorPathMatcher#getResourcePath(java.lang.String)
	 */
	@Override
	public String getResourcePath(String requestedPath) {

		return requestedPath.substring(prefix.length());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof PrefixedPathResolver)) {
			return false;
		}
		PrefixedPathResolver other = (PrefixedPathResolver) obj;
		if (prefix == null) {
			if (other.prefix != null) {
				return false;
			}
		} else if (!prefix.equals(other.prefix)) {
			return false;
		}
		return true;
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
		result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
		return result;
	}

}
