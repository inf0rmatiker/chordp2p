package org.chord.storedata;

import org.chord.messaging.GetRandomPeerRequest;
import org.chord.messaging.GetRandomPeerResponse;
import org.chord.messaging.LookupRequest;
import org.chord.messaging.LookupResponse;
import org.chord.messaging.MessageFactory;
import org.chord.messaging.StoreFileRequest;
import org.chord.networking.Client;
import org.chord.networking.Node;
import org.chord.peer.Identifier;
import org.chord.util.Constants;
import org.chord.util.FileUtil;
import org.chord.util.HashUtil;
import org.chord.util.Host;
import org.chord.util.InteractiveCommandParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Paths;

public class StoreData extends Node {
    private static final Logger log = LoggerFactory.getLogger(StoreData.class);

    private final String discoveryNodeHostname;
    private final int discoveryNodePort;

    // Hostname of the StoreData node
    public String hostname;

    // Populated by StoreDataProcessor once it receives a LookupResponse
    // TODO: think of a better solution, this is hacky
    public Identifier suitablePeerForCurrentFile;

    private InteractiveCommandParser commandParser;

    public StoreData(String discoveryNodeHostname, int discoveryNodePort) {
        this.discoveryNodeHostname = discoveryNodeHostname;
        this.discoveryNodePort = discoveryNodePort;
        this.hostname = Host.getHostname();
        commandParser = new InteractiveCommandParser(this);
        suitablePeerForCurrentFile = null;
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
            return;
        }

        // calculate 16-bit hash 'k'
        String fileId = HashUtil.hashFile(fileBytes);
        log.info("Identifier for file '{}': {}", filePath, fileId);

        // extract fileName of filePath
        String fileName = Paths.get(filePath).getFileName().toString();

        // retrieve random peer information from discovery node
        GetRandomPeerRequest grpRequest = new GetRandomPeerRequest(Host.getHostname(), Host.getIpAddress());
        try {
            Socket clientSocket = Client.sendMessage(this.discoveryNodeHostname, this.discoveryNodePort, grpRequest);
            // send getRandomPeerRequest, wait for response
            DataInputStream disDiscovery = new DataInputStream(clientSocket.getInputStream());
            GetRandomPeerResponse grpResponse = (GetRandomPeerResponse) MessageFactory.getInstance().createMessage(disDiscovery);
            clientSocket.close(); // done talking to discovery node

            // lookup(k) to find the most appropriate peer to store the file, wait for response
            Identifier randomPeerId = grpResponse.peerId;
            LookupRequest lookupRequest = new LookupRequest(Host.getHostname(), Host.getIpAddress(), fileId,
                    Host.getHostname(), Host.getIpAddress());

            log.info("Sending lookup({}) to peer {}", fileId, randomPeerId.toString());
            Socket randomPeerSocket = Client.sendMessage(randomPeerId.hostname, Constants.Peer.PORT, lookupRequest);
            randomPeerSocket.close();

            // contact the appropriate node and transfer file
            while (suitablePeerForCurrentFile == null) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    log.error(e.getLocalizedMessage());
                }
            }

            // suitablePeerForCurrentFile has been populated by StoreDataProcessor.processLookupResponse()
            log.info("Matching PeerId for FileId({}): {}", fileId, suitablePeerForCurrentFile);
            StoreFileRequest sfRequest = new StoreFileRequest(
                    Host.getHostname(), Host.getIpAddress(), fileId, fileName, fileBytes);
            Socket suitablePeerSocket = Client.sendMessage(
                    suitablePeerForCurrentFile.hostname, Constants.Peer.PORT, sfRequest);
            suitablePeerSocket.close();
            suitablePeerForCurrentFile = null;
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    public void startServer() {
        new StoreDataServer(this).launchAsThread();
        commandParser.start();
    }
}
