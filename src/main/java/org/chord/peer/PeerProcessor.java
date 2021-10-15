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
        log.info("Processing {}...", message.getType());

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
        log.info("Responding to GetPredecessorRequest from {} with {}: {}", message.getHostname(), pimResponse.getType(),
                pimResponse);
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
        log.info("Responding to GetSuccessorRequest from {} with {}: {}", message.getHostname(), pimResponse.getType(),
                pimResponse);
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

            Identifier finalSuccessor = ourFingerTable.successor(id);
            log.info("The final successor of id {} is: {}", message.getId(), finalSuccessor);
            response = new PeerIdentifierMessage(Host.getHostname(), Host.getIpAddress(), finalSuccessor);

        } else {

            // Forward GetSuccessorRequest Message to next best peer in finger table
            Identifier nextBestSuccessor = ourFingerTable.successor(id);
            log.info("The next best successor we know of {} is: {}", message.getId(), nextBestSuccessor);

            if (nextBestSuccessor.equals(this.peer.getIdentifier())) {
                log.info("Next best successor of {} is us; returning {} as final successor", message.getId(),
                        this.peer.getIdentifier());
                response = new PeerIdentifierMessage(Host.getHostname(), Host.getIpAddress(), nextBestSuccessor);
            } else if (nextBestSuccessor.equals(message.getRequesterId())) {
                log.info("Next best successor of {} is the requester; returning {} as final successor", message.getId(),
                        message.getRequesterId());
                response = new PeerIdentifierMessage(Host.getHostname(), Host.getIpAddress(), nextBestSuccessor);
            } else  { // forward request to next best successor
                try {
                    log.info("Forwarding FindSuccessorRequest message from {} to {}: {}", message.getHostname(),
                            nextBestSuccessor.getHostname(), message);

                    // Change message's hostname/ip address to ours and re-marshal
                    message.hostname = Host.getHostname();
                    message.ipAddress = Host.getIpAddress();
                    message.marshal();

                    Socket clientSocket = Client.sendMessage(nextBestSuccessor.getHostname(), Constants.Peer.PORT, message);
                    DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                    response = (PeerIdentifierMessage) MessageFactory.getInstance().createMessage(dataInputStream);
                    dataInputStream.close();
                    clientSocket.close(); // done talking with next best successor peer
                } catch (IOException e) {
                    log.error("Failed to forward FindSuccessorRequest Message to {}: {}", nextBestSuccessor.getHostname(),
                            e.getMessage());
                }
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
        sendResponse(this.socket, new StatusMessage(
                Host.getHostname(),
                Host.getIpAddress(),
                Message.Status.OK
        ));
    }

    public void processSuccessorNotification(SuccessorNotification message) throws IOException {
        this.peer.setSuccessor(message.getPeerId());
        log.info("Updated successor to peer: {}", message.getPeerId());
        sendResponse(this.socket, new StatusMessage(
                Host.getHostname(),
                Host.getIpAddress(),
                Message.Status.OK
        ));
    }

    public void processNetworkJoinNotification(NetworkJoinNotification message) throws IOException {
        if (!message.getPeerId().equals(this.peer.getIdentifier())) {
            log.info("Updating finger table with peer: {}", message.getPeerId());
            this.peer.updateFingerTable(message.getPeerId());
            log.debug("NetworkJoinNotification Message: {}", message);
            if (!message.getPeerId().equals(this.peer.getPredecessor())) {
                Client.sendMessage(this.peer.getPredecessor().getHostname(), Constants.Peer.PORT, message).close();
            }
        }
    }

}
