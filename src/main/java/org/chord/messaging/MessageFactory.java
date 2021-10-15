package org.chord.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Singleton class to instantiate concrete Message instances
 * from a byte array.
 */
public class MessageFactory {
    private static final Logger log = LoggerFactory.getLogger(MessageFactory.class);

    private static MessageFactory singletonInstance = null;

    /**
     * Note: this constructor can only be called from within the class.
     */
    private MessageFactory() {
    }

    /**
     * Gets the singleton instance, instantiating it if it has not been already.
     *
     * @return Singleton MessageFactory instance.
     */
    public static MessageFactory getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new MessageFactory();
        }
        return singletonInstance;
    }

    /**
     * Creates and returns a concrete Message subclass instance from the integer type specified by the byte message.
     *
     * @param dataInputStream DataInputStream on the Socket containing the message bytes.
     * @return A concrete Message subclass instance
     * @throws IOException If unable to read/write
     */
    public Message createMessage(DataInputStream dataInputStream) throws IOException {
        int integerType = dataInputStream.readInt();

        // Create concrete Message using type in byte message
        Message.MessageType type = Message.typeFromInteger(integerType);
        if (type != null) {
            switch (type) {
                case REGISTER_PEER_REQUEST: return new RegisterPeerRequest(dataInputStream);
                case REGISTER_PEER_RESPONSE: return new RegisterPeerResponse(dataInputStream);
                case PEER_IDENTIFIER_MESSAGE: return new PeerIdentifierMessage(dataInputStream);
                case GET_PREDECESSOR_REQUEST: return new GetPredecessorRequest(dataInputStream);
                case GET_SUCCESSOR_REQUEST: return new GetSuccessorRequest(dataInputStream);
                case NETWORK_JOIN_NOTIFICATION: return new NetworkJoinNotification(dataInputStream);
                case NETWORK_EXIT_NOTIFICATION: return new NetworkExitNotification(dataInputStream);
                case FIND_SUCCESSOR_REQUEST: return new FindSuccessorRequest(dataInputStream);
                case PREDECESSOR_NOTIFICATION: return new PredecessorNotification(dataInputStream);
                case SUCCESSOR_NOTIFICATION: return new SuccessorNotification(dataInputStream);
                case STATUS_MESSAGE: return new StatusMessage(dataInputStream);
                default: return null;
            }
        } else {
            throw new IOException("Unable to determine MessageType for integer " + integerType);
        }
    }
}
