package de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage;

import java.util.Collection;
import java.util.UUID;

public class RenewList {
    private final ProofOfIntegrity proofOfIntegrity;
    private final Collection<UUID> itemIDs;

    public RenewList(ProofOfIntegrity proofOfIntegrity, Collection<UUID> itemIDs) {
        this.proofOfIntegrity = proofOfIntegrity;
        this.itemIDs = itemIDs;
    }

    public ProofOfIntegrity getProofOfIntegrity() {
        return proofOfIntegrity;
    }

    public Collection<UUID> getItemIDs() {
        return itemIDs;
    }
}
