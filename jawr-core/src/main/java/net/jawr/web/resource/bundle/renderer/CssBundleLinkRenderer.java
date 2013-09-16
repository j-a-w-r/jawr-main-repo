/**
 * 
 */
package net.jawr.web.resource.bundle.renderer;

import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;

/**
 * The interface of CSS bundle renderer. 
 * 
 * @author Ibrahim Chaehoi
 */
public interface CssBundleLinkRenderer extends BundleRenderer {

	/**
	 * Initialize rhe Css bundle renderer
	 * @param bundler
	 *            the bundler
	 * @param useRandomParam
	 *            the flag indicating if we use the random flag
	 * @param media
	 *            the media
	 * @param alternate
	 *            the alternate flag
	 * @param displayAlternateStyles
	 *            the flag indicating if the alternate styles must be displayed
	 * @param title
	 *            the title
	 */
	public void init(ResourceBundlesHandler bundler, Boolean useRandomParam,
			String media, boolean alternate, boolean displayAlternateStyles,
			String title);

}
