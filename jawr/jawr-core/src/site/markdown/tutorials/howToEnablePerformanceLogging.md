How to enable performance logging in Jawr
------------

The goal of this tutorial is to enable performance logging in Jawr.  
This feature is available since the version 3.8 of Jawr.

### How it works ?

Since the version 3.8, Jawr provides a way to enable logging to get
performance information about the processing and the request.

There are 2 kinds of performance logging :

-   net.jawr.perf.processing : processing performance, the performance
    of the bundle processing and generation
-   net.jawr.perf.request : request handling performance, the
    performance of Jawr to respond to a request for a resource.

To enable performance logging, you need to define in your log
configuration file the DEBUG level for this logger.

For example, if you would like to monitor the processing phase, if
you're using log4j,  
you would declare in your log4j file :


    # Unable performance processing logging
    log4j.logger.net.jawr.perf.processing=DEBUG


To monitor request handling, you would declare in your log4j file :


    # Unable performance request handling logging
    log4j.logger.net.jawr.perf.request=DEBUG


To monitor request handling and bundle processing, you would declare in
your log4j file :


    # Unable performance processing and request logging
    log4j.logger.net.jawr.perf=DEBUG

