
Using DWR with Jawr
-------------------

[DWR](http://directwebremoting.org/dwr/index.html)
(Direct Web Remoting) is a java library which allows Javascript in a
browser to interact with Java on a server and helps you manipulate web
pages with the results.

DWR works by setting up one or several servlets which serve javascript
and also convert AJAX requests to java class method invocations. The
javascript served by DWR is either one of several static .js files or a
generated javascript stub that allows method calls to be routed to the
server via AJAX.

DWR already has a method to minify and compress javascript, but Jawr
adds extra functionality so that you serve your resources more
consistently. Specifically, with JAWR you can serve all the static .js
files in one bundle. You can also combine these with other scripts. For
instance, you could have a javascript bundle containing Prototype.js,
script.aculo.us, your own infrastructure and the DWR static scripts.

Also, you can bundle together the generated javascript stubs which
normally must be requested separately. For pages that use several remote
objects, this can speed up loading.

Finally, using this approach you have a consistent way of serving all
javascript, instead of using different methods to serve DWR scripts or
Jawr bundles.

Jawr supports DWR 2.x using the *jawr-dwr2.x-extension* extension
module. Maven users can add a dependency as the following:

            
        <dependency>
          <groupId>net.jawr.extensions</groupId>
          <artifactId>jawr-dwr2.x-extension</artifactId>
          <version>3.8</version>
        </dependency>
        

There is a sample application that you can download to check out how
integration with DWR works, which you can download from [this link](https://svn.java.net/svn/jawr~svn/extensions/dwr2.x/trunk/jawr-dwr2.x-webapp-sample).

Jawr supports also DWR 3.x using the *jawr-dwr3.x-extension* extension
module. Maven users can add a dependency as the following:


        <dependency>
          <groupId>net.jawr.extensions</groupId>
          <artifactId>jawr-dwr3.x-extension</artifactId>
          <version>3.8</version>
        </dependency>
        


### Set up

The jawr servlet must be started after the DWR one. To make sure this
order is followed, you must set up the *load-on-startup* parameter so
that the value for the Jawr servlet is greater than the one for the DWR
servlet:


            <servlet>
               <servlet-name>dwr-invoker</servlet-name>
               <display-name>DWR Servlet</display-name>
               <description>Direct Web Remoter Servlet</description>
               <servlet-class>org.directwebremoting.servlet.DwrServlet</servlet-class>
               ...
               <load-on-startup>1</load-on-startup> <-- Loads first 
            </servlet>
      
            <servlet>
                    <servlet-name>JavascriptServlet</servlet-name>
                    <servlet-class>net.jawr.web.servlet.JawrServlet</servlet-class>
               ...
               <load-on-startup>2</load-on-startup> <-- Loads after DWR
            </servlet>
            


Jawr needs to know the servlet mapping that points to DWR. Therefore, in
the properties config you must set up the **jawr.dwr.mapping** parameter
to the same value (without the wildcard) that you use for the DWR
servlet:


      Web.xml: 
      <servlet-mapping>
        <servlet-name>dwr-invoker</servlet-name>
        <url-pattern>/dwr/*</url-pattern>
      </servlet-mapping>
      
      Jawr.properties:
      jawr.dwr.mapping=/dwr/
      

#### Additional DWR servlet instances

If you declare more than one DWR servlet, Jawr will need to access the
additional mappings for these. In this case, for every additional
servlet you must add a special init-param, named **jawr\_mapping**. The
value, again, must be the servlet-mapping used for the servlet, without
the wildcard. Note that this init-param is added to the DWR servlet
config, **not** the Jawr servlet config.


       <servlet>
        <servlet-name>additional-dwr-invoker</servlet-name>
        <display-name>DWR Servlet</display-name>
        <description>Direct Web Remoter Servlet</description>
        <servlet-class>org.directwebremoting.servlet.DwrServlet</servlet-class>
        ...
        <init-param>
                <param-name>jawr_mapping</param-name>
                <param-value>/other/dwr/</param-value> <-- Matches servlet-mapping
        </init-param>
        ...
       </servlet>
      <servlet-mapping>
        <servlet-name>additional-dwr-invoker</servlet-name>
        <url-pattern>/other/dwr/*</url-pattern> <-- Use in jawr_mapping
      </servlet-mapping>
       

You need to do this for additional DWR servlets, but you can skip it for
the one to which mapping you point in the **jawr.dwr.mapping** param.
You can't skip using this param though, since it acts as a flag to Jawr
indicating that DWR integration is on.


### DWR generator and JS link renderer :

A specific generator is used for dwr resources (check below for more
information on dwr resources). Prior to the 3.5 version, this generator
was a built-in generator, now users must declare it specifically in
their jawr configuration file. To render the DWR bundles properly, the
declaration of a specific DWR JS bundle link renderer is needed.

For dwr 2.x, users will need to use the following configuration :


            # Configure DWR generator and DWR link renderer
            jawr.custom.generators=net.jawr.web.resource.bundle.generator.dwr.DWRBeanGenerator
            jawr.js.bundle.link.renderer.class=net.jawr.web.resource.bundle.renderer.DWRJsBundleLinkRenderer


For dwr 3.x, users will need to use the following configuration :


            # Configure DWR generator and DWR link renderer
            jawr.custom.generators=net.jawr.web.resource.bundle.generator.dwr.DWR3BeanGenerator
            jawr.js.bundle.link.renderer.class=net.jawr.web.resource.bundle.renderer.DWRJsBundleLinkRenderer


### Maping DWR resources in bundles

Once you have performed setup, you can start using DWR beans in your
bundle mappings. All DWR resources are mapped using the special prefix
'**dwr:**', which is the prefix for a specific generator. As stated
before, DWR has several static resources that you will probably want to
bundle with the rest of your javascript infrastructure code (Prototype,
YUI or whichever, etc). You map to these using several special keywords:

|  **Keyword**      |**DWR script** |
|  -----------------|---------------------------------------|
| dwr:\_engine      |engine.js |
| dwr:\_util        |util.js |
| dwr:\_auth        |auth.js |
| dwr:\_actionutil  |DWRActionUtil.js (webwork integration) |
| 

The engine.js script should always be included first since others depend
on it. Note that this file is not completely static, since DWR generates
some variables depending on the user's request. This is not optimal,
since users will get a new copy on every new session with the
application. Jawr solves this by generating these parameters in a very
small inline script that contains only the dynamic variables.

So in a typical use case, you would define your DWR generator, a
specific Javascript LinkRenderer and add these to a global bundle:


            # Configure DWR generator and DWR link renderer
            jawr.custom.generators=net.jawr.web.resource.bundle.generator.dwr.DWRBeanGenerator
            jawr.js.bundle.link.renderer.class=net.jawr.web.resource.bundle.renderer.DWRJsBundleLinkRenderer

            # Configure DWR bundle
            jawr.js.bundle.global.id=/bundles/global.js
            jawr.js.bundle.global.mappings=/js/protaculous/**, dwr:_engine, dwr:_util
            jawr.js.bundle.global.global=true


To add DWR beans (the javascript stubs to access java objects in the
server), you use the **dwr:** prefix followed by the name you specified
in the dwr.xml config file (or in the alternate configuration method of
your choice).

For instance, the following mapping adds two of these beans to a bundle:


        dwr.xml: 
        <create creator="new" javascript="People" scope="script"> <-- Use the value of the 'javascript' attribute
          <param name="class" value="org.getahead.dwrdemo.people.People"/>
        </create>
            <create creator="new" javascript="LiveHelp" scope="application">
          <param name="class" value="org.getahead.dwrdemo.livehelp.LiveHelp"/>
        </create>
        
        jawr.config:
        jawr.js.bundle.mybundle.mappings=dwr:People, dwr:LiveHelp
            


Note that Jawr does not support having several DWR servlets that map to
beans with the same names. You can't declare two DWR servlets with a
bean named 'Person', for instance.

One last thing you can do is to bundle **all** dwr beans into one
bundle. To do this, tou use the special keyword **dwr:\_\*\***:


       This adds all beans defined in dwr.xml: 
       jawr.js.bundle.alldwrbeans.mappings=dwr:_**
            

Be aware that this mapping does not include the static DWR scripts, only
the bean stubs. Here is an example of an all-in-one DWR bundle:


            jawr.js.bundle.dwrall.id=/bundles/dwrall.js
            jawr.js.bundle.dwrall.mappings=dwr:_engine, dwr:_util,dwr:_auth, dwr:_**        

