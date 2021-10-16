package org.chord.messaging;

import org.chord.peer.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegisterPeerResponse extends Message {

    private static final Logger log = LoggerFactory.getLogger(RegisterPeerResponse.class);

    // Identifier of a random peer to get in contact with
    public Identifier randomPeerId;

    // Informs peer if the original requested id is valid, and not colliding with another peer's id
    public Boolean isValidRequest;

    public RegisterPeerResponse(String hostname, String ipAddress, Identifier randomPeerId, Boolean isValidRequest) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.randomPeerId = randomPeerId;
        this.isValidRequest = isValidRequest;
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

    public Identifier getRandomPeerId() {
        return randomPeerId;
    }

    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream);
        writeIdentifier(dataOutputStream, this.randomPeerId);
        writeBoolean(dataOutputStream, this.isValidRequest);
    }

    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream);
        this.randomPeerId = readIdentifier(dataInputStream);
        this.isValidRequest = readBoolean(dataInputStream);
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
                this.randomPeerId.equals(rprOther.getRandomPeerId())
        );
    }

    @Override
    public String toString() {
        return "\nRegisterPeerResponse:\n" +
                String.format("\tisValidRequest: %b\n", this.isValidRequest) +
                String.format("\trandomPeerId: %s\n", this.randomPeerId);
    }
}
