/**
 * Copyright 2015  Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.generator.resolver;

import static net.jawr.web.JawrConstant.URL_SEPARATOR;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jawr.web.resource.bundle.generator.GeneratorMappingHelper;
import net.jawr.web.resource.bundle.generator.GeneratorRegistry;
import net.jawr.web.resource.bundle.generator.classpath.webjars.WebJarsLocatorCssGenerator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webjars.WebJarAssetLocator;

/**
 * This class define the WebJars locator resolver. If the webjars-jquery library
 * is in the classpath, 'webjars:jquery.js' will automatically locate the
 * resource instead of using the full ressource path
 * 'webjars:/jquery/2.1.4/jquery.js'
 * To avoid resource reference collision if there multiple resource with the same name in different webjars,
 * like below :</br>
 * webjars:/jquery.js[jquery]
 *
 * @author (Original) Ted Liang (https://github.com/tedliang)
 * @author Ibrahim Chaehoi
 */
public class WebJarsLocatorPathResolver extends PrefixedPathResolver {

	/** The logger */
	private static Logger LOGGER = LoggerFactory
			.getLogger(WebJarsLocatorCssGenerator.class);

	/** The webjars resource prefix */
	private final static String WEBJARS_RESOURCE_PREFIX = "META-INF/resources/webjars";

	/** The webjars resource prefix */
	private static Pattern WEBJARS_PREFIX_PATTERN = Pattern
			.compile(WEBJARS_RESOURCE_PREFIX + "(/[^/]*/[^/]*)");

	/** The webjars Asset locator */
	private final WebJarAssetLocator locator;

	/**
	 * The flag indicating that a check should be done on the resource path for
	 * information on resource path name
	 */
	private final boolean checkResourcePathForInfo;

	/**
	 * The flag indicating that a check should be done on the resource path for
	 * warning on X reference resource
	 */
	private final boolean checkResourcePathForWarning;

	/** The list of path checked to avoid polluting logs */
	private List<String> pathsChecked;

	/**
	 * Constructor
	 *
	 * @param prefix
	 *            the path prefix
	 */
	public WebJarsLocatorPathResolver(String prefix) {
		this(prefix, false, false);
	}

	/**
	 * Constructor
	 *
	 * @param prefix
	 *            the path prefix
	 * @param checkResourcePathForInfo
	 *            the flag indicating that we should check the resource path for
	 *            info
	 * @param checkResourcePathForWarning
	 *            the flag indicating that we should check the resource path for
	 *            warning
	 */
	public WebJarsLocatorPathResolver(String prefix,
			boolean checkResourcePathForInfo,
			boolean checkResourcePathForWarning) {
		super(prefix);
		this.locator = new WebJarAssetLocator();
		this.checkResourcePathForInfo = checkResourcePathForInfo;
		this.checkResourcePathForWarning = checkResourcePathForWarning;
		this.pathsChecked = new ArrayList<String>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.jawr.web.resource.bundle.generator.resolver.PrefixedPathResolver#
	 * getResourcePath(java.lang.String)
	 */
	@Override
	public String getResourcePath(String requestedPath) {
		String resourcePath = super.getResourcePath(requestedPath);
		GeneratorMappingHelper helper = new GeneratorMappingHelper(resourcePath);
		String fullPath = null; 
		if(StringUtils.isNotEmpty(helper.getBracketsParam())){
			// Use the webjars reference stored in the bracket params
			fullPath = locator.getFullPath(helper.getBracketsParam(), helper.getPath());
		}else{
			fullPath = locator.getFullPath(resourcePath);
		}
		if (checkResourcePathForInfo || checkResourcePathForWarning) {
			checkResourcePath(resourcePath, fullPath);
		}
		return fullPath
				.substring(GeneratorRegistry.WEBJARS_GENERATOR_HELPER_PREFIX
						.length() - 2);
	}

	/**
	 * Checks the resource path to warn users if the resource path used may lead
	 * to issues in binary resources (image, fonts, ...) references in the
	 * generated resource
	 * 
	 * @param path
	 *            the resource path
	 * @param the
	 *            resource full path the resource full path
	 */
	private void checkResourcePath(String path, String fullPath) {

		if (!pathsChecked.contains(path)) {

			pathsChecked.add(path);

			if (checkResourcePathForInfo) {

				String pathToCheck = fullPath.substring(WEBJARS_RESOURCE_PREFIX
						.length());
				if (path.equals(pathToCheck)
						|| (URL_SEPARATOR + path).equals(pathToCheck)) {
					if (LOGGER.isInfoEnabled()) {

						Matcher matcher = WEBJARS_PREFIX_PATTERN
								.matcher(fullPath);
						String useCorrectPathMsg = "";
						if (matcher.find()) {
							String shortPath = fullPath.substring(matcher
									.group().length());
							useCorrectPathMsg = "In your case, you should reference the resource '"
									+ GeneratorRegistry.WEBJARS_GENERATOR_PREFIX
									+ GeneratorRegistry.PREFIX_SEPARATOR
									+ path
									+ "' by '"
									+ GeneratorRegistry.WEBJARS_GENERATOR_PREFIX
									+ GeneratorRegistry.PREFIX_SEPARATOR
									+ shortPath + "'";
						}

						LOGGER.info("\nThe resource '"
								+ GeneratorRegistry.WEBJARS_GENERATOR_PREFIX
								+ GeneratorRegistry.PREFIX_SEPARATOR
								+ path
								+ "' is referenced with it's version number. If you change the version of your webjars package, you'll need to update your mapping.\n"
								+ "A better way to reference your resource is to avoid using the version number reference.\n"
								+ useCorrectPathMsg);

					}
				}

			}

			if (checkResourcePathForWarning) {

				Matcher matcher = WEBJARS_PREFIX_PATTERN.matcher(fullPath);
				if (matcher.find()) {
					String shortPath = fullPath.substring(matcher.group()
							.length());
					if (!path.equals(shortPath)) {
						if (LOGGER.isWarnEnabled()) {
							LOGGER.warn("\nThe reference to the CSS resource '"
									+ path
									+ "' could lead to issues "
									+ "in binary resources (image, fonts, ...) mappings for the generated CSS resource.\n"
									+ "Please update your bundle mapping to use webjars reference path which start after the version number.\n"
									+ "In your case, you should reference the resource '"
									+ GeneratorRegistry.WEBJARS_GENERATOR_PREFIX
									+ GeneratorRegistry.PREFIX_SEPARATOR
									+ path
									+ "' by '"
									+ GeneratorRegistry.WEBJARS_GENERATOR_PREFIX
									+ GeneratorRegistry.PREFIX_SEPARATOR
									+ shortPath + "'");

						}
					}
				}
			}
		}

	}
}
