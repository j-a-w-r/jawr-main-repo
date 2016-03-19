Javascript i18n message generator
---------------------------------

For any given rich application, chances are that many messages will be
handled and displayed by javascript components. On the other hand, java
web applications have always supported internationalization by means of
ResourceBundles. Messages are stored in properties files which are
versioned to different languages and stored using a file naming scheme
that uses a base name for the ResourceBundle (such as 'messages') and
suffixes that specify the different locales supported (such as
'messages\_es' for spanish).

There are a number of tag libraries (including JSTL) that support the
use of such messages in JSP pages, but in order to use these messages in
javascript there isn't a practical solution for transferring all messages
to js variables. The messages generator in Jawr will do just that,
generating automatically a javascript data structure that closely
resembles the properties format. Each property is converted to a
function that returns the message, supporting variable substitution with
the standard ResourceBundle format. For example, if you message
properties file (which could be names jsmessages.properties) contains
the following messages:


        main.hello.world=Hello world!
        main.user.salutation=Welcome, {0}!


By using a generator, you will be able to access the messages from
javascript like this:


            alert( messages.main.hello.world() );
            alert( messages.main .user.salutation('John') );        


The previous script would display two consecutive alert messages,
showing the texts 'Hello world!' and 'Welcome, John!'.

Each bundle containing a messages generator can declare language
variants, so Jawr will include a language specific version of the
messages script for any given user. Continuing with the last example, if
you had a jsmessages\_es.properties file with spanish messages, such as
the following:


        main.hello.world=Hola mundo!
        main.user.salutation=Bienvenido, {0}!


For any user using the es\_ES locale, the bundle will contain the
messages from this file rather than the ones in english. This way the
scripts which access the properties will receive the spanish version
messages. The way the locale is customizable to suit any application
specific way of defining the language.


### Mapping a message generator

Message bundles are defined as mappings within a bundle, using a special
syntax. Additionally, each language variant we want to use must be
declared. A simple mapping would look as this:


        jawr.js.bundle.lib.id=/bundles/global.js
        jawr.js.bundle.lib.mappings=messages:com.myapp.jsmessages,/js/lib/**


As you can see, a mapping was added to 'messages:com.myapp.jsmessages'.
All mappings to message bundles will start with 'messages:', followed by
a pipe character '|' separated list of the basenames for the
ResourceBundles. In this case, the mapping refers to properties files
located in the classpath under 'com/myap' with the name
'jsmessages.properties'.

The locale variants for this bundle will be found automatically by Jawr.
In this example, Jawr will search for all available message bundles
which match 'com/myapp/jsmessages\[locale\_variant\].properties'. Note
that the locale, which will be used by default if the user's locale does
not match any of the defined variants, will depend on the server's
default locale. Also note that variants do not need to overwrite every
property defined in the base locale messages file. The non overwritten
properties will be read from the base file (which is the normal behavior
when using java ResourceBundles).


#### Defining the message namespace.

You probably noticed that in the previous example, when referencing the
messages from javascript the properties were referenced using the name
of the property preceded by 'messages.'. Jawr will by default define a
'messages' variable and add all properties as attributes for this
variable. 'foo.bar' is going to be referenced always as
'messages.foo.bar()'. This is done so in order to avoid polluting the
javascript properties namespace: imagine you had a function named foo()
and a property named foo.hello: there would be a collision between the
two names.

You can change the name used for the messages namespace to whatever you
want. You will need to do it if you use two generators in the same page,
because otherwise the messages namespace for the second generator will
overwrite the first one's. So if you have a general messages
ResourceBundle and you use others for page specific messages, you should
declare those with a different namespace.

To define a namespace you enclose it in parentheses and add it to the
end of the mapping:


        jawr.js.bundle.lib.mappings=messages:com.myapp.messages(mynamespace),/js/lib/**    


With this mapping, you would access the 'hello.world' message using
'mynamespace.hello.world()'.

#### Filtering messages.

If you have each and every message for your application defined in a
single ResourceBundle, it might be impractical to pass all the messages
to javascript, since many will never be used. In this case, you can
specify a name filter (such as javascript.messages) so that only
properties for which the name starts with the filter are passed to
javascript. To do that you enclose the filter in brackets and add it to
the end of the mapping:

        jawr.js.bundle.lib.mappings=messages:com.myapp.messages[javascript.messages],/js/lib/**    


With this mapping, only the properties having a key that starts with
javascript.messages (like 'javascript.messages.hello.world=Hello
world!') will be included in the generated javascript.

If you define both a namespace and a filter, remember that the filter
needs to be defined after the namespace:


        jawr.js.bundle.lib.mappings=messages:com.myapp.messages(mynamespace)[javascript.messages],/js/lib/**    


#### Locale resolution.

There is not a universal way to determine which Locale to use for a
given user. Many applications roll their own, so Jawr has a way to plug
in a component to take care of custom Locale resolution strategies.
Normally Jawr will use the request.getLocale() method to find out the
user's Locale. If your app has different needs, you will have to create
a class that implements a simple interface
(net.jawr.web.resource.bundle.locale.LocaleResolver). This interface
defines a single method:


       public abstract String resolveLocaleCode(HttpServletRequest request);  


Your implementation will take the request, determine the user's locale
an return its String representation. Jawr will take care of the rest. To
plug in your class, you will define it in the properties file under the
key jawr.locale.resolver:


       jawr.locale.resolver=com.mypackage.myapp.MyLocaleResolverImplementation


Your class will be instantiated using reflection, so make sure it has a
public default no-arg constructor.


#### Grails usage.

Grails users do not need to implement the LocaleResolver interface,
since the default strategy used by Grails will be leveraged by Jawr.  
The Jawr Grails plugin will override the default message generator to
use the Jawr Grails one.  
Keep in mind that the default qualified name for the Grails
Resourcebundle is grails-app.i18n.messages, so map accordingly:


        jawr.js.bundle.lib.mappings=messages:grails-app.i18n.messages


Grails users can also use message from plugin using :

-   the name of the plugin (myPlugin)
-   the plugin path (my-plugin-1.4.6)

For example :

        jawr.js.bundle.lib.mappings=messages:messages:/plugins/my-plugin-1.4.6/grails-app/i18n/errors

Or 

        jawr.js.bundle.lib.mappings=messages:messages:/plugins/myPlugin/grails-app/i18n/errors

