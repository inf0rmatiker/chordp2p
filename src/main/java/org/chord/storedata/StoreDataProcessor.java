package org.chord.storedata;

import org.chord.messaging.LookupResponse;
import org.chord.messaging.Message;
import org.chord.networking.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;

public class StoreDataProcessor extends Processor {
    private static final Logger log = LoggerFactory.getLogger(StoreDataProcessor.class);

    public StoreData storeData;

    public StoreDataProcessor(Socket socket, StoreData storeData) {
        this.socket = socket;
        this.storeData = storeData;
    }
    
    @Override
    public void process(Message message) {
        log.info("Processing {} Message from {}", message.getType(), message.getHostname());
        switch (message.getType()) {
            case LOOKUP_RESPONSE:
                processLookupResponse((LookupResponse) message);
                break;
            default:
                log.warn("Unknown message type: {}", message.getType());
        }
    }

    private void processLookupResponse(LookupResponse message) {
        log.debug("Setting storeData.suitablePeerForCurrentFile = {}", message.peerId);
        storeData.suitablePeerForCurrentFile = message.peerId;
    }
}
