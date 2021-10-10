package org.chord.peer;

import org.chord.messaging.MessageFactory;
import org.chord.messaging.RegisterPeerRequest;
import org.chord.messaging.RegisterPeerResponse;
import org.chord.networking.Client;
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
    private String id;

    public Peer(String discoveryNodeHostname, int discoveryNodePort, String id) {
        this.discoveryNodeHostname = discoveryNodeHostname;
        this.discoveryNodePort = discoveryNodePort;
        this.fingerTable = new FingerTable(id);
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
        RegisterPeerRequest registerRequest = new RegisterPeerRequest(Host.getHostname(), Host.getIpAddress(), this.id);
        RegisterPeerResponse response = null;
        try {
            // Send registration request, wait for response
            Socket clientSocket = Client.sendMessage(this.discoveryNodeHostname, this.discoveryNodePort, registerRequest);
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
            response = (RegisterPeerResponse) MessageFactory.getInstance().createMessage(dataInputStream);

            if (response.getIsValidRequest()) {
                String peerHost = response.getRandomPeerHost();

                /* TODO:
                    Contact random peer and get successor/predecessor node info.
                    Contact successor and update/set self as its predecessor.
                    Contact predecessor and update/set self as its successor.
                    Initiate migration of data items > predecessor and <= self id
                 */
            } else {
                // TODO: Generate new id and retry
            }

            clientSocket.close();
        } catch (IOException e) {
            log.error("Unable to send registration request: {}", e.getMessage());
        }

    }

    public void startServer() {
        new PeerServer(this).launchAsThread();
    }
}
