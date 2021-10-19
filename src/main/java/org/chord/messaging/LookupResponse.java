package org.chord.messaging;

import org.chord.peer.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

public class LookupResponse extends PeerIdentifierMessage {
    private static final Logger log = LoggerFactory.getLogger(LookupResponse.class);

    public LookupResponse(String hostname, String ipAddress, Identifier peerId) {
        super(hostname, ipAddress, peerId);
    }

    public LookupResponse(DataInputStream dataInputStream) throws IOException {
        super(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.LOOKUP_RESPONSE;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof LookupResponse)) return false;
        LookupResponse lookupResponseOther = (LookupResponse) o;
        return this.peerId.equals(lookupResponseOther.peerId);
    }

    @Override
    public String toString() {
        return "\nLookupResponse:\n" +
                String.format("\tpeerId: %s\n", this.peerId);
    }
}
