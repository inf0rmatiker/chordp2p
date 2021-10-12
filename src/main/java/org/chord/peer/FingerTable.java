package org.chord.peer;

import org.chord.util.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.chord.util.Constants.MAX_ID;

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

    public Identifier successor(String id) {

        int k = HashUtil.hexToInt(id);
        int us = HashUtil.hexToInt(identifier.getId());
        log.debug("successor({}): k: {}", id, k);
        log.debug("successor({}): us: {}", id, us);

        // If it's the same position as us, return our identifier
        if (k == us) {
            log.debug("successor({}): k == us, returning our own identifier", id);
            return identifier;
        } else if (k > us) { // k after us in the ring, so no need to wrap around
            log.debug("successor({}): k > us", id);

            int ftIndex = 0;
            for (; ftIndex < peerIds.size(); ftIndex++) {
                int i = ringPositionOfIndex(ftIndex);

                // If the ring position of the FT index is the same as the requested id, return that successor
                if (i == k) {
                    return peerIds.get(i);
                }

                // If we overshot k and landed further in the ring,
                // or if we overshot k and landed past the wrapping point
                // which would be before us
                if ( i > k || i < us ) {
                    return peerIds.get(i-1);
                }
            }

        } else { // before us in the ring, so must wrap around (i.e. k < us)
            log.debug("successor({}): k < us", id);

            int ftIndex = 0;
            for (; ftIndex < peerIds.size(); ftIndex++) {
                int i = ringPositionOfIndex(ftIndex);

                // If the ring position of the FT index is the same as the requested id, return that successor
                if (i == k) {
                    return peerIds.get(i);
                }

                // i > k at two parts of the ring:
                // 1: before we wrap, but before we've reached k
                // 2: after we wrap, but after we've overshot k
                // Choose the latter, which means i < us
                if ( i > k && i < us ) {
                    return peerIds.get(i-1);
                }
            }
        }
        return peerIds.get(peerIds.size()-1); // we didn't reach k, so return the closest point we can get to it
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("FingerTable:\n");
        sb.append(String.format("\tidentifier: %s\n", this.identifier));
        sb.append("\tpeerIds: [\n");
        for (Identifier peerId: this.peerIds) {
            sb.append(String.format("\t  %s\n", peerId));
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
