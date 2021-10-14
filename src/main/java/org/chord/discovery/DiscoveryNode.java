package org.chord.discovery;

import org.chord.util.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryNode {
    private static final Logger log = LoggerFactory.getLogger(DiscoveryNode.class);

    public String hostname;

    public DiscoveryNode() {
        this.hostname = Host.getHostname();
        log.info("Started Discovery Node on {}", this.hostname);
    }

    public void startServer() {
        new DiscoveryNodeServer(this).launchAsThread();
    }

}
