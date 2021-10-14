package org.chord.storedata;

import org.chord.networking.Node;
import org.chord.util.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreData extends Node {
    private static final Logger log = LoggerFactory.getLogger(StoreData.class);

    // Hostname of the StoreData node
    public String hostname;

    public StoreData() {
        this.hostname = Host.getHostname();
    }

    public String getHostname() {
        return hostname;
    }

    public void addFile(String filePath) {

    }
}
