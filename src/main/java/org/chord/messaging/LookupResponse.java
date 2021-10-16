package org.chord.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LookupResponse extends Message {
    private static final Logger logger = LoggerFactory.getLogger(LookupResponse.class);

    @Override
    public MessageType getType() {
        return MessageType.LOOKUP_RESPONSE;
    }
}
