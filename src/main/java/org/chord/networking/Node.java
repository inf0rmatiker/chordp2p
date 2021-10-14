package org.chord.networking;

import org.chord.util.Host;

public abstract class Node {
    /**
     * Print the hostname of the current node
     * (Does not provide valuable information. Can be used for testing)
     */
    public void printHost() {
        System.out.println(Host.getHostname());
    }
}
