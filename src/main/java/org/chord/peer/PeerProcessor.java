package org.chord.peer;

import org.chord.messaging.FindSuccessorRequest;
import org.chord.messaging.GetPredecessorRequest;
import org.chord.messaging.GetSuccessorRequest;
import org.chord.messaging.LookupRequest;
import org.chord.messaging.LookupResponse;
import org.chord.messaging.Message;
import org.chord.messaging.MessageFactory;
import org.chord.messaging.NetworkJoinNotification;
import org.chord.messaging.PeerIdentifierMessage;
import org.chord.messaging.PredecessorNotification;
import org.chord.messaging.StatusMessage;
import org.chord.messaging.StoreFileRequest;
import org.chord.messaging.SuccessorNotification;
import org.chord.networking.Client;
import org.chord.networking.Processor;
import org.chord.util.Constants;
import org.chord.util.HashUtil;
import org.chord.util.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

import static org.chord.util.HashUtil.hexToInt;

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
                default: log.error("Unimplemented processing support for message type {}", message.getType());
            }
        } catch (IOException e) {
            log.error("Encountered IOException when processing {}: {}", message.getType(), e.getMessage());
        }

    }

    private void processStoreFileRequest(StoreFileRequest message) {
        try {
            this.peer.storeFile(message.fileId, message.fileName, message.bytes);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
    }

    /**
     * Processes a LookupRequest Message by sending the most suitable peer for given fileId
     * @param message LookupRequest Message
     */
    private void processLookupRequest(LookupRequest message) {
        String k = message.fileId;
        String hostname = message.hostname;
        String storeDataHost = message.storeDataHost;
        String storeDataIpAddress = message.storeDataIpAddress;
        log.info("{} initiating lookup({})", hostname, k);

        String p = this.peer.getIdentifier().id;

        LookupResponse lookupResponse;
        if (p.equals(k)  // self == key
                ||
                // self > key > predecessor
                (hexToInt(p) > hexToInt(k) && hexToInt(k) > hexToInt(peer.getPredecessor().id))

        ) {
            if (p.equals(k)) {
                log.debug("self == key");
            }

            if ((hexToInt(p) > hexToInt(k) && hexToInt(k) > hexToInt(peer.getPredecessor().id))) {
                log.debug("self > key > predecessor");
            }

            log.info("This Peer ({}) is the most suitable for storing file with id {}",
                    Host.getHostname(), k);
            lookupResponse = new LookupResponse(Host.getHostname(), Host.getIpAddress(), this.peer.getIdentifier());
            try {
                log.info("Peer {} sending LookupResponse to StoreData ({})", Host.getHostname(), storeDataHost);
                Socket clientSocket = Client.sendMessage(storeDataIpAddress, Constants.StoreData.PORT, lookupResponse);
                clientSocket.close();
                return;
            } catch (IOException e) {
                log.error(e.getLocalizedMessage());
            }
        }

        Identifier q = null;
        // lookup(k)
        // Current node p forwards query to node q with index j
        // in p's FT where q = FT(p)[j] <= k <= FT(p)[j+1]
        // or q = FT(p) when p < k < FT(p)[1]
        FingerTable fingerTable = peer.getFingerTable();
        List<Identifier> peerIds = fingerTable.getPeerIds();

        if (hexToInt(p) < hexToInt(k) && hexToInt(k) < hexToInt(peerIds.get(0).id)) {
            log.debug("p < k < FT(p)[1] satisfied");
            q = peerIds.get(0);
        } else {
            for (int j = 0, peerIdsSize = peerIds.size() - 1; j < peerIdsSize; j++) {
                if (peerIds.get(j).value() <= hexToInt(k) &&
                        hexToInt(k) < peerIds.get(j + 1).value()) {
                    log.debug("q = FT(p)[j] <= k < FT(p)[j+1] satisfied");
                    q = peerIds.get(j);
                    break;
                }
            }
        }

        if (q == null) {
            log.warn("No matching PeerID found for k = {}", k);
        } else {
            LookupRequest lookupRequest =
                    new LookupRequest(Host.getHostname(), Host.getIpAddress(), k, storeDataHost, storeDataIpAddress);
            log.debug("q: {}", q);
            try {
                log.info("Forwarding lookup({}) to {}", k, q.hostname);
                Socket peerSocket = Client.sendMessage(q.hostname, Constants.Peer.PORT, lookupRequest);
                peerSocket.close();
            } catch (IOException e) {
                log.error("Error forwarding LookupRequest: {}", e.getLocalizedMessage());
            }
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

            Identifier nextBestSuccessor = ourFingerTable.successor(id);
            log.info("The next best successor we know of {} is: {}", id, nextBestSuccessor);

            if (nextBestSuccessor.equals(this.peer.getIdentifier())) {

                log.info("Next best successor of {} is us; returning {} as final successor", id,
                        this.peer.getIdentifier());
                PeerIdentifierMessage response = new PeerIdentifierMessage(
                        Host.getHostname(),
                        Host.getIpAddress(),
                        nextBestSuccessor
                );
                sendResponse(this.socket, response);

            } else if (nextBestSuccessor.equals(message.getRequesterId())) {

                log.info("Next best successor of {} is the requester; returning {} as final successor", id,
                        message.getRequesterId());
                PeerIdentifierMessage response = new PeerIdentifierMessage(
                        Host.getHostname(),
                        Host.getIpAddress(),
                        nextBestSuccessor
                );
                sendResponse(this.socket, response);

            } else  { // forward request to next best successor

                try {
                    log.info("Forwarding FindSuccessorRequest message from {} to {}: {}", message.getHostname(),
                            nextBestSuccessor.getHostname(), message);

                    // Change message's hostname/ip address to ours and re-marshal
                    message.hostname = Host.getHostname();
                    message.ipAddress = Host.getIpAddress();
                    message.marshal();
                    if (this.socket.isClosed()) {
                        log.error("Socket has been closed 0!!!");
                    }

                    // Open Socket to next best successor, request successor(k), get response, re-marshal it
                    Socket successorSocket = Client.sendMessage(nextBestSuccessor.getHostname(), Constants.Peer.PORT, message);
                    DataInputStream dataInputStream = new DataInputStream(successorSocket.getInputStream());
                    PeerIdentifierMessage response = (PeerIdentifierMessage) MessageFactory.getInstance().
                            createMessage(dataInputStream);
                    response.marshal(); // important! received message is not automatically marshaled
                    dataInputStream.close();
                    successorSocket.close(); // done talking with next best successor peer

                    // Return response to original requester
                    log.info("Received final result of FindSuccessorRequest from {}: {}", response.getHostname(), response);
                    if (this.socket.isClosed()) {
                        log.error("Socket has been closed 1!!!");
                    }
                    sendResponse(this.socket, response);

                } catch (IOException e) {
                    log.error("Failed to forward FindSuccessorRequest Message to {}: {}", nextBestSuccessor.getHostname(),
                            e.getMessage());
                }
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
            log.debug("NetworkJoinNotification Message: {}", message);
            if (!message.getPeerId().equals(this.peer.getPredecessor())) {
                Client.sendMessage(this.peer.getPredecessor().getHostname(), Constants.Peer.PORT, message).close();
            }
        }
    }

}
