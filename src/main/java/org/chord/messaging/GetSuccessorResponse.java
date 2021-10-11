package org.chord.messaging;

import org.chord.peer.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class GetSuccessorResponse extends PeerIdentifierMessage {

    private static final Logger log = LoggerFactory.getLogger(GetSuccessorResponse.class);

    public GetSuccessorResponse(String hostname, String ipAddress, Identifier peerId) {
        super(hostname, ipAddress, peerId);
    }

    public GetSuccessorResponse(DataInputStream dataInputStream) throws IOException {
        super(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.GET_SUCCESSOR_RESPONSE;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof GetSuccessorResponse)) return false;
        GetSuccessorResponse gsrOther = (GetSuccessorResponse) o;
        return this.peerId.equals(gsrOther.getPeerId());
    }

    @Override
    public String toString() {
        return "GetSuccessorResponse:\n" +
                String.format("\tsuccessor: %s\n", this.peerId);
    }
}
