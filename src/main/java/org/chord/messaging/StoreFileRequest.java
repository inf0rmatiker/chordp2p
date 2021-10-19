package org.chord.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class StoreFileRequest extends Message {
    private static final Logger log = LoggerFactory.getLogger(StoreFileRequest.class);

    public String fileId; // 16-bit file digest
    public String fileName;
    public byte[] bytes; // file content

    public StoreFileRequest(String hostname, String ipAddress, String fileId, String fileName, byte[] bytes) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.fileId = fileId;
        this.fileName = fileName;
        this.bytes = bytes;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal: {}", e.getLocalizedMessage());
        }
    }

    public StoreFileRequest(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream);
        writeString(dataOutputStream, this.fileId);
        writeString(dataOutputStream, this.fileName);
        writeByteArray(dataOutputStream, bytes);
    }

    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream);
        fileId = readString(dataInputStream);
        fileName = readString(dataInputStream);
        bytes = readByteArray(dataInputStream);
    }

    @Override
    public String toString() {
        return "\nStoreFileRequest:\n" +
                String.format("\tfileId: %s\n", fileId) +
                String.format("\tfileName: %s\n", fileName);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof StoreFileRequest)) return false;
        StoreFileRequest sfrOther = (StoreFileRequest) o;
        return this.fileId.equals(sfrOther.fileId) &&
                this.fileName.equals(sfrOther.fileName) &&
                Arrays.equals(this.bytes, sfrOther.bytes);
    }

    @Override
    public MessageType getType() {
        return MessageType.STORE_FILE_REQUEST;
    }
}
