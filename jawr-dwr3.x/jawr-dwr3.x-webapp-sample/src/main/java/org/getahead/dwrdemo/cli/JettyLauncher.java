package org.getahead.dwrdemo.cli;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;


/**
 * Launch Jetty embedded.
 */
public class JettyLauncher
{
    /**
     * Sets up and runs server.
     * @param args The command line arguments
     * @throws Exception Don't care because top level
     */
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);
        server.setStopAtShutdown(true);
        server.setHandler(new WebAppContext("web","/dwr"));
        server.start();
        server.join();
    }
}
