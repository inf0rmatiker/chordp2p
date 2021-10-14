package org.chord.discovery;

import org.chord.peer.Identifier;
import org.chord.util.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class DiscoveryNode {
    private static final Logger log = LoggerFactory.getLogger(DiscoveryNode.class);

    // Hostname of the discovery server
    public String hostname;

    // Set of all known registered peers in network
    public ConcurrentHashMap<String, Identifier> registeredPeers;

    // For generating a sequence of pseudo-random numbers
    public Random random;


    public DiscoveryNode() {
        this.hostname = Host.getHostname();
        this.registeredPeers = new ConcurrentHashMap<>();
        this.random = new Random();
        log.info("Started Discovery Node on {}", this.hostname);
    }

    public String getHostname() {
        return hostname;
    }

    public ConcurrentHashMap<String, Identifier> getRegisteredPeers() {
        return registeredPeers;
    }

    public Random getRandom() {
        return random;
    }

    public boolean put(Identifier peerId) {
        // Return val of put() is previous value there, or null if nothing there already
        return this.registeredPeers.put(peerId.getId(), peerId) == null;
    }

    public boolean alreadyExists(Identifier peerId) {
        return this.registeredPeers.containsKey(peerId.getId());
    }

    public boolean remove(Identifier peerId) {
        return this.registeredPeers.remove(peerId.getId()) != null;
    }

    public boolean isEmpty() {
        return this.registeredPeers.isEmpty();
    }

    public Identifier getRandomPeer() {
        int randomIndex = this.random.nextInt() % registeredPeers.size();
        String randomPeerId = new ArrayList<>(registeredPeers.keySet()).get(randomIndex);
        return registeredPeers.get(randomPeerId);
    }

    public void startServer() {
        new DiscoveryNodeServer(this).launchAsThread();
    }

}
