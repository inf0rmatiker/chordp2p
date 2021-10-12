package org.chord.peer;

import org.chord.messaging.GetPredecessorRequest;
import org.chord.messaging.GetPredecessorResponse;
import org.chord.messaging.GetSuccessorRequest;
import org.chord.messaging.Message;
import org.chord.networking.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;

public class PeerProcessor extends Processor {

    private static final Logger log = LoggerFactory.getLogger(PeerProcessor.class);

    // Reference to our Peer instance
    private final Peer peer;

    public PeerProcessor(Socket socket, Peer peer) {
        this.socket = socket;
        this.peer = peer;
    }

    public Peer getPeer() {
        return peer;
    }

    @Override
    public void process(Message message) {
        log.info("Processing {} message", message.getType());

        try {
            switch (message.getType()) {
                case GET_PREDECESSOR_REQUEST:
                    processGetPredecessorRequest((GetPredecessorRequest) message);
                    return;
                case GET_SUCCESSOR_REQUEST:
                    processGetSuccessorRequest((GetSuccessorRequest) message);
                    return;
                default: log.error("Unimplemented processing support for message type {}", message.getType());
            }
        } catch (IOException e) {
            log.error("Encountered IOException when processing {}: {}", message.getType(), e.getMessage());
        }

    }

    public void processGetPredecessorRequest(GetPredecessorRequest message) throws IOException {
        FingerTable ourFingerTable = this.peer.getFingerTable();



    }

    public void processGetSuccessorRequest(GetSuccessorRequest message) throws IOException {
        FingerTable ourFingerTable = this.peer.getFingerTable();

    }


}
