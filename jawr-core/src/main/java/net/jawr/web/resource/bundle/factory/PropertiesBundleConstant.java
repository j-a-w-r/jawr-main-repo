/**
 * Copyright 2009-2014 Ibrahim Chaehoi
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
package net.jawr.web.resource.bundle.factory;

/**
 * This class contains constant for the properties based bundles.
 * 
 * @author Ibrahim Chaehoi
 *
 */
public class PropertiesBundleConstant {

	/** The jawr properties prefix */
	public static final String PROPS_PREFIX = "jawr.";
	
	/** The custom bundle property prefix */
	public static final String BUNDLE_FACTORY_CUSTOM_PROPERTY = "bundle.";
	
	/** The resource bundle base directory suffix */
	public static final String RESOURCES_BASEDIR = "bundle.basedir";
	
	/** The suffix for the property indicating if we must use the cache or not */
	public static final String RESOURCES_USE_CACHE = "use.cache";
	
	/** The suffix for the property indicating if we must use a single bundle for the orphan resources */
	public static final String FACTORY_USE_SINGLE_BUNDLE = "factory.use.singlebundle";

	/** The suffix for the property defining the bundle name for the single bundle of the orphan resources */
	public static final String FACTORY_SINGLE_FILE_NAME = "factory.singlebundle.bundlename";

	/** The suffix for the property indicating if we must use a directory mapper to define the bundles */
	public static final String FACTORY_USE_DIR_MAPPER = "factory.use.dirmapper";
	
	/** The suffix for the property defining the path which must be excluded from the directory mapper */
	public static final String FACTORY_DIR_MAPPER_EXCLUSION = "factory.dirmapper.excluded";
	
	/** The suffix for the property indicating if we must create a bundle for the orphans */ 
	public static final String FACTORY_PROCESS_ORPHANS = "factory.use.orphans.mapper";

	/** The suffix for the property defining the global preprocessors */ 
	public static final String BUNDLE_FACTORY_GLOBAL_PREPROCESSORS = "bundle.factory.global.preprocessors";

	/** The suffix for the property defining the global postprocessors */ 
	public static final String BUNDLE_FACTORY_GLOBAL_POSTPROCESSORS = "bundle.factory.global.postprocessors";

	/** The suffix for the property defining the bundle post processors */ 
	public static final String BUNDLE_FACTORY_POSTPROCESSOR = "bundle.factory.bundlepostprocessors";
	
	/** The suffix for the property defining the file post processors */ 
	public static final String BUNDLE_FACTORY_FILE_POSTPROCESSOR = "bundle.factory.filepostprocessors";

	/** The suffix for the property defining the composite bundle post processors */ 
	public static final String COMPOSITE_BUNDLE_FACTORY_POSTPROCESSOR = "bundle.factory.composite.bundlepostprocessors";
	
	/** The suffix for the property defining the file composite post processors */ 
	public static final String COMPOSITE_BUNDLE_FACTORY_FILE_POSTPROCESSOR = "bundle.factory.composite.filepostprocessors";

	/** The prefix for the property defining the custom post processors */
	public static final String CUSTOM_POSTPROCESSORS = "jawr.custom.postprocessors";
	
	/** The suffix for the property defining the names of custom post processors */
	public static final String CUSTOM_POSTPROCESSORS_NAMES = ".names";
	
	/** The suffix for the property defining the class of one custom post processor */
	public static final String CUSTOM_POSTPROCESSORS_CLASS = ".class";

	/** The suffix for the property defining the custom generators */
	public static final String CUSTOM_GENERATORS = "custom.generators";
	
	/** The suffix for the property defining the custom resolvers */
	public static final String CUSTOM_RESOLVERS = "custom.resolvers";
	
	// Custom bundle factory parameters
	/** The suffix for the property defining the names of the bundles  */
	public static final String BUNDLE_FACTORY_CUSTOM_NAMES = "bundle.names";
	
	/** The suffix for the property defining the id of the bundle  */
	public static final String BUNDLE_FACTORY_CUSTOM_ID = ".id";
	
	/** The suffix for the property defining the URL bundle prefix of the bundle  */
	public static final String BUNDLE_FACTORY_CUSTOM_BUNDLE_PREFIX = ".bundle.prefix";

	/** The suffix for the property defining the mappings of the bundle  */
	public static final String BUNDLE_FACTORY_CUSTOM_MAPPINGS = ".mappings";
	
	/** The suffix for the property indicating if the bundle is a global one or not */
	public static final String BUNDLE_FACTORY_CUSTOM_GLOBAL_FLAG = ".global";
	
	/** The suffix for the property defining the order of the bundle inclusion */
	public static final String BUNDLE_FACTORY_CUSTOM_ORDER = ".order";
	
	/** The suffix for the property defining the bundle dependencies */
	public static final String BUNDLE_FACTORY_CUSTOM_DEPENDENCIES = ".dependencies";
	
	/** The suffix for the property indicating if the bundle must be included only in debug */
	public static final String BUNDLE_FACTORY_CUSTOM_DEBUGONLY = ".debugonly";
	
	/** The suffix for the property indicating if the bundle must never be included in debug */
	public static final String BUNDLE_FACTORY_CUSTOM_DEBUGNEVER = ".debugnever";
	
	/** The suffix for the property defining the bundle post processors */
	public static final String BUNDLE_FACTORY_CUSTOM_POSTPROCESSOR = ".bundlepostprocessors";
	
	/** The suffix for the property defining the file post processors */
	public static final String BUNDLE_FACTORY_CUSTOM_FILE_POSTPROCESSOR = ".filepostprocessors";
	
	/** The suffix for the property defining the IE expression for the inclusion of the bundle */
	public static final String BUNDLE_FACTORY_CUSTOM_IE_CONDITIONAL_EXPRESSION = ".ieonly.condition";

	/** The suffix for the property indicating if the bundle is a composite one */
	public static final String BUNDLE_FACTORY_CUSTOM_COMPOSITE_FLAG = ".composite";
	
	/** The suffix for the property defining the composite bundle child names */
	public static final String BUNDLE_FACTORY_CUSTOM_COMPOSITE_NAMES = ".child.names";
	
	/** The suffix for the property defining the alternate static URL for production mode */
	public static final String BUNDLE_FACTORY_CUSTOM_PRODUCTION_ALT_URL = ".productionURL";

	/** The suffix for the property defining the debug static URL for debug mode */
	public static final String BUNDLE_FACTORY_CUSTOM_DEBUG_URL = ".debugURL";
	
	/** The suffix for the property defining the variants for a bundle */
	public static final String BUNDLE_FACTORY_CUSTOM_VARIANTS = ".variants";
	
	// Bundle hashcode
	/** The suffix for the property defining the bundle hascode for a bundle */
	public static final String BUNDLE_FACTORY_CUSTOM_HASHCODE = ".bundleHashcode";
	
	/** The suffix for the property defining the bundle hascode for a bundle with a locale variant */
	public static final String BUNDLE_FACTORY_CUSTOM_HASHCODE_VARIANT = ".bundleHashcode.";
	
	/** The suffix for the property defining the licence path list for a bundle */
	public static final String BUNDLE_FACTORY_CUSTOM_LICENCE_PATH_LIST = ".licencePathList";

	
}
