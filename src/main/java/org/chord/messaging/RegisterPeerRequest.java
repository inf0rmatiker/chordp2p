package org.chord.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegisterPeerRequest extends Message {
    private static final Logger log = LoggerFactory.getLogger(RegisterPeerRequest.class);

    private String id;

    public RegisterPeerRequest(String hostname, String ipAddress, String id) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.id = id;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal: {}", e.getLocalizedMessage());
        }
    }

    public RegisterPeerRequest(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream);
        writeString(dataOutputStream, id);
    }

    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream);
        this.id = readString(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.REGISTER_PEER_REQUEST;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof RegisterPeerRequest)) return false;
        RegisterPeerRequest rprOther = (RegisterPeerRequest) o;
        return this.id.equals(rprOther.getId());
    }

    @Override
    public String toString() {
        return "RegisterPeerRequest:\n" +
                String.format("\tid: %s\n", this.id);
    }
}
