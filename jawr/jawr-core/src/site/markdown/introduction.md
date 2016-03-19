Introduction
------------

### Motivation

The current trend for richer web applications has led to an increased
use of javascript for most web applications. This in turn means that the
javascript used now is more complex, using object or module oriented
approaches. However, a problem arises when the project modules are
organized in separate files. The nature of the HTTP protocol makes it
undesirable to have clients download many separate files. Content
negotiation and the fact that normally there will only be two concurrent
connections to the same host produce an overhead that results in
unacceptably long page loading times. Thus, it is faster to serve a 10KB
script file than to serve, say, eight 1KB separate script files. So,
from this perspective it is desirable to have as least files to serve as
possible.

The problem lies in the fact that, as javascript components grow in
complexity, it becomes increasingly inconvenient to keep all the code in
the same file. To change parts of the code, developers must first scan a
potentially huge file to find the parts that need change. When working
on related modules on the same file, it is necessary to scroll back and
forth to each of them.  

A related problem is an increased difficulty in tracking of changes when
using version control. Since all the modules reside in the same file,
any change made to a module at any point in time will be hard to find,
since every change to a module shows in version control history as a
change to the unified script file. To make things worse, if a file
locking version control system is being used, developers will not be
able to concurrently make changes to different modules at the same time.
 

A partial solution is to develop modules in separate files and use the
project build script (such as an Ant script) to join them as needed. The
caveat is that this will slow down the development process. Any small
change forces the developer to re-run the build script before the
changes can be tested. An alternative is to change the script import
declarations in the pages that use them in development time, so that an
exploded WAR deployment can be used to quickly see any change in the
scripts.  

### Goals

Thus, the goal for Jawr is to provide a system to easily map resources
to bundles using a simple descriptor, and a tag library to import these
bundles to JSP pages. Jawr has a development mode in which the script
files are imported separately and a production mode in which scripts are
bundled as specified in the descriptor. The tag library works in such a
way that there is no need to make any change in JSPs when any of these
modes is activated.  

Since code ordering matters in Javascript, it will be possible to easily
force the inclusion order of modules in the bundles. Also, Jawr provides
postprocessors to modify the generated bundles. These will for instance
provide minification, to reduce the size of the served files by removing
comments and unneeded whitespace. Although comments are removed, it is
possible to include the mandatory licenses for open source libraries.  

The bundles are served by a servlet which will use gzip compression for
compatible browsers. The bundles are created at startup time, in a
gzipped and not gzipped version, so no processing overhead is added to
requests. The zipped and unzipped versions will have different URLs, so
caching proxies will pose no problems when using Jawr. Also, every
bundle will have a version prefix in its URl which will be changed by
modifying the configuration file. Jawr will use aggressive caching
headers to ensure the bundles are requested only once by each client,
and the versioning prefix will be used to force clients to update to
newer versions of the files.  

Additionally, Jawr can bundle CSS files as well. Minification is
provided as well, and any relative URL within a CSS added to a bundle
will be conveniently rewritten so that its link is not broken.


### References

At <http://www.die.net/musings/page_load_time/> there are several
benchmarks that show how serving uncompressed, separate files is
considerably worse than using unified files.

There is an interesting read about the subject, by Cal Henderson of
Flickr, at
<http://www.thinkvitamin.com/features/webapps/serving-javascript-fast>.
It discusses solutions for PHP sites that Jawr tries to implement in the
Java web application space.  


