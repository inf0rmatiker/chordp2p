package org.chord.messagingNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagingNode {
    private static final Logger log = LoggerFactory.getLogger(MessagingNode.class);

    private final String hostname;
    private final int port;

    public MessagingNode(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }
}
