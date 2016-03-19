Jawr Ant task
-------------

The goal of the Jawr Ant task is to simplify the use of the bundle preprocessing for projects which are using Ant. If you have not read the documentation of the bundle preprocessing, please take a look at it before continuing.


### Requirements

The Jawr Ant task requires jawr-bundle-processor jar file.


### Usage

The Jawr Ant task uses the following properties:

-   **rootPath** The path to the root of the web application.
-   **tempDirPath** The path to the temporary directory for JAWR.
-   **destDirPath** The path to the destination directory.
-   **servletsToInitialize** The comma separated list of servlets name
    to initialize
-   **springConfigFiles** the comma separated list of spring
    configuration files, which will be used to initialize the Jawr
    spring controller. This property accepts any spring resource
    location definition like classpath:..., file:..., etc
-   **generateCDNFiles** The flag indicating if we should generate the
    CDN files or not Default value: true
-   **keepUrlMapping** The flag indicating if we want to keep the jawr
    URL mapping or if we rewrite it to remove resource hashcode. Default
    value: false


                <!-- Definition of the Jawr bundle task -->
                <taskdef name="jawrBundle" classname="net.jawr.ant.BundleTask">
                </taskdef>
                
                ...
                
                <jawrBundle rootPath="${webapp.dir}" tempDirPath="${temp.dir}/jawrFinal/" 
                                        destDirPath="${temp.dir}/jawrTemp/" />
                                        


   For the spring MVC projects, you can use:


                <!-- Definition of the Jawr bundle task -->
                <taskdef name="jawrBundle" classname="net.jawr.ant.BundleTask">
                </taskdef>
                
                ...
                
                <jawrBundle rootPath="${webapp.dir}" tempDirPath="${temp.dir}/jawrFinal/" 
                                        destDirPath="${temp.dir}/jawrTemp/" springConfigFiles="classpath:/spring-jawrConfig.xml,/WEB-INF/jawr-controllers.xml"/>
                                        

### Classpath issue

As you know, the bundle processor fakes the server startup. In the
current implementation, the bundle processor will add the WEB-INF/lib
directory in the classpath. But you could face some issue, if you are
using some jar files, which are define in the application server itself.

To resolve this issue, you can use the classpath property of the task definition like below:


            
            <!-- Definition of the Jawr bundle classpath -->
            <path id="jawr.build.class.path">
                    <pathelement path="${temp.dir.classes}"/>
                    <fileset dir="lib">
                            <include name="**/*.jar" />
                    </fileset>
                    <pathelement path="${webapp.container.dir}/shared/classes}"/>
                    <fileset dir="${webapp.container.dir}/shared" >
                            <include name="**/*.jar" />
                    </fileset>
                    <pathelement path="${webapp.container.dir}/common/classes}"/>
                    <fileset dir="${webapp.container.dir}/common" >
                            <include name="**/*.jar" />
                    </fileset>
            </path>
            
            <!-- Definition of the Jawr bundle task -->
            <taskdef name="jawrBundle" classname="net.jawr.ant.BundleTask">
                    <classpath refid="jawr.build.class.path" />
            </taskdef>
            
            ...
            
            <!-- PACKAGE -->
            <target name="package" depends="compile">
            ...
                    <!-- Call the Jawr bundling process -->
                    <jawrBundle rootPath="${webapp.dir}" tempDirPath="${temp.dir}/jawrFinal/" 
                                    destDirPath="${temp.dir}/jawrTemp/" />
            ...
            </target>                       
