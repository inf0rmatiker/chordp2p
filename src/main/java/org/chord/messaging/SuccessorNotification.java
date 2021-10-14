package org.chord.messaging;

import org.chord.peer.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * A Message notifying a peer p that another peer, identified in the message, is its successor.
 */
public class SuccessorNotification extends PeerIdentifierMessage {

    private static final Logger log = LoggerFactory.getLogger(PredecessorNotification.class);

    public SuccessorNotification(String hostname, String ipAddress, Identifier peerId) {
        super(hostname, ipAddress, peerId);
    }

    public SuccessorNotification(DataInputStream dataInputStream) throws IOException {
        super(dataInputStream);
    }

    @Override
    public Message.MessageType getType() {
        return MessageType.SUCCESSOR_NOTIFICATION;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof SuccessorNotification)) return false;
        SuccessorNotification snOther = (SuccessorNotification) o;
        return this.peerId.equals(snOther.peerId);
    }

    @Override
    public String toString() {
        return "SuccessorNotification:\n" +
                String.format("\tpeerId: %s\n", this.peerId);
    }
}