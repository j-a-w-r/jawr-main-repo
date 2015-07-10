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

import java.io.IOException;
import java.io.Writer;

import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.renderer.BundleRendererContext;
import net.jawr.web.resource.bundle.renderer.JavascriptHTMLBundleLinkRenderer;

/**
 * @author ibrahim Chaehoi
 * 
 */
public class DWRJsBundleLinkRenderer extends JavascriptHTMLBundleLinkRenderer {

	/** The servial version UID */
	private static final long serialVersionUID = 7183920443414458826L;

	/**
	 * Renders the links for the global bundles
	 * 
	 * @param ctx
	 *            the context
	 * @param out
	 *            the writer
	 * @param debugOn
	 *            the debug flag
	 * @throws IOException
	 *             if an IOException occurs.
	 */
	@Override
	protected void renderGlobalBundleLinks(BundleRendererContext ctx,
			Writer out, boolean debugOn) throws IOException {

		if (debugOn) {
			addComment("Start adding DWR global members.", out);
		}
		// If DWR is being used, add a path var to the page
		if (null != bundler.getConfig().getDwrMapping()) {

			String contextPath = ctx.getContextPath();
			StringBuffer sb = DWRParamWriter.buildRequestSpecificParams(
					contextPath, PathNormalizer.joinPaths(contextPath, bundler
							.getConfig().getDwrMapping()));
			out.write(sb.toString());
		}

		if (debugOn) {
			addComment("Finished adding DWR global members.", out);
		}
		
		super.renderGlobalBundleLinks(ctx, out, debugOn);
		
	}

}
