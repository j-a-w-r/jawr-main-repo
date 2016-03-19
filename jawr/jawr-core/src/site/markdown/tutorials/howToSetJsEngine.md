How to setup JS engine in Jawr
------------

The goal of this tutorial is to explain how to set JS engine for
processors and generators in Jawr.  
This feature is available since the version 3.8 of Jawr.


### How it works ?

Since the version 3.8, Jawr provides a way to define which JS engine to
use. You can set the default JS engine, with the property
*jawr.js.engine*. The default value for this property is
*mozilla.rhino*. So the default JS engine is Rhino.

Since Java 8, Nashorn JS engine is provided with the JRE. So to use
Nashorn, you need to specify in your configuration file :


            # Set Nashorn as default JS engine
            jawr.js.engine=nashorn


You can also specify which JS engine to use per processor or generator,
using the dedicated property. (See the processor or generator
documentation for more detail)


            # Set Nashorn as default JS engine for the coffescript generator
            jawr.js.generator.coffee.script.js.engine=nashorn

Here is the list of processors which are using a JS engine:

-   Uglify
    \[net.jawr.web.resource.bundle.postprocess.impl.js.uglify.UglifyPostProcessor\]
-   Autoprefixer
    \[net.jawr.web.resource.bundle.postprocess.impl.AutoPrefixerPostProcessor\]

Here is the list of generators which are using a JS engine:

-   CoffeeScript
    \[net.jawr.web.resource.bundle.generator.js.coffee.CoffeeScriptGenerator\]

    