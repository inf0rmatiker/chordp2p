package org.chord;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import org.chord.discovery.DiscoveryNode;
import org.chord.peer.Identifier;
import org.chord.peer.Peer;
import org.chord.storedata.StoreData;
import org.chord.util.Constants;
import org.chord.util.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static LongOpt[] generateValidOptions() {
        LongOpt[] longOpts = new LongOpt[3];
        longOpts[0] = new LongOpt("discovery-node", LongOpt.NO_ARGUMENT, null, 'd');
        longOpts[1] = new LongOpt("peer", LongOpt.REQUIRED_ARGUMENT, null, 'p');
        longOpts[2] = new LongOpt("store-data", LongOpt.REQUIRED_ARGUMENT, null, 's');
        return longOpts;
    }

    /**
     * @param g Getopt object
     * @param args Array of input args
     * @return String array of args for peer
     */
    public static String[] getPeerArgs(Getopt g, String[] args) {
        String[] peerArgs = new String[2];
        int index = g.getOptind() - 1;
        for (int i = 0; index < args.length; i++) {
            peerArgs[i] = args[index];
            index++;
        }
        g.setOptind(index - 1);
        return peerArgs;
    }

    public static void main(String[] args) {
        log.info("Starting main...");
        Getopt g = new Getopt("Main.java", args, "", generateValidOptions(), true);
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'd':
                    startDiscoveryNode();
                    break;
                case 'p':
                    String[] peerArgs = getPeerArgs(g, args);
                    startPeer(peerArgs[0], peerArgs[1]);
                    break;
                case 's':
                    String[] storeDataArgs = getPeerArgs(g, args);
                    startStoreData(storeDataArgs[0]);
                    break;
                default:
                    printUsage();
                    System.exit(1);
            }
        }
    }

    private static void startStoreData(String discoveryNodeHostname) {
        StoreData storeData = new StoreData(discoveryNodeHostname, Constants.DiscoveryNode.PORT);
        storeData.startServer();
    }

    private static void startPeer(String discoveryNodeHostname, String id) {
        Peer peer = new Peer(discoveryNodeHostname, Constants.DiscoveryNode.PORT, new Identifier(Host.getHostname(), id));
        peer.startServer();
        peer.joinNetwork();
    }

    private static void printUsage() {
        String usage = "Usage: Main [OPTIONS]\n\n" +
                "\t--discovery-node\tstart discovery node for current machine\n" +
                "\t--peer <discovery_node_hostname> <hex_identifier>\tstart peer node for current machine\n" +
                "\t--store-data <discovery_node_hostname>\t start store data for current machine\n";
        System.out.println(usage);
    }

    private static void startDiscoveryNode() {
        DiscoveryNode discoveryNode = new DiscoveryNode();
        discoveryNode.startServer();
    }
}
