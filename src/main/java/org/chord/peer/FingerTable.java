package org.chord.peer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class FingerTable {

    private static final Logger log = LoggerFactory.getLogger(FingerTable.class);

    public Identifier identifier;
    public List<Identifier> peerIds;

    public FingerTable(Identifier id) {
        this.identifier = id;
        this.peerIds = new ArrayList<>(16);
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
