package org.chord.peer;


import org.chord.networking.Processor;
import org.chord.networking.Server;
import org.chord.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;

public class PeerServer extends Server {
    private static final Logger log = LoggerFactory.getLogger(PeerServer.class);

    private final Peer peer;

    public PeerServer(Peer messagingNode) {
        this.peer = messagingNode;
        this.bindToPort(Constants.Peer.PORT);
    }

    @Override
    public void processConnection(Socket clientSocket) {
        Processor peerProcessor = new PeerProcessor(clientSocket, this.peer);
        peerProcessor.launchAsThread();
    }
}
