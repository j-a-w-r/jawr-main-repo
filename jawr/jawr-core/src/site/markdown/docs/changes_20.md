Changes and new features in version 2.0
---------------------------------------

### New features

-   **Bundle prefixes are now automatic**: It will no longer necessary
    to keep a track on bundle prefixes when deploying new versions. Jawr
    will generate prefixes based on the hash of generated bundles. The
    prefix will change only when changes are made to members of a
    bundle, so unchanged bundles will keep the previous bundle (thanks
    to frankern for the idea).
-   **Minification and obfuscation using the YUI compressor**: Now the
    YUI Compressor is available as a postprocessor to minify both js and
    CSS files. It can also obfuscate javascript.
-   **Grails support**: a plugin has been created to use with Grails
    (AKA Groovy on rails). Documentation can be found at [this
    page](../integration/grails.html).
-   **Facelets support**: Jawr can now be used wiithin Facelets
    applications, using a newly created, specific tag library. Check
    [the documentation](../integration/facelets.html).
-   **Internet Explorer only bundles**: now a bundle can be declared
    with a conditional comment expression, using the property
    jawr.\[type\].bundle.\[name\].ieonly.condition (for instance,
    'jawr.js.bundle.mybundle.ieonly.condition=if lte IE 6'). When
    imported to a page, Jawr will generate a conditional comment around
    the bundle, using the provided expression.
-   It is now possible to use **-Dnet.jawr.debug.on** as a jvm startup
    option to override debug mode configuration. Useful to add to
    production server startup script, avoiding to set debug mode in
    production by mistake.
-   Better error reporting for JSmin errors. A trace shows the line
    where the error happened, plus some of The previous content.

### Bug fixes

-   From the first release there has been an issue with css images when
    using a mapping to the servlet ('/somepath/\*' instead
    of '/\*.css'). The paths were broken in debug mode and now this has
    been fixed.
-   Issue \#7: Fixed random parameter skipping when debug and gzip
    options are activated.
-   Issue \#14: Domain relative URLs in CSS files are left unchanged by
    the postprocessor.
-   Issue \#16: CSS rules with whitespace other than spaces were being
    joined by the minifier.
-   Issue \#17: Using HTML comments was problematic when using
    conditional IE comments. Now Jawr generates a script tag with an
    embedded comment instead of an HTML comment.
-   Issue \#19: Classloader problem in Tomcat made config
    reloading impossible.
-   NPE in Tomcat during Jawr servlet init process.
-   Fixed a bug in config system, by which jawr.\[\].use.cache had no
    effect in config.
-   Fixed a bug in CachedResourceBundlesHandler, non-gzipped files were
    being served empty upon a first request. It's a fix for a problem
    known as 'works for debug only' in the forums.
