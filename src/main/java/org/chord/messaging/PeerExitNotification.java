package org.chord.messaging;

import org.chord.peer.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

public class PeerExitNotification extends PeerIdentifierMessage{
    private static final Logger log = LoggerFactory.getLogger(PeerExitNotification.class);

    public PeerExitNotification(String hostname, String ipAddress, Identifier peerId) {
        super(hostname, ipAddress, peerId);
    }

    public PeerExitNotification(DataInputStream dataInputStream) throws IOException {
        super(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.PEER_EXIT_NOTIFICATION;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof PeerExitNotification)) return false;
        PeerExitNotification penOther = (PeerExitNotification) o;
        return this.peerId.equals(penOther.peerId);
    }

    @Override
    public String toString() {
        return "PeerExitNotification:\n" +
                String.format("\tpeerId: %s\n", this.peerId);
    }
}
