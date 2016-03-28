How to use Smart bundling feature in Jawr
------------

### Smart bundling feature

Historically, Jawr regenerate all defined bundles at server startup.
Depending on the bundle definition, the start-up time could be long. To
fix this issue, Jawr introduces since Jawr 3.9 a new feature called
"smart bundling" which allow Jawr analyze the bundle which have changed,
and only rebuild these bundles. The smart bundling feature extends
existing features to help to speed up the bundling process. This
tutorial will explain you how to setup Jawr to use this feature.

The property **jawr.use.bundle.mapping** introduced in Jawr 3.0, when
set to true, force Jawr to create files which contains the result of the
bundle process.  
Once generated, Jawr was not rebuilding the bundles. This was a good
improvement to speed up start up time, but unfortunately when the
bundles were modified the bundles were not rebuild. 

Now Jawr is able to determine if the bundles have changed since the last
bundle processing, in every case (server started or stopped).

The property **jawr.config.reload.interval** was previously used to
determine the interval between Jawr configuration file reload to check
if there were changes in Jawr configuration. Now Jawr checks also if the
content of the files associated to the bundles have changed. This means
that if a file content changed Jawr will rebuild the bundles.

### Enabling "Smart bundling"

By default, the smart bundling feature is activated. The property
**jawr.use.smart.bundling** can be used to unable or disable it.


            jawr.use.smart.bundling=true #This will enable the smart bundling feature
            jawr.use.bundle.mapping=true #This will make allow Jawr to cache information on resource bundling


### Smart bundling with automatic bundle processing when application is started

If you want to use the automatic bundle processing when your application
is started, you have update your Jawr configuration file like below :

            jawr.config.reload.interval=10 #Here we configured Jawr to check for update every 10 sec

Using this configuration, if a change is detected on a bundle when the
server is started, the bundle will be rebuild.


### Smart bundling with automatic bundle processing when application is started and force bundling using JMX

If you want to use the automatic bundle processing when your application
is started, and also being able to force the refresh using JMX, you have
update your Jawr configuration file like below :

            jawr.config.reload.interval=3600 #Here we configured Jawr to check for update every hour
            

Using this configuration, if a change on a bundle is detected when Jawr
checks the configuration, the bundle will be rebuild. Please check the
[JMX documentation](../docs/jmx_support.html) for more detail on how to
setup JMX for Jawr.

To force bundle processing using JMX, you need to execute the method
**refreshConfig()**. You'll be able to refresh the configuration for one
resource type, or for all resources by selecting the right
**JawrConfigManagerMBean** or the **JawrApplicationConfigManagerMBean**.
Please refer to the [JMX documentation](../docs/jmx_support.html) for
more detail.

Note : The recommended way to force rebuild is to use JMX. It's a more
secure way to handle this case. In some cases, JMX could not be an
option, in these cases, you can use the refreshKey URL parameter. (see
below)

### Smart bundling with automatic bundle processing when application is started and a key to force bundle processing

If you want to use the automatic bundle processing when your application
is started, and also being able to force the refresh using a key, you
have update your Jawr configuration file like below :

            jawr.use.bundle.mapping=true #This will make allow Jawr to cache information on resource bundling
            jawr.config.reload.interval=3600 #Here we configured Jawr to check for update every hour
            jawr.config.reload.refreshKey=mySecretKey #To force a refresh of bundles which needs to be rebuild, hit any bundle URL and add ?refreshKey=mySecretKey to reload the bundles.
            

Using this configuration, if a change on a bundle is detected when Jawr
checks the configuration or if the refresh is forced using the refresh
key parameter, the bundle will be rebuild. The information are stored in
the bundle mapping files.

### Smart bundling only on server restart

If you want to use the smart bundling feature only on server restart,
you only have to set the **jawr.use.bundle.mapping** property to
**true**, in your Jawr configuration file like below :


            jawr.use.bundle.mapping=true #This will make allow Jawr to cache information on resource bundling


Using this configuration, if a change is detected on a bundle when the
server is started, the bundle will not be rebuild until next server
restart.

### Delay since last resource event

Jawr watch resources on server to detect resource creation, modification or deletion using WatchService.
When there are a lot of events, to ensure that all events have been processed before staart the bundling process, Jawr use a delay after the last event to make sure that there no more event to process before starting the bundling process.

This delay is configurable using the property **jawr.smart.bundling.delay.after.last.event** :

| **Property name** | **Type** | **Purpose** | **Default value** |
|-------------------|----------|-------------|-------------------|
| jawr.smart.bundling.delay.after.last.event | Integer | Defines the delay after the last event before starting the bundle processing (in second) | 2 |


### Global processing

Jawr allows the user to defines processor which can process all the
bundles at once. Some examples of these processor are the smartsprite
global preprocessor, which will retrieve information about CSS sprite
defines in every bundle and which will update sprite reference in all the bundles.


### Smartsprite global preprocessing

By nature, it is not possible to avoid the processing of all bundle for
these global processors, but Jawr will try to find the impacted bundles
after the global preprocessing phase, to only process those bundles.

To make sure that change are detected, you need to put **\$[md5]()**
property in the image of the sprite-ref, like below :


            
    /** sprite: mysprite; sprite-image: url('../../img/mysprite-${md5}.png'); sprite-layout: vertical */ 

    .header {
            background-image: url("../../../img/logo.png"); /** sprite-ref: mysprite; */
            background-repeat: no-repeat;
            height : 74px;
    }


### Force complete rebuild

There is two way to force the complete rebuild :
 - Clean the web application working directory where the files for cache are stored
 - Modify your Jawr configuration file


### Known limitations

For the time being, this feature only works with modification on the file resources associated to bundle like :
 - modifying a resource
 - creating or deleting a resource on a bundle which has a mapping defines with wildcards
 For example, if you have a bundle mapping like '/css/**', adding a resource to '/css' or deleting one will be handle as a bundle modification
   
If you modify your configuration, Jawr will consider this as a complete modification and will rebuild all the bundles.
So a modification in your configuration will trigger a full bundling process.

Jawr doesn't detect modification on Processor or Generator class. So if a Processor or a Generator has been modified, you'll have to clean your cache so Jawr will rebuild the bundles with this modification.
