package org.chord.util;

public class Constants {

    public static final int ID_SIZE_BITS = 16;
    public static final int MAX_ID = (int) (Math.pow(2, ID_SIZE_BITS) - 1); // i.e. 2^16 - 1 = 65535
    public static final int FINGER_TABLE_SIZE = ID_SIZE_BITS;

    public static class DiscoveryNode {
        public static final int PORT = 9000;
    }

    public static class Peer {
        public static final int PORT = 9001;
    }

    public static class StoreData {
        public static final int PORT = 9001;
        public static final String DATA_DIR = "/tmp";
    }
}
