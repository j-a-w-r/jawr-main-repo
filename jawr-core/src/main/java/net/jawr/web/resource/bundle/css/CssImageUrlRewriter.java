/**
 * Copyright 2010-2012 Ibrahim Chaehoi
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

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jawr.web.JawrConstant;
import net.jawr.web.config.JawrConfig;
import net.jawr.web.resource.BinaryResourcesHandler;
import net.jawr.web.resource.bundle.factory.util.PathNormalizer;
import net.jawr.web.resource.bundle.factory.util.RegexUtil;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.util.StringUtils;

/**
 * This class rewrites is used to rewrite CSS URLs according to the new
 * relative locations of the references, from the original CSS path to a new one. 
 * Since the path changes, the URLs must be rewritten accordingly. URLs in css files are
 * expected to be according to the css spec (see
 * http://www.w3.org/TR/REC-CSS2/syndata.html#value-def-uri). Thus, single
 * double, or no quotes enclosing the url are allowed (and remain as they are
 * after rewriting). Escaped parens and quotes are allowed within the url.
 * 
 * @author Ibrahim Chaehoi
 */
public class CssImageUrlRewriter {

	/** The URL separator */
	private static final String URL_SEPARATOR = "/";

	/** The URL regexp pattern */
	public static String URL_REGEXP = "url\\(\\s*" // 'url('
		// and any number of whitespaces
		+ "(?!(\"|')?(data|mhtml|cid):)(((\\\\\\))|[^)])*)" // any sequence of
		// characters not
		// starting with
		// 'data:',
		// 'mhtml:', or
		// 'cid:', except an
		// unescaped ')'
		+ "\\s*\\)"; // Any number of whitespaces, then ')'

	/** The url pattern */
	public static final Pattern URL_PATTERN = Pattern.compile(URL_REGEXP, // Any number of whitespaces, then ')'
			Pattern.CASE_INSENSITIVE); // works with 'URL('

	/** The binary resource handler */
	protected BinaryResourcesHandler binaryRsHandler;
	
	/** The context path */
	protected String contextPath;
	
	/**
	 * Constructor
	 */
	public CssImageUrlRewriter() {
		
	}
	
	/**
	 * Constructor
	 * @param config the jawr config
	 */
	public CssImageUrlRewriter(JawrConfig config) {
		setContextPath(config.getProperty(JawrConstant.JAWR_CSS_URL_REWRITER_CONTEXT_PATH));
		// Retrieve the binary resource handler
		binaryRsHandler = (BinaryResourcesHandler) config.getContext().getAttribute(JawrConstant.BINARY_CONTEXT_ATTRIBUTE);
		
	}

	/**
	 * Sets the context path
	 * @param contextPath the contextPath to set
	 */
	public void setContextPath(String contextPath) {
		if(StringUtils.isNotEmpty(contextPath)){
			if(contextPath.charAt(0) != '/'){
				contextPath = '/'+contextPath;
			}
			if(contextPath.charAt(contextPath.length()-1) != '/'){
				contextPath = contextPath+'/';
			}
			this.contextPath = contextPath;
		}else{
			this.contextPath = null;
		}
	}

	/**
	 * Rewrites the image URL
	 * @param originalCssPath the original CSS path
	 * @param newCssPath the new CSS path
	 * @param originalCssContent the original CSS content
	 * @return the new CSS content with image path rewritten
	 * @throws IOException
	 */
	public StringBuffer rewriteUrl(String originalCssPath, String newCssPath, String originalCssContent) throws IOException {
		
		// Rewrite each css image url path
		Matcher matcher = URL_PATTERN.matcher(originalCssContent);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {

			String url = getUrlPath(matcher.group(), originalCssPath, newCssPath);
			matcher.appendReplacement(sb, RegexUtil
					.adaptReplacementToMatcher(url));
		}
		matcher.appendTail(sb);
		return sb;
	}

	/**
	 * Transform a matched url so it points to the proper relative path with
	 * respect to the given path.
	 * 
	 * @param match
	 *            the matched URL
	 * @param newCssPath
	 *            the full bundle path
	 * @param status
	 *            the bundle processing status
	 * @return the image URL path
	 * @throws IOException
	 *             if an IO exception occurs
	 */
	protected String getUrlPath(String match, String originalPath, String newCssPath) throws IOException {

		String url = match.substring(match.indexOf('(') + 1,
				match.lastIndexOf(')')).trim();

		// To keep quotes as they are, first they are checked and removed.
		String quoteStr = "";
		if (url.startsWith("'") || url.startsWith("\"")) {
			quoteStr = url.charAt(0) + "";
			url = url.substring(1, url.length() - 1);
		}

		// Handle URL suffix like '/fonts/glyphicons-halflings-regular.eot?#iefix' or '../fonts/glyphicons-halflings-regular.svg#glyphicons_halflingsregular'
		String urlSuffix = "";
		int idxUrlSuffix = -1;
		int idx1 = url.indexOf("?");
		int idx2 = url.indexOf("#");
		if(idx1 != -1 && idx2 == -1 || idx1 == -1 && idx2 != -1){
			idxUrlSuffix = Math.max(idx1, idx2);
		}else if(idx1 != -1 && idx2 != -1) {
			idxUrlSuffix = Math.min(idx1, idx2);
		}
		
		if(idxUrlSuffix != -1){
			urlSuffix = url.substring(idxUrlSuffix);
			url = url.substring(0, idxUrlSuffix);
		}
		// Check if the URL is absolute, but in the application itself
		if(StringUtils.isNotEmpty(contextPath) && url.startsWith(contextPath)){
			String rootRelativePath = PathNormalizer.getRootRelativePath(originalPath);
			url = rootRelativePath + url.substring(contextPath.length());
		}
		
		// Check if the URL is absolute, if it is return it as is.
		int firstSlash = url.indexOf('/');
		if (0 == firstSlash
				|| (firstSlash != -1 && url.charAt(++firstSlash) == '/')) {
			StringBuffer sb = new StringBuffer("url(");
			sb.append(quoteStr).append(url).append(quoteStr).append(")");
			return sb.toString();
		}

		if (url.startsWith(URL_SEPARATOR))
			url = url.substring(1, url.length());
		else if (url.startsWith("./"))
			url = url.substring(2, url.length());

		String imgUrl = getRewrittenImagePath(originalPath, newCssPath, url);

		// Start rendering the result, starting by the initial quote, if any.
		String finalUrl = "url("+quoteStr+imgUrl+urlSuffix+quoteStr+")";
		Matcher urlMatcher = URL_PATTERN.matcher(finalUrl);
		if(urlMatcher.find()){ // Normalize only if a real URL
			finalUrl = PathNormalizer.normalizePath(finalUrl);
		}
		return finalUrl;
	}

	/**
	 * Returns the rewritten image path
	 * @param originalCssPath the original Css path
	 * @param newCssPath the new Css path
	 * @param url the image URL
	 * @return the rewritten image path
	 * @throws IOException if an IOException occurs
	 */
	protected String getRewrittenImagePath(String originalCssPath, String newCssPath, String url)
			throws IOException {

		String imgUrl = null;
		
		// Retrieve the current CSS file from which the CSS image is referenced
		boolean generatedImg = false;
		if(binaryRsHandler != null){
			GeneratorRegistry imgRsGeneratorRegistry = binaryRsHandler.getConfig().getGeneratorRegistry();
			generatedImg = imgRsGeneratorRegistry.isGeneratedBinaryResource(url);
		}
		
		String fullImgPath = PathNormalizer.concatWebPath(originalCssPath, url);
		if(!generatedImg){
			imgUrl = PathNormalizer.getRelativeWebPath(PathNormalizer
					.getParentPath(newCssPath), fullImgPath);
				
		}else{
			imgUrl = url;
		}
		
		return imgUrl;
	}
}
