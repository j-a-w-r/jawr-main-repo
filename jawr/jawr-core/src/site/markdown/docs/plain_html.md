Using Jawr with plain HTML pages
--------------------------------

Many applications have a mix of plain HTML and dynamic pages (be it JSP,
Facelets, etc.) While in dynamic pages you use a tag library to import
your bundles, that is not feasible with plain HTML pages for obvious
reasons. While you could link to the resources with regular links, this
would be bad for two reasons: first, you lose all the compression and
bundling abilities of Jawr, and second, you will load scripts that are
probably part of a bundle, so when a user goes visits a dynamic page he
will not have any script in cache, losing efficiency.

### The solution: using the script loader

To solve this problem Jawr provides a way to still use all of its
features in plain HTML pages. The mechanism is very simple: you add a
link to a special script named **/jawr\_loader.js** and use it to add
bundles just like you would with the tag library. This script contains
the mappings of all the bundles and also several methods you invoke to
add links to the page.

For example, imagine you have a JSP with the following tags:

             <jwr:style src="/bundles/all.css" />
             <jwr:script src="/bundles/global.js"/> 


To add the same bundles to a plain HTML page, you would include the
following code in the document HEAD section:


            <script type="text/javascript" src="./jawr_loader.js" ></script>
            <script>
                    JAWR.loader.style('/bundles/all.css');
                    JAWR.loader.script('/bundles/global.js');
            </script>


As you can see, first we include the link to the jawr\_loader.js script.
Note that the link needs to change according to the relative location of
the page within the application. Also, if the Jawr servlet is mapped to
something like '/jawr/\*' instead of directly attending all .js
requests, you will have to modify the URL accordingly (i.e.
'./jawr/jawr\_loader.js' for instance).

After including the script, there is an inline script where the actual
bundles are included using to methods provided by Jawr. These act
exactly like the JSP tag libraries. This means that the links they will
generate are consistent to the ones a user will find in your JSP pages,
so the browser cache will come into play as it should.

If you only plan to use global bundles in a page, you can skip the
inline script and just go with the link to jawr\_loader .js, since said
script will already add all the global bundles.


### Loader script syntax

**JAWR.loader.script** receives a single parameter, which is either the
name of a bundle or the name of a script belonging to a bundle. In
either case, the resulting link will be the same.

                    JAWR.loader.script('/bundles/global.js');
                    JAWR.loader.script('/js/aMemberOfSomeBundle.js');


**JAWR.loader.style** has a first parameter with the same effect as in
the script function, plus an optional 'media' parameter which will set
the media attribute in the style link. If not provided, the media
attribute is set to *screen*.


            JAWR.loader.style('/bundles/all.css');
            JAWR.loader.style('/bundles/all_print.css' 'print');


Note that these functions are meant to be invoked while the page is
loading (document.write is used). In the future an on-demand includer
version will be available, but for now don't try to invoke these
functions after the page has loaded.


### The tradeoff

There is a penalty in using this technique: an additional script is
included in every page. The loader script is itself small in size
(although this will vary according to the number of scripts in your
app). A worst-case scenario yielded a 14Kb size, with 4-6Kb being a more
normal size. Of course, the script is minified and gzipped according to
the configuration, so the final size the client receives will be notably
smaller (3-5Kb at worst). Even so, this should still reduce the total
number of imported scripts and its size. Also, all the imported
resources remain in cache so when the user visits a JSP that uses the
same ones, the page will load really fast.
