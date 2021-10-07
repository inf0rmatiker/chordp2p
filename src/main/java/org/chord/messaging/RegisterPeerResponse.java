package org.chord.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegisterPeerResponse extends Message {
    private static final Logger log = LoggerFactory.getLogger(RegisterPeerResponse.class);

    // to inform messaging node about collisions in the ID space
    private String statusMessage;

    public RegisterPeerResponse(String hostname, String ipAddress, int port, String statusMessage) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.port = port;
        this.statusMessage = statusMessage;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal: {}", e.getLocalizedMessage());
        }
    }

    public RegisterPeerResponse(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream);
        writeString(dataOutputStream, statusMessage);
    }

    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream);
        statusMessage = readString(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.REGISTER_PEER_RESPONSE;
    }
}
