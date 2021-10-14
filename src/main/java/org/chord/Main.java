package org.chord;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import org.chord.discovery.DiscoveryNode;
import org.chord.peer.Identifier;
import org.chord.peer.Peer;
import org.chord.util.Constants;
import org.chord.util.Host;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static Peer peer;

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
                case 'e':
                    stopPeer();
                default:
                    printUsage();
                    System.exit(1);
            }
        }
    }

    private static void stopPeer() {
        peer.leaveNetwork();
    }

    private static void startPeer(String discoveryNodeHostname, String id) {
        peer = new Peer(discoveryNodeHostname, Constants.DiscoveryNode.PORT, new Identifier(Host.getHostname(), id));
        peer.startServer();
        peer.joinNetwork();
    }

    private static void printUsage() {
        String usage = "Usage: Main [OPTIONS]\n\n" +
                "\tdiscovery-node\tstart discovery node for current machine\n" +
                "\tpeer\tstart peer node for current machine\n";
        System.out.println(usage);
    }

    private static void startDiscoveryNode() {
        log.info("Starting discovery node");
        DiscoveryNode discoveryNode = new DiscoveryNode();
        discoveryNode.startServer();
    }
}
