package org.chord.peer;

import org.chord.messaging.Message;
import org.chord.networking.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;

public class PeerProcessor extends Processor {

    private static final Logger log = LoggerFactory.getLogger(PeerProcessor.class);

    private final Peer messagingNode;

    public PeerProcessor(Socket socket, Peer messagingNode) {
        this.socket = socket;
        this.messagingNode = messagingNode;
    }

    @Override
    public void process(Message message) {

    }

    public Peer getMessagingNode() {
        return messagingNode;
    }
}
