package org.chord.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

public class GetPredecessorRequest extends Message {

    private static final Logger log = LoggerFactory.getLogger(GetPredecessorRequest.class);

    public GetPredecessorRequest(String hostname, String ipAddress) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
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

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof GetPredecessorRequest)) return false;
        GetPredecessorRequest gprOther = (GetPredecessorRequest) o;
        return this.hostname.equals(gprOther.getHostname());
    }

    @Override
    public String toString() {
        return "\nGetPredecessorRequest\n";
    }
}
