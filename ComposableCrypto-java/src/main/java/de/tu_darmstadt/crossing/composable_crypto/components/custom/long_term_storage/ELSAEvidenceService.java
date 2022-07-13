package de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage;

import de.tu_darmstadt.crossing.composable_crypto.interfaces.LongTermStorage;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.TimestampScheme;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.VectorCommitmentScheme;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class ELSAEvidenceService implements LongTermStorage.EvidenceService {
    private final VectorCommitmentScheme vectorCommitmentScheme;
    private final TimestampScheme timestampScheme;
    private final EvidenceServiceStorageAdapter storageAdapter;

    public ELSAEvidenceService(VectorCommitmentScheme vectorCommitmentScheme, TimestampScheme timestampScheme, EvidenceServiceStorageAdapter storageAdapter) {
        this.vectorCommitmentScheme = vectorCommitmentScheme;
        this.timestampScheme = timestampScheme;
        this.storageAdapter = storageAdapter;
    }

    @Override
    public Iterable<UUID> getAllItemIDs() {
        return storageAdapter.getAllItemIDs();
    }

    @Override
    public Iterable<LongTermStorage.EvidenceItem> getProofOfIntegrity(UUID itemID) {
        return storageAdapter.getRenewListByItemID(itemID).getProofOfIntegrity().getEvidenceItems();
    }

    @Override
    public void addCommitment(Collection<UUID> itemIDs, String vectorCommitmentScheme, byte[] commitment, String timestampService) {
        CommitmentWithSchemeLabel commitmentWithSchemeLabel = new CommitmentWithSchemeLabel(vectorCommitmentScheme, commitment);
        TimestampScheme.Stamper stamper = timestampScheme.createTimestamp();
        try {
            stamper.getStream().write(commitmentWithSchemeLabel.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TimestampScheme.Timestamp stamp = stamper.stamp();
        LongTermStorage.EvidenceItem evidenceItem = new LongTermStorage.EvidenceItem(vectorCommitmentScheme, commitment, new byte[0], timestampScheme.getName(), stamp.getStamp(), stamp.getTime());

        Set<UUID> unprocessedItemIDs = new HashSet<>(itemIDs);
        List<UUID> newItemIDs = new ArrayList<>();
        while (!unprocessedItemIDs.isEmpty()) {
            UUID itemID = unprocessedItemIDs.stream().findAny().get();
            unprocessedItemIDs.remove(itemID);
            RenewList renewList = storageAdapter.getRenewListByItemID(itemID);
            if (renewList == null) {
                newItemIDs.add(itemID);
            } else {
                renewList.getProofOfIntegrity().getEvidenceItems().add(evidenceItem);
                unprocessedItemIDs.removeAll(renewList.getItemIDs());
            }
        }

        if (!newItemIDs.isEmpty()) {
            RenewList renewList = new RenewList(new ProofOfIntegrity(new ArrayList<>(Collections.singletonList(evidenceItem))), newItemIDs);
            storageAdapter.addRenewList(renewList);
        }
    }

    @Override
    public void renewTimestamps() {
        List<RenewList> renewLists = storageAdapter.getAllRenewLists();
        VectorCommitmentScheme.Committer committer = vectorCommitmentScheme.createCommitment();
        for (RenewList renewList : renewLists) {
            try (OutputStream os = committer.addStream()) {
                os.write(renewList.getProofOfIntegrity().toByteArray());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        VectorCommitmentScheme.CommitResult commitResult = committer.commit();
        byte[] commitment = new CommitmentWithSchemeLabel(vectorCommitmentScheme.getName(), commitResult.getCommitment()).toByteArray();
        TimestampScheme.Stamper stamper = timestampScheme.createTimestamp();
        try {
            stamper.getStream().write(commitment);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        TimestampScheme.Timestamp timestamp = stamper.stamp();
        VectorCommitmentScheme.Opener opener = vectorCommitmentScheme.open();
        for (int i = 0; i < renewLists.size(); i++) {
            byte[] decommitment = opener.open(commitResult.getDecommitment(), i);
            LongTermStorage.EvidenceItem evidenceItem = new LongTermStorage.EvidenceItem(vectorCommitmentScheme.getName(),
                    commitResult.getCommitment(),
                    new DecommitmentWithIndex(decommitment, i).toByteArray(),
                    timestampScheme.getName(),
                    timestamp.getStamp(),
                    timestamp.getTime());
            renewLists.get(i).getProofOfIntegrity().getEvidenceItems().add(evidenceItem);
        }
    }
}
