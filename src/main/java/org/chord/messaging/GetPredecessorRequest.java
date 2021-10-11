package org.chord.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class GetPredecessorRequest extends Message {

    private static final Logger log = LoggerFactory.getLogger(GetPredecessorRequest.class);

    // String hex identifier of either a node or data item
    public String id;

    public GetPredecessorRequest(String hostname, String ipAddress, String id) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.id = id;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal: {}", e.getLocalizedMessage());
        }
    }

    public GetPredecessorRequest(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.GET_PREDECESSOR_REQUEST;
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
        if (!(o instanceof GetPredecessorRequest)) return false;
        GetPredecessorRequest gprOther = (GetPredecessorRequest) o;
        return this.id.equals(gprOther.getId());
    }

    @Override
    public String toString() {
        return "GetPredecessorRequest:\n" +
                String.format("\tid: %s\n", this.id);
    }
}
