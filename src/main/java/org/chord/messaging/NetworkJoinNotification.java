package org.chord.messaging;

import org.chord.peer.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Message sent from peer to discovery node after successfully joining the network.
 * This notifies the discovery node that it can add the peer to the list of network nodes.
 */
public class NetworkJoinNotification extends PeerIdentifierMessage {

    private static final Logger log = LoggerFactory.getLogger(NetworkJoinNotification.class);

    public NetworkJoinNotification(String hostname, String ipAddress, Identifier peerId) {
        super(hostname, ipAddress, peerId);
    }

    public NetworkJoinNotification(DataInputStream dataInputStream) throws IOException {
        super(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.NETWORK_JOIN_NOTIFICATION;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof NetworkJoinNotification)) return false;
        NetworkJoinNotification njnOther = (NetworkJoinNotification) o;
        return this.peerId.equals(njnOther.getPeerId());
    }

    @Override
    public String toString() {
        return "\nNetworkJoinNotification:\n" +
                String.format("\tpeerId: %s\n", this.peerId);
    }
}
