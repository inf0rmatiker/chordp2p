package org.chord.discovery;

import org.chord.networking.Node;
import org.chord.peer.Identifier;
import org.chord.util.Host;
import org.chord.util.InteractiveCommandParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class DiscoveryNode extends Node {
    private static final Logger log = LoggerFactory.getLogger(DiscoveryNode.class);

    // Hostname of the discovery server
    public String hostname;

    // Set of all known registered peers in network
    public Vector<Identifier> registeredPeers;

    // For generating a sequence of pseudo-random numbers
    public Random random;

    private InteractiveCommandParser commandParser;

    public DiscoveryNode() {
        this.hostname = Host.getHostname();
        this.registeredPeers = new Vector<>();
        this.random = new Random();
        commandParser = new InteractiveCommandParser(this);
        log.info("Started Discovery Node on {}", this.hostname);
    }

    public String getHostname() {
        return hostname;
    }

    public Vector<Identifier> getRegisteredPeers() {
        return registeredPeers;
    }

    public Random getRandom() {
        return random;
    }

    public boolean put(Identifier peerId) {
        // Return val of put() is previous value there, or null if nothing there already
        return this.registeredPeers.add(peerId);
    }

    public boolean alreadyExists(Identifier peerId) {
        return this.registeredPeers.contains(peerId);
    }

    public boolean remove(Identifier peerId) {
        return this.registeredPeers.remove(peerId);
    }

    public boolean isEmpty() {
        return this.registeredPeers.isEmpty();
    }

    public Identifier getRandomPeer() {
        int randomIndex = this.random.nextInt() % registeredPeers.size();
        log.debug("Choosing random index {}", randomIndex);
        return this.registeredPeers.get(randomIndex);
    }

    public void startServer() {
        new DiscoveryNodeServer(this).launchAsThread();
        commandParser.start();
    }

}
