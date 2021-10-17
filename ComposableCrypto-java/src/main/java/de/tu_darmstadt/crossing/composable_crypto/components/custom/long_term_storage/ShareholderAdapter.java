package de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage;

import java.util.UUID;

public interface ShareholderAdapter {
    byte[] getData(String serviceURL, UUID itemID);
    void storeData(String serviceURL, UUID itemID, byte[] data);
    byte[] getDecommitment(String serviceURL, UUID itemID, int index);
    void addDecommitment(String serviceURL, UUID itemID, int index, byte[] decommitment);
}
