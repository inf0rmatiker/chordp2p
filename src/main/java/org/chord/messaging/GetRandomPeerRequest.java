package org.chord.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetRandomPeerRequest extends Message {
    private static final Logger logger = LoggerFactory.getLogger(GetRandomPeerRequest.class);

    @Override
    public MessageType getType() {
        return MessageType.GET_RANDOM_PEER_REQUEST;
    }
}
