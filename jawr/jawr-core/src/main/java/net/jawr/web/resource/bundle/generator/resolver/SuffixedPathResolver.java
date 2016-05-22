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
 * This class defines the resolver for ResourceGenerator which are resolved from
 * the suffixed path : (/mypackage/myscript<b>.jsx</b>)
 * 
 * @author ibrahim Chaehoi
 */
public class SuffixedPathResolver implements ResourceGeneratorResolver {

	/** The generator suffix separator */
	public static final String SUFFIX_SEPARATOR = ".";

	/** The prefix */
	private String suffix;

	/**
	 * Constructor The default separator is '.'
	 * 
	 * @param prefix
	 *            the path prefix
	 */
	public SuffixedPathResolver(String suffix) {
		this(suffix, SUFFIX_SEPARATOR);
	}

	/**
	 * Constructor
	 * 
	 * @param prefix
	 *            the path prefix
	 * @param separator
	 *            the suffix separator
	 */
	public SuffixedPathResolver(String suffix, String separator) {
		this.suffix = separator + suffix;
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
		return ResolverType.SUFFIXED;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.matcher.
	 * ResourceGeneratorPathMatcher#matchPath(java.lang.String)
	 */
	public boolean matchPath(String path) {

		boolean match = false;
		if (StringUtils.isNotEmpty(path)) {
			match = path.endsWith(suffix);
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
	public boolean isSameAs(ResourceGeneratorResolver matcher) {

		return equals(matcher);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.jawr.web.resource.bundle.generator.matcher.
	 * ResourceGeneratorPathMatcher#getResourcePath(java.lang.String)
	 */
	public String getResourcePath(String requestedPath) {

		return requestedPath;
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
		if (!(obj instanceof SuffixedPathResolver)) {
			return false;
		}
		SuffixedPathResolver other = (SuffixedPathResolver) obj;
		if (suffix == null) {
			if (other.suffix != null) {
				return false;
			}
		} else if (!suffix.equals(other.suffix)) {
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
		result = prime * result + ((suffix == null) ? 0 : suffix.hashCode());
		return result;
	}

}
