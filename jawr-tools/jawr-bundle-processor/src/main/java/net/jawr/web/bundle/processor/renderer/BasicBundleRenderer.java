/**
 *    Copyright 2008-2009 Andreas Andreou, Ibrahim Chaehoi
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package net.jawr.web.bundle.processor.renderer;

import java.util.ArrayList;
import java.util.List;

import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.AbstractBundleLinkRenderer;

/**
 * This class defines the basic Bundle renderer.
 * It is used by the BundleProcessor to determine the links rendered for a specified bundle. 
 * 
 * @author Ibrahim Chaehoi
 * 
 * original author :
 * @author Andreas Andreou
 */
public class BasicBundleRenderer extends AbstractBundleLinkRenderer {

	/** The serial version UID */
	private static final long serialVersionUID = 2370837423827332688L;

	/** The resource type */
	private String resourceType;
	
	/** The list of links rendered by the BasicBundleRenderer */
	private List<RenderedLink> renderedLinks = new ArrayList<RenderedLink>();
	
	/**
	 * Constructor
	 * @param bundler the resource bundles handler
	 * @param resourceType the resource type
	 * @param useRandomParam the flag indicating if we should use the random parameter
     */
	public BasicBundleRenderer(ResourceBundlesHandler bundler, String resourceType) {
		this(bundler, resourceType, Boolean.FALSE);
	}
	
	/**
	 * Constructor
	 * @param bundler the resource bundles handler
	 * @param useRandomParam the flag indicating if we should use teh random parameter
     */
	public BasicBundleRenderer(ResourceBundlesHandler bundler, String resourceType, Boolean useRandomParam) {
		this.resourceType = resourceType;
		super.init(bundler, useRandomParam);
	}
	
	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.renderer.BundleRenderer#getResourceType()
	 */
	public String getResourceType() {
		return resourceType;
	}
	
	/**
	 * Returns the list of rendered links
	 * @return the list of rendered links
	 */
	public List<RenderedLink> getRenderedLinks() {
		return renderedLinks;
	}

	/* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.renderer.AbstractBundleLinkRenderer#renderLink(java.lang.String)
	 */
	protected String renderLink(String fullPath) {
		renderedLinks.add(new RenderedLink(fullPath, bundler.getConfig().isDebugModeOn()));
		return fullPath;
	}

	/**
     * Creates a link to a bundle in the page. 
     * @param bundleId the bundle id
     * @param contextPath the context path
     * @return the link to a bundle in the page
     */
    protected String createBundleLink(String bundleId, String contextPath) {
    	
    	// When debug mode is on and the resource is generated the path must include a parameter
    	JawrConfig config = bundler.getConfig();
		if( config.isDebugModeOn() && 
    		config.getGeneratorRegistry().isPathGenerated(bundleId)) {
    		bundleId = PathNormalizer.createGenerationPath(bundleId, config.getGeneratorRegistry(), null);
    	}
    	String fullPath = PathNormalizer.joinPaths(config.getServletMapping(), bundleId);
    	fullPath = PathNormalizer.joinPaths(contextPath,fullPath);

    	return renderLink(fullPath);
    }

	
}
