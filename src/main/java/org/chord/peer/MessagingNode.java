package org.chord.peer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagingNode {
    private static final Logger log = LoggerFactory.getLogger(MessagingNode.class);

    private final String discoveryNodeHostname;
    private final int discoveryNodePort;

    public MessagingNode(String discoveryNodeHostname, int discoveryNodePort) {
        this.discoveryNodeHostname = discoveryNodeHostname;
        this.discoveryNodePort = discoveryNodePort;
    }

    public String getHostname() {
        return discoveryNodeHostname;
    }

    public int getDiscoveryNodePort() {
        return discoveryNodePort;
    }

    public void startServer() {
        new MessagingNodeServer(this).launchAsThread();
    }
}
