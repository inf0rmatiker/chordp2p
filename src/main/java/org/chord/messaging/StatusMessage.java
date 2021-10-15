package org.chord.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class StatusMessage extends Message {

    private static final Logger log = LoggerFactory.getLogger(StatusMessage.class);

    public Status status;

    public StatusMessage(String hostname, String ipAddress, Status status) {
        this.hostname = hostname;
        this.ipAddress = ipAddress;
        this.status = status;
        try {
            marshal();
        } catch (IOException e) {
            log.error("Unable to self-marshal: {}", e.getLocalizedMessage());
        }
    }

    public StatusMessage(DataInputStream dataInputStream) throws IOException {
        this.unmarshal(dataInputStream);
    }

    @Override
    public MessageType getType() {
        return MessageType.STATUS_MESSAGE;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public void marshal(DataOutputStream dataOutputStream) throws IOException {
        super.marshal(dataOutputStream);
        writeString(dataOutputStream, this.status.name());
    }

    @Override
    public void unmarshal(DataInputStream dataInputStream) throws IOException {
        super.unmarshal(dataInputStream);
        this.status = Status.valueOf(readString(dataInputStream));
    }

    @Override
    public String toString() {
        return "\nStatusMessage:\n" +
                String.format("\tstatus: %s\n", this.status.name());
    }
}
