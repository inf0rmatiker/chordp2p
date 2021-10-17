package org.chord.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Utilized by Peers to move stored files when the finger tables get updated.
 */
public class MoveFileRequest extends Message {
    private static final Logger log = LoggerFactory.getLogger(MoveFileRequest.class);

    public String fileId; // 16-bit file digest
    public String fileName;
    public byte[] fileBytes; // file content

    public MoveFileRequest(String hostname, String ipAddress, String fileId, String fileName, byte[] fileBytes) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileBytes = fileBytes;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal: {}", e.getLocalizedMessage());
        }
    }

    public MoveFileRequest(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream);
        writeString(dataOutputStream, this.fileId);
        writeString(dataOutputStream, this.fileName);
        writeByteArray(dataOutputStream, this.fileBytes);
    }

    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream);
        this.fileId = readString(dataInputStream);
        this.fileName = readString(dataInputStream);
        this.fileBytes = readByteArray(dataInputStream);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof MoveFileRequest)) return false;
        MoveFileRequest mfrOther = (MoveFileRequest) o;
        return this.fileId.equals(mfrOther.fileId) &&
                this.fileName.equals(mfrOther.fileName) &&
                Arrays.equals(this.fileBytes, mfrOther.fileBytes);
    }

    @Override
    public String toString() {
        return "\nMoveFileRequest:\n" +
                String.format("\tfileId: %s\n", this.fileId) +
                String.format("\tfileName: %s\n", this.fileName);
    }

    @Override
    public MessageType getType() {
        return MessageType.MOVE_FILE_REQUEST;
    }
}
