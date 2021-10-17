package de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage;

import de.tu_darmstadt.crossing.composable_crypto.interfaces.LongTermStorage;

import java.util.Collection;
import java.util.UUID;

public interface EvidenceServiceAdapter {
    interface EvidenceService {
        Iterable<UUID> getAllItemIDs(String serviceURL);
        Iterable<LongTermStorage.EvidenceItem> getProofOfIntegrity(String serviceURL, UUID itemID);
        void addCommitment(String serviceURL, Collection<UUID> itemIDs, String vectorCommitmentScheme, byte[] commitment, String timestampService);
        void addCommitmentRenew(String serviceURL, String vectorCommitmentScheme, byte[] commitment, String timestampService);
    }
}
