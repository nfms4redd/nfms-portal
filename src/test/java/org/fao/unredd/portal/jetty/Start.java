package org.fao.unredd.portal.jetty;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.thread.BoundedThreadPool;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Jetty starter, will run Portal inside the Jetty web container.<br>
 * Useful for debugging, especially in IDE were you have direct dependencies between the sources of
 * the various modules (such as Eclipse).
 * 
 * @author wolf
 */
public class Start {
    private static final Logger log = Logger.getLogger(Start.class.getName());

    public static void main(String[] args) {
        Server jettyServer = null;

        try {
            jettyServer = new Server();
            BoundedThreadPool tp = new BoundedThreadPool();
            tp.setMaxThreads(50);

            SocketConnector conn = new SocketConnector();
            String portVariable = System.getProperty("jetty.port");
            int port = parsePort(portVariable);

            if (port <= 0) {
                port = 8084;
            }

            conn.setPort(port);
            conn.setThreadPool(tp);
            conn.setAcceptQueueSize(100);
            jettyServer.setConnectors(new Connector[] { conn });

            WebAppContext wah = new WebAppContext();
            wah.setContextPath("/portal");
            wah.setWar("src/main/webapp");
            jettyServer.setHandler(wah);

            jettyServer.start();

            // use this to test normal stop behavior, that is, to check stuff
            // that
            // need to be done on container shutdown (and yes, this will make
            // jetty stop just after you started it...)
            // jettyServer.stop();
        } catch (Throwable e) {
            log.log(Level.SEVERE, "Could not start the Jetty server: " + e.getMessage(), e);

            if (jettyServer != null) {
                try {
                    jettyServer.stop();
                } catch (Exception e1) {
                    log.log(Level.SEVERE, "Unable to stop the " + "Jetty server:" + e1.getMessage(), e1);
                }
            }
        }
    }

    private static int parsePort(String portVariable) {
        if (portVariable == null) {
            return -1;
        }

        try {
            return Integer.valueOf(portVariable).intValue();
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
