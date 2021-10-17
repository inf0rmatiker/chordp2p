package org.chord.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Initially sent by StoreData to a random Peer. The Peer then performs a Lookup operation
 * for given fileId (digest) and forwards the request to other Peers as needed.
 * When the request is processed by the most suitable peer for the file, that peer will
 * send a corresponding LookupResponse back to StoreData.
 */
public class LookupRequest extends Message {
    private static final Logger log = LoggerFactory.getLogger(LookupRequest.class);

    public String fileId;

    // same as hostname when the request is initiated by StoreData
    // different if initiated by a peer
    public String storeDataHost;

    // same as ipAddress when the request is initiated by StoreAddress
    // different if initiated by a peer
    public String storeDataIpAddress;

    public LookupRequest(String hostname, String ipAddress, String fileId, String storeDataHost, String storeDataIpAddress) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.fileId = fileId;
        this.storeDataHost = storeDataHost;
        this.storeDataIpAddress = storeDataIpAddress;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshall: {}", e.getLocalizedMessage());
        }
    }

    public LookupRequest(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream);
        writeString(dataOutputStream, this.fileId);
        writeString(dataOutputStream, this.storeDataHost);
        writeString(dataOutputStream, this.storeDataIpAddress);
    }

    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream);
        this.fileId = readString(dataInputStream);
        this.storeDataHost = readString(dataInputStream);
        this.storeDataIpAddress = readString(dataInputStream);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof LookupRequest)) return false;
        LookupRequest lookupRequestOther = (LookupRequest) o;
        return this.fileId.equals(lookupRequestOther.fileId) &&
                this.storeDataHost.equals(lookupRequestOther.storeDataHost);
    }

    @Override
    public String toString() {
        return "\nLookupRequest:\n" +
                String.format("\tfileId: %s\n", this.fileId) +
                String.format("\tstoreDataHost: %s\n", this.storeDataHost) +
                String.format("\tstoreDataIpAddress: %s\n", this.storeDataIpAddress);
    }

    @Override
    public MessageType getType() {
        return MessageType.LOOKUP_REQUEST;
    }
}
