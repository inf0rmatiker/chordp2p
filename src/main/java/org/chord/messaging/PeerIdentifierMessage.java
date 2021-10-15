package org.chord.messaging;

import org.chord.peer.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PeerIdentifierMessage extends Message {

    private static final Logger log = LoggerFactory.getLogger(PeerIdentifierMessage.class);

    public Identifier peerId;

    public PeerIdentifierMessage(String hostname, String ipAddress, Identifier peerId) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.peerId = peerId;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal: {}", e.getLocalizedMessage());
        }
    }

    public PeerIdentifierMessage(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.PEER_IDENTIFIER_MESSAGE;
    }

    public Identifier getPeerId() {
        return peerId;
    }

    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream);
        writeIdentifier(dataOutputStream, this.peerId);
    }

    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream);
        this.peerId = readIdentifier(dataInputStream);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof PeerIdentifierMessage)) return false;
        PeerIdentifierMessage pimOther = (PeerIdentifierMessage) o;
        return this.peerId.equals(pimOther.getPeerId());
    }

    @Override
    public String toString() {
        return "\nPeerIdentifierMessage:\n" +
                String.format("\tpeerId: %s\n", this.peerId);
    }
}
