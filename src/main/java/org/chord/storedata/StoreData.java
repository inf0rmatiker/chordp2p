package org.chord.storedata;

import org.chord.discovery.DiscoveryNodeServer;
import org.chord.networking.Node;
import org.chord.util.Host;
import org.chord.util.InteractiveCommandParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreData extends Node {
    private static final Logger log = LoggerFactory.getLogger(StoreData.class);

    private final String discoveryNodeHostname;
    private final int discoveryNodePort;

    // Hostname of the StoreData node
    public String hostname;

    private InteractiveCommandParser commandParser;

    public StoreData(String discoveryNodeHostname, int discoveryNodePort) {
        this.discoveryNodeHostname = discoveryNodeHostname;
        this.discoveryNodePort = discoveryNodePort;
        this.hostname = Host.getHostname();
        commandParser = new InteractiveCommandParser(this);
    }

    public String getHostname() {
        return hostname;
    }

    public void addFile(String filePath) {
        log.info("add-file {}", filePath);
        // TODO: implement
    }

    public void startServer() {
        new StoreDataServer(this).launchAsThread();
        commandParser.start();
    }
}
