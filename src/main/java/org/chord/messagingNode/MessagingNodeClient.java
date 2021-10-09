package org.chord.messagingNode;

import org.chord.networking.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagingNodeClient extends Client {
    private static final Logger log = LoggerFactory.getLogger(MessagingNodeClient.class);

    private final MessagingNode messagingNode;

    public MessagingNodeClient(MessagingNode messagingNode) {
        this.messagingNode = messagingNode;
    }

    public MessagingNode getMessagingNode() {
        return messagingNode;
    }
}
