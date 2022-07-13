package de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage;

import de.tu_darmstadt.crossing.composable_crypto.interfaces.*;

import java.util.UUID;

public class ELSAClient implements LongTermStorage.DataOwner {
    private final ELSA elsa;
    private final SecretSharingScheme secretSharingScheme;
    private final SignatureScheme signatureScheme;
    private final TimestampScheme timestampScheme;
    private final VectorCommitmentScheme vectorCommitmentScheme;
    private final String evidenceServiceURL;
    private final String[] shareholderURLs;
    private final EvidenceServiceAdapter evidenceServiceAdapter;
    private final ShareholderAdapter shareholderAdapter;
    private final int secretSharingThreshold;

    ELSAClient(ELSA elsa, SecretSharingScheme secretSharingScheme, SignatureScheme signatureScheme, TimestampScheme timestampScheme, VectorCommitmentScheme vectorCommitmentScheme, String evidenceServiceURL, String[] shareholderURLs, EvidenceServiceAdapter evidenceServiceAdapter, ShareholderAdapter shareholderAdapter, int secretSharingThreshold) {
        this.elsa = elsa;
        this.secretSharingScheme = secretSharingScheme;
        this.signatureScheme = signatureScheme;
        this.timestampScheme = timestampScheme;
        this.vectorCommitmentScheme = vectorCommitmentScheme;
        this.evidenceServiceURL = evidenceServiceURL;
        this.shareholderURLs = shareholderURLs;
        this.evidenceServiceAdapter = evidenceServiceAdapter;
        this.shareholderAdapter = shareholderAdapter;
        this.secretSharingThreshold = secretSharingThreshold;
    }

    public static ELSAClient getInstance(ELSA elsa) {
        return elsa.createClient();
    }

    @Override
    public ELSAStoreOperation storeDataItem() {
        return new ELSAStoreOperation(elsa, secretSharingScheme, signatureScheme, vectorCommitmentScheme, evidenceServiceURL, shareholderURLs, evidenceServiceAdapter, shareholderAdapter, secretSharingThreshold);
    }

    @Override
    public ELSARetrieveOperation retrieveDataItem() {
        return new ELSARetrieveOperation(secretSharingScheme, signatureScheme, timestampScheme, vectorCommitmentScheme, evidenceServiceURL, shareholderURLs, evidenceServiceAdapter, shareholderAdapter, secretSharingThreshold, elsa, this);
    }

    @Override
    public RenewCommitmentOperation renewCommitmentOperation() {
        return new ELSARenewCommitmentOperation(secretSharingScheme, vectorCommitmentScheme, evidenceServiceURL, shareholderURLs, evidenceServiceAdapter, shareholderAdapter, secretSharingThreshold, elsa, this);
    }

    byte[] retrieveSignedFileFromShareholders(UUID itemId) {
        byte[][] shares = new byte[secretSharingThreshold][];
        int index = 0;
        for (int i = 0; i < shareholderURLs.length && index < secretSharingThreshold && secretSharingThreshold - index <= shareholderURLs.length - i; i++) {
            try {
                byte[] share = shareholderAdapter.getData(shareholderURLs[i], itemId);
                if (share != null) {
                    shares[index++] = share;
                }
            } catch (Exception ignored) {}
        }

        if (index != secretSharingThreshold) {
            throw new RuntimeException(String.format("ELSA.RenewCom failed: Unable to retrieve file %s because not enough shareholders have the required shares to reconstruct it.", itemId));
        }

        try {
            return secretSharingScheme.reconstruct(shares);
        } catch (SecretSharingScheme.InvalidSharesException e) {
            throw new RuntimeException(e);
        }
    }

    byte[] receiveDecommitmentFromShareholders(UUID itemId, int decomIndex) {
        byte[][] shares = new byte[secretSharingThreshold][];
        int index = 0;
        for (int i = 0; i < shareholderURLs.length && index < secretSharingThreshold && secretSharingThreshold - index <= shareholderURLs.length - i; i++) {
            try {
                byte[] share = shareholderAdapter.getDecommitment(shareholderURLs[i], itemId, decomIndex);
                if (share != null) {
                    shares[index++] = share;
                }
            } catch (Exception ignored) {}
        }

        if (index != secretSharingThreshold) {
            throw new RuntimeException(String.format("ELSA.RenewCom failed: Unable to retrieve decommitment %d for %s because not enough shareholders have the required shares to reconstruct it.", decomIndex, itemId));
        }

        try {
            return secretSharingScheme.reconstruct(shares);
        } catch (SecretSharingScheme.InvalidSharesException e) {
            throw new RuntimeException(e);
        }
    }
}
