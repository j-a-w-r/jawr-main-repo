/**
 * Copyright 2010-2016 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.css;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jawr.web.resource.bundle.factory.util.PathNormalizer;

/**
 * This class defines the Url rewriter for CSS in debug mode. It handles
 * references to generated binary resources
 * 
 * @author ibrahim Chaehoi
 */
public class CssDebugUrlRewriter {

	/** The generated image pattern */
	private static final Pattern GENERATED_BINARY_RESOURCE_PATTERN = Pattern
			.compile("(url\\(([\"' ]*))(?!(http|https|data|mhtml))(([a-zA-Z]+):(/)?)([^\\)\"']*)([\"']?\\))");

	/**
	 * Rewrites the generated binary resource URL for debug mode
	 * 
	 * @param requestPath
	 *            the request path
	 * @param content
	 *            the content to rewrite
	 * @param binaryServletMapping
	 *            the binary servlet mapping
	 * @return the rewritten content
	 */
	public static String rewriteGeneratedBinaryResourceDebugUrl(String requestPath, String content, String binaryServletMapping) {

		// Write the content of the CSS in the Stringwriter
		if (binaryServletMapping == null) {
			binaryServletMapping = "";
		}

		// Define the replacement pattern for the generated binary resource
		// (like jar:img/myImg.png)
		String relativeRootUrlPath = PathNormalizer.getRootRelativePath(requestPath);
		String replacementPattern = PathNormalizer
				.normalizePath("$1" + relativeRootUrlPath + binaryServletMapping + "/$5_cbDebug/$7$8");

		Matcher matcher = GENERATED_BINARY_RESOURCE_PATTERN.matcher(content);

		// Rewrite the images define in the classpath, to point to the image
		// servlet
		StringBuffer result = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(result, replacementPattern);
		}
		matcher.appendTail(result);

		return result.toString();
	}
}
