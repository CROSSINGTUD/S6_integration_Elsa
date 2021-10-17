package de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage;

import de.tu_darmstadt.crossing.composable_crypto.interfaces.LongTermStorage;

public class ELSAClient implements LongTermStorage.DataOwner {
    public static ELSAClient getInstance(ELSA elsa) {
        return elsa.createClient();
    }

    @Override
    public ELSAStoreOperation storeDataItem() {
        return null;
    }

    @Override
    public ELSARetrieveOperation retrieveDataItem() {
        return null;
    }

    @Override
    public RenewCommitmentOperation renewCommitmentOperation() {
        return null;
    }
}
