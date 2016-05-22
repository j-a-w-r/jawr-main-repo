/**
 * Copyright 20122016  Ibrahim Chaehoi
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

import java.io.Serializable;

import net.jawr.web.resource.bundle.generator.ResourceGenerator;

/**
 * This class defines the wrapper which link the resource resolver and its
 * generator
 * 
 * @author ibrahim Chaehoi
 */
public class ResourceGeneratorResolverWrapper implements ResourceGeneratorResolver, Comparable<ResourceGeneratorResolverWrapper>, Serializable {

	/** The serial version UID */
	private static final long serialVersionUID = -5731212158947768492L;

	/** The resource generator */
	private ResourceGenerator generator;

	/** The resolver */
	private ResourceGeneratorResolver resolver;

	public ResourceGeneratorResolverWrapper(ResourceGenerator generator, ResourceGeneratorResolver resolver) {
		this.generator = generator;
		this.resolver = resolver;
	}

	/**
	 * Returns the resource generator
	 * 
	 * @return the resource generator
	 */
	public ResourceGenerator getResourceGenerator() {
		return generator;
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
		return resolver.getType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.matcher.ResourceGeneratorResolver#
	 * matchPath(java.lang.String)
	 */
	public boolean matchPath(String path) {
		return resolver.matchPath(path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.matcher.ResourceGeneratorResolver#
	 * getResourcePath(java.lang.String)
	 */
	public String getResourcePath(String requestedPath) {
		return resolver.getResourcePath(requestedPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.matcher.ResourceGeneratorResolver#
	 * isSameAs(net.jawr.web.resource.bundle.generator.matcher.
	 * ResourceGeneratorResolver)
	 */
	public boolean isSameAs(ResourceGeneratorResolver matcher) {
		return resolver.isSameAs(matcher);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(ResourceGeneratorResolverWrapper rWrapper) {
		
		ResolverComparator comparator = new ResolverComparator();
		return comparator.compare(resolver, rWrapper.resolver);
	}

}
