package org.chord.storedata;

import org.chord.discovery.DiscoveryNodeServer;
import org.chord.messaging.GetRandomPeerRequest;
import org.chord.messaging.LookupRequest;
import org.chord.messaging.MessageFactory;
import org.chord.messaging.PeerIdentifierMessage;
import org.chord.networking.Client;
import org.chord.networking.Node;
import org.chord.peer.Identifier;
import org.chord.util.Constants;
import org.chord.util.FileUtil;
import org.chord.util.Host;
import org.chord.util.InteractiveCommandParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

public class StoreData extends Node {
    private static final Logger log = LoggerFactory.getLogger(StoreData.class);

    private final String discoveryNodeHostname;
    private final int discoveryNodePort;

    // Hostname of the StoreData node
    public String hostname;

    private InteractiveCommandParser commandParser;

    public StoreData(String discoveryNodeHostname, int discoveryNodePort) {
        this.discoveryNodeHostname = discoveryNodeHostname;
        this.discoveryNodePort = discoveryNodePort;
        this.hostname = Host.getHostname();
        commandParser = new InteractiveCommandParser(this);
    }

    public String getHostname() {
        return hostname;
    }

    public void addFile(String filePath) {
        log.info("add-file {}", filePath);
        // read file as bytes
        byte[] fileBytes;
        try {
            fileBytes = FileUtil.readFileAsBytes(filePath);
        } catch (IOException e) {
            log.warn("Error reading file {}", filePath);
            log.error(e.getLocalizedMessage());
        }

        // calculate 16-bit hash 'k'
        String fileId = "";

        // retrieve random peer information from discovery node
        GetRandomPeerRequest grpRequest = new GetRandomPeerRequest(Host.getHostname(), Host.getIpAddress());
        try {
            Socket clientSocket = Client.sendMessage(this.discoveryNodeHostname, this.discoveryNodePort, grpRequest);
            // send getRandomPeerRequest, wait for response
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
            PeerIdentifierMessage pimResponse =
                    (PeerIdentifierMessage) MessageFactory.getInstance().createMessage(dataInputStream);
            clientSocket.close(); // done talking to discovery node

            // lookup(k) to find the most appropriate peer to store the file
            Identifier randomPeerId = pimResponse.peerId;
            LookupRequest lookupRequest = new LookupRequest(Host.getHostname(), Host.getIpAddress(), fileId);
            log.info("Sending lookup({}) peer {}", fileId, randomPeerId.toString());
            Client.sendMessage(randomPeerId.hostname, Constants.Peer.PORT, lookupRequest);

            // contact the appropriate node and transfer file
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }

    }

    public void startServer() {
        new StoreDataServer(this).launchAsThread();
        commandParser.start();
    }
}
