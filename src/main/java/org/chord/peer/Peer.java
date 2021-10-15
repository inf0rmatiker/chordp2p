package org.chord.peer;

import org.chord.messaging.*;
import org.chord.networking.Client;
import org.chord.networking.Node;
import org.chord.util.Constants;
import org.chord.util.Host;
import org.chord.util.InteractiveCommandParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class Peer extends Node {
    private static final Logger log = LoggerFactory.getLogger(Peer.class);

    private final String discoveryNodeHostname;
    private final int discoveryNodePort;

    private FingerTable fingerTable;
    private Identifier identifier;
    private Identifier predecessor;
    private Identifier successor;

    private InteractiveCommandParser commandParser;

    public Peer(String discoveryNodeHostname, int discoveryNodePort, Identifier identifier) {
        this.discoveryNodeHostname = discoveryNodeHostname;
        this.discoveryNodePort = discoveryNodePort;
        this.identifier = this.predecessor = this.successor = identifier; // init all known peers to our id
        this.fingerTable = new FingerTable(Constants.FINGER_TABLE_SIZE, identifier);
        commandParser = new InteractiveCommandParser(this);
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

    public Identifier getPredecessor() { return predecessor; }

    public void setPredecessor(Identifier predecessor) {
        this.predecessor = predecessor;
    }

    public Identifier getSuccessor() { return successor; }

    public void setSuccessor(Identifier successor) {
        this.successor = successor;
        this.fingerTable.updateWithSuccessor(successor);
    }

    public Identifier getIdentifier() { return identifier; }

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
                this.identifier
        );

        try {

            // Send registration request to discovery node, wait for response
            Socket clientSocket = Client.sendMessage(this.discoveryNodeHostname, this.discoveryNodePort, registerRequest);
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
            RegisterPeerResponse rprResponse = (RegisterPeerResponse) MessageFactory.getInstance().createMessage(dataInputStream);
            log.info("Received {} Message: {}", rprResponse.getType(), rprResponse);
            clientSocket.close(); // done talking to discovery server

            if (rprResponse.getIsValidRequest()) {

                // We are the first node in the network
                if (rprResponse.getRandomPeerId().equals(this.identifier)) {

                    log.info("We are the first peer to join the network");

                } else { // There are other nodes in the network

                    log.info("There are other nodes in the network");
                    String randomPeerHost = rprResponse.getRandomPeerId().getHostname();

                    /* TODO:
                        Contact random peer and get successor/predecessor node info.
                        Contact successor and update/set self as its predecessor.
                        Contact predecessor and update/set self as its successor.
                        Initiate migration of data items > predecessor and <= self id
                     */

                    // Get Identifier of successor peer
                    FindSuccessorRequest successorRequest = new FindSuccessorRequest(
                            Host.getHostname(),
                            Host.getIpAddress(),
                            this.identifier.getId()
                    );
                    Socket peerSocket = Client.sendMessage(randomPeerHost, Constants.Peer.PORT, successorRequest);
                    dataInputStream = new DataInputStream(peerSocket.getInputStream());
                    PeerIdentifierMessage pimResponse = (PeerIdentifierMessage) MessageFactory.getInstance()
                            .createMessage(dataInputStream);
                    log.info("Received {} Message: {}", pimResponse.getType(), pimResponse);
                    peerSocket.close();
                    this.successor = pimResponse.getPeerId();

                    // Now that we know our successor peer, we can directly query its known predecessor
                    // which will become our predecessor
                    GetPredecessorRequest gpRequest = new GetPredecessorRequest(
                            Host.getHostname(),
                            Host.getIpAddress()
                    );
                    peerSocket = Client.sendMessage(this.successor.getHostname(), Constants.Peer.PORT, gpRequest);
                    dataInputStream = new DataInputStream(peerSocket.getInputStream());
                    pimResponse = (PeerIdentifierMessage) MessageFactory.getInstance().createMessage(dataInputStream);
                    log.info("Received {} Message: {}", pimResponse.getType(), pimResponse);
                    peerSocket.close();
                    this.predecessor = pimResponse.getPeerId();

                    // Notify our successor that we are its new predecessor
                    PredecessorNotification predecessorNotification = new PredecessorNotification(
                            Host.getHostname(),
                            Host.getIpAddress(),
                            this.identifier
                    );
                    Client.sendMessage(this.successor.getHostname(), Constants.Peer.PORT, predecessorNotification).close();

                    // Notify our predecessor that we are its new successor
                    SuccessorNotification successorNotification = new SuccessorNotification(
                            Host.getHostname(),
                            Host.getIpAddress(),
                            this.identifier
                    );
                    Client.sendMessage(this.predecessor.getHostname(), Constants.Peer.PORT, successorNotification).close();
                }
            } else {
                // TODO: Generate new id and retry
                log.warn("Peer with ID {} already exists in the network! TODO: Implement retry", this.identifier);
            }

            // Notify discovery server of successful network join
            NetworkJoinNotification notification = new NetworkJoinNotification(
                    Host.getHostname(),
                    Host.getIpAddress(),
                    this.identifier
            );
            Client.sendMessage(this.discoveryNodeHostname, this.discoveryNodePort, notification).close();

        } catch (IOException e) {
            log.error("Unable to send registration request: {}", e.getMessage());
        }

        log.info("After joining the network:\n{}", this); // log our state
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

    public synchronized void updateFingerTable(Identifier newPeer) {
        this.fingerTable.updateWithSuccessor(newPeer);
    }

    public void printFingerTable() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Finger Table for %s:\n", getHostname()));
        List<Identifier> peerIds = this.fingerTable.peerIds;
        for (int i = 0, peerIdsSize = peerIds.size(); i < peerIdsSize; i++) {
            Identifier peerId = peerIds.get(i);
            sb.append(String.format("%d: %s\n", i, peerId));
        }
        System.out.println(sb);
    }

    public void printId() {
        System.out.println(identifier.id);
    }

    /**
     * Launches the Peer server as a thread.
     */
    public void startServer() {
        new PeerServer(this).launchAsThread();
        commandParser.start();
    }

    @Override
    public String toString() {
        return "Peer:\n" +
                String.format("\tid: %s\n", this.identifier) +
                String.format("\tpredecessor: %s\n", this.predecessor) +
                String.format("\tsuccessor: %s\n", this.successor);
    }

}
