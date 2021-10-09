package org.chord;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import org.chord.discovery.DiscoveryNode;
import org.chord.peer.MessagingNode;
import org.chord.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static LongOpt[] generateValidOptions() {
        LongOpt[] longOpts = new LongOpt[2];
        longOpts[0] = new LongOpt("discovery-node", LongOpt.REQUIRED_ARGUMENT, null, 's');
        longOpts[0] = new LongOpt("messaging-node", LongOpt.REQUIRED_ARGUMENT, null, 'm');

        return longOpts;
    }

    public static void main(String[] args) {
        Getopt g = new Getopt("Main.java", args, "", generateValidOptions(), true);
        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'd':
                    startDiscoveryNode();
                    break;
                case 'm':
                    startMessagingNode(g.getOptarg());
                    break;
                default:
                    printUsage();
                    System.exit(1);
            }
        }
    }

    private static void startMessagingNode(String discoveryNodeHostname) {
        MessagingNode messagingNode = new MessagingNode(discoveryNodeHostname, Constants.DiscoveryNode.PORT);
        messagingNode.startServer();
    }

    private static void printUsage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Usage: Main [options]\n\n");
        sb.append("--discovery-node");
        sb.append("\t start discovery node for current machine");
        System.out.println(sb);
    }

    private static void startDiscoveryNode() {
        DiscoveryNode discoveryNode = new DiscoveryNode();
        discoveryNode.startServer();
    }
}
