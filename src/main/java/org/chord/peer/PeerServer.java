package org.chord.peer;


import org.chord.networking.Server;
import org.chord.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;

public class PeerServer extends Server {
    private static final Logger log = LoggerFactory.getLogger(PeerServer.class);

    private final Peer messagingNode;

    public PeerServer(Peer messagingNode) {
        this.messagingNode = messagingNode;
        this.bindToPort(Constants.MessagingNode.PORT);
    }

    @Override
    public void processConnection(Socket clientSocket) {

    }
}
