Using popular open source libraries with Jawr
---------------------------------------------

This guide will give you basic guidelines and hint to use popular
javascript libraries in your web applications, taking full advantage of
Jawr. Note that there is no attempt at documenting how any of these
libraries work. Please refer to the official documentation of each
library for that.   All the tutorials assume you have successfully
installed Jawr in your web application. If you haven't refer to the
manual for installation.  


### jQuery

From the jQuery site:


            jQuery is a fast, concise, JavaScript Library that simplifies how you traverse HTML documents, 
            handle events, perform animations, and add Ajax interactions to your web pages. 
            jQuery is designed to change the way that you write JavaScript.


To use jQuery within a Jawr enabled web application, we would start off
by [downloading the jQuery distribution](http://jquery.com/). There are several
download options: gzipped, packed (minified) and regular. Since Jawr
supports serving both gzipped and non-gzipped versions of the same
resource, we will use the 'packed' version. Download it, create a
directory named /js/lib at the root of the web application and place the
file there.

During the tests, I experienced some problems when bundling the minified
jquery with other scripts. These are easily solved by adding a semicolon
(;) at the end of the jQuery packed file. Do add the semicolon or you
will get errors in Firefox and possibly in other browsers.

When using jQuery it is likely that you will eventually use some of the
many plugins available for it. In this case we will setup the jQuery
date picker plugin, which you can download from [this
page](http://kelvinluck.com/assets/jquery/datePicker/v2/demo/).
You will need to download three files from there: jquery.datePicker.js,
datePicker.css and from the requirements section, date.js. from there:
Once you have them, create a directory named /js/lib/plugins and place
the two scripts there (not the css). Create a .license file in this
directory and copy and paste the licenses at the top of the scripts, so
once minified they will get the licenses appended back in. Finally,
create a file named .sorting and simply write 'date.js' (without the
quotes) in it. Finally, create a dir named /css and place the css file
there.

Using the date picker plugin yields an interesting problem for Jawr to
solve: we have the minified version of jQuery and, on the other hand, we
have the date picker component, and the associated date.js library, for
which there are no minified versions available. We intend to bundle all
together but we face the problem that we don't want to run the minifier
on jQuery but we do want to minify the date picker component. To achieve
this we will use a composite bundle made up of one non minified bundle
with jQuery and a minified plugins bundle:


    # Our composite bundle
    jawr.js.bundle.lib.id=/bundles/lib.js
    jawr.js.bundle.lib.global=true
    jawr.js.bundle.lib.composite=true
    # We specify the members of the bundle
    jawr.js.bundle.lib.child.names=jquery, jqueryplugins

    # This mapping is non recurring, so subdirs are not included
    jawr.js.bundle.jquery.mappings=/js/lib/
    # No minification for the jQuery bundle
    jawr.js.bundle.jquery.bundlepostprocessors=none

    # Plugins bundle, will be minified
    jawr.js.bundle.jqueryplugins.mappings=/js/lib/plugins/**

    # An 'all in one' css bundle

    jawr.css.bundle.all.id=/bundles/all.css
    jawr.css.bundle.all.mappings=/css/**


In the event that you had other plugins did have minified versions
available, it would be easy to add a third bundle to the composite, or
simply to add more mappings to the jQuery bundle. The point is that all
your libraries are served as a single, minified and potentially gzipped
file.

The basic setup is finished, so now let's test it. To do that, create a
javascript file at /js/index.js, with the following code:


    $(function()
    {
            $('.date-pick').datePicker();
    });


This will setup the date picker for any input with a class of date-pick,
and will require all the scripts you downloaded to work properly.

Finally, add an index.jsp test page with this contents:

    <%@ taglib uri="http://jawr.net/tags" prefix="jwr" %>
    <%@ page contentType="text/html;charset=UTF-8" %>
    <html>
    <head>
    <jwr:style src="/bundles/all.css" />
    <jwr:script src="/js/index.js"/> 
    </head>
    <body>
            <input name="date1" id="date1" class="date-pick" />
    </body>
    </html>


Now deploy the application and open the index.jsp page. You should see a
link by the input. If you click on it, a calendar widget should popup.
Check the script that the page imports: it should contain the jquery
library plus the date and date picker code minified.

### Prototype.js and script.aculo.us

From their respective websites:

            Prototype is a JavaScript Framework that aims to ease development of dynamic web applications. 
            
            script.aculo.us provides you with easy-to-use, cross-browser user interface JavaScript libraries 
            to make your web sites and web applications fly

Prototype and script.aculo.us are popular javascript tools, and it is
easy to integrate them in a Jawr-enabled application. The first step
would be to [download the script.aculo.us
distribution](http://script.aculo.us/downloads) which
already includes the prototype library.

Create a /js/lib directory at the root of your web application and copy
prototype.js there. Prototype must be loaded first in order for
script.aculo.us to work, so what we will do is to create a subdirectory
(/js/lib/scriptaculous for instance) and put there the script.aculo.us.
files. Using a recurring mapping we make sure this way that prototype
goes first. Note that for this example we are including every one of the
script.aculo.us components, but keep in mind that in a real application
you would only add the ones you were going to use.

There is one problem about using script.aculo.us with Jawr.
Script.aculo.us expects you to load only one of the scripts of its
distribution, namely scriptaculous.js. This file contains a javascript
module which inserts script tags in your document to import all the
other files. This is not needed when using Jawr, since all the files are
bundled together as a single resource. Therefore we need to disable this
behavior.

The quickest way to accomplish that is to open scriptaculous.js and
remove the last line in this file, which should read as follows:


            Scriptaculous.load();

There are possibly better ways to accomplish this but this one is the
fastest to implement and will do the trick.

Another thing to keep in mind is that the effects.js file must be loaded
before the others, so we will create a .sorting file at
/js/lib/scriptaculous, with the following content:


            effects.js


Now we need to create a license file with the copyright information.
Since these libraries are being minified, all comments are removed but
we are required to include the copyright notice in our bundle. To solve
this, create a file named *.license* at /js/lib/. Now open prototype.js
and copy and paste the copyright notice to the license file. Do the same
with the scriptaculous.js. You should end up with a .license file
containing this text:


    /*  Prototype JavaScript framework, version 1.6.0
     *  (c) 2005-2007 Sam Stephenson
     *
     *  Prototype is freely distributable under the terms of an MIT-style license.
     *  For details, see the Prototype web site: http://www.prototypejs.org/
     *
     *--------------------------------------------------------------------------*/
    // script.aculo.us scriptaculous.js v1.8.0, Tue Nov 06 15:01:40 +0300 2007

    // Copyright (c) 2005-2007 Thomas Fuchs (http://script.aculo.us, http://mir.aculo.us)
    // 
    // Permission is hereby granted, free of charge, to any person obtaining
    // a copy of this software and associated documentation files (the
    // "Software"), to deal in the Software without restriction, including
    // without limitation the rights to use, copy, modify, merge, publish,
    // distribute, sublicense, and/or sell copies of the Software, and to
    // permit persons to whom the Software is furnished to do so, subject to
    // the following conditions:
    // 
    // The above copyright notice and this permission notice shall be
    // included in all copies or substantial portions of the Software.
    //
    // THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
    // EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
    // MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
    // NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
    // LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
    // OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
    // WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
    //
    // For details, see the script.aculo.us web site: http://script.aculo.us/


Note that these notices may change over time, so don't copy and paste
from this page, use the licenses in the scripts instead.

Now the last thing you need to have everything set up is to add a
mapping to the Jawr configuration file. It would go like this:


    jawr.js.bundle.lib.id=/bundles/lib.js
    jawr.js.bundle.lib.global=true
    jawr.js.bundle.lib.mappings=/js/lib/**


With this mapping, the bundle with an id of /bundles/lib.js will contain
prototype and all the script.aculo.us files, all minified and with the
appropriate licensing info at the top. For a gzip enabled client, the
download will be of approximately 50KB, and for a non-gzip enabled
client the size would be at around 182KB. In your project, the files
comprising the bundle will be at around 262KB. This means you can get up
to a 5-1 compression ratio using Jawr.

To test the setup, we will create a simple script that uses
script.aculo.us and prototype, and a test JSP page to see it in action.
First create js file named index.js at the /js/ directory. Copy the
following code into it:


    function attachFadeOutBehavior() {
            Event.observe($('fadeOut'),'click',function(){new Effect.SwitchOff($('fadeOut'))});
    }

    Event.observe(window, 'load', attachFadeOutBehavior);


This code uses prototype to hook up a couple of event listeners which
fire a script.aculo.us effect when you click on an element in the page.
Create an index.jsp page at the root of you application, and copy the
following code there:


    <%@ taglib uri="http://jawr.net/tags" prefix="jwr" %>
    <%@ page contentType="text/html;charset=UTF-8" %>
    <html>
    <head>
    <jwr:script src="/js/index.js"/> 
    </head>
    <body>
            <div id="fadeOut">
                    If everything is setup correctly, this will disappear when you click on it. 
            </div>  
    </body>
    </html>


When you open the page in your browser, you should be able to make the
text disappear by clicking on it. You will then be ready to use
Prototype and script.aculo.us in your Jawr enabled application.


### Yahoo! UI library

YUI is a collection of javascript and CSS files that help in building
rich web applications. You can download it from <http://developer.yahoo.com/yui/>.  

The YUI distribution brings a huge collection of .js files, some of them
with related .css stylesheets. Normally you will not use every one of
them, so you must pick whichever you want to use and include them in
your application.  

Once you download and decompress the YUI distribution, you will find all
the scripts under a dir named 'build'. There is one directory for each
component, plus an assets directory with required CSS and image files.
Note that all files come in UTF-8 encoding, so you may want to use that
as Jawr encoding, or otherwise convert all files to whichever encoding
you want to use.  

Within each script directory, javascript files come in three flavors: a
normal, uncompressed version, another one targeted at debugging which
writes to a console component, and a minified version. As we will see,
having a debug version plays well with the debug-only bundles feature in
Jawr. Many components depend on others to be loaded in order to work,
you will need to check the documentation to find out about this
dependencies.  

For the example we will use the modules that make up for the YUI Panel
component. The required scripts are the following:

-   yahoo
-   event
-   dom
-   dragdrop
-   container
-   element
-   button

All these can be found at the build/ folder of the YUI distribution,
each one in a folder by the same name. Each folder contains the three
flavors of the script (some may include more than one script file).  

Since we have debug versions of the scripts, we will create two bundles,
one for development mode and another for regular use. This way, when we
use the development mode of Jawr, we will get a console where all
components will write log messages to. Of course, you can skip the debug
bundle if you don't need it.  

In the root dir of our application, create two folders: **/js/yui** and
**js/yui\_debug**. For the resources, create a **css/** directory. Now
follow this steps:

-   From each directory of the list above, copy the scripts which have
    names ending with -min to the **/js/yui** folder. If you choose to
    use more widgets from YUI, you will simply add them this directory.
    These are minified versions of the YUI libraries, and likely will be
    smaller than if you used Jawr to do the minification (at least for
    as long as Jawr does not support the YUI compressor). However, there
    is one thing you can do to optimize the size even further. Every
    file will contain the copyright license, which will add unneeded
    weight to the bundle (there is no point to repeat the same copypright
    notice n times in the same script). Thus, remove the copyright
    notice from these files.
-   From each directory of the list above, copy the debug scripts(those
    whose end with -debug) to the **/js/yui\_debug** folder.
-   Copy the build/logger dir to the **/js/yui\_debug** dir in your app.
-   Optionally, create a script at **/js/yui\_debug/logger** that
    creates a console component every time a page is loaded. It will only
    execute in debug mode, so that you get all needed logging messages.
    You can name it anyway you want, just copy the following code into
    it:

                function createLogPanel() {
                        var myLogReader = new YAHOO.widget.LogReader();
                }
                
                YAHOO.util.Event.addListener(window, "load", createLogPanel);


-   Copy the build/assets dir to the **/css** dir in your app. It
    contains the default YUI skin to style the widgets.

Now that you have all files in place, you need to specify ordering.
Create a **.sorting** file at **/js/yui**, with the following content:


            yahoo-min.js
            dom-min.js
            event-min.js
            dragdrop-min.js
            element-beta-min.js


If you add more widgets you might have to add more lines to the sorting
file to force the loading order of dependencies. For most cases, yahoo,
dom and event will be required to load first.  

For the debug bundle, you will need a similar sorting file, only
changing the filenames to add the -debug suffix:


            yahoo-debug.js
            dom-debug.js
            event-debug.js
            dragdrop-debug.js
            element-beta-debug.js

If you created the optional script at **/js/yui\_debug/logger** to
launch the console on page load, you need a .sorting file at this
directory to force logger.js to load first. Just type 'logger.js' in it
and you are ready to go.  

Another thing to keep in mind is that you will need to include the
copyright notice you removed from the minified scripts. To do this, you
will have to create a **.license** file at **/js/yui**. Simply create
the file, open any of the script files from YUI and copy and paste the
copyright notice at the top. For CSS files you need to do likewise, just
create a .license file at /css/assets and copy the same text.  

Now you are ready to create the bundles in the Jawr descriptor. Since
the YUI libraries will normally be used for most pages in a site, they
are defined as global. First thing to do is declare a CSS bundle for the
required assets:


            jawr.css.bundle.yui.id=/bundles/yui.css
            jawr.css.bundle.yui.mappings=/css/assets/**
            jawr.css.bundle.yui.global=true

Then we create two bundles, one for production mode and another for
development mode. To do this we use the **.debugnever** and
**.debugonly** parameters. Also, for the production bundle we specify a
postprocessor which will include the license notice but will not minify
the bundle since we are using the pre-minified YUI scripts:


            # Define the production yui library
            jawr.js.bundle.yui.id=/bundles/yui.js
            jawr.js.bundle.yui.mappings=/js/yui/**
            jawr.js.bundle.yui.global=true
            
            # Never import this bundle in debug mode
            jawr.js.bundle.yui.debugnever=true
            
            # No minification for this bundle, only include the license
            jawr.js.bundle.yui.bundlepostprocessors=license
            
            # Define the debug mode yui library     
            jawr.js.bundle.yuidebug.id=/bundles/yui-debug.js
            jawr.js.bundle.yuidebug.mappings=/js/yui_debug/**
            jawr.js.bundle.yuidebug.global=true
            
            # Only import this bundle in debug mode
            jawr.js.bundle.yuidebug.debugonly=true

Now we are ready to start using YUI in our application. We can try to
reproduce the example script found at /examples/container/panel.html in
the YUI distribution. First, we create a script which will launch the
example at /js/index.js, with the following content:


    YAHOO.namespace("example.container");

    function init() {
     // Instantiate a Panel from markup
     YAHOO.example.container.panel1 = new YAHOO.widget.Panel("panel1", 
                                                            { width:"320px", 
                                                            visible:false, 
                                                            constraintoviewport:true } );
     YAHOO.example.container.panel1.render(); 

     // Instantiate a Panel from script
     YAHOO.example.container.panel2 = new YAHOO.widget.Panel("panel2",
                                                             { width:"320px", 
                                                             visible:false, 
                                                             draggable:false, 
                                                             close:false } );
                                                                                                                     
     YAHOO.example.container.panel2.setHeader("Panel #2 from Script &mdash; This Panel Isn't Draggable");
     YAHOO.example.container.panel2.setBody("This is a dynamically generated Panel.");
     YAHOO.example.container.panel2.setFooter("End of Panel #2");
     YAHOO.example.container.panel2.render("container");

     YAHOO.util.Event.addListener("show1", 
                                  "click", 
                                  YAHOO.example.container.panel1.show, 
                                  YAHOO.example.container.panel1, 
                                  true);
     YAHOO.util.Event.addListener("hide1", 
                                  "click", 
                                  YAHOO.example.container.panel1.hide, 
                                  YAHOO.example.container.panel1, true); 

     YAHOO.util.Event.addListener("show2", 
                                  "click", 
                                  YAHOO.example.container.panel2.show, 
                                  YAHOO.example.container.panel2, 
                                  true);
     YAHOO.util.Event.addListener("hide2", 
                                  "click", 
                                  YAHOO.example.container.panel2.hide, 
                                  YAHOO.example.container.panel2, 
                                  true);
    }

    YAHOO.util.Event.addListener(window, "load", init);

This script will setup an onload event for any page that loads it. The
final thing we need is the test JSP. Create an index.jsp with the
following contents:


    <%@ taglib uri="http://jawr.net/tags" prefix="jwr" %>
    <%@ page contentType="text/html;charset=UTF-8" %>
    <html>

    <head>
    <jwr:style src="/bundles/yui.css"/>
    <jwr:script src="/bundles/yui.js"/> 
    <jwr:script src="/js/index.js"/> 
    </head>
    <body class="yui-skin-sam">
                                            
    <!--BEGIN SOURCE CODE FOR EXAMPLE =============================== -->
    <style>
     #container {height:15em;}
    </style>

    <div id="container">
            <div>
                    <button id="show1">Show panel1</button> 
                    <button id="hide1">Hide panel1</button>
            </div>
            
            <div id="panel1">
                    <div class="hd">Panel #1 from Markup &mdash; This Panel is Draggable</div>
                    <div class="bd">This is a Panel that was marked up in the document.</div>
                    <div class="ft">End of Panel #1</div>
            </div>
            
            <div>
                    <button id="show2">Show panel2</button> 
                    <button id="hide2">Hide panel2</button>
            </div>
    </div>  
    <!--END SOURCE CODE FOR EXAMPLE =============================== -->             
    </body>
    </html>


Without going into details of how YUI works, opening this page should
show a couple buttons which, when pressed, will display a YUI panel
widget. You may notice how we imported only the js/index.js script.
Since the YUI bundles are global, we don't need to explicitly import
them. Give it a try and start Jawr in production and debug modes to see
the difference. Be aware that in debug mode, the console logger might
slow your page down a lot unless you remove messages from several
components or set the logging lever to error.  

With this setup, you can easily add more YUI components as you go, all
of which will be joined into a bundle. You may also split YUI into
several bundles to better fit the usage pattern for your site. Also, you
get a debug mode in which you get a console to log to as needed.

