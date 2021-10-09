package org.chord.discoveryNode;

import org.chord.networking.Processor;
import org.chord.networking.Server;
import org.chord.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;

public class DiscoveryNodeServer extends Server {
    private static final Logger log = LoggerFactory.getLogger(DiscoveryNodeServer.class);

    private final DiscoveryNode discoveryNode;

    public DiscoveryNodeServer(DiscoveryNode discoveryNode) {
        this.discoveryNode = discoveryNode;
        this.bindToPort(Constants.DiscoveryNode.PORT);
    }

    public DiscoveryNode getDiscoveryNode() {
        return discoveryNode;
    }

    @Override
    public void processConnection(Socket clientSocket) {
        Processor processor = new DiscoveryNodeProcessor(clientSocket, getDiscoveryNode());
        processor.launchAsThread();
    }
}
