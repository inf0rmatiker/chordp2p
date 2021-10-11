package org.chord.peer;

public class Identifier {

    // The hostname of the peer
    public String hostname;

    // The hex identifier of the peer
    public String id;

    public Identifier(String hostname, String id) {
        this.hostname = hostname;
        this.id = id;
    }

    public String getHostname() {
        return hostname;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (!(o instanceof Identifier)) return false;
        Identifier idOther = (Identifier) o;
        return this.hostname.equals(idOther.getHostname()) && this.id.equals(idOther.getId());
    }

    @Override
    public String toString() {
        return "Identifier:\n" +
                String.format("\thostname: %s\n", this.hostname) +
                String.format("\tid: %s\n", this.id);
    }
}
