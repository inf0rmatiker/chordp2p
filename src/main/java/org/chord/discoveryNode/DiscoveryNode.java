package org.chord.discoveryNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryNode {
    private static final Logger log = LoggerFactory.getLogger(DiscoveryNode.class);

    private final String discoveryNodeHostname;
    private final int discoveryNodePort;

    public DiscoveryNode(String discoveryNodeHostname, int discoveryNodePort) {
        this.discoveryNodeHostname = discoveryNodeHostname;
        this.discoveryNodePort = discoveryNodePort;
    }


    public String getDiscoveryNodeHostname() {
        return discoveryNodeHostname;
    }

    public int getDiscoveryNodePort() {
        return discoveryNodePort;
    }
}
