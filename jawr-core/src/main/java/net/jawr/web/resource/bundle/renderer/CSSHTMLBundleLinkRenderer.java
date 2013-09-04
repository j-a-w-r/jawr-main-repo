/**
 * Copyright 2007-2012 Jordi Hernández Sellés, Ibrahim Chaehoi
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

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.jawr.web.DebugMode;
import net.jawr.web.JawrConstant;
import net.jawr.web.exception.JawrLinkRenderingException;
import net.jawr.web.resource.bundle.JoinableResourceBundle;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.iterator.ResourceBundlePathsIterator;
import net.jawr.web.resource.bundle.variant.VariantUtils;
import net.jawr.web.servlet.RendererRequestUtils;
import net.jawr.web.util.StringUtils;

import org.apache.log4j.Logger;

/**
 * Renderer that creates css link tags. 
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class CSSHTMLBundleLinkRenderer extends AbstractBundleLinkRenderer implements BundleRenderer{
    
	/** The serial version UID */
	private static final long serialVersionUID = 8478334123266702133L;

	/** The logger */
	private static final Logger LOGGER = Logger
			.getLogger(CSSHTMLBundleLinkRenderer.class);

	/** The start tag */
	private static final String PRE_TAG = "<link rel=\"stylesheet\" type=\"text/css\" media=\"";
    
	/** The start tag */
	private static final String PRE_TAG_ALTERNATE = "<link rel=\"alternate stylesheet\" type=\"text/css\" media=\"";
    
	/** The Title prefix */
	private static final String TITLE_PREFIX_TAG = "\" title=\"";
   
	/** The HREF prefix */
	private static final String MID_TAG = "\" href=\"";
    
	/** The end tag */
	private static final String POST_TAG = "\" />\n";
    
	/** The end HTML tag */
	private static final String POST_HTML_TAG = "\" >\n";
    
	/** The end XHTML tag */
	private static final String POST_XHTML_EXT_TAG = "\" ></link>\n";
    
	/** The XHTML flavor */
	public static final String FLAVORS_XHTML = "xhtml";

	/** The XHTML extended flavor */
	public static final String FLAVORS_XHTML_EXTENDED = "xhtml_ext";
    
	/** The HTML flavor */
	public static final String FLAVORS_HTML = "html";
    
	/** The closing tag flavor */
    private static String closingFlavor = POST_TAG;
    
    /** The media attribute */
    private String media;
    
    /** The flag indicating if it's an alternate stylesheet */
    private boolean alternate;
    
    /** The flag indicating if the tag should display the alternate styles */
    private boolean displayAlternateStyles;
    
    /** The title */
    private String title;
    
    /**
     * Constructor
     * @param bundler the bundler
     * @param useRandomParam the flag indicating if we use the random flag
     * @param media the media
     * @param alternate the alternate flag
     * @param displayAlternateStyles the flag indicating if the alternate styles must be displayed
     * @param title the title
     */
    public CSSHTMLBundleLinkRenderer(ResourceBundlesHandler bundler, Boolean useRandomParam, String media, 
    		boolean alternate, boolean displayAlternateStyles, String title) {
        super(bundler, useRandomParam);
        
        this.media = null == media ? "screen" : media;
        this.displayAlternateStyles = displayAlternateStyles;
        this.alternate = alternate;
        this.title = title;
        if(displayAlternateStyles && alternate){
        	throw new IllegalArgumentException("You can't use the displayAlternateStyle and alternate properties together.");
        }
        if(displayAlternateStyles && StringUtils.isNotEmpty(title)){
        	this.title = null;
        	LOGGER.warn("As you are using the displayAlternateStyle property, the title value will be overrided.");
        }
    }
    
    /**
     * Utility method to get the closing tag value based on 
     * a config parameter. 
     * @param flavor the flavor
     * @return the closing tag
     */
    public static void setClosingTag(String flavor) {
    	
    	if(FLAVORS_XHTML_EXTENDED.equalsIgnoreCase(flavor)) {
        	closingFlavor = POST_XHTML_EXT_TAG;
        }
        else if(FLAVORS_HTML.equalsIgnoreCase(flavor))
        	closingFlavor = POST_HTML_TAG;
        else closingFlavor = POST_TAG;
    	
    }
    
    /* (non-Javadoc)
	 * @see net.jawr.web.resource.bundle.renderer.BundleRenderer#getResourceType()
	 */
	public String getResourceType() {
		return JawrConstant.CSS_TYPE;
	}

	/**
	 * Returns true if the renderer must render a CSS bundle link even in debug mode
	 * @param ctx the context
	 * @param debugOn the debug flag
	 * @return true if the renderer must render a CSS bundle link even in debug mode
	 */
	private boolean isForcedToRenderIeCssBundleInDebug(BundleRendererContext ctx,
			boolean debugOn) {
		
		return debugOn && getResourceType().equals(JawrConstant.CSS_TYPE) && 
				bundler.getConfig().isForceCssBundleInDebugForIEOn() && RendererRequestUtils.isIE(ctx.getRequest());
	}
	
	/**
	 * Performs the global bundle rendering
	 * @param ctx the context
	 * @param out the writer
	 * @param debugOn the flag indicating if we are in debug mode or not
	 * @throws IOException if an IO exception occurs
	 */
	protected void performGlobalBundleLinksRendering(BundleRendererContext ctx,
			Writer out, boolean debugOn) throws IOException {
		
		if(isForcedToRenderIeCssBundleInDebug(ctx, debugOn)){
			ResourceBundlePathsIterator resourceBundleIterator = bundler.getGlobalResourceBundlePaths(DebugMode.FORCE_NON_DEBUG_IN_IE, new ConditionalCommentRenderer(out), ctx.getVariants());
			while(resourceBundleIterator.hasNext()){
				String globalBundlePath = resourceBundleIterator.nextPath();
				renderIeCssBundleLink(ctx, out, globalBundlePath);
			}
		}else{
			
			super.performGlobalBundleLinksRendering(ctx, out, debugOn);
		}
	}
	
	/**
	 * Renders the bundle links
	 * @param bundle the bundle
	 * @param ctx the context
	 * @param variant the variant
	 * @param out the writer
	 * @param debugOn the flag indicating if we are in debug mode
	 * @throws IOException if an IOException occurs
	 */
	protected void renderBundleLinks(JoinableResourceBundle bundle,
			BundleRendererContext ctx, Map<String, String> variant, Writer out, boolean debugOn)
			throws IOException {
		
		if(alternate && StringUtils.isNotEmpty(title)){
			
			// force alternate variant
			Map<String, String> variants = ctx.getVariants();
			variants.put(JawrConstant.SKIN_VARIANT_TYPE, title);
		}
		
		if(isForcedToRenderIeCssBundleInDebug(ctx, debugOn)){
			
			ResourceBundlePathsIterator it = bundler.getBundlePaths(DebugMode.FORCE_NON_DEBUG_IN_IE, bundle.getId(), new ConditionalCommentRenderer(out), variant);
		    while(it.hasNext()){
				String bundlePath = it.nextPath();
				renderIeCssBundleLink(ctx, out, bundlePath);
			}
		}else{
			super.renderBundleLinks(bundle, ctx, variant, out, debugOn);
		}
	}
	
	/**
	 * Renders the links for a bundle
	 * @param bundle the bundle
	 * @param requestedPath the requested path
	 * @param ctx the renderer context
	 * @param out the writer
	 * @param debugOn the debug flag
	 * @param renderDependencyLinks the flag indicating if we must render the dependency links
	 * @throws IOException if an IOException occurs
	 */
	protected void renderBundleLinks(JoinableResourceBundle bundle,
			String requestedPath, BundleRendererContext ctx, Writer out,
			boolean debugOn, boolean renderDependencyLinks) throws IOException {
		
		boolean bundleAlreadyIncluded = ctx.getIncludedBundles().contains(bundle.getId());
		
		super.renderBundleLinks(bundle, requestedPath, ctx, out, debugOn, renderDependencyLinks);
		
		if(!bundleAlreadyIncluded && displayAlternateStyles){
			
			if (debugOn) {
				addComment("Start adding members resolved by '" + requestedPath + "'. Bundle id is: '" + bundle.getId() + "'", out);
			}
			
			List<Map<String, String>> variants = VariantUtils.getAllVariants(bundle.getVariants());
			Map<String, String> currentVariant = bundler.getConfig().getGeneratorRegistry().getAvailableVariantMap(bundle.getVariants(), ctx.getVariants());
			String currentLocale = (String) currentVariant.get(JawrConstant.LOCALE_VARIANT_TYPE);
			variants.remove(currentVariant);
			
			// Renders the different variant as alternate stylesheet
			alternate = true;
			for (Iterator<Map<String, String>> itVariantMap = variants.iterator(); itVariantMap.hasNext();) {
				Map<String, String> variant = itVariantMap.next();
				if(variant != null){
					String skin = variant.get(JawrConstant.SKIN_VARIANT_TYPE);
					if(skin == null){
						throw new JawrLinkRenderingException("You are trying to render alternate CSS for a bundle which don't have skin variant defined.");
					}
					
					// Only apply if the locale doesn't exists or is the current one  
					String locale = variant.get(JawrConstant.LOCALE_VARIANT_TYPE);
					if(currentLocale == null || currentLocale.equals(locale)){
							
						title = skin;
						renderBundleLinks(bundle, ctx, variant, out, debugOn);
					}	
				}
			}
			alternate = false;
		}
	}
	
    /* (non-Javadoc)
     * @see net.jawr.web.resource.bundle.renderer.AbstractBundleLinkRenderer#createBundleLink(java.lang.String, java.lang.String)
     */
    protected String renderLink(String fullPath) {
    	
        StringBuffer sb = new StringBuffer();
        
        //displayAlternateStyles
        if(alternate){
        	sb.append(PRE_TAG_ALTERNATE);
        }else{
        	sb.append(PRE_TAG);
        }
        
		sb.append(media);
		if(StringUtils.isNotEmpty(title)){
			sb.append(TITLE_PREFIX_TAG).append(title);
		}
		sb.append(MID_TAG).append(fullPath).append(closingFlavor); 
        
		return sb.toString();
    }
    
    /**
	 * Renders the CSS link to retrieve the CSS bundle for IE in debug mode.
	 * @param ctx the context
	 * @param out the writer
	 * @param bundle the bundle
	 * @throws IOException if an IOException occurs
	 */
	private void renderIeCssBundleLink(BundleRendererContext ctx, Writer out,
			String bundlePath) throws IOException {
		Random randomSeed = new Random();
		int random = randomSeed.nextInt();
		if (random < 0)
			random *= -1;
		String path = GeneratorRegistry.IE_CSS_GENERATOR_PREFIX+GeneratorRegistry.PREFIX_SEPARATOR+bundlePath;
		path = PathNormalizer.createGenerationPath(path, bundler.getConfig().getGeneratorRegistry(), "d="+random);
		out.write(createBundleLink(path, null, ctx.getContextPath(), ctx.isSslRequest()));
	}
}
