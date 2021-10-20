package org.chord.peer;

import org.chord.messaging.FindSuccessorRequest;
import org.chord.messaging.GetPredecessorRequest;
import org.chord.messaging.GetSuccessorRequest;
import org.chord.messaging.LookupRequest;
import org.chord.messaging.LookupResponse;
import org.chord.messaging.Message;
import org.chord.messaging.MessageFactory;
import org.chord.messaging.MoveFileRequest;
import org.chord.messaging.MoveFileResponse;
import org.chord.messaging.NetworkJoinNotification;
import org.chord.messaging.PeerIdentifierMessage;
import org.chord.messaging.PredecessorNotification;
import org.chord.messaging.StatusMessage;
import org.chord.messaging.StoreFileRequest;
import org.chord.messaging.StoreFileResponse;
import org.chord.messaging.SuccessorNotification;
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
                case LOOKUP_REQUEST:
                    processLookupRequest((LookupRequest) message);
                    break;
                case STORE_FILE_REQUEST:
                    processStoreFileRequest((StoreFileRequest) message);
                    break;
                case MOVE_FILE_REQUEST:
                    processMoveFileRequest((MoveFileRequest) message);
                    break;
                default:
                    log.error("Unimplemented processing support for message type {}", message.getType());
            }
        } catch (IOException e) {
            log.error("Encountered IOException when processing {}: {}", message.getType(), e.getMessage());
        }

    }

    private void processMoveFileRequest(MoveFileRequest message) {
        try {
            peer.storeFile(message.fileId, message.fileName, message.fileBytes);
            MoveFileResponse mfResponse = new MoveFileResponse(
                    Host.getHostname(),
                    Host.getIpAddress(),
                    message.fileId,
                    message.fileName
            );
            sendResponse(this.socket, mfResponse);
        } catch (IOException e) {
            log.error("Unable to store file {}({}): {}", message.fileName, message.fileId, e.getLocalizedMessage());
        }
    }

    private void processStoreFileRequest(StoreFileRequest message) {
        try {
            this.peer.storeFile(message.fileId, message.fileName, message.bytes);
            StoreFileResponse storeFileResponse = new StoreFileResponse(
                    Host.getHostname(),
                    Host.getIpAddress(),
                    message.fileId,
                    message.fileName
            );
            sendResponse(this.socket, storeFileResponse);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    /**
     * Processes a LookupRequest Message by finding the most suitable peer for given fileId k
     *
     * @param message LookupRequest Message
     */
    private void processLookupRequest(LookupRequest message) {
        String k = message.fileId;
        String hostname = message.hostname;
        String storeDataHost = message.storeDataHost;
        String storeDataIpAddress = message.storeDataIpAddress;
        log.info("{} initiating lookup({})", hostname, k);

        FindSuccessorRequest successorRequest = new FindSuccessorRequest(
                Host.getHostname(),
                Host.getIpAddress(),
                k
        );

        // send successorRequest to the successor of current peer
        try {
            Socket peerSocket =
                    Client.sendMessage(this.peer.getSuccessor().hostname, Constants.Peer.PORT, successorRequest);
            DataInputStream dataInputStream = new DataInputStream(peerSocket.getInputStream());
            PeerIdentifierMessage pimResponse =
                    (PeerIdentifierMessage) MessageFactory.getInstance().createMessage(dataInputStream);
            peerSocket.close();

            // matching peer for fileId k
            Identifier matchingPeer = pimResponse.getPeerId();
            log.info("Matching peer for file({}): {}, message received from {}", k, matchingPeer, pimResponse.hostname);

            // send lookup response to store data
            LookupResponse lookupResponse = new LookupResponse(
                    Host.getHostname(),
                    Host.getIpAddress(),
                    matchingPeer
            );
            sendResponse(this.socket, lookupResponse);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    /**
     * Processes a GetPredecessorRequest Message, by sending back our predecessor
     *
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
     *
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
     * finger table such that p is the smallest value >= k. This is the nextBestSuccessor.
     * The response from the forward recipient is then sent back to the requester.
     *
     * @param message FindSuccessorRequest Message containing k
     * @throws IOException If unable to read/write from streams/sockets
     */
    public void processFindSuccessorRequest(FindSuccessorRequest message) throws IOException {
        message.incrementHops();
        log.info("Processing FindSuccessorRequest with {} hops", message.getCurrentHops());
        FingerTable ourFingerTable = this.peer.getFingerTable();
        String id = message.getId();

        if (ourFingerTable.knowsFinalSuccessorOf(id)) {

            Identifier finalSuccessor = ourFingerTable.successor(id);
            log.info("The final successor of id {} is: {}", id, finalSuccessor);
            PeerIdentifierMessage response = new PeerIdentifierMessage(
                    Host.getHostname(),
                    Host.getIpAddress(),
                    finalSuccessor
            );
            sendResponse(this.socket, response);

        } else { // We don't know the final successor of k, so forward request to next best successor in finger table

            Identifier bestPredecessor = ourFingerTable.bestPredecessorOf(id);
            log.info("The best predecessor we know of {} is: {}", id, bestPredecessor);

            try {
                log.info("Forwarding FindSuccessorRequest message from {} to {}: {}", message.getHostname(),
                        bestPredecessor.getHostname(), message);

                // Change message's hostname/ip address to ours and re-marshal
                message.hostname = Host.getHostname();
                message.ipAddress = Host.getIpAddress();
                message.marshal();
                if (this.socket.isClosed()) {
                    log.error("Socket has been closed 0!!!");
                }

                // Open Socket to next best successor, request successor(k), get response, re-marshal it
                Socket successorSocket = Client.sendMessage(bestPredecessor.getHostname(), Constants.Peer.PORT, message);
                DataInputStream dataInputStream = new DataInputStream(successorSocket.getInputStream());
                PeerIdentifierMessage response = (PeerIdentifierMessage) MessageFactory.getInstance().
                        createMessage(dataInputStream);
                response.marshal(); // important! received message is not automatically marshaled
                dataInputStream.close();
                successorSocket.close(); // done talking with next best successor peer

                // Return response to original requester
                log.info("Received final result of FindSuccessorRequest from {}: {}", response.getHostname(), response);
                sendResponse(this.socket, response);

            } catch (IOException e) {
                log.error("Failed to forward FindSuccessorRequest Message to {}: {}", bestPredecessor.getHostname(),
                        e.getMessage());
            }

        }
    }

    public void processPredecessorNotification(PredecessorNotification message) throws IOException {
        this.peer.setPredecessor(message.getPeerId());
        log.info("Updated predecessor to peer: {}", message.getPeerId());
        this.peer.updateFingerTable(message.getPeerId());
        sendResponse(this.socket, new StatusMessage(
                Host.getHostname(),
                Host.getIpAddress(),
                Message.Status.OK
        ));

        peer.moveFilesToNewPredecessor(peer.getPredecessor());
    }

    public void processSuccessorNotification(SuccessorNotification message) throws IOException {
        this.peer.setSuccessor(message.getPeerId());
        log.info("Updated successor to peer: {}", message.getPeerId());
        this.peer.updateFingerTable(message.getPeerId());
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
            log.debug("Forwarding NetworkJoinNotification Message to {}: {}", this.peer.getSuccessor(), message);
            message.marshal();
            Client.sendMessage(this.peer.getSuccessor().getHostname(), Constants.Peer.PORT, message).close();
        }
    }
}
