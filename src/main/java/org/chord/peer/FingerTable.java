package org.chord.peer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class FingerTable {

    private static final Logger log = LoggerFactory.getLogger(FingerTable.class);

    public String id;
    public List<String> peerIds;

    public FingerTable(String id) {
        this.id = id;
        this.peerIds = new ArrayList<>(16);
    }

    public FingerTable(String id, List<String> peerIds) {
        this.id = id;
        this.peerIds = peerIds;
    }

    public String getId() {
        return id;
    }

    public List<String> getPeerIds() {
        return peerIds;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("FingerTable:\n");
        sb.append(String.format("\tid: %s\n", this.id));
        sb.append("\tpeerIds: [\n");
        for (String peerId: this.peerIds) {
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
        return this.id.equals(ftOther.getId()) && this.peerIds.equals(ftOther.getPeerIds());
    }

}
