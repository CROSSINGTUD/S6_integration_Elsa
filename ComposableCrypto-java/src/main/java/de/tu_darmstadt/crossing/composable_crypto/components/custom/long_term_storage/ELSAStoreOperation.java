package de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponent;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.LongTermStorage;

import java.io.InputStream;
import java.util.UUID;

public class ELSAStoreOperation implements LongTermStorage.DataOwner.StoreOperation {
    public static ELSAStoreOperation getInstance(ELSAClient elsaClient) {
        return elsaClient.storeDataItem();
    }

    @Override
    public CryptographicComponent getComponent() {
        return null;
    }

    @Override
    public UUID addFile(InputStream data) {
        return null;
    }

    @Override
    public void store() {

    }
}
