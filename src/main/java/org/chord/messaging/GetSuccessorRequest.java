package org.chord.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class GetSuccessorRequest extends Message {

    private static final Logger log = LoggerFactory.getLogger(GetSuccessorRequest.class);

    // String hex identifier of either a node or data item
    public String id;

    public GetSuccessorRequest(String hostname, String ipAddress, String id) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.id = id;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal: {}", e.getLocalizedMessage());
        }
    }

    public GetSuccessorRequest(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.GET_SUCCESSOR_REQUEST;
    }

    public String getId() {
        return id;
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
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof GetSuccessorRequest)) return false;
        GetSuccessorRequest gsrOther = (GetSuccessorRequest) o;
        return this.id.equals(gsrOther.getId());
    }

    @Override
    public String toString() {
        return "GetSuccessorRequest:\n" +
                String.format("\tid: %s\n", this.id);
    }
}
