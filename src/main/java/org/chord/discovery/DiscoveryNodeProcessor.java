package org.chord.discovery;

import org.chord.messaging.GetRandomPeerRequest;
import org.chord.messaging.GetRandomPeerResponse;
import org.chord.messaging.Message;
import org.chord.messaging.NetworkJoinNotification;
import org.chord.messaging.NetworkExitNotification;
import org.chord.messaging.PeerIdentifierMessage;
import org.chord.messaging.RegisterPeerRequest;
import org.chord.messaging.RegisterPeerResponse;
import org.chord.networking.Processor;
import org.chord.peer.Identifier;
import org.chord.util.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

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
            case NETWORK_JOIN_NOTIFICATION:
                processNetworkJoinNotification((NetworkJoinNotification) message);
                break;
            case NETWORK_EXIT_NOTIFICATION:
                processPeerExitNotification((NetworkExitNotification) message);
                break;
            case GET_RANDOM_PEER_REQUEST:
                processGetRandomPeerRequest((GetRandomPeerRequest) message);
                break;
            default:
                log.error("Unimplemented Message type: \"{}\"", message.getType());
        }
    }

    private void processGetRandomPeerRequest(GetRandomPeerRequest message) {
        log.info("StoreData ({}) requesting random peer", message.hostname);
        Identifier randomPeer = discoveryNode.getRandomPeer();
        GetRandomPeerResponse response = new GetRandomPeerResponse(Host.getHostname(), Host.getIpAddress(), randomPeer);
        sendResponse(this.socket, response);
    }

    private void processPeerExitNotification(NetworkExitNotification message) {
        boolean ok = this.discoveryNode.remove(message.getPeerId());
        if (!ok) {
            log.error("Peer does not exist. Unable to remove: {}", message.getPeerId());
        } else {
            log.info("Peer removed from the discovery node: {}", message.getPeerId());
        }
    }

    private void processNetworkJoinNotification(NetworkJoinNotification message) {
        boolean ok = this.discoveryNode.put(message.getPeerId());
        if (!ok) {
            log.error("Unable to add peer to list of tracked peers: {}", message.getPeerId());
        } else {
            log.info("Successfully added peer to list of tracked peers: {}", message.getPeerId());
        }
    }

    private void processRegisterPeerRequest(RegisterPeerRequest message) {
        log.info("{} requesting registration", message.getPeerId());

        RegisterPeerResponse response;
        if (this.discoveryNode.isEmpty()) {
            log.info("{} - First peer to join the network", message.getHostname());
            response = new RegisterPeerResponse(Host.getHostname(), Host.getIpAddress(), message.getPeerId(),
                    true);
            sendResponse(this.socket, response);
            return;
        }

        if (this.discoveryNode.alreadyExists(message.getPeerId())) { // id collision detected
            log.warn("Peer with ID {} already exists.", message.getPeerId().getId());
            response = new RegisterPeerResponse(Host.getHostname(), Host.getIpAddress(), message.getPeerId(),
                    false);
        } else {
            response = new RegisterPeerResponse(
                    Host.getHostname(),
                    Host.getIpAddress(),
                    this.discoveryNode.getRandomPeer(),
                    true);
        }
        sendResponse(this.socket, response);
    }
}
