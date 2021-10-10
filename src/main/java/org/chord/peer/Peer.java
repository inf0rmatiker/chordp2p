package org.chord.peer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    }

    public void startServer() {
        new PeerServer(this).launchAsThread();
    }
}
