package de.tu_darmstadt.crossing.composable_crypto.interfaces;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponent;
import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicOperation;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

public interface LongTermStorage extends CryptographicComponent {
    DataOwner createClient();
    EvidenceService createEvidenceService();
    Shareholder createShareholder();

    interface DataOwner {
        StoreOperation storeDataItem();
        RetrieveOperation retrieveDataItem();
        RenewCommitmentOperation renewCommitmentOperation();

        interface StoreOperation extends CryptographicOperation {
            UUID addFile(InputStream data);
            void store();
        }

        interface RetrieveOperation extends CryptographicOperation {
            void retrieveWithoutVerification(UUID itemID, OutputStream output);
            void retrieveAndVerify(UUID itemID, OutputStream output) throws ELSAVerificationFailedException;
            class ELSAVerificationFailedException extends Exception {

            }
        }

        interface RenewCommitmentOperation extends CryptographicOperation {
            void renewCommitments();
        }
    }

    interface EvidenceService {
        Iterable<UUID> getAllItemIDs();
        Iterable<EvidenceItem> getProofOfIntegrity(UUID itemID);
        void addCommitment(Collection<UUID> itemIDs, String vectorCommitmentScheme, byte[] commitment, String timestampService);
        void addCommitmentRenew(String vectorCommitmentScheme, byte[] commitment, String timestampService);
    }

    interface Shareholder {
        byte[] getData(UUID itemID);
        void storeData(UUID itemID, byte[] data);
        byte[] getDecommitment(UUID itemID, int index);
        void addDecommitment(UUID itemID, int index, byte[] decommitment);
    }

    class EvidenceItem {
        private String vectorCommitmentScheme;
        private byte[] commitment;
        private byte[] decommitment;
        private String timeStampService;
        private byte[] timestamp;
        private Instant time;

        public EvidenceItem(String vectorCommitmentScheme, byte[] commitment, byte[] decommitment, String timeStampService, byte[] timestamp, Instant time) {
            this.vectorCommitmentScheme = vectorCommitmentScheme;
            this.commitment = commitment;
            this.decommitment = decommitment;
            this.timeStampService = timeStampService;
            this.timestamp = timestamp;
            this.time = time;
        }

        public String getVectorCommitmentScheme() {
            return vectorCommitmentScheme;
        }

        public void setVectorCommitmentScheme(String vectorCommitmentScheme) {
            this.vectorCommitmentScheme = vectorCommitmentScheme;
        }

        public byte[] getCommitment() {
            return commitment;
        }

        public void setCommitment(byte[] commitment) {
            this.commitment = commitment;
        }

        public byte[] getDecommitment() {
            return decommitment;
        }

        public void setDecommitment(byte[] decommitment) {
            this.decommitment = decommitment;
        }

        public String getTimeStampService() {
            return timeStampService;
        }

        public void setTimeStampService(String timeStampService) {
            this.timeStampService = timeStampService;
        }

        public byte[] getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(byte[] timestamp) {
            this.timestamp = timestamp;
        }

        public Instant getTime() {
            return time;
        }

        public void setTime(Instant time) {
            this.time = time;
        }
    }
}
