package org.chord.discovery;

import org.chord.messaging.Message;
import org.chord.messaging.NetworkJoinNotification;
import org.chord.messaging.NetworkExitNotification;
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
    private ConcurrentHashMap<String, Identifier> registeredPeers;
    private Random random;

    public DiscoveryNodeProcessor(Socket socket, DiscoveryNode discoveryNode) {
        this.socket = socket;
        this.discoveryNode = discoveryNode;
        registeredPeers = new ConcurrentHashMap<>();
        random = new Random();
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
            default:
                log.error("Unimplemented Message type: \"{}\"", message.getType());
        }
    }

    private void processPeerExitNotification(NetworkExitNotification message) {
        String id = message.getPeerId().id;
        if (registeredPeers.containsKey(id)) {
            registeredPeers.remove(id);
            log.info("Peer '{}' left the network", id);
        } else {
            log.warn("Peer '{}' does not exist. Unable to remove", id);
        }
    }

    private void processNetworkJoinNotification(NetworkJoinNotification message) {
        registeredPeers.put(message.peerId.id, new Identifier(message.hostname, message.peerId.id));
        log.info("{} successfully joined the network with ID '{}'", message.hostname, message.peerId.id);
    }

    private void processRegisterPeerRequest(RegisterPeerRequest message) {
        String id = message.getId();
        log.info("{} attempting to join with ID '{}'", message.getHostname(), id);

        RegisterPeerResponse response;
        if (registeredPeers.isEmpty()) {
            log.info("{} - First peer to join the network", message.getHostname());
            response = new RegisterPeerResponse(Host.getHostname(), Host.getIpAddress(), true,
                    message.getId(), message.getHostname());
            sendResponse(this.socket, response);
            return;
        }

        if (registeredPeers.containsKey(id)) {
            // id collision detected
            log.warn("Peer with ID {} already exists.", id);
            response = new RegisterPeerResponse(Host.getHostname(), Host.getIpAddress(), false,
                    message.getId(), message.getHostname());
        } else {
            int noOfRegisteredPeers = registeredPeers.size();
            String randomPeerId = new ArrayList<>(registeredPeers.keySet()).get(random.nextInt() % noOfRegisteredPeers);
            Identifier randomPeer = registeredPeers.get(randomPeerId);
            response = new RegisterPeerResponse(Host.getHostname(), Host.getIpAddress(), true,
                    randomPeerId, randomPeer.getHostname());
        }

        sendResponse(this.socket, response);
    }
}
