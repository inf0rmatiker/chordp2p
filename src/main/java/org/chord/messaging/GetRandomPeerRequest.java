package org.chord.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Initiated by StoreData to store a new file
 */
public class GetRandomPeerRequest extends Message {
    private static final Logger log = LoggerFactory.getLogger(GetRandomPeerRequest.class);

    public GetRandomPeerRequest(String hostname, String ipAddress) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshall: {}", e.getLocalizedMessage());
        }
    }

    public GetRandomPeerRequest(DataInputStream dataInputStream) throws IOException {
        unmarshal(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.GET_RANDOM_PEER_REQUEST;
    }

    @Override
    public String toString() {
        return "GetRandomPeerRequest\n";
    }
}
