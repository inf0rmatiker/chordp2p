package org.chord;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import org.chord.discovery.DiscoveryNode;
import org.chord.peer.Peer;
import org.chord.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static LongOpt[] generateValidOptions() {
        LongOpt[] longOpts = new LongOpt[2];
        longOpts[0] = new LongOpt("discovery-node", LongOpt.REQUIRED_ARGUMENT, null, 'd');
        longOpts[1] = new LongOpt("peer", LongOpt.REQUIRED_ARGUMENT, null, 'p');
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
                default:
                    printUsage();
                    System.exit(1);
            }
        }
    }

    private static void startPeer(String discoveryNodeHostname, String id) {
        Peer messagingNode = new Peer(discoveryNodeHostname, Constants.DiscoveryNode.PORT, id);
        messagingNode.startServer();
    }

    private static void printUsage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Usage: Main [options]\n\n");
        sb.append("  discovery-node\tstart discovery node for current machine\n");
        sb.append("  peer\tstart peer node for current machine\n");
        System.out.println(sb);
    }

    private static void startDiscoveryNode() {
        DiscoveryNode discoveryNode = new DiscoveryNode();
        discoveryNode.startServer();
    }
}
