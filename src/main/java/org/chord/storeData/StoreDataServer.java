package org.chord.storeData;

import org.chord.networking.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;

public class StoreDataServer extends Server {
    private static final Logger logger = LoggerFactory.getLogger(StoreDataServer.class);
    
    @Override
    public void processConnection(Socket clientSocket) {

    }
}
