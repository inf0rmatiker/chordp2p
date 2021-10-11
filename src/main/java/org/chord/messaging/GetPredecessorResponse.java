package org.chord.messaging;

import org.chord.peer.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class GetPredecessorResponse extends PeerIdentifierMessage {

    private static final Logger log = LoggerFactory.getLogger(GetPredecessorResponse.class);

    public GetPredecessorResponse(String hostname, String ipAddress, Identifier peerId) {
        super(hostname, ipAddress, peerId);
    }

    public GetPredecessorResponse(DataInputStream dataInputStream) throws IOException {
        super(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.GET_PREDECESSOR_RESPONSE;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof GetPredecessorResponse)) return false;
        GetPredecessorResponse gprOther = (GetPredecessorResponse) o;
        return this.peerId.equals(gprOther.getPeerId());
    }

    @Override
    public String toString() {
        return "GetPredecessorResponse:\n" +
                String.format("\tpredecessor: %s\n", this.peerId);
    }
}
