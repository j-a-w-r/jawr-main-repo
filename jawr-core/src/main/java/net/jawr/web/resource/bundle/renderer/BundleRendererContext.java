/**
 * Copyright 2009-2010 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.renderer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.servlet.RendererRequestUtils;

/**
 * This class defines the Bundle renderer context
 * 
 * @author Ibrahim Chaehoi
 */
public class BundleRendererContext {

	/** The context path */
	private String contextPath;
	
	/** The variants */
	private Map<String, String> variants = new HashMap<String, String>();
	
	/** The included bundles */
	private Set<String> includedBundles;
	
	/** The included resources in debug mode */
	private Set<String> includedResources;
	
	/** The flag indicating if the global bundles have already been added */
	private boolean globalBundleAdded;
	
	/** The flag indicating if we are using Gzip or not */
	private boolean useGzip;
	
	/** The flag indicating if it's an SSL request or not */
	private boolean isSslRequest;
	
	/** The servlet request */
	private HttpServletRequest request;
	
	/**
	 * Constructor
	 * @param contextPath the context path
	 * @param variantKey the variant key
	 * @param useGzip the flag indicating if we are using Gzip or not
	 * @param isSslRequest the flag indicating if it's an SSL request or not
	 */
	public BundleRendererContext(String contextPath, Map<String, String> variants,
			boolean useGzip, boolean isSslRequest) {
		super();
		this.contextPath = contextPath;
		this.variants = variants;
		this.includedBundles = new HashSet<String>();
		this.includedResources = new HashSet<String>();
		this.useGzip = useGzip;
		this.isSslRequest = isSslRequest;
	}

	/**
	 * Constructor
	 * @param request the HTTP request
	 * @param jawrConfig the Jawr config
	 */
	public BundleRendererContext(HttpServletRequest request, JawrConfig jawrConfig) {
		super();
		this.request = request;
		this.contextPath = request.getContextPath();
		this.variants = jawrConfig.getGeneratorRegistry().resolveVariants(request);
		this.useGzip = RendererRequestUtils.isRequestGzippable(request,jawrConfig);
		this.isSslRequest = RendererRequestUtils.isSslRequest(request);
		
		this.includedBundles = new HashSet<String>();
		this.includedResources = new HashSet<String>();
	}
	
	/**
	 * Returns the context path
	 * @return the contextPath
	 */
	public String getContextPath() {
		
		return contextPath;
	}

	/**
	 * Sets the request
	 * @param request the request to set
	 */
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	/**
	 * Returns the request
	 * @return the request
	 */
	public HttpServletRequest getRequest() {
		return request;
	}

	/**
	 * Sets the context path
	 * @param contextPath the contextPath to set
	 */
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	/**
	 * Returns the variant map
	 * @return the variants
	 */
	public Map<String, String> getVariants() {
		return variants;
	}

	/**
	 * Sets  the variant map
	 * @param variants the variants to set
	 */
	public void setVariants(Map<String, String> variants) {
		this.variants = variants;
	}

	/**
	 * Returns true if the global bundles have already been added
	 * @return true if the global bundles have already been added
	 */
	public boolean isGlobalBundleAdded() {
		return globalBundleAdded;
	}

	/**
	 * Sets the flag indicating if the global bundles have already been added
	 * @param globalBundleAdded the flag to set
	 */
	public void setGlobalBundleAdded(boolean globalBundleAdded) {
		this.globalBundleAdded = globalBundleAdded;
	}

	/**
	 * Returns true if we use Gzip
	 * @return true if we use Gzip
	 */
	public boolean isUseGzip() {
		return useGzip;
	}

	/**
	 * Sets the flag indicating if we use Gzip
	 * @param useGzip the flag to set
	 */
	public void setUseGzip(boolean useGzip) {
		this.useGzip = useGzip;
	}

	/**
	 * Returns true if it's an SSL request
	 * @return true if it's an SSL request
	 */
	public boolean isSslRequest() {
		return isSslRequest;
	}

	/**
	 * Sets the flag indicating if it's an SSL request
	 * @param isSslRequest the flag to set
	 */
	public void setSslRequest(boolean isSslRequest) {
		this.isSslRequest = isSslRequest;
	}

	/**
	 * Returns the included bundles
	 * @return the included bundles
	 */
	public Set<String> getIncludedBundles() {
		return includedBundles;
	}
	
	/**
	 * Sets the included bundle
	 * @param includedBundles the included bundles to set
	 */
	public void setIncludedBundles(Set<String> includedBundles) {
		this.includedBundles = includedBundles;
	}

	/**
	 * Returns the included resources
	 * @return the includedResources
	 */
	public Set<String> getIncludedResources() {
		return includedResources;
	}

	/**
	 * Sets the included resources
	 * @param includedResources the includedResources to set
	 */
	public void setIncludedResources(Set<String> includedResources) {
		this.includedResources = includedResources;
	}
	
}
