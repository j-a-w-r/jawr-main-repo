/**
 * Copyright 2007-2014 Jordi Hernández Sellés, Ibrahim Chaehoi
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
package net.jawr.web;

import java.util.Arrays;
import java.util.List;

import net.jawr.web.resource.bundle.handler.ClientSideHandlerGeneratorImpl;
import net.jawr.web.resource.bundle.renderer.CSSHTMLBundleLinkRenderer;
import net.jawr.web.resource.bundle.renderer.JavascriptHTMLBundleLinkRenderer;
import net.jawr.web.resource.bundle.renderer.image.ImgHTMLRenderer;
import net.jawr.web.resource.handler.reader.BaseServletContextResourceReader;

/**
 * The constant value for Jawr.
 * 
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class JawrConstant {

	/** The URL separator */
	public static final String URL_SEPARATOR = "/";

	/** The URL separator character. */
	public static final char URL_SEPARATOR_CHAR = '/';

	/** The comma separator */
	public static final String COMMA_SEPARATOR = ",";

	/** The init parameter servlet for the resource type */
	public static final String TYPE_INIT_PARAMETER = "type";

	/** The binary web resource type */
	public static final String BINARY_TYPE = "binary";

	/** The js type */
	public static final String JS_TYPE = "js";

	/** The css type */
	public static final String CSS_TYPE = "css";

	/** The classpath resource prefix */
	public static final String CLASSPATH_RESOURCE_PREFIX = "jar:";

	/** The cache buster separator */
	public static final String CACHE_BUSTER_PREFIX = "cb";

	/** The Jawr application config manager attribute */
	public static final String JAWR_APPLICATION_CONFIG_MANAGER = "net.jawr.web.jmx.JAWR_APPLICATION_CONFIG_MANAGER";

	/** The javascript servlet context attribute name */
	public static final String JS_CONTEXT_ATTRIBUTE = "net.jawr.web.resource.bundle.JS_CONTEXT_ATTRIBUTE";

	/** The css servlet context attribute name */
	public static final String CSS_CONTEXT_ATTRIBUTE = "net.jawr.web.resource.bundle.CSS_CONTEXT_ATTRIBUTE";

	/** The image servlet context attribute name */
	//public static final String IMG_CONTEXT_ATTRIBUTE = "net.jawr.web.resource.bundle.IMG_CONTEXT_ATTRIBUTE";

	/** The binary servlet context attribute name */
	public static final String BINARY_CONTEXT_ATTRIBUTE = "net.jawr.web.resource.bundle.BINARY_CONTEXT_ATTRIBUTE";

	/** The http scheme */
	public static final String HTTP = "http";
	
	/** The https scheme */
	public static final String HTTPS = "https";
	
	/** The ssl protocole name */
	public static final String SSL = "ssl";
	
	/** The https url prefix */
	public static final String HTTPS_URL_PREFIX = "https://";

	/** The http url prefix */
	public static final String HTTP_URL_PREFIX = "http://";

	/** The jawr bundle mapping properties file name for JS resources */
	public static final String JAWR_JS_MAPPING_PROPERTIES_FILENAME = "jawr-js-mapping.properties";
	
	/** The jawr bundle mapping properties file name for CSS resources */
	public static final String JAWR_CSS_MAPPING_PROPERTIES_FILENAME = "jawr-css-mapping.properties";
	
	/** The jawr bundle mapping properties file name for binary resources */
	public static final String JAWR_BINARY_MAPPING_PROPERTIES_FILENAME = "jawr-binary-mapping.properties";
	
	/** The jawr bundle mapping properties file name for image resources */
	//public static final String JAWR_IMG_MAPPING_PROPERTIES_FILENAME = "jawr-img-mapping.properties";
	
	/** The servlet mapping property name */
	public static final String SERVLET_MAPPING_PROPERTY_NAME = "mapping";

	/** The spring servlet mapping property name */
	public static final String SPRING_SERVLET_MAPPING_PROPERTY_NAME = "springServletMapping";

	/** The file URI prefix */
	public static final String FILE_URI_PREFIX = "file://";

	/** The override key parameter name */
	public static final String OVERRIDE_KEY_PARAMETER_NAME = "overrideKey";

	/** The property which enables the use of JMX */
	public static final String JMX_ENABLE_FLAG_SYSTEL_PROPERTY = "com.sun.management.jmxremote";
	
	/** The servlet temp directory property name */
	public static final String SERVLET_CONTEXT_TEMPDIR = "javax.servlet.context.tempdir";

	/** The smartsprites temporary directory */
	public static final String CSS_SMARTSPRITES_TMP_DIR = "/cssSprites/src/";

	/** The ID of the CSS sprite global preprocessor */
	public static final String GLOBAL_CSS_SMARTSPRITES_PREPROCESSOR_ID = "smartsprites";

	/** The ID of the empty global preprocessor */
	public static final String EMPTY_GLOBAL_PREPROCESSOR_ID = "none";
	
	/** The ID of the empty global postprocessor */
	public static final String EMPTY_GLOBAL_POSTPROCESSOR_ID = "none";
	
	/** The ID of the google closure postprocessor */
	public static final String GLOBAL_GOOGLE_CLOSURE_POSTPROCESSOR_ID = "closure";

	/** The META-INF directory prefix */
	public static final String META_INF_DIR_PREFIX = "/META-INF/";

	/** The WEB-INF directory prefix */
	public static final String WEB_INF_DIR_PREFIX = "/WEB-INF/";

	/** The WEB-INF directory  */
	public static final String WEB_INF_DIR = "/WEB-INF";
	
	/** The META-INF directory  */
	public static final String META_INF_DIR = "/META-INF";
	
	/** The directory for CSS created by generators */
	public static final String SPRITE_GENERATED_CSS_DIR = "/generatedCss/";

	/** The directory for image created the sprite generator */
	public static final String SPRITE_GENERATED_IMG_DIR = "/generatedSpriteImg/";

	/** The name for the default value */
	public static final String DEFAULT = "default";
	
	/** The MD5 algorithm name */
	public static final String MD5_ALGORITHM = "MD5";

	/** The CRC32 algorithm name */
	public static final String CRC32_ALGORITHM = "CRC32";

	/** The name of the cookie where the Jawr skin is stored by default */
	public static final String JAWR_SKIN = "jawrSkin";

	/** The locale variant type */
	public static final String LOCALE_VARIANT_TYPE = "locale";

	/** The skin variant type */
	public static final String SKIN_VARIANT_TYPE = "skin";

	/** The browser variant type */
	public static final String BROWSER_VARIANT_TYPE = "browser";

	/** The property name for the default root directories for Css skin */
	public static final String SKIN_DEFAULT_ROOT_DIRS = "skin.default.root.dirs";

	/** The default variant finder for CSS skin generator */
	public static final String DEFAULT_SKIN_VARIANT_FINDER = "net.jawr.web.resource.bundle.generator.variant.css.CssSkinVariantFinder";

	/** The property name for the default root directories for Css skin */
	public static final String SKIN_TYPE_MAPPING_CONFIG_PARAM = "jawr.css.skin.type.mapping";

	/** The "skin_locale" type for the skin resource mapping */
	public static final String SKIN_TYPE_MAPPING_SKIN_LOCALE = "skin_locale";

	/** The "locale_skin" type for the skin resource mapping */
	public static final String SKIN_TYPE_MAPPING_LOCALE_SKIN = "locale_skin";

	/** The variant separator character */
	public static final char VARIANT_SEPARATOR_CHAR = '@';
	
	/** The request parameter to check that the refresh has already been taken in account for the request */
	public static final String JAWR_BUNDLE_REFRESH_CHECK = "jawr.bundle.refresh.check";

	/** The refresh key parameter */
	public static final String REFRESH_KEY_PARAM = "refreshKey";
	
	// Tag
	/** The name of the media attribute */
	public static final String MEDIA_ATTR = "media";
	
	/** The name of the title attribute */
	public static final String TITLE_ATTR = "title";
	
	/** The name of the alternate attribute */
	public static final String ALTERNATE_ATTR = "alternate";
	
	/** The name of the displayAlternate attribute */
	public static final String DISPLAY_ALTERNATE_ATTR = "displayAlternate";
	
	/** The name of the href attribute */
	public static final String HREF_ATTR = "href";
	
	/** The name of the src attribute */
	public static final String SRC_ATTR = "src";

	/** The name of the async attribute */
	public static final String ASYNC_ATTR = "async";
	
	/** The name of the defer attribute */
	public static final String DEFER_ATTR = "defer";
	
	/** Max file size jawr property name */
	public static final String BASE64_MAX_IMG_FILE_SIZE = 
		"jawr.css.postprocessor.base64ImageEncoder.maxFileLength";

	public static final String BASE64_ENCODE_BY_DEFAULT = "jawr.css.postprocessor.base64ImageEncoder.encode.by.default";
	
	public static final String BASE64_ENCODE_SPRITE = "jawr.css.postprocessor.base64ImageEncoder.encode.sprite";
	
	public static final String POST_PROCESSING_CTX_JAWR_BINARY_MAPPING = "jawrBinaryMapping";

	public static final String BASE64_ENCODED_RESOURCES = "BASE64_ENCODED_RESOURCES";

	public static final String BROWSER_IE7 = "ie7";

	public static final String BROWSER_IE6 = "ie6";

	public static final String CONNECTION_TYPE_VARIANT_TYPE = "connectionType";

	public static final String URL_SCHEME_VARIANT_TYPE = "urlScheme";

	public static final String JAWR_BUNDLE_PATH_PLACEHOLDER = "{JAWR_BUNDLE_PATH}";

	public static final String JAWR_BUNDLE_PATH_PLACEHOLDER_PATTERN = "\\{JAWR_BUNDLE_PATH\\}";

	public static final String ILLEGAL_BUNDLE_REQUEST_HANDLER = "jawr.illegal.bundle.request.handler";

	public static final String JAWR_CSS_URL_REWRITER_CONTEXT_PATH = "jawr.css.url.rewriter.context.path";

	/** The Jawr config property to set the base context directory */
	public static final String JAWR_BASECONTEXT_DIRECTORY = "jawr.basecontext.directory";

	/** The Jawr config property to set if the priorrty of the base context directory*/
	public static final String JAWR_BASECONTEXT_DIRECTORY_HIGH_PRIORITY = "jawr.basecontext.directory.high.priority";

	/** The Jawr config property to set if the locale generator should fallback to the systemn locale or not */
	public static final String JAWR_LOCALE_GENERATOR_FALLBACK_TO_SYSTEM_LOCALE = "jawr.locale.generator.fallbackToSystemLocale";
	
	/** The Jawr config property to set if the locale generator should add quote to the message key or noy */
	public static final String JAWR_LOCALE_GENERATOR_ADD_QUOTE_TO_MSG_KEY = "jawr.locale.generator.quoteMsgKey";

	/** The ResourceBundle charset. The default one is "ISO-8859-1" */
	public static final String JAWR_LOCALE_GENERATOR_RESOURCE_BUNDLE_CHARSET = "jawr.locale.generator.resourceBundle.charset";
	
	/** The default resource extensions */
	public static final List<String> DEFAULT_RESOURCE_EXTENSIONS = Arrays.asList("sorting","license","eot","svg","ttf","woff");

	/** The Jawr property name for MBean prefix */
	public static final String JAWR_JMX_MBEAN_PREFIX = "jawr.jmx.mbean.prefix";

	/** The default JS client side handler generator */
	public static final String DEFAULT_JS_CLIENTSIDE_HANDLER_CLASS = ClientSideHandlerGeneratorImpl.class.getName();
	
	/** The default JS bundle link renderer class */
	public static final String DEFAULT_JS_BUNDLE_LINK_RENDERER_CLASS = JavascriptHTMLBundleLinkRenderer.class.getName();

	/** The default CSS bundle link renderer class */
	public static final String DEFAULT_CSS_BUNDLE_LINK_RENDERER_CLASS = CSSHTMLBundleLinkRenderer.class.getName();

	/** The default Image renderer class */
	public static final String DEFAULT_IMG_RENDERER_CLASS = ImgHTMLRenderer.class.getName();

	/** The default servlet context resource reader class */
	public static final String DEFAULT_SERVLET_CTX_RESOURCE_READER_CLASS = BaseServletContextResourceReader.class.getName();

	/** The uglify JS engine property name */
	public static final String UGLIFY_POSTPROCESSOR_JS_ENGINE = "jawr.js.postprocessor.uglify.js.engine";

	/** The property name for Uglify js scripts location */
	public static final String UGLIFY_POSTPROCESSOR_SCRIPT_LOCATION = "jawr.js.postprocessor.uglify.script.location";

	/** The Uglify scripts location */
	public static final String UGLIFY_POSTPROCESSOR_DEFAULT_JS_BASE_LOCATION = "/net/jawr/web/resource/bundle/postprocessor/js/uglify/";

	/** The Uglify scripts location */
	public static final String UGLIFY_POSTPROCESSOR_OPTIONS = "jawr.js.postprocessor.uglify.options";

	/** The Javascript engine property name */
	public static final String JS_ENGINE_PROPERTY = "jawr.js.engine";
	
	/** The default Javascript engine */
	public static final String DEFAULT_JS_ENGINE = "mozilla.rhino";

	/** The performance processing logger */
	public static final String PERF_PROCESSING_LOGGER = "net.jawr.perf.processing";
	
	/** The performance request handling logger */
	public static final String PERF_REQ_HANDLING_LOGGER = "net.jawr.perf.request";

	/** The JAWR config hashcode property name */
	public static final String JAWR_CONFIG_HASHCODE = "jawr.config.hashcode";
	
}
