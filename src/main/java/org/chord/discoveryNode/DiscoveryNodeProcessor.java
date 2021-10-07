package org.chord.discoveryNode;

import org.chord.messaging.Message;
import org.chord.networking.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;

public class DiscoveryNodeProcessor extends Processor {
    private static final Logger log = LoggerFactory.getLogger(DiscoveryNodeProcessor.class);

    public DiscoveryNode discoveryNode;

    public DiscoveryNodeProcessor(Socket socket, DiscoveryNode discoveryNode) {
        this.socket = socket;
        this.discoveryNode = discoveryNode;
    }

    @Override
    public void process(Message message) {

    }
}
