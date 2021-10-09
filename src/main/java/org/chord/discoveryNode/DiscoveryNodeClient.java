package org.chord.discoveryNode;

import org.chord.networking.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryNodeClient extends Client {
    private static final Logger log = LoggerFactory.getLogger(DiscoveryNodeClient.class);

    private DiscoveryNode discoveryNode;

    public DiscoveryNodeClient(DiscoveryNode discoveryNode) {
        this.discoveryNode = discoveryNode;
    }

    public DiscoveryNode getDiscoveryNode() {
        return discoveryNode;
    }
}
