package de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage;

import java.util.List;
import java.util.UUID;

public interface EvidenceServiceStorageAdapter {
    List<UUID> getAllItemIDs();
    List<RenewList> getAllRenewLists();
    RenewList getRenewListByItemID(UUID itemID);
    void addRenewList(RenewList renewList);
}
