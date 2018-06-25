Descriptor file syntax
----------------------

The Jawr descriptor file consists of a simple properties file. All
property names begin with *jawr.*, followed by either *js.*, *css.* or
*img.* to denote which servlet will use them. A few common properties
will not have the filetype prefix since they apply globally. Some
configuration properties are using name of the class to use, the full
qualified name containing package prefix is expected.

### Basic global properties

| **Property name** | **Type** | **Purpose** | **Default value** |
|-------------------|----------|-------------|-------------------|
|  jawr.charset.name| Any valid charset name (ISO-8859-1, UTF-8, etc) | Specifies the encoding to use when reading the resources to bundle and also the encoding with which the servlet will serve its responses. Note that **all** your resources (js or css files) must use the encoding you specify here, or Jawr will not work properly. | UTF8 	|
|  jawr.debug.on    |  Boolean            	 | Toggle development mode on/off. If the JVM is started with the **net.jawr.debug.on** flag (-Dnet.jawr.debug.on=true), its value takes precedence over whatever value is set on the properties file.| false	|
| jawr.debug.overrideKey | String            | Override production mode on a request by request basis. |	none	|   
| jawr.debug.ie.force.css.bundle |Boolean | Force Jawr to aggregate CSS bundle for IE in debug mode. This allows you to overcome the IE limitation, which can only handle 30 stylesheets. If you set this flag to true, with an IE browser in debug mode, Jawr will render each CSS bundle as a unique CSS resource, instead of referencing all the resources of the bundle.|  false
| jawr.debug.use.random.parameter | Boolean | The flag indicating if the random parameter must be added by default to all debug URL | true |
| jawr.gzip.on | Boolean | Enable the ability to serve gzipped resources to browsers that support it.| true |
| jawr.gzip.ie6.on | Boolean | Disable the serving of gzipped resources to Internet Explorer 6 or less.| true |
| jawr.use.generator.cache | Boolean | Define if we must use cache for the generated content. | true |
| jawr.use.smart.bundling | Boolean | Define if we must use the "smart bundling" feature (processing only modified bundle). | false |
| jawr.use.bundle.mapping | Boolean | Define if we must use the generated bundle mapping or not. | false |
| jawr.smart.bundling.delay.after.last.event | Integer | Defines the delay after the last event before starting the bundle processing (in second) | 2 |
| jawr.working.directory | String | Path to the jawr working directory. | *javax.servlet.context.tempdir*/jawrTmp |
| jawr.basecontext.directory | String | the path to the external base context directory where the resource can be found |	none |      
| jawr.basecontext.directory.high.priority | Boolean | the flag indicating if the resource must be searched in priority in the basecontext directory of not | false |
| jawr.config.reload | int.interval | The interval in seconds in which Jawr checks whether the configuration or the bundles have changed. If this value is set, when you change the properties file or a bundle file, Jawr will detect it and redeploy itself so you don't need to restart the server to test your changes. | none |
| jawr.config.reload.refreshKey| String | Force a refresh of all bundles or the ones detected as modified if in smart bundling mode. Hit any bundle URL and add ?refreshKey=value to reload the bundles. |  none |
| jawr.browser.resolver | String | Name of a class implementing net.jawr.web.resource.bundle.variant.VariantResolver. An instance of this class will be created and used to determine the Browser type to use for a given request. |   net.jawr.web.resource.bundle.variant.resolver.BrowserResolver |
| jawr.url.connection.type.resolver | String | Name of a class implementing net.jawr.web.resource.bundle.variant.VariantResolver. An instance of this class will be created and used to determine the connection type (standard or ssl) of a given request. | net.jawr.web.resource.bundle.variant.resolver.ConnectionTypeResolver
| jawr.css.skin.resolver | String   | Name of a class extending net.jawr.web.resource.bundle.variant.css.AbstractCssSkinResolver. An instance of this class will be created and used to determine the css skin used in a given request. | net.jawr.web.resource.bundle.variant.css.CssSkinResolver |
| jawr.locale.resolver | String  | Name of a class implementing net.jawr.web.resource.bundle.locale.LocaleResolver. An instance of this class will becreated and used to determine the Locale to use for a given request when using an [i18n messages generator](./messages_gen.html). | none |
| jawr.locale.generator.fallbackToSystemLocale | Boolean |The flag indicating if Jawr must use the System locale if no locale is found for a request. | true |
| jawr.locale.generator.quoteMsgKey | Boolean | The flag indicating if Jawr must use quote character for the messages keys in the generated javascript bundle. | false |
| jawr.locale.generator.resourceBundle.charset | String | The charset of the message ResourceBundle. | ISO-8859-1 |
| jawr.url.contextpath.override | URL (absolute or fragment) \[**http://...**\] ,\[**//somepath** protocol relative\],\[**/somepath** \] or \[**/**\] or \[\]. | Override the use of the application's contextpath to prefix every generated URL. Using '**/**' will cause Jawr to skip the context path altogether ('/001/bundles/mybundle.js'). Using an empty value will cause Jawr to generate page relative urls('001/bundles/mybundle.js'). | none |
| jawr.url.contextpath.ssl.override | URL (absolute or fragment) \[**https://...**\] ,\[**//somepath** protocol relative\],\[**/somepath** \] or \[**/**\] or \[\]. | For HTTPS request, override the use of the application's contextpath to prefix every generated URL. Using '**/**' will cause Jawr to skip the context path altogether ('/001/bundles/myb undle.js'). Using an empty value will cause Jawr to generate page relative urls ('001/bundles/mybu ndle.js'). | none |
| jawr.url.contextpath.override.used.in.debug.mode | Boolean | The flag indicating if we should retrieve the debug resources from the overridden context path or not. | false |
| jawr.factory.use.orphans.mapper | Boolean | Enable/disable auto scanning of non explicitly mapped files to auto-compress and to generate a one-file bundle out of each. | true |
| jawr.css.classpath.handle.image | Boolean | The flag indicating if the CSS images should be retrieved from the classpath, for the CSS defined in the classpath. | none |
| jawr.csslinks.flavor | String | Sets how the LINK tags are rendered by the style tag. See the [taglib docs.](./taglibs.html) | xhtml |
| jawr.dwr.mapping | String | Value of the servlet-mapping that points to a DWR servlet instance. See [DWR integration](../integration/dwr.html ). | none |
| jawr.strict.mode | Boolean | Enable/disable strict mode for bundle request. | False |
| jawr.illegal.bundle.request.handler | String | The class name of the handler for illegal bundle request. | net.jawr.web.servlet.IllegalBundleRequestHandlerImpl |
| jawr.bundle.hashcode.generator | String | The class name of the hashcode bundle generator or MD5 if you want to use the MD5 algorithm for the hashcode. | none |
| jawr.js.engine | String | The default JS engine to use by processors and generators, which are based on JS engine (Possible values are : mozilla.rhino, nashorn, ...) | mozilla.rhino |
| jawr.css.url.rewriter.context.path | String | The webapp context path. This property is used in the CSS URL rewriter to determine if an absolute path is in the web application or not. | none |
| jawr.css.postprocessor.base64ImageEncoder.encode.by.default | Boolean | Enable/disable the base64 image encoding by default | true |
| jawr.css.postprocessor.base64ImageEncoder.maxFileLength | Integer  | The maximum size (in bytes) of the image to encode in base64 | 30000 |
| jawr.css.postprocessor.base64ImageEncoder.encode.sprite | Boolean | Enable/disable the base64 image encode on generated sprite image.| False |
| jawr.css.postprocessor.cssmin.keepLicence | Boolean | Flag indicating if the licence comments (/\*! ... \*/) should be kept or not.| False |
| jawr.css.skin.type.mapping | String | The type of mapping for the skin directory structure. The acceptable values are : *skin\_locale* and *locale\_skin*. | skin\_locale |
| jawr.css.skin.default.root.dirs | String | The comma separated list of skin base directory. | none |
| jawr.css.skin.cookie | String | The name of the cookie where the current user CSS skin name is stored. | jawrSkin |
| jawr.jmx.mbean.prefix | String | The prefix for Jawr MBean. This can be useful for application which are deployed on different server using the same application context. | default |


### Basic JS/CSS servlet-specific properties

These properties are used to configure basic aspects of an instance of
the Jawr Servlet. There is a variant to each one, depending on whether
the servlet will be used for javascript or CSS resources.

| **Property name** | **Type** |  **Purpose**  | **Default value** |
|-------------------|----------|---------------|-------------------|
|  jawr.js.use.cache jawr.css.use.cache                                                                         |  Boolean                                                |   Toggle the use of a cache manager that will store and serve all the bundles directly from system memory.                                                                                                                                                                | true |
|  jawr.js.bundle.basedir jawr.css.bundle.basedir                                                               | URL fragment                                            |  This optional parameter tells Jawr to look for js or css files only in the specified directory, which may speed the starting time up a bit. The URL fragment denotes a path from the root of the WAR file ('/js', for instance).                                         | None |
|  jawr.js.bundle.factory.global.preprocessors jawr.css.bundle.factory.global.preprocessors                     |  Comma separated list.                                  |   List of global preprocessors to apply to every resource before the bundling process. Check the [global preprocessors manual](./global_preprocessors.html) for more info.                                                                                                | None |
|  jawr.js.bundle.factory.bundlepostprocessors jawr.css.bundle.factory.bundlepostprocessors                     |  Comma separated list.                                  |   List of postprocessors to apply to every generated bundle. Check the [postprocessors manual](./postprocessors.html) for more info.                                                                                                                                      | None |
|  jawr.js.bundle.factory.filepostprocessors jawr.css.bundle.factory.filepostprocessors                         |  Comma separated list.                                  |   List of postprocessors to apply to every resource before including it in a bundle. Check the [postprocessors manual](./postprocessors.html) for more info.                                                                                                              | None |
|  jawr.js.bundle.factory.composite.bundlepostprocessors jawr.css.bundle.factory.composite.bundlepostprocessors |  Comma separated list.                                  |   List of postprocessors to apply to every composite bundle. Check the [postprocessors manual](./postprocessors.html) for more info.                                                                                                                                      | None |
|  jawr.js.bundle.factory.composite.filepostprocessors jawr.css.bundle.factory.composite.filepostprocessors     |  Comma separated list.                                  |   List of postprocessors to apply to every resource before including it in a composite bundle. Check the [postprocessors manual](./postprocessors.html) for more info.                                                                                                    | None |
|  jawr.js.factory.use.singlebundle jawr.css.factory.use.singlebundle                                           |  Boolean                                                |   If true, all resources which don't belong to any defined bundle are joined onto a single bundle, as opposed to the default behavior which is to map each one to a sinle-file bundle.                                                                                    | false |
|  jawr.js.factory.singlebundle.bundlename jawr.css.factory.singlebundle.bundlename                             |  URL fragment. ('/script.js', 'bundles/script.js', etc.)|   When factory.use.singlebundle is set to true, this parameter defines the name of the bundle containing all orphans. It is mandatory to set this parameter when factory.use.singlebundle is set to true.                                                                 | None |
|  jawr.js.factory.use.dirmapper jawr.css.factory.use.dirmapper                                                 |  Boolean                                                |   If true, a bundle will be created automatically from every directory under the defined basedir (or the root dir if none is defined). These bundles contain every resource within (javascript or css, depending on the case). Each bundle has the name of its root dir.  | false |
|  jawr.js.factory.dirmapper.excluded jawr.css.factory.dirmapper.excluded                                       |  Comma separated list.                                  |   List of directories to exclude when using the dirmapper. You must exclude directory names for those resources already mapped explicitly to a bundle.                                                                                                                    | None |
|  jawr.js.bundle.link.renderer.class                                                                           |  String                                                 |   The JS link render class, which must implements net.jawr.web.resource.bundle.renderer.JsBundleLinkRenderer                                                                                                                                                              | net.jawr.web.resource.bundle.renderer.JavascriptHTMLBundleLinkRenderer |
|  jawr.css.bundle.link.renderer.class                                                                          |  String                                                 |   The CSS link render class, which must implements net.jawr.web.resource.bundle.renderer.CssBundleLinkRenderer                                                                                                                                                            | net.jawr.web.resource.bundle.renderer.CSSHTMLBundleLinkRenderer |

### JS/CSS bundle definition properties

This table is meant as a quick reference. To learn more about bundles definition, check the [bundle definitions tutorial page](./custom_bundles.html).

| **Property name** | **Type** | **Purpose** | **Default value** |
|-------------------|----------|-------------|-------------------|
|  jawr.\[type\].bundle.\[name\].id                     | URL fragment (ex: '/bundles/global.js')   | Defines a bundle by giving it a name (the \[name\] part of the property) and a path, which must be unique. For members of a composite, this property must NOT be defined. The bundle Id must follow the following pattern '/\[a-zA-Z0-9-\_/\]\* .((js)                                               | (css))' for example : /myBundles/bundle-tree12.js. The bundle ID must not starts with '/WEB-INF/' or '/META-INF/' | 
|  jawr.\[type\].bundle.\[name\].mappings               | Comma separated list.                     | Mapping of the resources to include in the bundle. Values can be either a directory path ('/someDir'), a directory path with wildcards to include subdirs ('/someDir/\*\*'), or a single resource path ('/js/foo.js'). It is mandatory for each custom bundle unless it is a child of a composite.   | None | 
|  jawr.\[type\].bundle.\[name\].global                 | Boolean                                   | Set whether a bundle is global or not. If it is, it will be included before any non-global bundle.                                                                                                                                                                                                    | false | 
|  jawr.\[type\].bundle.\[name\].order                  | Integer                                   | Sets precedence for global bundles, when you have more than one. Bundles with a lower value will be included in pages before bundles with a higher value.                                                                                                                                            | 0 | 
|  jawr.\[type\].bundle.\[name\].dependencies           | Comma separated list.                     | This property sets the names of bundles, on which the bundle depends. If you include your bundle in a page, for each name in this list, Jawr will include the bundle associated if it's not already included. This property is not allowed for global bundles                                        | None | 
|  jawr.\[type\].bundle.\[name\].composite              | Boolean                                   | Set whether a bundle is a composite or not. If it is, the mappings attribute must not be set and instead, the child.names property will be used.                                                                                                                                                      | false | 
|  jawr.\[type\].bundle.\[name\].child.names            | Comma separated list.                     | For a composite bundle, this property sets the names of bundles that must be defined in further properties. For each name in this list you must specify properties for a child bundle that must have no id and cannot be global.                                                                     | None | 
|  jawr.\[type\].bundle.\[name\].bundle.prefix          | String                                    | Defines the prefix for the bundle, which will be preprend before the bundle hashcode. The generated bundle URL would look like /*app ctx*/pub/gzip\_xxxxxxxxxx/path\_to\_bundle if **pub** is defined as bundle prefox. This can be helpful to define security filters to access bundles             | None | 
|  jawr.\[type\].bundle.\[name\].productionURL          | URL string                                | Defines a URL to use when this bundle is invoked under production mode. This is helpful when using publicly served libraries such as YUI.                                                                                                                                                            | None | 
|  jawr.\[type\].bundle.\[name\].bundlepostprocessors   | Comma separated list.                     | Overrides the global bundles postprocessor.                                                                                                                                                                                                                                                          | None | 
|  jawr.\[type\].bundle.\[name\].filepostprocessors     | Comma separated list.                     | Overrides the global file postprocessor.                                                                                                                                                                                                                                                             | None | 
|  jawr.\[type\].bundle.\[name\].debugonly              | Boolean                                   | If set to true, the bundle will only be included in pages when development mode is on. Useful for debugging script such as console writers.                                                                                                                                                          | false | 
|  jawr.\[type\].bundle.\[name\].debugnever             | Boolean                                   | If set to true, the bundle will never be included in pages when development mode is on. Useful for empty debugging script replacements (faux console writers, for instance).                                                                                                                         | false | 
|  jawr.\[type\].bundle.\[name\].ieonly.condition       | IE expression                             | If set, the bundle will be included within a conditional comment for internet explorer. The value must correspond to a conditional comment selector expression (such as 'if lt IE 6').                                                                                                               | None | 
 
### Image Resources definition properties

Jawr is also able to manage image resources. A dedicated servlet is used
to handle them.

| **Property name** | **Type** | **Purpose** | **Default value** |
|-------------------|----------|-------------|-------------------|
|  jawr.binary.resources                 | String   | The comma separated list of binary web resources, whose the hashcode will be calculated at Jawr Binary servlet startup. If a binary resource is not defined here, the hashcode will be calculated at runtime and put in cache.   | None | 
|  jawr.binary.hash.algorithm            | String   | The hash algorithm to use for the binary resource (images, font, ...). Two values are possible : CRC32 and MD5                                                                                                                   | CRC32 | 
|  jawr.img.bundle.link.renderer.class   | String   | The image link render class, which must implements net.jawr.web.resource.bundle.renderer.image.ImgRenderer                                                                                                                       | net.jawr.web.resource.bundle.renderer.image.ImgHTMLRenderer | 
 
### JS/CSS custom global preprocessor definition properties

| **Property name** | **Type** | **Purpose** | **Default value** |
|-------------------|----------|-------------|-------------------|
| jawr.custom.global.preprocessor.\[name\].class   | String     | Qualified classname of the global preprocessor for JS resources to later be referenced with the id \[name\]. | None |


### JS/CSS custom bundle postprocessor definition properties

| **Property name** | **Type** | **Purpose** | **Default value** |
|-------------------|----------|-------------|-------------------|
|  jawr.custom.postprocessors.\[name\].class   | String     | Qualified classname of the postprocessor to later be referenced with the id \[name\].   | None |

### JS/CSS custom generators definition properties

| **Property name** | **Type** | **Purpose** | **Default value** |
|-------------------|----------|-------------|-------------------|
| jawr.custom.generators   | Comma separated list.   |List of all the ResourceGenerator implementations you want to use.   | None |


### JS/CSS custom resolvers definition properties

| **Property name** | **Type** | **Purpose** | **Default value** |
|-------------------|----------|-------------|-------------------|
|  jawr.custom.resolvers   | Comma separated list.  | List of all the VariantResolver implementations you want to use.  | None |

### Deprecated properties

These properties are no longer used in the current version of Jawr.

|  **Property name** | **Type**   | **Purpose** | **Default value** | **Deprecated at version**  |
|--------------------|------------|-------------|-------------------|----------------------------|
|  jawr.js.commonURLPrefix jawr.css.commonURLPrefix  | URL fragment ('/V001', for instance). The prefixes '/static' and '/gzip\_' are reserved, so you can't use any prefix starting with either string. The prefix can't denote more than one path name ('/foo/bar' would be wrong, for instance). | Version prefix to append to all the URLs generated by the tags in the pages. Its purpose is to keep clients from using older cached versions of your bundles when a new one is deployed. It can be overridden by individual bundle definitions. This prefix is mandatory, Jawr will not start up without it.                                                                    | None               | 2.0 | 
|  jawr.\[type\].bundle.\[name\].prefix              | URL fragment ('/V001', for instance).                                                                                                                                                                                                        | Overrides the globalPrefix attribute for individual bundles, for increased control over client caching of versions. It has the same constraints as globalPreffix (prefixes starting with '/static' and '/gzip\_' are not allowed)                                                                                                                                             | None               | 2.0 | 
|  jawr.js.bundle.names jawr.css.bundle.names        | Comma separated list.                                                                                                                                                                                                                        | List of all the bundles you wish to define in the descriptor. For every item in this list, you must define bundle properties in the form 'jawr.\[type\].bundle.\[name\].\[property\]', where \[type\] is either js or css, \[name\] is the name defined in this list, and \[property\] is any of the bundle parameters defined below.                                         | None               | 2.7 | 
|  jawr.custom.postprocessors.names                  | Comma separated list.                                                                                                                                                                                                                        | List of all the custom postprocessors you wish to define in the descriptor. For every item in this list, you must define the postprocessor implementation class as defined below. Afterwards, you may use the names defined in this list to define postprocessor chains, as in: *jawr.js.bundle.factory.bundlepostprocessors=JSMin,\[myUserDefinedPostprocessor\],license*.   | None               | 2.8 | 
|  jawr.css.imagepath.override                       | String                                                                                                                                                                                                                                       | Value to prepend to normalized css urls. So css background: url(../../img/bkrnd/bg.gif); will = background: url(\${jawr.css.imagepath.override}img/bkrnd/bg.gif);                                                                                                                                                                                                             | None               | 3.0 | 
|  jawr.js.bundle.\[name\].locales                   | Comma separated list.                                                                                                                                                                                                                        | Defines the locale variants to generate for this bundle, which will be available whenever a locale resolver returns a key matching any of the specified values.                                                                                                                                                                                                               | None               | 3.0 | 
|  jawr.css.classpath.handle.image                   | Boolean                                                                                                                                                                                                                                      | The flag indicating if the CSS images should be retrieved from the classpath, for the CSS defined in the classpath.                                                                                                                                                                                                                                                           | none               | 3.2 not supported since 3.5 | 
|  factory.use.orphans.mapper                        | Boolean                                                                                                                                                                                                                                      | Enable/disable auto scanning of non explicitly mapped files to auto-compress and to generate a one-file bundle out of each.                                                                                                                                                                                                                                                   | true               | 3.2.1 | 
|  jawr.image.resources                              | String                                                                                                                                                                                                                                       | The comma separated list of images, whose the hashcode will be calculated at Jawr image servlet startup. If an image is not defined here, the hashcode will be calculated at runtime and put in cache.                                                                                                                                                                        | None               | Not supported since 3.6 (Please use *jawr.binary.resources* instead) | 
|  jawr.image.hash.algorithm                         | String                                                                                                                                                                                                                                       | The hash algorithm to use for the images. Two values are possible : CRC32 and MD5                                                                                                                                                                                                                                                                                             | CRC32              | Not supported since 3.6 (Please use *jawr.binary.hash.algorithm* instead) | 
 
