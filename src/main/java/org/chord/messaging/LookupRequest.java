package org.chord.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LookupRequest extends Message {
    private static final Logger logger = LoggerFactory.getLogger(LookupRequest.class);

    @Override
    public MessageType getType() {
        return MessageType.LOOKUP_REQUEST;
    }
}
