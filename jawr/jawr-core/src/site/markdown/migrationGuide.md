Introduction
------------


The version 3.5 and 3.6 are not backward compatible.  
We've tried to make this transition as simple as possible.  
You will find below a migration guide to migrate from 3.5.x to 3.6.

-------------------------------------------------------------------


### Migration Guide from 3.5.x to 3.6

-   Image resource handling :

   Before the 3.6 version, Jawr used a specific type for image handling (img).
   Since 3.6, Jawr introduces a more general binary handler component,
    which can manages every binary resources (images, fonts, ...).  
    So you will have to update your web.xml file like below.

   Here is an example of a web.xml configuration before the 3.6 version :

                                  
                <servlet>
                        <servlet-name>JawrImgServlet</servlet-name>
                        <servlet-class>net.jawr.web.servlet.JawrServlet</servlet-class>
                        <init-param>
                                <param-name>configLocation</param-name>
                                <param-value>/jawr.properties</param-value>
                        </init-param>
                        <!-- init-param>
                                <param-name>mapping</param-name>
                                <param-value>/jwrImg/</param-value>
                        </init-param -->
                        <init-param>
                                <param-name>type</param-name>
                                <param-value>binary</param-value>
                        </init-param>
                        <!-- Start Jawr Img servlet before Jawr CSS servlet -->
                        <load-on-startup>1</load-on-startup>
                </servlet>
                                

  
   You'll have to update the **type** init-parameter from **img** to
    **binary**.

                                  
                <servlet>
                        <servlet-name>JawrBinaryServlet</servlet-name>
                        <servlet-class>net.jawr.web.servlet.JawrServlet</servlet-class>
                        <init-param>
                                <param-name>configLocation</param-name>
                                <param-value>/jawr.properties</param-value>
                        </init-param>
                        <!-- init-param>
                                <param-name>mapping</param-name>
                                <param-value>/jwrBin/</param-value>
                        </init-param -->
                        <init-param>
                                <param-name>type</param-name>
                                <param-value>binary</param-value>
                        </init-param>
                        <!-- Start Jawr Binary servlet before Jawr CSS servlet -->
                        <load-on-startup>1</load-on-startup>
                </servlet>
                                

  
   It's important to note that for previous version, this servlet
    should be started before the CSS one.

-   Jawr configuration

   There are 2 properties which have been renamed :

   -   The property **jawr.image.hash.algorithm** has been renamed to
        **jawr.binary.hash.algorithm**.
   -   The property **jawr.image.resources** has been renamed to
        **jawr.binary.resources**.
-   Advanced development :
    -   Image Resource Handler :

   Jawr used internally a specific handler for image :         **net.jawr.web.resource.ImageResourcesHandler**.
   This class has been refactored to **net.jawr.web.resource.BinaryResourcesHandler**.  
   To retrieve this handler, Jawr used below code :

                                    
                    ImageResourcesHandler imgRsHandler = (ImageResourcesHandler) servletContext.getAttribute(
                                                            JawrConstant.IMG_CONTEXT_ATTRIBUTE);
                                    

   Now, you'll have to use :

                    BinaryResourcesHandler binaryRsHandler = (BinaryResourcesHandler) servletContext.getAttribute(
                                                            JawrConstant.BINARY_CONTEXT_ATTRIBUTE);
                                    

   One method has been renamed :


| **Old method name in ImageResourcesHandler** | **New method name in ImageResourcesHandler** |
|  public Map*String, String* getImageMap()    |  public Map*String, String* getBinaryPathMap() |

   -   JMX

   Before the 3.6 version, the MBean which handled images was named
        **imgMBean**. It has been renamed to **binaryMBean**.


### Migration Guide from 3.3.x to 3.5


It is important to note that the 3.5 version requires at least the use of java 6 and servlet API 2.5, while the 3.3.x required at least the use of java 1.4 and servlet API 2.3

-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

-   Jawr Project modules

   For better maintainability, the Jawr project has been divided in
    multiple modules.

   Since, the version 3.5, Jawr has the following modules :

   - *jawr-core* : The Jawr core module 
   - *jawr-wicket-extension* : The Jawr wicket extension module 
   - *jawr-spring-2.0.x-extension* : The Jawr spring 2.0.x extension module 
   - *jawr-dwr2.x-extension* : The Jawr DWR 2.x extension module 
   - *jawr-dwr3.x-extension* : The jawr DWR 3.x extension module 
   - *jawr-grails-extension* : The jawr grails extension module

   *jawr-core* : The jawr core module

   Prior to the 3.5 version, the user used the jawr.jar (maven
    reference net.jawr:jawr-3.3.jar). Now the pom reference has changed.
    To used the new version, you need to replace the old jawr dependency
    by the following one :

                                   
                                <dependency>
                                        <groupId>net.jawr</groupId>
                                        <artifactId>jawr-core</artifactId>
                                        <version>3.5</version>
                                </dependency>
                                

   
   Now to use the extensions, you'll need to add them in your
    dependencies (See below for more detail)

   *jawr-wicket-extensions* : The jawr wicket extension module

   This module allows the integration of Jawr with Wicket. Please check
    the [wicket integration documentation](./integration/wicket.html)
    for more detail. The wicket version supported is 6.x versions. Here
    is the dependency to add to your pom to add this module.

   
                                
                                <dependency>
                                        <groupId>net.jawr.extensions</groupId>
                                        <artifactId>jawr-wicket-extension</artifactId>
                                        <version>3.5</version>
                                </dependency>
                                

   
   *jawr-spring-2.0.x-extension* : The jawr spring 2.0.x integration
    module

   This module allows the integration of jawr with spring 2.0.x. Please
    check the [spring integration
    documentation](./integration/spring.html) for more detail.
    Unfortunately for the time being, there is no support for spring
    2.5.x and spring 3.x. This will probably be done in a
    future release. If someone wants to help, he is welcome. Here is the
    dependency to add to your pom to add this module.


                                <dependency>
                                        <groupId>net.jawr.extensions</groupId>
                                        <artifactId>jawr-spring-2.0.x-extension</artifactId>
                                        <version>3.5</version>
                                </dependency>
                                


   *jawr-dwr-2.x-extension* : The jawr dwr 2.x integration module

   This module allows the integration of jawr with dwr 2.x. Please
    check the [dwr integration documentation](./integration/dwr.html)
    for more detail. Unfortunately for the time being, there is no
    support for dwr 3.x. This will probably be done in a future release.
    If someone wants to help, he is welcome. Here is the dependency to
    add to your pom to add this module :

                                
                                <dependency>
                                        <groupId>net.jawr.extensions</groupId>
                                        <artifactId>jawr-dwr-2.x-extension</artifactId>
                                        <version>3.5</version>
                                </dependency>
                                


   *jawr-grails-extension* : The jawr grails integration module

   This module allows the integration of jawr with grails. Please check
    the [grails integration documentation](./integration/grails.html)
    for more detail. Here is the dependency to add to your pom to add
    this module :

                                
                                <dependency>
                                        <groupId>net.jawr.extensions</groupId>
                                        <artifactId>jawr-grails-extension</artifactId>
                                        <version>3.5</version>
                                </dependency>
                                

-   Generators

   Since the version 3.5, Jawr allows the users to define their
    custom GeneratorPathResolver. This means that the users can now
    define generators, which will be triggered by prefix path (like
    built-in 'jar:' or 'message:' prefixes) or with for example a
    specific path suffix (like '\*.less' resources).

   In the version 3.3.x, the generator should implement the
    method "getMappingPrefix". This method has been replaced by
    "ResourceGeneratorResolver getResolver()" method. Jawr defines
    built-in ResourceGeneratorResolvers :

   -   net.jawr.web.resource.bundle.generator.resolver.PrefixedPathResolver
        : For prefix path resolver 
   -   net.jawr.web.resource.bundle.generator.resolver.SuffixedPathResolver
        : For suffix path resolver

   To replace the following your custom generator, you need to replace

   
                                                        public class SampleJsGenerator extends AbstractJavascriptGenerator {

                                                                /* (non-Javadoc)
                                                                 * @see net.jawr.web.resource.bundle.generator.ResourceGenerator#getResolver()
                                                                 */
                                                                public String getMappingPrefix() {
                                                                        return "foo";
                                                                }

                                                            ...
                                                        }

   
   With the following :

   
                                                        public class SampleJsGenerator extends AbstractJavascriptGenerator {

                                                                /** The resolver */
                                                                private ResourceGeneratorResolver resolver;


                                                                public SampleJsGenerator(){
                                                                
                                                                        resolver = new PrefixedPathResolver("foo");
                                                                }
                                                                
                                                                /* (non-Javadoc)
                                                                 * @see net.jawr.web.resource.bundle.generator.ResourceGenerator#getResolver()
                                                                 */
                                                                public ResourceGeneratorResolver getResolver() {
                                                                        return resolver;
                                                                }

                                                            ...
                                                        }

   
-   ResourceBundlePathsIterator :

   Jawr use internally an object to iterate over resource bundle path.  
    The signature of the following method has changed :

| **Old method name in ResourceBundlePathsIterator** | **New method name in ResourceBundlePathsIterator** |
| public String nextPath()  | public BundlePath nextPath() |


-   JSF namespace :

   From version 3.3.x to 3.5 the jsf namespace has changed.

   Namespace until 3.3.x was :

                xmlns:jawr="https://jawr.dev.java.net/jsf/facelets"


   Namespace since 3.5 version is :

    
                xmlns:jawr="https://jawr.java.net/jsf/facelets"

    
