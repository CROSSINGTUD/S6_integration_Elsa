package de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponent;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.LongTermStorage;

import java.io.OutputStream;
import java.util.UUID;

public class ELSARetrieveOperation implements LongTermStorage.DataOwner.RetrieveOperation {
    public static ELSARetrieveOperation getInstance(ELSAClient elsaClient) {
        return elsaClient.retrieveDataItem();
    }

    @Override
    public CryptographicComponent getComponent() {
        return null;
    }

    @Override
    public void retrieveWithoutVerification(UUID itemID, OutputStream output) {

    }

    @Override
    public void retrieveAndVerify(UUID itemID, OutputStream output) throws ELSAVerificationFailedException {

    }
}
