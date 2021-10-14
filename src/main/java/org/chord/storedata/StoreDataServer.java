package org.chord.storedata;

import org.chord.networking.Processor;
import org.chord.networking.Server;
import org.chord.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;

public class StoreDataServer extends Server {
    private static final Logger log = LoggerFactory.getLogger(StoreDataServer.class);

    private StoreData storeData;

    public StoreDataServer(StoreData storeData) {
        this.storeData = storeData;
        this.bindToPort(Constants.StoreData.PORT);
    }

    public StoreData getStoreData() {
        return storeData;
    }
    
    @Override
    public void processConnection(Socket clientSocket) {
        Processor processor = new StoreDataProcessor(clientSocket, getStoreData());
        processor.launchAsThread();
    }
}
