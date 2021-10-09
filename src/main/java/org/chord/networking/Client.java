package org.chord.networking;

import org.chord.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;


public class Client {
    private static final Logger log = LoggerFactory.getLogger(Client.class);

    /**
     * Opens a Socket to a hostname:port destination and sends a Message
     *
     * @param hostname the String host name we are opening a Socket to
     * @param port     the Integer port number we are opening a Socket to
     * @param message  The Message to be sent, must have been previously marshaled
     * @return The Socket we sent the Message on, and on which a response may be expected
     */
    public static Socket sendMessage(String hostname, Integer port, Message message) throws IOException {
        log.info("Sending {} Message", message.getType());
        Socket clientSocket = new Socket(hostname, port);
        clientSocket.getOutputStream().write(message.getMarshaledBytes());
        return clientSocket;
    }

}
