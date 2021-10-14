package org.chord.messaging;

import org.chord.peer.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Message from a peer to the discovery node requesting to be registered under the identifier specified by the message.
 */
public class RegisterPeerRequest extends PeerIdentifierMessage {

    private static final Logger log = LoggerFactory.getLogger(RegisterPeerRequest.class);

    public RegisterPeerRequest(String hostname, String ipAddress, Identifier id) {
        super(hostname, ipAddress, id);
    }

    public RegisterPeerRequest(DataInputStream dataInputStream) throws IOException {
        super(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.REGISTER_PEER_REQUEST;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof RegisterPeerRequest)) return false;
        RegisterPeerRequest rprOther = (RegisterPeerRequest) o;
        return this.peerId.equals(rprOther.getPeerId());
    }

    @Override
    public String toString() {
        return "RegisterPeerRequest:\n" +
                String.format("\tid: %s\n", this.peerId);
    }
}
