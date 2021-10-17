package org.chord.storedata;

import org.chord.messaging.*;
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
import java.util.Locale;

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

        log.info("Adding file {}", filePath);
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
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
            GetRandomPeerResponse grpResponse = (GetRandomPeerResponse) MessageFactory.getInstance().createMessage(dataInputStream);
            dataInputStream.close();
            clientSocket.close(); // done talking to discovery node

            // Get successor of file id
            FindSuccessorRequest successorRequest = new FindSuccessorRequest(
                    Host.getHostname(),
                    Host.getIpAddress(),
                    fileId,
                    null
            );
            clientSocket = Client.sendMessage(grpResponse.getHostname(), Constants.Peer.PORT, successorRequest);
            dataInputStream = new DataInputStream(clientSocket.getInputStream());
            PeerIdentifierMessage successorResponse = (PeerIdentifierMessage) MessageFactory.getInstance()
                    .createMessage(dataInputStream);
            dataInputStream.close();
            clientSocket.close(); // done talking to peer

            // Request successor to store file
            log.info("Requesting to store file {} with id {} at successor {}", fileName, fileId,
                    successorResponse.getPeerId());
            StoreFileRequest sfRequest = new StoreFileRequest(
                    Host.getHostname(),
                    Host.getIpAddress(),
                    fileId,
                    fileName,
                    fileBytes
            );
            Client.waitForStatusResponseAndClose(
                    Client.sendMessage(successorResponse.getHostname(), Constants.Peer.PORT, sfRequest)
            );

        } catch (IOException e) {
            log.error("Unable to store file! {}", e.getMessage());
        }
    }
}
