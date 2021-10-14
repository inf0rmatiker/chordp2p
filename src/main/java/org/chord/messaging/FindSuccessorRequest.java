package org.chord.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FindSuccessorRequest extends Message {

    private static final Logger log = LoggerFactory.getLogger(FindSuccessorRequest.class);

    // Id of data item or node we want the successor of
    public String id;

    public FindSuccessorRequest(String hostname, String ipAddress, String id) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.id = id;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal: {}", e.getLocalizedMessage());
        }
    }

    public FindSuccessorRequest(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.FIND_SUCCESSOR_REQUEST;
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
        if (!(o instanceof FindSuccessorRequest)) return false;
        FindSuccessorRequest fsrOther = (FindSuccessorRequest) o;
        return this.id.equals(fsrOther.getId());
    }

    @Override
    public String toString() {
        return "FindSuccessorRequest:\n" +
                String.format("\tid: %s\n", this.id);
    }
}
