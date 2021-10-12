package org.chord.peer;

import org.chord.messaging.*;
import org.chord.networking.Client;
import org.chord.util.Constants;
import org.chord.util.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class Peer {
    private static final Logger log = LoggerFactory.getLogger(Peer.class);

    private final String discoveryNodeHostname;
    private final int discoveryNodePort;

    private FingerTable fingerTable;
    private Identifier identifier;
    private Identifier predecessor;
    private Identifier successor;

    public Peer(String discoveryNodeHostname, int discoveryNodePort, Identifier identifier) {
        this.discoveryNodeHostname = discoveryNodeHostname;
        this.discoveryNodePort = discoveryNodePort;
        this.identifier = identifier;
        this.fingerTable = new FingerTable(Constants.FINGER_TABLE_SIZE, identifier);
    }

    public String getHostname() {
        return discoveryNodeHostname;
    }

    public int getDiscoveryNodePort() {
        return discoveryNodePort;
    }

    public FingerTable getFingerTable() {
        return fingerTable;
    }

    /**
     * Joins the Peer to the Chord ring network:
     * 1. Sends a RegisterPeerRequest to the discovery node, with the proposed id of our peer
     * 2. Receives a RegisterPeerResponse from the discovery node, with:
     *     - If request accepted, the id and contact information of a random node in the network
     *     - If request rejected, we retry with a different proposed id
     */
    public void joinNetwork() {

        RegisterPeerRequest registerRequest = new RegisterPeerRequest(
                Host.getHostname(),
                Host.getIpAddress(),
                this.identifier.getId()
        );

        try {

            // Send registration request to discovery node, wait for response
            Socket clientSocket = Client.sendMessage(this.discoveryNodeHostname, this.discoveryNodePort, registerRequest);
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
            RegisterPeerResponse rprResponse = (RegisterPeerResponse) MessageFactory.getInstance().createMessage(dataInputStream);

            if (rprResponse.getIsValidRequest()) {

                // We are the first node in the network
                if (rprResponse.getRandomPeerId().equals(this.identifier.getId())) {

                    log.info("First peer to join the network");
                    // TODO: We are first node in network

                } else { // There are other nodes in the network
                    String randomPeerHost = rprResponse.getRandomPeerHost();

                    /* TODO:
                        Contact random peer and get successor/predecessor node info.
                        Contact successor and update/set self as its predecessor.
                        Contact predecessor and update/set self as its successor.
                        Initiate migration of data items > predecessor and <= self id
                     */

                    // Request peer Identifier of predecessor node
                    GetPredecessorRequest predecessorRequest = new GetPredecessorRequest(
                            Host.getHostname(),
                            Host.getIpAddress(),
                            this.identifier.getId()
                    );
                    Socket peerSocket = Client.sendMessage(randomPeerHost, Constants.Peer.PORT, predecessorRequest);
                    dataInputStream = new DataInputStream(peerSocket.getInputStream());
                    GetPredecessorResponse gprResponse = (GetPredecessorResponse) MessageFactory.getInstance()
                            .createMessage(dataInputStream);
                    peerSocket.close();
                    this.predecessor = gprResponse.getPeerId();

                    // Request peer Identifier of successor node
                    GetSuccessorRequest successorRequest = new GetSuccessorRequest(
                            Host.getHostname(),
                            Host.getIpAddress(),
                            this.identifier.getId()
                    );
                    peerSocket = Client.sendMessage(randomPeerHost, Constants.Peer.PORT, predecessorRequest);
                    dataInputStream = new DataInputStream(peerSocket.getInputStream());
                    GetSuccessorResponse gsr = (GetSuccessorResponse) MessageFactory.getInstance()
                            .createMessage(dataInputStream);
                    peerSocket.close();
                    this.successor = gsr.getPeerId();
                }
            } else {
                // TODO: Generate new id and retry
                log.warn("Peer with ID {} already exists in the network", this.identifier);
            }

            clientSocket.close();
        } catch (IOException e) {
            log.error("Unable to send registration request: {}", e.getMessage());
        }
    }

    /**
     * Sends a NetworkExitNotification Message to the Discovery node before exiting the network.
     * This allows the Discovery node to remove us from the list of tracked peers.
     */
    public void leaveNetwork() {
        NetworkExitNotification exitNotification = new NetworkExitNotification(Host.getHostname(), Host.getIpAddress(),
                this.identifier);
        try {
            Socket clientSocket = Client.sendMessage(this.discoveryNodeHostname, this.discoveryNodePort, exitNotification);
            clientSocket.close();
        } catch (IOException e) {
            log.error("Unable to send NetworkExitNotification: {}", e.getLocalizedMessage());
        }
    }

    /**
     * Launches the Peer server as a thread.
     */
    public void startServer() {
        new PeerServer(this).launchAsThread();
    }
}
