package org.chord.messaging;

import org.chord.peer.FingerTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegisterPeerResponse extends Message {
    private static final Logger log = LoggerFactory.getLogger(RegisterPeerResponse.class);

    // Informs peer if the original requested id is valid, and not colliding with another peer's id
    private Boolean isValidRequest;

    // The id of a random peer in the system
    private String randomPeerId;

    // The hostname of the random peer
    private String randomPeerHost;

    public RegisterPeerResponse(String hostname, String ipAddress, Boolean isValidRequest, String randomPeerId,
                                String randomPeerHost) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.isValidRequest = isValidRequest;
        this.randomPeerId = randomPeerId;
        this.randomPeerHost = randomPeerHost;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal: {}", e.getLocalizedMessage());
        }
    }

    public RegisterPeerResponse(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    public Boolean getIsValidRequest() {
        return isValidRequest;
    }

    public String getRandomPeerId() {
        return randomPeerId;
    }

    public String getRandomPeerHost() {
        return randomPeerHost;
    }

    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream);
        writeBoolean(dataOutputStream, this.isValidRequest);
        writeString(dataOutputStream, this.randomPeerId);
        writeString(dataOutputStream, this.randomPeerHost);
    }

    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream);
        this.isValidRequest = readBoolean(dataInputStream);
        this.randomPeerId = readString(dataInputStream);
        this.randomPeerHost = readString(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.REGISTER_PEER_RESPONSE;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof RegisterPeerResponse)) return false;
        RegisterPeerResponse rprOther = (RegisterPeerResponse) o;
        return (this.isValidRequest.equals(rprOther.getIsValidRequest()) &&
                this.randomPeerId.equals(rprOther.getRandomPeerId()) &&
                this.randomPeerHost.equals(rprOther.getRandomPeerHost())
        );
    }

    @Override
    public String toString() {
        return "RegisterPeerResponse:\n" +
                String.format("\tisValidRequest: %b\n", this.isValidRequest) +
                String.format("\trandomPeerId: %s\n", this.randomPeerId) +
                String.format("\trandomPeerHost: %s\n", this.randomPeerHost);
    }
}
