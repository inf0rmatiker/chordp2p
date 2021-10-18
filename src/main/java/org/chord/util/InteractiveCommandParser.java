package org.chord.util;

import org.chord.discovery.DiscoveryNode;
import org.chord.networking.Node;
import org.chord.peer.Peer;
import org.chord.storedata.StoreData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class InteractiveCommandParser extends Thread {
    private static final Logger log = LoggerFactory.getLogger(InteractiveCommandParser.class);

    private final Scanner scanner;
    private final Node node;
    private boolean acceptingCommands;

    private enum Mode {
        Discovery, Peer, StoreData
    }

    private Mode mode;

    public InteractiveCommandParser(Node node) {
        this.node = node;
        scanner = new Scanner(System.in);
        acceptingCommands = true;
        if (node instanceof DiscoveryNode) {
            mode = Mode.Discovery;
        } else if (node instanceof Peer) {
            mode = Mode.Peer;
        } else if (node instanceof StoreData) {
            mode = Mode.StoreData;
        }
    }

    @Override
    public void run() {
        log.info("Starting Command Parser...");
        switch (mode) {
            case Discovery:
                System.out.println("Enter commands for DiscoveryNode: (Type 'help' for usage)");
                parseDiscoveryNodeCommands();
                break;
            case Peer:
                System.out.println("Enter commands for Peer: (Type 'help' for usage)");
                parsePeerCommands();
                break;
            case StoreData:
                System.out.println("Enter commands for StoreData: (Type 'help' for usage)");
                parseStoreDataCommands();
                break;
            default:
                log.error("Unknown node type: {}", mode);
        }

        // TODO: make acceptingCommands = false if interrupted
    }

    private void parseStoreDataCommands() {
        String nextCommand;
        StoreData storeData = (StoreData) node;
        while (acceptingCommands) {
            nextCommand = scanner.nextLine().trim();
            if (nextCommand.contains("add-file")) {
                // example: add-file test.txt
                String[] args = nextCommand.split("\\s+");
                if (args.length == 2) {
                    storeData.addFile(args[1]);
                } else {
                    System.out.println("Invalid parameters. Enter 'add-file <file-path>'");
                }
            } else if (nextCommand.equals("get-file")) {
                // TODO: implement file look up
            } else if (nextCommand.equals("get-host")) {
                storeData.printHost();
            } else if (nextCommand.equals("")) {
                continue;
            } else if (nextCommand.equals("help")) {
                printStoreDataUsage();
            } else {
                System.out.printf("Invalid command '%s'\n", nextCommand);
            }
        }
        log.info("Shutting down StoreData");
        scanner.close();
    }

    private void printStoreDataUsage() {
        String help = "Peer Usage:\n" +
                "\tget-host                : Print hostname\n" +
                "\tadd-file <filename>     : Add new file\n" +
                "\t\n";
        System.out.println(help);
    }

    private void parsePeerCommands() {
        String nextCommand;
        Peer peer = (Peer) node;
        while (acceptingCommands) {
            nextCommand = scanner.nextLine().trim();
            if (nextCommand.equals("get-ft")) {
                peer.printFingerTable();
            } else if (nextCommand.equals("get-files")) {
                peer.printFiles();
            } else if (nextCommand.equals("get-host")) {
                peer.printHost();
            } else if (nextCommand.equals("get-id")) {
                peer.printId();
            } else if (nextCommand.equals("get-successor") || nextCommand.equals("get-s")) {
                peer.printSuccessor();
            } else if (nextCommand.equals("get-predecessor") || nextCommand.equals("get-p")) {
                peer.printPredecessor();
            } else if (nextCommand.equals("exit")) {
                peer.leaveNetwork();
            } else if (nextCommand.equals("")) {
                continue;
            } else if (nextCommand.equals("help")) {
                printPeerUsage();
            } else {
                System.out.printf("Invalid command '%s'\n", nextCommand);
            }
        }
        log.info("Shutting down peer");
        scanner.close();
    }

    private void printPeerUsage() {
        String help = "Peer Usage:\n" +
                "\tget-host                : Print hostname\n" +
                "\tget-id                  : Print this Peer's ID\n" +
                "\tget-ft                  : Print Finger Table\n" +
                "\tget-predecessor | get-p : Print Predecessor\n" +
                "\tget-successor | get-s   : Print Successor\n" +
                "\tget-files               : Print files stored on this Peer\n" +
                "\texit                    : Leave network\n" +
                "\t\n";
        System.out.println(help);
    }



    private void parseDiscoveryNodeCommands() {
        String nextCommand;
        DiscoveryNode discoveryNode = (DiscoveryNode) node;
        while (acceptingCommands) {
            nextCommand = scanner.nextLine().trim();
            if (nextCommand.equals("get-host")) {
                discoveryNode.printHost();
            } else if (nextCommand.equals("")) {
                continue;
            } else if (nextCommand.equals("help")) {
                printDiscoveryUsage();
            } else {
                System.out.printf("Invalid command '%s'\n", nextCommand);
            }
        }
        log.info("Shutting down DiscoveryNode");
        scanner.close();
    }

    private void printDiscoveryUsage() {
        String help = "Discovery Usage:\n" +
                "\tget-host                : Print hostname\n";
        System.out.println(help);
    }
}
