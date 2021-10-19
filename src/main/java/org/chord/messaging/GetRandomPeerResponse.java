package org.chord.messaging;

import org.chord.peer.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

public class GetRandomPeerResponse extends PeerIdentifierMessage {
    private static final Logger log = LoggerFactory.getLogger(GetRandomPeerResponse.class);

    public GetRandomPeerResponse(String hostname, String ipAddress, Identifier peerId) {
        super(hostname, ipAddress, peerId);
    }

    public GetRandomPeerResponse(DataInputStream dataInputStream) throws IOException {
        super(dataInputStream);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof GetRandomPeerResponse)) return false;
        GetRandomPeerResponse grpResponseOther = (GetRandomPeerResponse) o;
        return this.peerId.equals(grpResponseOther.peerId);
    }

    @Override
    public String toString() {
        return "\nGetRandomPeerResponse\n" +
                String.format("\tpeerId: %s\n", this.peerId);
    }

    @Override
    public MessageType getType() {
        return MessageType.GET_RANDOM_PEER_RESPONSE;
    }
}
