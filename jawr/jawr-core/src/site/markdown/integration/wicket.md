Jawr with wicket
----------------

If you are developing a Wicket application, Jawr provides Components for
Javascript, CSS and images. It works the same way as the JSP taglib,
although there is a few extra configuration steps to take in order to
use it.  


### Setup Jawr in a Wicket application

There is a very small difference in the way you will use Jawr in your
Wicket application, compared to using JSPs. Everything is setup the same
way except that you need to reference the bundles in your pages in a
different way. You need to add the jawr-wicket-extension module in your
classpath. For maven users, you'll need to add the following dependency
in your pom file :

        <dependency>
          <groupId>net.jawr.extensions</groupId>
          <artifactId>jawr-wicket-extension</artifactId>
          <version>3.5</version>
        </dependency>


Note that this module supports the version 6.x of wicket


#### 1. Configure the Jawr servlet

The first step is to declare the Jawr servlet in the web.xml descriptor.
This works exactly the same as in a regular web application, so the
[Jawr Servlet Documentation page](../docs/servlet.html) has all the info
you need to do it. You can create the properties file normally, since
the only difference in using Wicket with Jawr has to do with the
reference of your web resources in your HTML page or in your Page class.


#### 2. Setup your wicket application

In order to use the Jawr in your Wicket application, you must initialize
your application. Jawr provides an utility class, which initialize your
wicket application and register the Jawr link resolver and the Jawr tag
handler:

             
            /* (non-Javadoc)
             * @see org.apache.wicket.protocol.http.WebApplication#init()
             */
            protected void init() {
                    
                    JawrWicketApplicationInitializer.initApplication(this);
            }
            

#### 3. Reference the Jawr bundles

To reference your bundles in your page, there is 2 options:

-   **wicket:jawr** tag

   With the **wicket:jawr** tag, you only need to wrap your bundle
    references with a **&lt;wicket:jawr&gt;** tag like this:
                 
        <html xmlns:wicket="http://wicket.apache.org/dtds.data/wicket-xhtml1.4-strict.dtd" >
            <head>  
                <title>Wicket Quickstart Archetype Homepage</title>
                <wicket:jawr>
                  <link rel="stylesheet" type="text/css" href="/fwk/core/component.css" media="all" />
                  <script type="text/javascript" src="/js/bundle/msg.js" ></script>
                </wicket:jawr>
            </head>
        ...
        <wicket:jawr>
        <img src="/img/icons/logo.png">
        <wicket:jawr>
        ...

        <wicket:jawr>
        <input type="image" src="/img/icons/add.png" name="add" >
        </wicket:jawr>
        ...

        </html>

-   Use directly the Jawr wicket components

   Jawr provides 4 components for Wicket:

   - **net.jawr.web.wicket.JawrStylesheetReference** for the CSS bundles  
   - **net.jawr.web.wicket.JawrJavascriptReference** for the javascript bundles  
   - **net.jawr.web.wicket.JawrHtmlImageReference** for the HTML image reference  
   - **net.jawr.web.wicket.JawrImageReference** for the input image reference 
    
    
   You can also attach a Jawr component to a web resources in your page like this:

                 
        <html xmlns:wicket="http://wicket.apache.org/dtds.data/wicket-xhtml1.4-strict.dtd" >
            <head>  
                <title>Wicket Quickstart Archetype Homepage</title>
                <wicket:head>
                  <link rel="stylesheet" type="text/css" wicket:id="componentCssBundle" href="/fwk/core/component.css" media="all" />
                  <script type="text/javascript" wicket:id="messageJsBundle" src="/js/bundle/msg.js" ></script>
                </wicket:head> 
            </head>
        ...
        <img wicket:id="imgLogo" src="/img/icons/logo.png">
        ...
        <input type="image" wicket:id="imgAdd" src="/img/icons/add.png" name="add" >
        ...

        </html>
                

   And in your page, you must reference your component like this:

                /**
                 * Constructor that is invoked when page is invoked without a session.
                 * 
                 * @param parameters
                 *            Page parameters
                 */
            public HomePage(final PageParameters parameters) {
               
                // Add Jawr CSS bundle component 
                add(new JawrStylesheetReference("componentCssBundle"));
                // Add Jawr JS bundle component 
                add(new JawrJavascriptReference("messageJsBundle"));
                // Add Jawr HTML image component 
                add(new JawrHtmlImageReference("imgLogo"));
                // Add Jawr input image component 
                add(new JawrImageReference("imgAdd"));
            }


   That's it. As you can see in the example, the integration of Jawr is
    pretty simple. You only need to configure Jawr normally, initialize
    your application properly and then reference your bundles using the
    **&lt;wicket:jawr&gt;** tag in your HTML page or directly by
    defining the components used in your Wicket page class.


### Sample application

You will find below the link to a sample application source code showing
the integration of Jawr and Wicket :

[source code](https://github.com/ic3fox/jawr-wicket/tree/master/jawr-wicket-webapp-sample)

