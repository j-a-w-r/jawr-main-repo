// locations to search for config files that get merged into the main config;
// config files can be ConfigSlurper scripts, Java properties files, or classes
// in the classpath in ConfigSlurper format

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if (System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

grails.project.groupId = appName // change this to alter the default package name and Maven publishing destination

// The ACCEPT header will not be used for content negotiation for user agents containing the following strings (defaults to the 4 major rendering engines)
grails.mime.disable.accept.header.userAgents = ['Gecko', 'WebKit', 'Presto', 'Trident']
grails.mime.types = [ // the first one is the default format
    all:           '*/*', // 'all' maps to '*' or the first available format in withFormat
    atom:          'application/atom+xml',
    css:           'text/css',
    csv:           'text/csv',
    form:          'application/x-www-form-urlencoded',
    html:          ['text/html','application/xhtml+xml'],
    js:            'text/javascript',
    json:          ['application/json', 'text/json'],
    multipartForm: 'multipart/form-data',
    rss:           'application/rss+xml',
    text:          'text/plain',
    hal:           ['application/hal+json','application/hal+xml'],
    xml:           ['text/xml', 'application/xml']
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// Legacy setting for codec used to encode data with ${}
grails.views.default.codec = "html"

// The default scope for controllers. May be prototype, session or singleton.
// If unspecified, controllers are prototype scoped.
grails.controllers.defaultScope = 'singleton'

// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside ${}
                scriptlet = 'html' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        // filteringCodecForContentType.'text/html' = 'html'
    }
}


grails.converters.encoding = "UTF-8"
// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart=false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false

// configure passing transaction's read-only attribute to Hibernate session, queries and criterias
// set "singleSession = false" OSIV mode in hibernate configuration after enabling
grails.hibernate.pass.readonly = false
// configure passing read-only to OSIV session by default, requires "singleSession = false" OSIV mode
grails.hibernate.osiv.readonly = false

environments {
    development {
        grails.logging.jul.usebridge = true
    }
    production {
        grails.logging.jul.usebridge = false
        // TODO: grails.serverURL = "http://www.changeme.com"
    }
}

// log4j configuration
log4j.main = {
    // Example of changing the log pattern for the default console appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

    error  'org.codehaus.groovy.grails.web.servlet',        // controllers
           'org.codehaus.groovy.grails.web.pages',          // GSP
           'org.codehaus.groovy.grails.web.sitemesh',       // layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping',        // URL mapping
           'org.codehaus.groovy.grails.commons',            // core / classloading
           'org.codehaus.groovy.grails.plugins',            // plugins
           'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'
		   
	debug	'net.jawr'
}

// Common properties
jawr.debug.on=true
jawr.gzip.on=false
jawr.charset.name='UTF-8'
jawr.use.bundle.mapping=false
jawr.factory.use.orphans.mapper=false
jawr.debug.ie.force.css.bundle=true
jawr.strict.mode=false
jawr.bundle.hashcode.generator=MD5

// Custom Generator
jawr.custom.generators='net.jawr.web.resource.bundle.locale.GrailsResourceBundleMessagesGenerator,net.jawr.resource.generator.SampleImageGenerator'

// Custom Post processors
jawr.custom.postprocessors.sample.class='net.jawr.resource.postprocessor.SamplePostProcessor'
jawr.custom.postprocessors.sample2.class='net.jawr.resource.postprocessor.SamplePostProcessor2'

// Javascript properties and mappings
jawr.js.bundle.basedir='/js/'

jawr.js.bundle.one.id='/js/bundle/main.js'
jawr.js.bundle.one.mappings='/js/global/**,/js/index/'

jawr.js.bundle.two.id='/js/bundle/msg.js'
jawr.js.bundle.two.mappings='messages:grails-app.i18n.messages[ui]'

jawr.js.bundle.common.id='/js/common.js'
jawr.js.bundle.common.mappings='/js/yui/yahoo-dom-event/yahoo-dom-event.js,/js/yui/element/element.js,/js/yui/tabview/tabview.js,/js/yui/container/container.js,skinSwitcher:switcher.js'

// CSS properties and mappings

// Comment the following line to disable the sprite generation or if you are running the application with a Java 1.4
jawr.css.bundle.factory.global.preprocessors='smartsprites'

jawr.css.skin.default.root.dirs='/css/themes/oceanBlue/en_US'
jawr.csslinks.flavor='html'

jawr.css.bundle.commonLayout.id='/css/commonLayout.css'
jawr.css.bundle.commonLayout.mappings='/css/main.css,css/mobile.css'

jawr.css.bundle.common.id='/css/common.css'
jawr.css.bundle.common.mappings='/js/yui/fonts/fonts-min.css,skin:/css/themes/oceanBlue/en_US/theme.css,skin:/css/themes/oceanBlue/en_US/tabview.css,skin:/css/themes/oceanBlue/en_US/container.css'
jawr.css.bundle.common.filepostprocessors='csspathrewriter'

jawr.css.bundle.specific.id='/css/specific.css'
jawr.css.bundle.specific.mappings='jar:fwk/css/temp.css,/css/one.css'
jawr.css.bundle.specific.filepostprocessors='base64ImageEncoder'
jawr.css.bundle.specific.bundlepostprocessors='cssminify,base64ImageEncoder'

jawr.css.classpath.handle.image=true
jawr.binary.hash.algorithm='MD5'

