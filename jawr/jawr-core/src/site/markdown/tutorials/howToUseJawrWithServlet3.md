How to use Jawr with Servlet 3 API
------------

### Introduction

Since Java EE6, users can define there application using Servlet 3 API.
This allows in particular to create servlet programmatically by moving
configuration from web.xml file to a configuration through the code.  
Here we will just present how to instanciate the **Jawr** servlets using
the Servlet 3 API.

You need first to make sure that you have a dependency to
**javax.servlet-api** in your project.  
For maven user, you should have something like this in your POM file:


            <dependency>
                    <groupId>javax.servlet</groupId>
                    <artifactId>javax.servlet-api</artifactId>
                    <version>3.0.1</version>
                    <scope>provided</scope>
            </dependency>


### Create your WebListener

You'll need to create the class which will handle the Jawr servlet
initialisation, and which implements
**javax.servlet.ServletContextListener**.  

Here is an example :

    package net.jawr.web;

    import javax.servlet.ServletContext;
    import javax.servlet.ServletContextEvent;
    import javax.servlet.ServletContextListener;
    import javax.servlet.ServletRegistration;
    import javax.servlet.annotation.WebListener;

    /**
     * The web application initializer
     */
    @WebListener
    public class WebApplicationInitializer implements ServletContextListener {

        /*
             * (non-Javadoc)
             * 
             * @see
             * javax.servlet.ServletContextListener#contextInitialized(javax.servlet
             * .ServletContextEvent)
             */
            @Override
            public void contextInitialized(ServletContextEvent evt) {
                    // TODO
            }

            /*
             * (non-Javadoc)
             * 
             * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.
             * ServletContextEvent)
             */
            @Override
            public void contextDestroyed(ServletContextEvent evt) {
                    // TODO
            }
            


### Configure instances of the Jawr Servlet

Use the Servlet 3 API as followed to create your servlet configuration.  
As in web.xml, you'll need to ensure that the **binary** servlet is
instanciated before the CSS one, by using the **setLoadOnStartup**
method.

            /*
             * (non-Javadoc)
             * 
             * @see
             * javax.servlet.ServletContextListener#contextInitialized(javax.servlet
             * .ServletContextEvent)
             */
            @Override
            public void contextInitialized(ServletContextEvent evt) {
                    ServletContext sc = evt.getServletContext();

                    // Initialize Jawr JS servlet
                    ServletRegistration.Dynamic sr = sc.addServlet("JavascriptServlet",
                                    "net.jawr.web.servlet.JawrServlet");
                    sr.setInitParameter("configLocation", "/jawr.properties");
                    sr.addMapping("*.js");
                    sr.setLoadOnStartup(0);

                    // Initialize Jawr CSS servlet
                    sr = sc.addServlet("CssServlet", "net.jawr.web.servlet.JawrServlet");
                    sr.setInitParameter("configLocation", "/jawr.properties");
                    sr.setInitParameter("type", JawrConstant.CSS_TYPE);
                    sr.addMapping("*.css");
                    sr.setLoadOnStartup(1);

                    // Initialize Jawr Binary servlet
                    sr = sc.addServlet("BinaryServlet", "net.jawr.web.servlet.JawrServlet");
                    sr.setInitParameter("configLocation", "/jawr.properties");
                    sr.setInitParameter("type", JawrConstant.BINARY_TYPE);
                    sr.addMapping("*.jpg", "*.png", "*.gif", "*.woff", "*.ttf", "*.svg", "*.eot");
                    sr.setLoadOnStartup(0); // Ensure that the binary servlet starts before the CSS one 
            }


With this configuration, Jawr will load its configuration from
jawr.properties file in the classpath.

You'll find an example of web application using the Servlet 3 API at the
following link :

[basicwebapp-java-ee6] (https://github.com/ic3fox/jawr-core/tree/master/basicwebapp-java-ee6)
