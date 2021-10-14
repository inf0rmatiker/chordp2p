package org.chord.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

public class GetSuccessorRequest extends Message {

    private static final Logger log = LoggerFactory.getLogger(GetSuccessorRequest.class);

    public GetSuccessorRequest(String hostname, String ipAddress) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
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

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof GetSuccessorRequest)) return false;
        GetSuccessorRequest gsrOther = (GetSuccessorRequest) o;
        return this.hostname.equals(gsrOther.getHostname());
    }

    @Override
    public String toString() {
        return "GetSuccessorRequest\n";
    }
}
