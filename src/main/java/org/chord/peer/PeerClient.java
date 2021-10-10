package org.chord.peer;

import org.chord.networking.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeerClient extends Client {

    private static final Logger log = LoggerFactory.getLogger(PeerClient.class);

    private final Peer messagingNode;

    public PeerClient(Peer messagingNode) {
        this.messagingNode = messagingNode;
    }

    public Peer getMessagingNode() {
        return messagingNode;
    }

}
