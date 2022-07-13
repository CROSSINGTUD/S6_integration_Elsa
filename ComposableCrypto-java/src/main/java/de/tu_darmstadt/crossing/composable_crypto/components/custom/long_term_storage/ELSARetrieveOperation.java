package de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponent;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.*;
import org.apache.commons.collections4.IterableUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

public class ELSARetrieveOperation implements LongTermStorage.DataOwner.RetrieveOperation {
    private final SecretSharingScheme secretSharingScheme;
    private final SignatureScheme signatureScheme;
    private final TimestampScheme timestampScheme;
    private final VectorCommitmentScheme vectorCommitmentScheme;
    private final String evidenceServiceURL;
    private final String[] shareholderURLs;
    private final EvidenceServiceAdapter evidenceServiceAdapter;
    private final ShareholderAdapter shareholderAdapter;
    private final int secretSharingThreshold;
    private final ELSA elsa;
    private final ELSAClient client;

    public ELSARetrieveOperation(SecretSharingScheme secretSharingScheme, SignatureScheme signatureScheme, TimestampScheme timestampScheme, VectorCommitmentScheme vectorCommitmentScheme, String evidenceServiceURL, String[] shareholderURLs, EvidenceServiceAdapter evidenceServiceAdapter, ShareholderAdapter shareholderAdapter, int secretSharingThreshold, ELSA elsa, ELSAClient client) {
        this.secretSharingScheme = secretSharingScheme;
        this.signatureScheme = signatureScheme;
        this.timestampScheme = timestampScheme;
        this.vectorCommitmentScheme = vectorCommitmentScheme;
        this.evidenceServiceURL = evidenceServiceURL;
        this.shareholderURLs = shareholderURLs;
        this.evidenceServiceAdapter = evidenceServiceAdapter;
        this.shareholderAdapter = shareholderAdapter;
        this.secretSharingThreshold = secretSharingThreshold;
        this.elsa = elsa;
        this.client = client;
    }

    public static ELSARetrieveOperation getInstance(ELSAClient elsaClient) {
        return elsaClient.retrieveDataItem();
    }

    @Override
    public CryptographicComponent getComponent() {
        return elsa;
    }

    @Override
    public void retrieveWithoutVerification(UUID itemID, OutputStream output) {
        try {
            SignedFile signedFile = SignedFile.fromByteArray(client.retrieveSignedFileFromShareholders(itemID));
            output.write(signedFile.getFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void retrieveAndVerify(UUID itemID, OutputStream output) throws ELSAVerificationFailedException {
        byte[] signedFileBytes = client.retrieveSignedFileFromShareholders(itemID);
        SignedFile signedFile = SignedFile.fromByteArray(signedFileBytes);
        ProofOfIntegrity e = new ProofOfIntegrity(IterableUtils.toList(evidenceServiceAdapter.getProofOfIntegrity(evidenceServiceURL, itemID)));
        ProofOfIntegrity ePrime = new ProofOfIntegrity(e);
        for (int i = 0; i < ePrime.getEvidenceItems().size(); i++) {
            LongTermStorage.EvidenceItem item = ePrime.getEvidenceItems().get(i);
            if (item.getDecommitment() == null || item.getDecommitment().length == 0) {
                item.setDecommitment(client.receiveDecommitmentFromShareholders(itemID, i));
            }
        }

        SignatureScheme.Verifier sigVerifier = signatureScheme.verifySignature();
        try {
            sigVerifier.getStream().write(signedFile.getFile());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        boolean b = sigVerifier.verify(signedFile.getSignature());

        VectorCommitmentScheme.Verifier vcVerifier = vectorCommitmentScheme.verifyCommitment();
        try {
            vcVerifier.getStream().write(signedFileBytes);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        LongTermStorage.EvidenceItem evidenceItem = ePrime.getEvidenceItems().get(0);
        DecommitmentWithIndex decommitmentWithIndex = DecommitmentWithIndex.fromByteArray(evidenceItem.getDecommitment());
        b &= vcVerifier.verify(evidenceItem.getCommitment(), decommitmentWithIndex.getDecommitment(), decommitmentWithIndex.getIndex());

        TimestampScheme.Verifier tsVerifier = timestampScheme.verifyTimestamp();
        try {
            tsVerifier.getStream().write(new CommitmentWithSchemeLabel(evidenceItem.getVectorCommitmentScheme(), evidenceItem.getCommitment()).toByteArray());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        b &= tsVerifier.verify(new TimestampScheme.Timestamp(evidenceItem.getTime(), evidenceItem.getTimestamp()));

        SignedFileWithPoI signedFileWithPoI = new SignedFileWithPoI(signedFile, ePrime);
        for (int i = 1; i < e.getEvidenceItems().size(); i++) {
            evidenceItem = ePrime.getEvidenceItems().get(i);
            if (e.getEvidenceItems().get(i).getDecommitment() == null || e.getEvidenceItems().get(i).getDecommitment().length == 0) {
                /* In this case, the i-th entry came from a commitment renewal. */
                vcVerifier = vectorCommitmentScheme.verifyCommitment();
                try {
                    vcVerifier.getStream().write(signedFileWithPoI.toByteArray(i));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                decommitmentWithIndex = DecommitmentWithIndex.fromByteArray(evidenceItem.getDecommitment());
                b &= vcVerifier.verify(evidenceItem.getCommitment(), decommitmentWithIndex.getDecommitment(), decommitmentWithIndex.getIndex());

                tsVerifier = timestampScheme.verifyTimestamp();
                try {
                    tsVerifier.getStream().write(new CommitmentWithSchemeLabel(evidenceItem.getVectorCommitmentScheme(), evidenceItem.getCommitment()).toByteArray());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                b &= tsVerifier.verify(new TimestampScheme.Timestamp(evidenceItem.getTime(), evidenceItem.getTimestamp()));
            }
            else {
                /* Otherwise, the i-th entry of e came from a timestamp renewal. */
                vcVerifier = vectorCommitmentScheme.verifyCommitment();
                try {
                    vcVerifier.getStream().write(e.toByteArray(i));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                decommitmentWithIndex = DecommitmentWithIndex.fromByteArray(evidenceItem.getDecommitment());
                b &= vcVerifier.verify(evidenceItem.getCommitment(), decommitmentWithIndex.getDecommitment(), decommitmentWithIndex.getIndex());

                tsVerifier = timestampScheme.verifyTimestamp();
                try {
                    tsVerifier.getStream().write(new CommitmentWithSchemeLabel(evidenceItem.getVectorCommitmentScheme(), evidenceItem.getCommitment()).toByteArray());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                b &= tsVerifier.verify(new TimestampScheme.Timestamp(evidenceItem.getTime(), evidenceItem.getTimestamp()));
            }
        }

        if (!b) {
            throw new ELSAVerificationFailedException();
        }

        try {
            output.write(signedFile.getFile());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
