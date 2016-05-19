/**
 * Copyright 2013 Ibrahim Chaehoi
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

import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.bundle.factory.util.ClassLoaderResourceUtils;
import net.jawr.web.resource.bundle.handler.ResourceBundlesHandler;
import net.jawr.web.resource.bundle.renderer.image.ImgRenderer;

/**
 * This class defines the bundleRendererLink factory The factory which
 * instanciates the link renderer.
 * 
 * @author ibrahim Chaehoi
 * 
 */
public class RendererFactory {

	/**
	 * Returns the JS Bundle renderer
	 * 
	 * @param bundler
	 *            the ResourceBundlesHandler
	 * @param type
	 *            the script type attribute
	 * @param useRandomParam
	 *            the flag indicating if it should use the random param
	 * @param async
	 *            the flag indicating the value of the async attribute
	 * @param defer
	 *            the flag indicating the value of the deferred attribute
	 * @param crossorigin
	 *            the value of the crossorigin attribute
	 * @return the JS Bundle renderer
	 */
	public final static JsBundleLinkRenderer getJsBundleRenderer(
			ResourceBundlesHandler bundler, String type, Boolean useRandomParam,
			Boolean async, Boolean defer, String crossorigin) {
		JsBundleLinkRenderer renderer = (JsBundleLinkRenderer) ClassLoaderResourceUtils
				.buildObjectInstance(bundler.getConfig()
						.getJsBundleLinkRenderClass());
		renderer.init(bundler, type, useRandomParam, async, defer, crossorigin);

		return renderer;
	}

	/**
	 * Returns the CSS Bundle renderer
	 * 
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
	 * @return the Css Bundle renderer
	 */
	public final static CssBundleLinkRenderer getCssBundleRenderer(
			ResourceBundlesHandler bundler, Boolean useRandomParam,
			String media, boolean alternate, boolean displayAlternateStyles,
			String title) {
		CssBundleLinkRenderer renderer = (CssBundleLinkRenderer) ClassLoaderResourceUtils
				.buildObjectInstance(bundler.getConfig()
						.getCssBundleLinkRenderClass());
		renderer.init(bundler, useRandomParam, media, alternate,
				displayAlternateStyles, title);
		return renderer;
	}

	/**
	 * Returns the image renderer
	 * 
	 * @param config
	 *            the Jawr config
	 * @param isPlainImg
	 *            the flag indicating if it a plain image to render or not
	 * @return the image renderer
	 */
	public final static ImgRenderer getImgRenderer(JawrConfig config,
			boolean isPlainImg) {
		ImgRenderer renderer = (ImgRenderer) ClassLoaderResourceUtils
				.buildObjectInstance(config.getImgRendererClass());
		renderer.init(isPlainImg);
		return renderer;
	}
}
