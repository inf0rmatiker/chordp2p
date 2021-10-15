package org.chord.peer;

import org.chord.messaging.*;
import org.chord.networking.Client;
import org.chord.networking.Processor;
import org.chord.util.Constants;
import org.chord.util.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
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
                case FIND_SUCCESSOR_REQUEST:
                    processFindSuccessorRequest((FindSuccessorRequest) message);
                    return;
                case PREDECESSOR_NOTIFICATION:
                    processPredecessorNotification((PredecessorNotification) message);
                    return;
                case SUCCESSOR_NOTIFICATION:
                    processSuccessorNotification((SuccessorNotification) message);
                    return;
                case NETWORK_JOIN_NOTIFICATION:
                    processNetworkJoinNotification((NetworkJoinNotification) message);
                    return;
                default: log.error("Unimplemented processing support for message type {}", message.getType());
            }
        } catch (IOException e) {
            log.error("Encountered IOException when processing {}: {}", message.getType(), e.getMessage());
        }

    }

    /**
     * Processes a GetPredecessorRequest Message, by sending back our predecessor
     * @param message GetPredecessorRequest Message
     * @throws IOException If unable to read/write from/to the Socket, or marshal/unmarshal a Message
     */
    public void processGetPredecessorRequest(GetPredecessorRequest message) throws IOException {
        PeerIdentifierMessage pimResponse = new PeerIdentifierMessage(
                Host.getHostname(),
                Host.getIpAddress(),
                this.peer.getPredecessor()
        );
        sendResponse(this.socket, pimResponse);
    }

    /**
     * Processes a GetSuccessorRequest Message, by sending back our successor
     * @param message GetSuccessorRequest Message
     * @throws IOException If unable to read/write from/to the Socket, or marshal/unmarshal a Message
     */
    public void processGetSuccessorRequest(GetSuccessorRequest message) throws IOException {
        PeerIdentifierMessage pimResponse = new PeerIdentifierMessage(
                Host.getHostname(),
                Host.getIpAddress(),
                this.peer.getSuccessor()
        );
        sendResponse(this.socket, pimResponse);
    }

    /**
     * Processes a FindSuccessorRequest Message, containing an id, k, by:
     * - If we know the final successor of k (it's the first entry in our finger table), return that.
     * - If we don't know the successor of k, we forward the message to the first successor p in our
     *      finger table such that p is the smallest value >= k. This is the nextBestSuccessor.
     *      The response from the forward recipient is then sent back to the requester.
     * @param message FindSuccessorRequest Message containing k
     * @throws IOException If unable to read/write from streams/sockets
     */
    public void processFindSuccessorRequest(FindSuccessorRequest message) throws IOException {
        FingerTable ourFingerTable = this.peer.getFingerTable();
        String id = message.getId();
        PeerIdentifierMessage response = null;

        if (ourFingerTable.knowsFinalSuccessorOf(message.getId())) {
            log.info("We know the final successor of id {}", message.getId());
            Identifier finalSuccessor = ourFingerTable.successor(id);
            response = new PeerIdentifierMessage(Host.getHostname(), Host.getIpAddress(), finalSuccessor);
        } else {

            // Forward GetSuccessorRequest Message to next best peer in finger table
            Identifier nextBestSuccessor = ourFingerTable.successor(id);
            log.info("We don't know the final successor of id {}, forwarding request to next best successor {}",
                    message.getId(), nextBestSuccessor);

            try {
                Socket clientSocket = Client.sendMessage(nextBestSuccessor.getHostname(), Constants.Peer.PORT, message);
                DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                response = (PeerIdentifierMessage) MessageFactory.getInstance().createMessage(dataInputStream);
                clientSocket.close(); // done talking with next best successor peer
            } catch (IOException e) {
                log.error("Failed to forward GetSuccessorRequest Message to {}: {}", nextBestSuccessor.getHostname(),
                        e.getMessage());
            }
        }

        if (response != null) {
            sendResponse(this.socket, response); // return response from upstream to requester
        } else {
            log.error("GetSuccessorResponse is null!");
        }
    }

    public void processPredecessorNotification(PredecessorNotification message) throws IOException {
        this.peer.setPredecessor(message.getPeerId());
        log.info("Updated predecessor to peer: {}", message.getPeerId());
    }

    public void processSuccessorNotification(SuccessorNotification message) throws IOException {
        this.peer.setSuccessor(message.getPeerId());
        log.info("Updated successor to peer: {}", message.getPeerId());
    }

    public void processNetworkJoinNotification(NetworkJoinNotification message) throws IOException {
        if (!message.getPeerId().equals(peer.getIdentifier())) {
            log.info("Updating finger table with peer: {}", message.getPeerId());
            this.peer.updateFingerTable(message.getPeerId());
            Client.sendMessage(this.peer.getPredecessor().getHostname(), Constants.Peer.PORT, message).close();
        }
    }

}
