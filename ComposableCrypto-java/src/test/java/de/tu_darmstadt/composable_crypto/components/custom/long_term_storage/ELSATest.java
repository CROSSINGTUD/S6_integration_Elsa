package de.tu_darmstadt.composable_crypto.components.custom.long_term_storage;

import de.tu_darmstadt.crossing.composable_crypto.components.custom.commitment.HM96;
import de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage.*;
import de.tu_darmstadt.crossing.composable_crypto.components.custom.secret_sharing.ShamirsSecretSharing;
import de.tu_darmstadt.crossing.composable_crypto.components.custom.timestamp.RemoteNonInteractiveSignatureBasedTimestamp;
import de.tu_darmstadt.crossing.composable_crypto.components.custom.timestamp.RemoteNonInteractiveTimestampServiceAdapter;
import de.tu_darmstadt.crossing.composable_crypto.components.custom.vector_commitment.ELSA_C2;
import de.tu_darmstadt.crossing.composable_crypto.components.custom.vector_commitment.MerkleTreeVectorCommitment;
import de.tu_darmstadt.crossing.composable_crypto.components.jca.PrivateKeyProvider;
import de.tu_darmstadt.crossing.composable_crypto.components.jca.PublicKeyProvider;
import de.tu_darmstadt.crossing.composable_crypto.components.jca.hash_function.SHA256;
import de.tu_darmstadt.crossing.composable_crypto.components.jca.signature.RSAWithHash;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.LongTermStorage;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.TimestampScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class ELSATest {
    private ELSAClient client;
    private ELSAEvidenceService evidenceService;

    @BeforeEach
    public void setup() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PublicKeyProvider publicKeyProvider = operation -> keyPair.getPublic();
        PrivateKeyProvider privateKeyProvider = operation -> keyPair.getPrivate();

        RemoteNonInteractiveSignatureBasedTimestamp timestampServer = RemoteNonInteractiveSignatureBasedTimestamp.builder(
                SHA256.builder(),
                RSAWithHash.builder(SHA256.builder())
                        .privateKeyProvider(privateKeyProvider)
                        .publicKeyProvider(publicKeyProvider)
        ).operationMode(RemoteNonInteractiveSignatureBasedTimestamp.OperationMode.SERVER).build();

        RemoteNonInteractiveTimestampServiceAdapter timestampServiceAdapter = data -> {
            TimestampScheme.Stamper stamper = timestampServer.createTimestamp();
            try {
                stamper.getStream().write(data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return stamper.stamp();
        };

        ShareholderAdapter shareholderAdapter = new ShareholderAdapter() {
            private final List<Map<UUID, byte[]>> dataItems = new ArrayList<>();
            private final List<Map<UUID, Map<Integer, byte[]>>> decommitments = new ArrayList<>();

            {
                for (int i = 0; i < 5; i++) {
                    dataItems.add(new HashMap<>());
                    decommitments.add(new HashMap<>());
                }
            }

            @Override
            public byte[] getData(String serviceURL, UUID itemID) {
                return dataItems.get(shareholderIndex(serviceURL)).get(itemID);
            }

            @Override
            public void storeData(String serviceURL, UUID itemID, byte[] data) {
                dataItems.get(shareholderIndex(serviceURL)).put(itemID, data);
            }

            @Override
            public byte[] getDecommitment(String serviceURL, UUID itemID, int index) {
                return decommitments.get(shareholderIndex(serviceURL)).get(itemID).get(index);
            }

            @Override
            public void addDecommitment(String serviceURL, UUID itemID, int index, byte[] decommitment) {
                Map<UUID, Map<Integer, byte[]>> map = decommitments.get(shareholderIndex(serviceURL));
                map.computeIfAbsent(itemID, ignored -> new HashMap<>());
                map.get(itemID).put(index, decommitment);
            }

            private int shareholderIndex(String serviceURL) {
                int index = serviceURL.length() - 1;
                while (index > 0 && Character.isDigit(serviceURL.charAt(index)))
                    index--;
                return Integer.parseInt(serviceURL.substring(index + 1)) - 1;
            }
        };

        EvidenceServiceAdapter evidenceServiceAdapter = new EvidenceServiceAdapter() {
            @Override
            public Iterable<UUID> getAllItemIDs(String serviceURL) {
                return evidenceService.getAllItemIDs();
            }

            @Override
            public Iterable<LongTermStorage.EvidenceItem> getProofOfIntegrity(String serviceURL, UUID itemID) {
                return evidenceService.getProofOfIntegrity(itemID);
            }

            @Override
            public void addCommitment(String serviceURL, Collection<UUID> itemIDs, String vectorCommitmentScheme, byte[] commitment, String timestampService) {
                evidenceService.addCommitment(itemIDs, vectorCommitmentScheme, commitment, timestampService);
            }
        };

        EvidenceServiceStorageAdapter evidenceServiceStorageAdapter = new EvidenceServiceStorageAdapter() {
            private final Map<UUID, RenewList> renewLists = new HashMap<>();
            private final List<RenewList> allRenewLists = new ArrayList<>();

            @Override
            public List<UUID> getAllItemIDs() {
                return new ArrayList<>(renewLists.keySet());
            }

            @Override
            public List<RenewList> getAllRenewLists() {
                return allRenewLists;
            }

            @Override
            public RenewList getRenewListByItemID(UUID itemID) {
                return renewLists.get(itemID);
            }

            @Override
            public void addRenewList(RenewList renewList) {
                allRenewLists.add(renewList);
                renewList.getItemIDs().forEach(id -> renewLists.put(id, renewList));
            }
        };

        ELSA elsa = ELSA.builder(
                ShamirsSecretSharing.builder(),
                RSAWithHash.builder(SHA256.builder())
                        .publicKeyProvider(publicKeyProvider)
                        .privateKeyProvider(privateKeyProvider),
                RemoteNonInteractiveSignatureBasedTimestamp.builder(
                    SHA256.builder(),
                    RSAWithHash.builder(SHA256.builder())
                            .publicKeyProvider(publicKeyProvider)
                )
                        .operationMode(RemoteNonInteractiveSignatureBasedTimestamp.OperationMode.CLIENT)
                        .timestampServiceAdapter(timestampServiceAdapter),
                ELSA_C2.builder(
                        HM96.builder(SHA256.builder()),
                        MerkleTreeVectorCommitment.builder(SHA256.builder())
                )
        )
                .secretSharingThreshold(3)
                .shareholderAdapter(shareholderAdapter)
                .evidenceServiceAdapter(evidenceServiceAdapter)
                .evidenceServiceStorageAdapter(evidenceServiceStorageAdapter)
                .evidenceServiceURL("//")
                .shareholderURLs(new String[]{ "//1", "//2", "//3", "//4", "//5" })
                .build();

        client = elsa.createClient();
        evidenceService = elsa.createEvidenceService();
    }

    @Test
    public void test() throws LongTermStorage.DataOwner.RetrieveOperation.ELSAVerificationFailedException {
        byte[][] files = new byte[][] {
                "https://www.crossing.tu-darmstadt.de/crc_1119/index.en.jsp".getBytes(StandardCharsets.UTF_8),
                "https://www.crossing.tu-darmstadt.de/research_crossing/cognicrypt/index.en.jsp".getBytes(StandardCharsets.UTF_8),
                "https://www.crossing.tu-darmstadt.de/research_crossing/project_areas/solutions/s6/index.en.jsp".getBytes(StandardCharsets.UTF_8)
        };
        ELSAStoreOperation storeOperation = client.storeDataItem();
        UUID[] uuids = new UUID[files.length];
        for (int i = 0; i < uuids.length; i++) {
            uuids[i] = storeOperation.addFile(new ByteArrayInputStream(files[i]));
        }
        storeOperation.store();

        ELSARetrieveOperation retrieveOperation = client.retrieveDataItem();
        for (int i = 0; i < files.length; i++) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                retrieveOperation.retrieveWithoutVerification(uuids[i], baos);
                byte[] retrievedFile = baos.toByteArray();
                assertArrayEquals(files[i], retrievedFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        evidenceService.renewTimestamps();
        client.renewCommitmentOperation().renewCommitments();
        evidenceService.renewTimestamps();
        client.renewCommitmentOperation().renewCommitments();
        client.renewCommitmentOperation().renewCommitments();
        evidenceService.renewTimestamps();
        evidenceService.renewTimestamps();

        for (int i = 0; i < files.length; i++) {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                retrieveOperation.retrieveAndVerify(uuids[i], baos);
                byte[] retrievedFile = baos.toByteArray();
                assertArrayEquals(files[i], retrievedFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
