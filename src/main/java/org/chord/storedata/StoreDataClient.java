package org.chord.storedata;

import org.chord.networking.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreDataClient extends Client {
    private static final Logger log = LoggerFactory.getLogger(StoreDataClient.class);

    private StoreData storeData;

    public StoreDataClient(StoreData storeData) {
        this.storeData = storeData;
    }

    public StoreData getStoreData() {
        return storeData;
    }
}
