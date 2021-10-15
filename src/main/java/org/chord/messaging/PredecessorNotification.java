package org.chord.messaging;

import org.chord.peer.Identifier;
import org.chord.peer.Peer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * A Message notifying a peer p that another peer, identified in the message, is its predecessor.
 */
public class PredecessorNotification extends PeerIdentifierMessage {

    private static final Logger log = LoggerFactory.getLogger(PredecessorNotification.class);

    public PredecessorNotification(String hostname, String ipAddress, Identifier peerId) {
        super(hostname, ipAddress, peerId);
    }

    public PredecessorNotification(DataInputStream dataInputStream) throws IOException {
        super(dataInputStream);
    }

    @Override
    public Message.MessageType getType() {
        return MessageType.PREDECESSOR_NOTIFICATION;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof PredecessorNotification)) return false;
        PredecessorNotification pnOther = (PredecessorNotification) o;
        return this.peerId.equals(pnOther.peerId);
    }

    @Override
    public String toString() {
        return "\nPredecessorNotification:\n" +
                String.format("\tpeerId: %s\n", this.peerId);
    }
}
