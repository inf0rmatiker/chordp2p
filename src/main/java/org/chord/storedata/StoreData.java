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

    public void initialize() {
        this.commandParser.start();
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

        // Calculate 16-bit hash 'k'
        String fileId = HashUtil.hashFile(fileBytes);
        log.info("Identifier for file '{}': {}", filePath, fileId);

        // Extract fileName of filePath
        String fileName = Paths.get(filePath).getFileName().toString();

        // Retrieve random peer information from discovery node
        GetRandomPeerRequest grpRequest = new GetRandomPeerRequest(Host.getHostname(), Host.getIpAddress());
        try {
            Socket clientSocket = Client.sendMessage(this.discoveryNodeHostname, this.discoveryNodePort, grpRequest);
            // send getRandomPeerRequest, wait for response
            DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
            GetRandomPeerResponse grpResponse = (GetRandomPeerResponse) MessageFactory.getInstance().createMessage(dataInputStream);
            dataInputStream.close();
            clientSocket.close(); // done talking to discovery node

            // Lookup(k) to find the most appropriate peer to store the file, wait for response
            Identifier randomPeerId = grpResponse.getPeerId();
            FindSuccessorRequest fsRequest = new FindSuccessorRequest(
                    Host.getHostname(),
                    Host.getIpAddress(),
                    fileId
            );

            log.info("Sending FindSuccessorRequest for file id {} to peer {}: {}",  fileId, grpResponse.getPeerId(), fsRequest);
            clientSocket = Client.sendMessage(randomPeerId.getHostname(), Constants.Peer.PORT, fsRequest);
            dataInputStream = new DataInputStream(clientSocket.getInputStream());
            PeerIdentifierMessage pimResponse = (PeerIdentifierMessage) MessageFactory.getInstance().createMessage(dataInputStream);
            dataInputStream.close();
            clientSocket.close();

            Identifier successor = pimResponse.getPeerId();

            log.info("Successor of file id {}: {}", fileId, successor);
            StoreFileRequest sfRequest = new StoreFileRequest(
                    Host.getHostname(), Host.getIpAddress(), fileId, fileName, fileBytes);
            clientSocket = Client.sendMessage(successor.getHostname(), Constants.Peer.PORT, sfRequest);
            dataInputStream = new DataInputStream(clientSocket.getInputStream());
            StoreFileResponse sfResponse = (StoreFileResponse) MessageFactory.getInstance().createMessage(dataInputStream);
            dataInputStream.close();
            clientSocket.close();

            log.info("File '{}' with id {} successfully stored on Peer {}",
                    sfResponse.fileName, sfResponse.fileId, sfResponse.hostname);

        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
    }
}
