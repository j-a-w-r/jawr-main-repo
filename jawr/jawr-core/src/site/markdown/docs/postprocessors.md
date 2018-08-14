Jawr Postprocessors
-------------------

Postprocessors are filters that Jawr applies to resources and bundles
during startup. These filters provide such things as minification
(removal of unneeded whitespace and comments), URL rewriting, and the
like. There are two types: file and bundle postprocessors. The file
postprocessors are applied to resources before adding them to the
bundle. On the other hand, bundle postprocessors are applied to a whole
bundle once all the files have been joined into it.  

Normally, you won't need to worry about postprocessors, since Jawr will
use them automatically: javascript will be minified using JSMin, and the
license comments (if any) will be added afterwards. CSS files will also
be minified, license comments will be added and their image URLs will be
rewritten so they keep working from within a bundle. But there are
countless combinations you can use, plus you may create and use your own
postprocessors, so here is how to configure the filter chain applied to
files and bundles.

For configuration purposes, every postprocessor has a unique name key,
which you use in a comma-separated property in the descriptor, for
instance:


            jawr.js.bundle.factory.bundlepostprocessors=JSMin,license
                    
            jawr.css.bundle.factory.bundlepostprocessors=cssminify,license
            jawr.css.bundle.factory.filepostprocessors=csspathrewriter


As you see, we specify a couple of bundle postprocessors for both js and
css resources, plus a file postprocessor for css files. We didn't
specify any file postprocessor for javascript because, at the time of
writing, there are none available yet.   Note that the previous example
configuration matches the default behavior of Jawr, so it actually makes
no difference whether or not you include it in your descriptor. You would
set these parameters only to change the default behavior, like for
example, to remove minification:


            jawr.js.bundle.factory.bundlepostprocessors=license             
            jawr.css.bundle.factory.bundlepostprocessors=license


Optionally, you may specify a different set of postprocessors for any
given bundle, so you can mix and match different schemes within your
application. In order to do that, you specify similar attributes for a
particular bundle:


            jawr.js.bundle.foo.bundlepostprocessors=license
            jawr.js.bundle.foo.fileprocessors=none
            

In this example, the bundle postprocessor for the *foo* bundle is set to
*license*, meaning no minification will occur. And for the file-by-file
postprocessor, it is specified that none will be used.

Jawr allows you to define the postprocessors for composite bundles. The
format of the postprocessor properties is:

-   jawr.*resourceType*.bundle.*bundleName*.composite.bundlepostprocessors
-   jawr.*resourceType*.bundle.*bundleName*.composite.filepostprocessors


### Custom postprocessors

You can also implement your own postprocessor components (for js, css,
or both) to perform any functionality not offered by the included ones.
To do that, you must create a class with a no-params constructor that
implements the interface
*net.jawr.web.resource.bundle.postprocess.ResourceBundlePostProcessor*.
This interface defines a single method:


            /**
             * Postprocess a bundle of resources. 
             * @param StringBuffer Joined resources. 
             * @return StringBuffer a buffer containing the postprocessed bundle. 
             */
            public StringBuffer postProcessBundle(BundleProcessingStatus status,StringBuffer bundleString);


The first parameter is an object which encapsulates the status of a
bundling process and also gives you access to Jawr configuration plus
other data which may be useful under certain circumstances. Most of the
time, though, you won't need to use it at all.

The bundleString StringBuffer will contain different data depending on
whether the postprocessor is used as a bundle postprocessor or a file
postprocessor. In the first case, it will contain the text for the whole
bundle, while in the latter case it will contain the text for just a
single item in the bundle.

Finally, the return StringBuffer should contain the bundleString data
after it is modified by the postprocessor. As an example, here is an
implementation of a postprocessor which we would use to wrap all our
scripts in a function that is immediately executed:


    package net.jawr.test;
    // [import statements...]

    public class FunctionWrapperPostProcessor implements ResourceBundlePostProcessor {
            
            public StringBuffer postProcessBundle(BundleProcessingStatus status,StringBuffer bundleString) {
                    StringBuffer ret = new StringBuffer();
                    ret.append("function(){");
                    ret.append(bundleString);
                    ret.append("}()");
                    return ret;
            }
    }


To use this postprocessor with our application, we need to declare it in
the properties configuration, by giving it a name and declaring the
class so that Jawr may create an instance when starting up. The name you
give to your postprocessor can then be used to define the
bundlepostprocessors and filepostprocessors properties, thus allowing
you to create a chain that combines your postprocessors with those of
Jawr.

The name and class are defined by declaring a property in the form
jawr.custom.postprocessors.\[name\].class=\[class\]. For example, the
following configuration would add two custom postprocessors named *func*
and *sample* and map them in different postprocessing chains:


    jawr.custom.postprocessors.func.class=net.jawr.test.FunctionWrapperPostProcessor
    jawr.custom.postprocessors.sample.class=net.jawr.test.SamplePostProcessor

    jawr.js.bundle.factory.bundlepostprocessors=JSMin,func,sample,license
    jawr.js.bundle.foo.bundlepostprocessors=license=func,license



### Custom postprocessors for composite bundle

For a composite bundle, you can define the child bundle associated to it
using the **child.names** property:


    jawr.css.bundle.myCompositeBundle.child.names=child_1,child_2


When you define a filepostprocessor on a composite bundle, this
postprocessor will be applied on the child bundle content. The child
bundle content will be treated as a single file. If you define a bundle
postprocessor on a composite bundle, the postprocessor will be applied
on the whole bundle.

To disable postprocessing on the composite bundle, you just need to
configure your composite bundle like this:

    jawr.css.bundle.myCompositeBundle.fileprocessors=none
    jawr.css.bundle.myCompositeBundle.bundlepostprocessors=none


### Jawr included postprocessors reference


#### Css and Js common postprocessors


##### [**Licenses includer**]()

-   **Type**: Bundle
-   **Properties Key**: license

The licenses includer will add the content of .license files at the top
of a bundle. That way you will be able to add open source mandatory
licenses or any other comment to the top of your resources. Please check
the [license files page](./license_files.html) for more info.

------------------------------------------------------------------------

Note that licenses are themselves code comments, so you should be
careful to always use this processor after any minification or
compression processor has executed and not before. Otherwise, the
license might be deleted from the bundle right after it was inserted.

------------------------------------------------------------------------

##### [**YUI compressor**]()

-   **Type**: Bundle
-   **Properties Key**: YUI, YUIobf

This processor uses [Julien Lecomte's YUI
compressor](http://developer.yahoo.com/yui/compressor/).
This is a javascript and CSS minification tool that can also perform
code obfuscation in the case of javascript bundles. If you want
obfuscation, you should use the **YUIobf** key when defining
postprocessors, but remember that this is only valid for javascript
bundles. The YUI compressor has an advantage over JSMin and Jawr's
custom CSS compressor in that it will achieve better minification
(although in the case of CSS the difference is minimal). On the other
hand it will add a dependency to the YUI compressor and Rhino libraries.
You can get YUI
[here](http://www.julienlecomte.net/yuicompressor/), and
rhino [here](http://www.mozilla.org/rhino/download.html).
Maven users can get both by adding a single dependency, like this:


            <dependency>
                <groupId>com.yahoo.platform.yui</groupId>
                <artifactId>yuicompressor</artifactId>
                <version>2.2.5</version>
            </dependency>


Unfortunately, this is the only way I know to get YUI from maven, and it
is somewhat ugly since it will include both the YUI compressor and
rhino, which might be problematic if you already have rhino on your
server's classpath. YUI overwrites some of Rhino's classes also, so keep
in mind that having another copy of Rhino.jar in your classpath might
cause class loading issues.

#### Javascript-specific postprocessors


##### [**JSMin**]()

-   **Type**: Bundle
-   **Properties Key**: JSMin

This processor uses [Douglas Crockford's
JSMin](http://www.crockford.com/javascript/jsmin.html)
minificator. It will remove any comments in code and unnecessary
whitespace in a very safe manner.


##### [**Uglify**]()

-   **Type**: Bundle
-   **Properties Key**: uglify

This processor uses
[UglifyJS2](https://github.com/mishoo/UglifyJS2)
compressor. The built in version used is the version v2.4.15. The user is
allowed to update the version used by specifying the directory where the
Uglify JS sources will be found, by defining the property
**jawr.js.postprocessor.uglify.script.location**. This postprocessor is
based on javascript engine. The default JS engine used in Jawr is the
Rhino javascript engine. You can also use Nashorn engine since Java 8.
Please check [the tutorial about the use of JS
engine](../tutorials/howToSetJsEngine.html) for more info. To use the
Rhino engine, you'll have to add it to your project classpath to be able
to us this postprocessor. For maven user, you can add the following
snippet configuration to your pom.xml:


                    <dependency>
                            <groupId>org.mozilla</groupId>
                            <artifactId>rhino</artifactId>
                            <version>1.7R4</version>
                    </dependency>
                             


Here is the list of JS scripts that the processor will try to load from
the web application or from the classpath.

-   utils.js
-   ast.js
-   parse.js
-   transform.js
-   scope.js
-   output.js
-   compress.js
-   sourcemap.js

The user can also define the compression options by using the
**jawr.js.postprocessor.uglify.options** property. This property is a
JSON containing the different options for compression, output format,
...

For more information about uglify options, please check the following
links: [UglifyJS2 site](http://lisperator.net/uglifyjs/)
and [UglifyJS2 source repository](https://github.com/mishoo/UglifyJS2).

Here is an sample configuration where the script will be loaded from
/js/uglify/ directory and with custom options:


            
            jawr.js.postprocessor.uglify.script.location=/js/uglify/
            jawr.js.bundle.factory.bundlepostprocessors=uglify
            # Use unsafe compression options and preserve comments containing @preserve keyword
            jawr.js.postprocessor.uglify.options={ compress : { unsafe : true}, output : { compress : /@preserve/ }}
            


-   **Configuration properties**

   Jawr provides the following properties to configure the Uglify
    postprocessor:

| **Property name** | **Type** | **Purpose** | **Default value** |
|-------------------|----------|-------------|-------------------|
| jawr.js.postprocessor.uglify.script.location | String | The uglify JS scripts location | /net/jawr/web/resource/bundle/postprocessor/js/uglify/ |
| jawr.js.postprocessor.uglify.options | String | The uglify options in JSON format | {} |
| jawr.js.postprocessor.uglify.js.engine | String | The JS engine to use for UglifyJS. | The value of the 'jawr.js.engine' property |

#### CSS-specific postprocessors

##### [**CSS Minificator**]()

-   **Type**: Bundle
-   **Properties Key**: cssminify

This processor removes comments and unneeded whitespace using search and
replace with regular expressions. It is on by default and is almost as
efficient as the YUI compressor.

The user can define if the licence comment (/\*! ... \*/) should be kept
or not by using the following attribute
*jawr.css.postprocessor.cssmin.keepLicence* in the configuration file.


##### [**CSS combine Media**]()

-   **Type**: Bundle
-   **Properties Key**: cssCombineMedia

This processor will wrap the content of the bundle with the "media"
value defined in the Jawr configuration file associated to this bundle.  
Jawr will search for th value associated to:
jawr.css.bundle.**bundleName**.media to find the media value to set. If
no media value is found, Jawr will set the media to **screen**.

For example, if your bundle is define like this:


            jawr.css.bundle.combine_print.id=/bundles/combine_print.css
            jawr.css.bundle.combine_print.mappings=/resources/css/combine/print_1.css,/resources/css/combine/print_2.css
            jawr.css.bundle.combine_print.bundlepostprocessors=cssCombineMedia
            jawr.css.bundle.combine_print.media=print


The content of the bundle will be wrap around a media rule declaration
set to "print".


    @media print {

            ... /* Content of the bundle goes here */
    }        


##### [**CSS Path rewriter**]()

-   **Type**: File
-   **Properties Key**: csspathrewriter

This processor rewrites relative paths in URLs for each file that is
added to a bundle. A bundle will have a different URL to that of its
contained resources, so in order for relative paths to image files and
the like to keep working, URLs must be rewritten accordingly.


##### [**CSS Import resolver**]()

-   **Type**: File
-   **Properties Key**: cssimport

This processor imports the CSS resources referenced by **@import
url(...)** statement. These resources are included in the generated CSS
bundle.

##### [**CSS base64 postprocessor**]()

-   **Type**: Bundle
-   **Properties Key**: base64ImageEncoder

This processor encodes the CSS images in base64 except for the images
which has as annotation **/\*\* jawr:base64-skip \*/** statement. This
postprocessor should not be used with the **csspathrewriter**, because
they are doing the same job rewriting the image URL.

**WARNING :** It is also important to set the base64ImageEncoder not
only as a **bundle post processor** but as a **file post processor** AND
a **bundle post processor**, because this post processor needs to
prepend the MHTML part to the bundle and also to rewrite the URL for
each file.  
So **never** use this post processor only as a bundle post processor nor
only as a file post processor.

##### [**Autoprefixer postprocessor**]()

-   **Type**: Bundle,File
-   **Properties Key**: autoprefixer

This processor adds vendor prefix to CSS resources.  
The user can define the autoprefixer options by using the
**jawr.css.autoprefixer.options** property.  
This processor is using a JS engine to perform the processing.  
The user can define the JS engine to use by using the
**jawr.css.autoprefixer.js.engine** property.  
If not set the JS engine used will be the one associated to
**jawr.js.engine**.

**WARNING :** The current version of autoprefixer doesn't work with the
Nashorn JS engine.

-   **Configuration properties**

   Jawr provides the following properties to configure the Autoprefixer
    postprocessor:

| **Property name** | **Type** | **Purpose** | **Default value** |
|-------------------|----------|-------------|-------------------|
| jawr.css.autoprefixer.script | String | The autoprefixer script location | /net/jawr/web/resource/bundle/postprocessor/css/autoprefixer/autoprefixer-5.2.1.js |
| jawr.css.autoprefixer.options | String | The autoprefixer options in JSON format | {} |
| jawr.css.autoprefixer.js.engine | String | The JS engine to use for Autoprefixer. | The value of the 'jawr.js.engine' property |

### User submitted postprocessors

Some users are so kind as to submit their own custom postprocessors. If
you have one that you think may be useful to others, please post it at
the support forum and they will be shared among the Jawr community of
users. To use any of these, download the source file and add it to your
project source. Then declare the class and a mapping key as described at
the *custom postprocessors* section above and you are ready to go. You
can even go ahead and customize the class to suit your own needs.

**Console Log Statement Remover**
---------------------------------

-   **Submitted by**: Ryan Wilson (<http://blog.augmentedfragments.com>)
-   **Source**:
    [ConsolePostProcessor.java](../java/ConsolePostProcessor.java)
-   **Type**: Bundle

This postprocessor uses a regular expression to find and remove all the
statements that write to the Firebug/Safari console. Specifically, it
removes statements with the form *window.console.log(\[...\])*. If you
use a different form you can change the regular expression in the source
file.

Thanks to Ryan Wilson for posting this work.

