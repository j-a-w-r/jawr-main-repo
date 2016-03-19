How to use Google closure compiler with Jawr
------------

The goal of this tutorial is to explain how to use with Jawr.  
This feature is available since the version 3.5 of Jawr.


### How it works ?

Since the version 3.5, Jawr provides by default a postprocessor to
compress your JS bundles with Google Closure Compiler.  
The google closure compiler postprocessor is a global postprocessor,
which means that it will be launched at the end of the bundling
process.  This google closure compiler postprocessor is defined as a
global postprocessor, because in advance mode, the compiler will check
the JS functions to remove the dead code.

The default compilation level is **WHITESPACE\_ONLY**.  
Here is the list of available level :

-   WHITESPACE\_ONLY
-   SIMPLE\_OPTIMIZATIONS
-   ADVANCED\_OPTIMIZATIONS

The default warning level is **VERBOSE**.

You can pass arguments to the closure compiler using the prefix
*jawr.js.closure.*

For instance, you can pass the boolean "third\_party" argument using
*jawr.js.closure.third\_party* in the jawr properties file. The full
list of arguments are define in
*com.google.javascript.jscomp.CommandLineRunner*, so you can check it at
the following [link](http://closure-compiler.googlecode.com/svn/trunk/src/com/google/javascript/jscomp/CommandLineRunner.java)

There are 2 arguments which are not allowed because they are managed by
Jawr :

-   js
-   module

To exclude a bundle from the closure processing, you can use the
following property **jawr.js.closure.bundles.excluded**, whose the value
is a comma separated list of the bundles to exclude.


            # Use Google closure as global postprocessor
            jawr.js.bundle.factory.global.postprocessors=closure
            jawr.js.closure.compilation_level=SIMPLE_OPTIMIZATIONS
            jawr.js.closure.bundles.excluded=sample2
            
            jawr.js.bundle.sample.id=/js/sample1.js
            jawr.js.bundle.sample.mappings=/js/sample01.js,....
            
            
            jawr.js.bundle.sample2.id=/js/sample2.js
            jawr.js.bundle.sample.mappings=/js/sample02.js,...
            


### Set up Jawr in your project

Please check the [quickstart](./quickstart.html) tutorial for the
instruction about Jawr installation in your project.


### Configure instances of the Jawr Servlet

You must configure Jawr to use at least the JS servlet.


### Jawr configuration file

We will follow the following example :


            # Use Google closure as global postprocessor
            jawr.js.bundle.factory.global.postprocessors=closure
            jawr.js.closure.compilation_level=ADVANCED_OPTIMIZATIONS
            
            jawr.js.bundle.sample.id=/js/sample.js
            jawr.js.bundle.sample.mappings=/js/sample01.js,/js/sample02.js
            

### Create a test JS file

Write a test JS file named **/js/sample01.js** and add the following
content:


    /**
     * Hello function
     * Add two variables
     */
    function hello(name){

            alert("hello "+name); 
    }

    window['hello'] = hello;


Write a test JS file named **/js/sample02.js** and add the following
content:

    /**
     * Add function
     * Returns the addition of the two parameter
     */
    function add(c1, c2){

            return c1 + c2; 
    }


Write a test JSP page and add the following content:


    <%@ taglib uri="http://jawr.net/tags" prefix="jawr" %>
    <%@ page contentType="text/html;charset=UTF-8" %>
    <html>
    <head>
            <jawr:script src="/js/sample.js" />
    </head>
    <body>
            ...
            <button type="button" onclick="hello('world!')">Click Me!</button>
    </body>
    </html>

Deploy your application to a server and open the JSP you created. You
will notice that the **add** function has been removed from the
generated bundle as it is considered as not used.

