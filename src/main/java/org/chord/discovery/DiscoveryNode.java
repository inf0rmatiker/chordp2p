package org.chord.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryNode {
    private static final Logger log = LoggerFactory.getLogger(DiscoveryNode.class);

    public DiscoveryNode() {

    }

    public void startServer() {
        new DiscoveryNodeServer(this).launchAsThread();
    }

}
