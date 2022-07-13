package de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponent;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.LongTermStorage;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.SecretSharingScheme;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.SignatureScheme;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.VectorCommitmentScheme;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.IteratorUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class ELSARenewCommitmentOperation implements LongTermStorage.DataOwner.RenewCommitmentOperation {
    private final SecretSharingScheme secretSharingScheme;
    private final VectorCommitmentScheme vectorCommitmentScheme;
    private final String evidenceServiceURL;
    private final String[] shareholderURLs;
    private final EvidenceServiceAdapter evidenceServiceAdapter;
    private final ShareholderAdapter shareholderAdapter;
    private final int secretSharingThreshold;
    private final ELSA elsa;
    private final ELSAClient client;

    public ELSARenewCommitmentOperation(SecretSharingScheme secretSharingScheme, VectorCommitmentScheme vectorCommitmentScheme, String evidenceServiceURL, String[] shareholderURLs, EvidenceServiceAdapter evidenceServiceAdapter, ShareholderAdapter shareholderAdapter, int secretSharingThreshold, ELSA elsa, ELSAClient client) {
        this.secretSharingScheme = secretSharingScheme;
        this.vectorCommitmentScheme = vectorCommitmentScheme;
        this.evidenceServiceURL = evidenceServiceURL;
        this.shareholderURLs = shareholderURLs;
        this.evidenceServiceAdapter = evidenceServiceAdapter;
        this.shareholderAdapter = shareholderAdapter;
        this.secretSharingThreshold = secretSharingThreshold;
        this.elsa = elsa;
        this.client = client;
    }

    @Override
    public CryptographicComponent getComponent() {
        return elsa;
    }

    @Override
    public void renewCommitments() {
        Map<UUID, Integer> comCount = new HashMap<>();
        List<SignedFileWithPoI> L = new ArrayList<>();

        List<UUID> allItemIDs = IterableUtils.toList(evidenceServiceAdapter.getAllItemIDs(evidenceServiceURL));
        for (UUID itemID : allItemIDs) {
            SignedFile signedFile = SignedFile.fromByteArray(client.retrieveSignedFileFromShareholders(itemID));
            ProofOfIntegrity proofOfIntegrity = new ProofOfIntegrity(new ProofOfIntegrity(IterableUtils.toList(evidenceServiceAdapter.getProofOfIntegrity(evidenceServiceURL, itemID))));
            for (int i = 0; i < proofOfIntegrity.getEvidenceItems().size(); i++) {
                LongTermStorage.EvidenceItem item = proofOfIntegrity.getEvidenceItems().get(i);
                if (item.getDecommitment() == null || item.getDecommitment().length == 0) {
                    item.setDecommitment(client.receiveDecommitmentFromShareholders(itemID, i));
                }
            }
            L.add(new SignedFileWithPoI(signedFile, proofOfIntegrity));
            comCount.put(itemID, proofOfIntegrity.getEvidenceItems().size());
        }

        VectorCommitmentScheme.Committer committer = vectorCommitmentScheme.createCommitment();
        for (SignedFileWithPoI l : L) {
            try (OutputStream os = committer.addStream()) {
                byte[] bytes = l.toByteArray();
                os.write(bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        VectorCommitmentScheme.CommitResult commit = committer.commit();
        VectorCommitmentScheme.Decommitment D = commit.getDecommitment();
        VectorCommitmentScheme.Opener opener = vectorCommitmentScheme.open();

        for (int i = 0; i < allItemIDs.size(); i++) {
            UUID itemID = allItemIDs.get(i);
            byte[] decom = opener.open(D, i);
            byte[] decomWithIndex = new DecommitmentWithIndex(decom, i).toByteArray();

            byte[][] shares = secretSharingScheme.share(shareholderURLs.length, secretSharingThreshold, decomWithIndex);
            for (int j = 0; j < shareholderURLs.length; j++) {
                shareholderAdapter.addDecommitment(shareholderURLs[j], itemID, comCount.get(itemID), shares[j]);
            }
        }

        evidenceServiceAdapter.addCommitment(evidenceServiceURL, allItemIDs, vectorCommitmentScheme.getName(), commit.getCommitment(), null /* reserved for future use */);
    }
}
