package org.chord.discovery;

import org.chord.messaging.Message;
import org.chord.messaging.RegisterPeerRequest;
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
        log.info("Processing {} Message from {}", message.getType(), message.getHostname());
        switch (message.getType()) {
            case REGISTER_PEER_REQUEST:
                processRegisterPeerRequest((RegisterPeerRequest) message);
                break;
            default:
                log.error("Unimplemented Message type: \"{}\"", message.getType());
        }
    }

    private void processRegisterPeerRequest(RegisterPeerRequest message) {
        String id = message.getId();
        log.info("{} trying to join with ID '{}'", message.getHostname(), id);
    }
}
