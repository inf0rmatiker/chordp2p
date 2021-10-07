package org.chord.messagingNode;


import org.chord.networking.Server;
import org.chord.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;

public class MessagingNodeServer extends Server {
    private static final Logger log = LoggerFactory.getLogger(MessagingNodeServer.class);

    private final MessagingNode messagingNode;

    public MessagingNodeServer(MessagingNode messagingNode) {
        this.messagingNode = messagingNode;
        this.bindToPort(Constants.MessagingNode.PORT);
    }

    @Override
    public void processConnection(Socket clientSocket) {

    }
}
