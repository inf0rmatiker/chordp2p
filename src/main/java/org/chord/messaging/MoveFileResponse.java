package org.chord.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MoveFileResponse extends Message {
    private static final Logger log = LoggerFactory.getLogger(MoveFileResponse.class);

    public String fileId;
    public String fileName;

    public MoveFileResponse(String hostname, String ipAddress, String fileId, String fileName) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.fileId = fileId;
        this.fileName = fileName;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal: {}", e.getLocalizedMessage());
        }
    }

    public MoveFileResponse(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }


    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream);
        writeString(dataOutputStream, fileId);
        writeString(dataOutputStream, fileName);
    }

    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream);
        fileId = readString(dataInputStream);
        fileName = readString(dataInputStream);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof MoveFileResponse)) return false;
        MoveFileResponse mfrOther = (MoveFileResponse) o;
        return this.fileId.equals(mfrOther.fileId) &&
                this.fileName.equals(mfrOther.fileName);
    }

    @Override
    public String toString() {
        return "\nMoveFileResponse:\n" +
                String.format("\tfileId: %s\n", fileId) +
                String.format("\tfileName: %s\n", fileName);
    }

    @Override
    public MessageType getType() {
        return MessageType.MOVE_FILE_RESPONSE;
    }
}
