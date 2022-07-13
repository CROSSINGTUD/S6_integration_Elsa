package de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponent;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.LongTermStorage;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.SecretSharingScheme;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.SignatureScheme;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.VectorCommitmentScheme;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ELSAStoreOperation implements LongTermStorage.DataOwner.StoreOperation {
    private final ELSA elsa;
    private final SecretSharingScheme secretSharingScheme;
    private final SignatureScheme signatureScheme;
    private final VectorCommitmentScheme vectorCommitmentScheme;
    private final String evidenceServiceURL;
    private final String[] shareholderURLs;
    private final EvidenceServiceAdapter evidenceServiceAdapter;
    private final ShareholderAdapter shareholderAdapter;
    private final int secretSharingThreshold;

    private final List<FileInfo> filesToStore = new ArrayList<>();

    public ELSAStoreOperation(ELSA elsa, SecretSharingScheme secretSharingScheme, SignatureScheme signatureScheme, VectorCommitmentScheme vectorCommitmentScheme, String evidenceServiceURL, String[] shareholderURLs, EvidenceServiceAdapter evidenceServiceAdapter, ShareholderAdapter shareholderAdapter, int secretSharingThreshold) {
        this.elsa = elsa;
        this.secretSharingScheme = secretSharingScheme;
        this.signatureScheme = signatureScheme;
        this.vectorCommitmentScheme = vectorCommitmentScheme;
        this.evidenceServiceURL = evidenceServiceURL;
        this.shareholderURLs = shareholderURLs;
        this.evidenceServiceAdapter = evidenceServiceAdapter;
        this.shareholderAdapter = shareholderAdapter;
        this.secretSharingThreshold = secretSharingThreshold;
    }

    public static ELSAStoreOperation getInstance(ELSAClient elsaClient) {
        return elsaClient.storeDataItem();
    }

    @Override
    public CryptographicComponent getComponent() {
        return elsa;
    }

    @Override
    public UUID addFile(InputStream data) {
        UUID uuid = UUID.randomUUID();
        try {
            byte[] bytes = IOUtils.toByteArray(data);
            filesToStore.add(new FileInfo(uuid, bytes));
            return uuid;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void store() {
        try {
            VectorCommitmentScheme.Committer committer = vectorCommitmentScheme.createCommitment();

            for (FileInfo fileInfo : filesToStore) {
                // Sign file
                SignatureScheme.Signer signer = signatureScheme.createSignature();
                signer.getStream().write(fileInfo.getContents());
                byte[] signature = signer.sign();
                SignedFile signedFile = new SignedFile(fileInfo.getContents(), signature);

                // Create secret shares
                byte[] signedFileBytes = signedFile.toByteArray();
                byte[][] shares = secretSharingScheme.share(shareholderURLs.length, secretSharingThreshold, signedFileBytes);

                for (int i = 0; i < shareholderURLs.length; i++) {
                    shareholderAdapter.storeData(shareholderURLs[i], fileInfo.getUUID(), shares[i]);
                }

                // Add to vector commitment
                try (OutputStream os = committer.addStream()) {
                    os.write(signedFileBytes);
                }
            }

            // Commit
            VectorCommitmentScheme.CommitResult commitResult = committer.commit();
            VectorCommitmentScheme.Opener opener = vectorCommitmentScheme.open();

            // Open commitment and store decommitments
            for (int i = 0; i < filesToStore.size(); i++) {
                byte[] decom = opener.open(commitResult.getDecommitment(), i);
                byte[] decomWithIndex = new DecommitmentWithIndex(decom, i).toByteArray();
                byte[][] shares = secretSharingScheme.share(shareholderURLs.length, secretSharingThreshold, decomWithIndex);
                for (int j = 0; j < shareholderURLs.length; j++) {
                    shareholderAdapter.addDecommitment(shareholderURLs[j], filesToStore.get(i).getUUID(), 0, shares[j]);
                }
            }

            // ES.AddCom
            evidenceServiceAdapter.addCommitment(evidenceServiceURL,
                    filesToStore.stream().map(FileInfo::getUUID).collect(Collectors.toList()),
                    vectorCommitmentScheme.getName(),
                    commitResult.getCommitment(),
                    null /* reserve for future use */);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class FileInfo {
        private final UUID uuid;
        private final byte[] contents;

        public FileInfo(UUID uuid, byte[] contents) {
            this.uuid = uuid;
            this.contents = contents;
        }

        public UUID getUUID() {
            return uuid;
        }

        public byte[] getContents() {
            return contents;
        }
    }
}
