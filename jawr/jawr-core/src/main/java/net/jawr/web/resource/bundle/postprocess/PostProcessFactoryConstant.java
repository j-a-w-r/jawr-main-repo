/**
 * Copyright 2009-2015 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.postprocess;

/**
 * This class defines the constants for the post processors.
 * 
 * @author Ibrahim Chaehoi
 */
public class PostProcessFactoryConstant {
	
	/** The ID of the CSS minify post processor */
	public static final String CSS_MINIFIER = "cssminify";
	
	/** The ID of the CSS path rewriter post processor */
	public static final String URL_PATH_REWRITER = "csspathrewriter";
	
	/** The ID of the base 64 image encoder */
	public static final String BASE64_IMAGE_ENCODER = "base64ImageEncoder";
	
	/** The ID of the CSS charset filter post processor */
	public static final String CSS_CHARSET_FILTER = "csscharset";
	
	/** The ID of the LESS CSS post processor */
	public static final String LESS = "less";
	
	/** The ID of the CSS import post processor */
	public static final String CSS_IMPORT = "cssimport";
	
	/** The ID of the CSS combine media post processor */
	public static final String CSS_COMBINE_MEDIA = "cssCombineMedia";
	
	/** The ID of the YUI compressor post processor */
	public static final String YUI_COMPRESSOR = "YUI";
	
	/** The ID of the JSMin post processor */
	public static final String JSMIN = "JSMin";
	
	/** The ID of the UglifyJS post processor */
	public static final String UGLIFY_JS = "uglify";
	
	/** The ID of the YUI Compressor with obfuscator post processor */
	public static final String YUI_COMPRESSOR_OBFUSCATOR = "YUIobf";
	
	/** The ID of the licence includer post processor */
	public static final String LICENSE_INCLUDER = "license";
	
	/** The ID of the "no postprocessing" post processor */
	public static final String NO_POSTPROCESSING_KEY = "none";

	/** The ID of the Autoprefixer postprocessor */
	public static final String AUTOPREFIXER = "autoprefixer";
	
}
