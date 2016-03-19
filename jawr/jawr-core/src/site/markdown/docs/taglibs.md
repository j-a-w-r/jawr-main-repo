Jawr JSP tag libraries
----------------------

Jawr provides a tag library used to generate tags that import our
bundles to clients of our application.

### JS and CSS tags

One tag will used to generate script tags for javascript resources, and
another will generate link tags for CSS resources.  

Both tags have a **src** attribute in which you specify either the name
of a bundle or the path to a member of a bundle. In either case, the
taglib will do as follows:

-   The page request headers will be analyzed to check wether the
    current client browser will accept gzip encoding or not. Depending
    on this, the generated URLs will point to the regular or the gzipped
    versions of the bundles.
    -   Internet Explorer has issues with gzip encoded resources under
        certain circumstances. This affects version 6 partially, since
        Service Pack 2 fixed all problems. If you wish to be 100% sure
        that you will not run into such problems, you may set the
        **jawr.gzip.ie6.on** flag to **false** in the descriptor. This
        will disable gzipping for IE versions 6 and lower. However, the
        worst problems in Internet Explorer are related to Vary headers
        needed when the same URL points to a resource that is gzipped
        on demand. Jawr does not use this header by having a completely
        different path for gzipped and regular versions of the same
        file, so for most cases it should be fine to use gzipping
        with IE.  

  On some versions, though, it is said that compressed CSS in
        cache is not decompressed after restarting the browser and
        visiting a page that uses the resource, which results in the CSS
        not working properly.  

  The bottom line is that, unless your application runs on a
        controlled environment such as a corporate intranet, it will be
        the safest to set the **jawr.gzip.ie6.on** flag to **false**.

-   Any global bundle will be added in the specified order. See the
    bundle definition manual for more info on this. If the global
    bundles have already been added to the page by a previous tag, this
    step is skipped.
-   Then the corresponding bundle is resolved, either by name or by
    finding a bundle to which the path belongs to.
    -   If the debug mode is off, a link to the bundle is then rendered,
        unless it had already been included by a previous tag.
    -   Otherwise, a link is rendered to each member of the bundle,
        adding a random request parameter to the URL, to prevent the
        browser from caching any resource. Again, no resources will be
        linked again if a previous tag did it first. Also, processing
        HTML comments are added so a developer will easily follow how
        bundles would be processed in production mode.

Another attribute common to both tags is **useRandomParam**. As stated
before, in development mode a random request parameter is added to the
URL to avoid caching. However, sometimes a developer may need to use a
cached version of a file to debug it (firebug, for instance 'forgets'
break point locations when reloading a parameterized script). To avoid
this, add this attribute and set it to 'false' so that the parameter is
no longer added. Note that in production mode this attribute is ignored
and thus has no effect.


### Usage

In order to use any of the tags, you will need to import the taglib to
your page, like this:


            <%@ taglib uri="http://jawr.net/tags" prefix="jwr" %>
            

### The script tag

This tag has only one mandatory attribute, the aforementioned **src**.
To see how it works, imagine we had a mapping like this in our config file:

            
            jawr.js.bundle.globalBundle.id=/bundles/global.js
            jawr.js.bundle.globalBundle.global=true
            jawr.js.bundle.globalBundle.mappings=/js/lib/** 
            jawr.js.bundle.globalBundle.prefix=/global01
            
            jawr.js.bundle.fooBundle.id=/bundles/fooBundle.js
            jawr.js.bundle.fooBundle.mappings=/js/foo.js,/js/bar.js 
            jawr.js.bundle.fooBundle.prefix=/foo01


Then, in a JSP we type the following:


            <%@ taglib uri="http://jawr.net/tags" prefix="jwr" %>
            <html>
            <head>
                    <!-- Invoking the bundle by name -->
                    <jwr:script src="/bundles/fooBundle.js"/> 
                    
                    <!-- Invoking the bundle by using the path of one of its members -->
                    <jwr:script src="/js/bar.js"/> 
            </head>
            ...
            

The result is that when the JSP is executed with Jawr in production
mode, something like the following is written to the page:


            <script type="text/javascript" src="/myWarContext/global01/bundles/global.js" ></script>
            <script type="text/javascript" src="/myWarContext/foo01/bundles/fooBundle.js" ></script>


This might be unexpected: a link to the global bundle was written first,
even though no specific tag invoked it. That is precisely what global
bundles are about: they will always be included whenever you use a Jawr
tag in a page, before any other bundle. This way, commonly used
libraries are implicitly available everywhere.  

Although we wrote two tags in the JSP with paths corresponding to the
foo bundle, only one script link is written out. It would be pointless
to link twice to the same resource in a page, so Jawr will only write
each different link once.  

The link itself, as you can see, is composed of the application context
path, the prefix we specified for the bundle, and the bundle id.  

Here is the list of the Javascript tag attributes:

| **Attribute name** | **Type**        |  **Purpose**     |  **Default value** |
|--------------------|-----------------|------------------|--------------------|
| src                | String          | The bundle path  | none			   |   
| useRandomParam     | Boolean         | The flag indicating if we must use random must use random in debug mode | true |
| async              | Boolean         | The async flag.  | false |
| defer              | String          | The defer lag.   | false |


### The style tag

Aside from the mandatory **src** attribute, this tag has an optional
**media** attribute. Use it if you need to set a specific media type for
a CSS bundle. If this attribute is not set it will be rendered as
media="screen". For example:


            <!-- 
                            Invoking the bundle by name. 
                            The generated link will have media="screen"  
            -->
            <jwr:style src="/bundles/someCSSbundle.css"/>
            
            <!-- Invoking the bundle by using the path of one of its members and setting the media attribute. -->
            <jwr:style src="/css/printer.css" media="print" />
            


-   Style links closing type Depending on which type of pages you are
    serving (html or xhtml) you might need the link tags to be closed in
    different ways in order for your pages to validate or even work
    properly across all browsers. To change the way the tags are closed,
    you can set a configuration property (at your jawr.properties file),
    named **jawr.csslinks.flavor**. The possible values and their result
    is:
    -   **xhtml**: this is the default, so you don't really need to set
        the property unless you need to be explicit about it. With this
        value, the tags are closed inline as in the previous
        example (/&gt;).
    -   **xhtml\_ext**: tags will be closed with a separate closing tab
        (&gt;*/link*).
    -   **html**: tags will not be closed, as determined by the HTML
        spec (&gt;).
-   Alternate / title attribute

   Jawr allows you to define alternate stylesheets and to set the title
    attribute of your CSS links.


                <jwr:style src="/css/cssBundle.css" alternate="true" title="myBlueStyle"/>
                

   will generate :


                <link href="/myWarContext/gzip_6c3901a019f50bad406084bd7a1b8a5/css/cssBundle.css" media="all" type="text/css" rel="alternate stylesheet" title="myBlueStyle">
                

   **Warning**:  
    If the reference of your cssBundle is a CSS bundle with skin
    variants, you can **force** the skin variant to use by setting the
    alternate attribute to **true** and the title to the skin name
    to use. This will override the skin set by the user.

-   Display CSS skin alternate styles

   Since the version 3.3, Jawr support CSS skins. This means that a CSS
    bundle can have multiple skins, which will be displayed depending on
    the user current skin. This skin information is stored in a cookie.
    Please check the following [link](../tutorials/howToUseJawrCssSkin.html) for more detail.  
    By default, if you reference a skinned CSS bundle, Jawr will render
    the one which match the user defined skin.  
    If you use the property **displayAlternate**, Jawr will render one
    link to the bundle which match the user skin, and it will also
    renders the other CSS links as **alternate** stylesheets.  
    For example, if you have the bundle which has 3 skins :
    "aqua","greyStorm" and "blueSky".  
    If the current user skin is greyStorm, Jawr will render the links as followed.

   
                <jwr:style src="/css/skinnedBundle.css" displayAlternate="true"/>
                

   
   will generate :


                <link href="/myWarContext/gzip_6c3901a019f50bad406084bd7a1b8a5.greyStorm/css/skinnedBundle.css" media="all" type="text/css" rel="stylesheet">
                <link href="/myWarContext/gzip_f569a1f55841b7a97f0d176719fe872.aqua/css/skinnedBundle.css" title="aqua" media="all" type="text/css" rel="alternate stylesheet">
                <link href="/myWarContext/gzip_f45cd4b558411325870d176719fe872.blueSky/css/skinnedBundle.css" title="blueSky" media="all" type="text/css" rel="alternate stylesheet">
                


   Jawr provides also a Javascript generator **skinSwitcher**, whose
    defined a function to switch from one skin to another.  
    You need to add the skinSwitcher to a JS bundle and then you will be
    able to use the following function.


                JAWR.skin.switchToStyle("aqua"); 


   The above code snippet will set the current user skin to "aqua" in
    the cookie and it will switch the current skin to "aqua".  For more
    information about the **skinSwitcher** generator, please take a look
    at the [generators](./generators.html) documentation.

   To sum up:

| **Attribute name** | **Type**        |  **Purpose**     |  **Default value** |
|--------------------|-----------------|------------------|--------------------|
| src                | String          | The bundle path  | none			   |   
| media              | String          | The media attribute of the stylesheet | none |          
| useRandomParam     | Boolean         | The flag indicating if we must use random parameter in debug mode | true |
| alternate          | Boolean         | This flag is used to render link as an alternate style | false | 
| title | String | The title to use for the style | none |
| displayAlternate | Boolean | This flag is used to render the skin variants of the CSS bundle as alternate style | false |

   -   **src** This attribute defines the CSS bundle path to use.
   -   **src** This attribute defines the CSS bundle path to use.
   -   **alternate** This attribute allows you to define the CSS bundle
       as an alternate style.
   -   **title** This attribute allows you to define the title
       attribute of the stylesheet link.
   -   **displayAlternate** This attribute allows you to render all the
       skin variants of the CSS bundle as alternate styles.

### The image tags

Jawr provides 3 tags for images. One tag will generate an HTML image,
the second will generate an input image, and the last one will display
the url generated by Jawr to reference an image.

All tags have a **src** attribute in which you specify the path to the
image. In either case, the generated URL will contains :

-   The image servlet mapping if it exists
-   A prefix with the hash code of the image file.

This is used to define a unique URL depending on the content of the
image file. The property **jawr.image.hash.algorithm** is used to define
which hash algorithm should be used. The default one is CRC32.


            <!-- 
                            Display the HTML image tag. 
            -->
            <jwr:img src="/img/icons/ok.png"/>
            
            <!-- 
                            Display input image tag. 
            -->
            <jwr:image src="/img/icons/add.png" />
            
            <!-- 
                            Display the url generated by Jawr to reference the clock image. 
            -->
            The url of the clock image is : <jwr:imagePath src="/img/icons/clock.png" />
            


If the image servlet mapping is set to "/jawrImg", the result will look
like :


            <!-- 
                            Display the HTML image tag. 
            -->
            <img src="/jawrImg/cb2654321654/img/icons/ok.png"/>
            
            <!-- 
                            Display input image tag. 
            -->
            <input type="image" src="jawrImg/cb46543132165/img/icons/add.png" />
            
            <!-- 
                            Display the url generated by Jawr to reference the clock image. 
            -->
            The url of the clock image is : jawrImg/cb465465321/img/icons/clock.png
            


-   base64 encoded images

   It is also possible to generate base64 encoded image with any image
    tags using the property base64 as followed.


                <!-- 
                                Display the HTML image tag. 
                -->
                <jwr:img src="/img/icons/ok.png" base64="true"/>

    </div>

    This will generate something like :

    <div class="source">

                <!-- 
                                Display the HTML image tag. 
                -->
                <img src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUg...Z7oYAAAAASUVORK5CYII="/>

    </div>

   It is important to note that Jawr will generate the base64 encoded
    image for all browsers except IE6 and IE7, which doesn't handle
    base64 encoded image.


### How to skip/modify the context path

The context path can be replaced or removed from all generated URLs if
you set the **jawr.url.contextpath.override** configuration param to
some value at the config properties file. This can be useful when you
are serving your application behind an HTTP server. The values for this
param can be:

-   **/aCustomPathPrefix**: Set a custom prefix for URLs to be used
    **instead of** the context path.
-   **/**: Skip the context path, all URLs are relative to the domain
    (as in /global01/bundles/global.js).
-   \[declare but leave value empty\]: Skip the context path, all URLs
    are relative to the page (as in global01/bundles/global.js).
-   \[not declared at all\]: Use the context path, this is the
    default behavior.


### EL Expressions

The taglibs accept EL expressions out of the box on servers that support
it. If you run on a servlet container of version 2.3, however, you will
need some tweaking to use EL with Jawr tags. Note that even if you use a
2.4 or greater container, you still need to declare the proper web
application version in web.xml. There is a very good explanation of this
at [this page](http://www.mularien.com/blog/2008/04/24/how-to-reference-and-use-jstl-in-your-web-application/)

So what do you need to use EL on an older container? The first thing you
need is to add an additional dependency on the Apache standard taglib
implementation. If you use Maven, just add this to your POM:

        <dependency>
          <groupId>taglibs</groupId>
          <artifactId>standard</artifactId>
          <version>1.1.2</version>
        </dependency>


If you are not using Maven, then head to the [apache taglib project page](http://jakarta.apache.org/taglibs/doc/standard-doc/intro.html)
and download the jar from there.

The other thing you need to do is to change the taglib import in JSP
pages, to use a special version that supports EL using the Apache STL
implementation. The proper import declaration would be:


            <%@ taglib uri="http://jawr.net/tags-el" prefix="jwr" %>
            

With the library in place and using this import declaration, you should
be able to use EL expressions in your application.
