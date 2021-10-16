package org.chord.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class LookupRequest extends Message {
    private static final Logger log = LoggerFactory.getLogger(LookupRequest.class);

    public String fileId;

    public LookupRequest(String hostname, String ipAddress, String fileId) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.fileId = fileId;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshall: {}", e.getLocalizedMessage());
        }
    }

    public LookupRequest(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream);
        writeString(dataOutputStream, this.fileId);
    }

    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream);
        this.fileId = readString(dataInputStream);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof LookupRequest)) return false;
        LookupRequest lookupRequestOther = (LookupRequest) o;
        return this.fileId.equals(lookupRequestOther.fileId);
    }

    @Override
    public String toString() {
        return "\nLookupRequest:\n" +
                String.format("\tfileId: %s\n", this.fileId);
    }

    @Override
    public MessageType getType() {
        return MessageType.LOOKUP_REQUEST;
    }
}
