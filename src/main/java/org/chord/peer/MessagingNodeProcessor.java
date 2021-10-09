package org.chord.peer;

import org.chord.messaging.Message;
import org.chord.networking.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;

public class MessagingNodeProcessor extends Processor {
    private static final Logger log = LoggerFactory.getLogger(MessagingNodeProcessor.class);

    private final MessagingNode messagingNode;

    public MessagingNodeProcessor(Socket socket, MessagingNode messagingNode) {
        this.socket = socket;
        this.messagingNode = messagingNode;
    }

    @Override
    public void process(Message message) {

    }

    public MessagingNode getMessagingNode() {
        return messagingNode;
    }
}
