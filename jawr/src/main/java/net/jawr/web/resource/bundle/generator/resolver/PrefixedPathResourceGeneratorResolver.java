/**
 * Copyright 2012  Ibrahim Chaehoi
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
 * This class defines the resolver which are resolved using a prefix in the path : (jar:/mypackage/myscript.js)
 * 
 * @author ibrahim Chaehoi
 */
public class PrefixedPathResourceGeneratorResolver implements ResourceGeneratorResolver {

	/** The generator prefix separator */
	public static final String PREFIX_SEPARATOR = ":";
	
	/** The prefix */
	private String prefix;
	
	/** The flag which indicates that in order to retrieve the resource path the prefix should be removed or not */
	private boolean skipPrefix;
	
	/**
	 * Constructor
	 * 
	 * @param prefix the path prefix
	 */
	public PrefixedPathResourceGeneratorResolver(String prefix) {
		this(prefix, PREFIX_SEPARATOR, true);
	}
	
	/**
	 * Constructor
	 * 
	 * @param prefix the path prefix
	 * @param separator the prefix separator
	 */
	public PrefixedPathResourceGeneratorResolver(String prefix, String separator) {
		this(prefix, separator, true);
	}
	
	/**
	 * Constructor
	 * 
	 * @param prefix the path prefix
	 * @param separator the prefix separator
	 * @param skipPrefix The flag which indicates that in order to retrieve 
	 * the resource path the prefix should be removed or not
	 */
	public PrefixedPathResourceGeneratorResolver(String prefix, String separator, boolean skipPrefix) {
		this.prefix = prefix+separator;
		this.skipPrefix = skipPrefix;
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.matcher.ResourceGeneratorPathMatcher#matchPath(java.lang.String)
	 */
	public boolean matchPath(String path) {
		
		boolean match = false;
		if(StringUtils.isNotEmpty(path)){
			match = path.startsWith(prefix);
		}
		return match;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.matcher.ResourceGeneratorPathMatcher#isSameAs(net.jawr.web.resource.bundle.generator.matcher.ResourceGeneratorPathMatcher)
	 */
	public boolean isSameAs(ResourceGeneratorResolver matcher) {
		
		return equals(matcher);
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.generator.matcher.ResourceGeneratorPathMatcher#getResourcePath(java.lang.String)
	 */
	public String getResourcePath(String requestedPath) {
		
		String resourcePath = requestedPath;
		if(skipPrefix){
			resourcePath = requestedPath.substring(prefix.length());
		}
		return resourcePath;
	}

	
	/* (non-Javadoc)
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
		if (!(obj instanceof PrefixedPathResourceGeneratorResolver)) {
			return false;
		}
		PrefixedPathResourceGeneratorResolver other = (PrefixedPathResourceGeneratorResolver) obj;
		if (prefix == null) {
			if (other.prefix != null) {
				return false;
			}
		} else if (!prefix.equals(other.prefix)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
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
