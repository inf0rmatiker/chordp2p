package org.chord.peer;

import org.chord.util.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.chord.util.Constants.MAX_ID;

/**
 * Maintains information about log(N) successor nodes (clockwise) in the chord ring network.
 * Let p be the numerical id of this node in the ring.
 * At each index i in the peer table, we store the successor(p + 2^i) Identifier.
 * Example: Where i = 0, we store the Identifier of the successor for the id 1 hop away (successor(p+1)),
 * and where i = 1, we store the Identifier of the successor for the id 2 hops away (successor(p+2)),
 * and where i = 2, we store the Identifier of the successor for the id 4 hops away (successor(p+4)).
 *
 * The successor of an id k (successor(k)) is defined by the node with the smallest id p, such that p >= k.
 */
public class FingerTable {

    private static final Logger log = LoggerFactory.getLogger(FingerTable.class);

    public Identifier identifier;
    public List<Identifier> peerIds;

    /**
     * Initializes the Finger Table with 16 copies of our peer's Identifier.
     * This is because at initialization time, we don't know about any other peers in the network.
     * @param id Our peer's Identifier
     */
    public FingerTable(int size, Identifier id) {
        this.identifier = id;
        this.peerIds = new ArrayList<>(Collections.nCopies(size, id));
    }

    public FingerTable(Identifier id, List<Identifier> peerIds) {
        this.identifier = id;
        this.peerIds = peerIds;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public List<Identifier> getPeerIds() {
        return peerIds;
    }

    public void set(int index, Identifier peerId) {
        this.peerIds.set(index, peerId);
    }

    public int size() {
        return this.peerIds.size();
    }

    /**
     * Converts a number of hops to an exponential representation.
     * @param i The number of hops we are making.
     * @return The number of ids we cover with those hops, specifically 2^(i-1)
     */
    public int hopsAwayToDistance(int i) {
        return (int) Math.pow(2, i-1);
    }

    /**
     * Converts a finger table index to a position in the ring, based on hops away from our position.
     * @param fingerTableIndex The finger table index value
     * @return The ring position based on the table index
     */
    public int ringPositionOfIndex(int fingerTableIndex) {
        int ourRingPosition = HashUtil.hexToInt(this.identifier.getId());
        int ringPositionOfIndex = (ourRingPosition + hopsAwayToDistance(fingerTableIndex+1)) % (int) (Math.pow(2, this.peerIds.size()));
        log.debug("ringPositionOfIndex({}): {}", fingerTableIndex, ringPositionOfIndex);
        return ringPositionOfIndex;
    }

    /**
     * Calculates the raw clockwise distance away from us to another id. Takes into account that the id space wraps back
     * to 0.
     * @param id The hex id we are trying to find the raw distance to, going clockwise around the ring.
     * @return The distance to the hex id, going clockwise around the ring.
     */
    public int distanceTo(String id) {
        int idValue = HashUtil.hexToInt(id);
        int ourRingPosition = HashUtil.hexToInt(this.identifier.getId());
        return distanceBetween(ourRingPosition, idValue);
    }

    /**
     * Calculates raw clockwise distance between two positions on the chord ring.
     * @param a First position
     * @param b Second position
     * @return Number of sequential hops between them, traveling clockwise
     */
    public int distanceBetween(int a, int b) {
        return (b >= a) ? b - a : ((MAX_ID + 1) - a) + b;
    }

    /**
     * Determines if we know the successor of id k:
     * If our id p < k, and k <= the first entry in our
     * finger table, then the first entry in our finger table is k's successor.
     * @param id Hex representation of k
     * @return True if we know k's successor, false if not
     */
    public boolean knowsFinalSuccessorOf(String id) {
        int k = Identifier.valueOf(id);
        int us = identifier.value();
        if (us == k) return true;

        int firstSuccessor = peerIds.get(0).value();
        return (isSuccessorOf(firstSuccessor, k));
    }

    /**
     * Tells if p is the successor of k, taking into account ring structure.
     * @param p potential successor value
     * @param k an id of a data item or node
     * @return true if p is a successor of k, false if not
     */
    public boolean isSuccessorOf(int p, int k) {
        int us = this.identifier.value();
        return isBetween(k, us, p);
    }

    /**
     * Checks if, within a ring-based index structure, k is between range i and j.
     * @param k Number we are checking
     * @param i Beginning of range
     * @param j End of range
     * @return True if k is between i and j, false otherwise
     */
    public boolean isBetween(int k, int i, int j) {
        if (i <= j) {
            return i < k && k <= j;
        } else {
            return i < k || k <= j;
        }
    }

    /**
     * Finds the successor of an identifier, using the information available in our finger table.
     * The successor(k) is the successor node with the smallest id p, such that p >= k.
     * @param id The String hex representation of k
     * @return The Identifier of the successor for k, with respect to this peer p.
     */
    public Identifier successor(String id) {
        int k = Identifier.valueOf(id);
        int p = identifier.value();
        log.debug("successor({}): k: {}", id, k);
        log.debug("successor({}): p: {}", id, p);

        // If it's the same position as p, return our identifier
        if (k == p) {
            log.debug("successor({}): k == p, returning our own identifier", id);
            return identifier;
        } else {
            for (Identifier peerId : peerIds) {
                int successor = peerId.value();
                if (isSuccessorOf(successor, k)) {
                    log.debug("successor({}): found successor {} of k {}", id, successor, k);
                    return peerId;
                }
            }
        }

        log.debug("successor({}): did not find successor of k {}, returning last successor in finger table: {}",
                id, k, peerIds.get(peerIds.size()-1));
        return peerIds.get(peerIds.size()-1); // we didn't reach k, so return the closest point we can get to it
    }

    /**
     * Iterates over finger table indices and updates their value with the new successor
     * @param successorId Identifier of our new successor
     */
    public void updateWithSuccessor(Identifier successorId) {
        int newSuccessor = successorId.value();
        for (int ftIndex = 0; ftIndex < this.peerIds.size(); ftIndex++) {
            int k = ringPositionOfIndex(ftIndex);
            int currentSuccessor = this.peerIds.get(ftIndex).value();
            if (isBetween(newSuccessor, k, currentSuccessor)) {
                this.peerIds.set(ftIndex, successorId);
                log.info("Updated successor for finger table index={}, position={}, from {} to {}", ftIndex, k,
                        currentSuccessor, newSuccessor);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\nFingerTable:\n");
        sb.append(String.format("\tidentifier: %s\n", this.identifier));
        sb.append("\tpeerIds: [\n");
        int i = 0;
        for (Identifier peerId: this.peerIds) {
            sb.append(String.format("\t %d : %s\n", i, peerId));
            i++;
        }
        sb.append("\t]\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof FingerTable)) return false;
        FingerTable ftOther = (FingerTable) o;
        return this.identifier.equals(ftOther.getIdentifier()) && this.peerIds.equals(ftOther.getPeerIds());
    }

}
