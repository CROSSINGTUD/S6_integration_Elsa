package de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage;

import de.tu_darmstadt.crossing.composable_crypto.interfaces.LongTermStorage;
import de.tu_darmstadt.crossing.composable_crypto.utils.ByteUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ProofOfIntegrity {
    private static final byte VERSION = 1;
    private final List<LongTermStorage.EvidenceItem> evidenceItems;

    public ProofOfIntegrity(List<LongTermStorage.EvidenceItem> evidenceItems) {
        this.evidenceItems = evidenceItems;
    }

    public ProofOfIntegrity(ProofOfIntegrity itemToCopy) {
        evidenceItems = new ArrayList<>(itemToCopy.evidenceItems.size());
        for (LongTermStorage.EvidenceItem evidenceItem : itemToCopy.evidenceItems) {
            evidenceItems.add(new LongTermStorage.EvidenceItem(evidenceItem));
        }
    }

    public List<LongTermStorage.EvidenceItem> getEvidenceItems() {
        return evidenceItems;
    }

    public int getByteSize() {
        return getByteSize(evidenceItems.size());
    }

    public int getByteSize(int firstNItems) {
        return 1 + Integer.BYTES + firstNItems * Integer.BYTES + evidenceItems.stream().limit(firstNItems).mapToInt(this::evidenceItemByteSize).sum();
    }

    public int writeToByteArray(byte[] buffer, int offset) {
        return writeToByteArray(buffer, offset, evidenceItems.size());
    }

    public int writeToByteArray(byte[] buffer, int offset, int firstNItems) {
        int index = offset;
        buffer[index++] = VERSION;
        ByteUtils.intToBytes(Math.min(firstNItems, evidenceItems.size()), buffer, index);
        index += Integer.BYTES;
        for (int i = 0; i < evidenceItems.size() && i < firstNItems; i++) {
            LongTermStorage.EvidenceItem evidenceItem = evidenceItems.get(i);
            index += writeEvidenceItem(evidenceItem, buffer, index);
        }
        return index - offset;
    }

    public byte[] toByteArray() {
        return toByteArray(evidenceItems.size());
    }

    public byte[] toByteArray(int firstNItems) {
        // Version: 1 byte
        // Length: 4 byte
        // Length of each entry (4 byte each)
        // Each entry
        byte[] result = new byte[getByteSize(firstNItems)];
        writeToByteArray(result, 0, firstNItems);
        return result;
    }

    private int evidenceItemByteSize(LongTermStorage.EvidenceItem evidenceItem) {
        int size = 0;
        size += 1; // Version: 1 byte
        size += 4; // Length of vector commitment length
        size += 4; // Length of commitment
        size += 4; // Length of decommitment
        size += 4; // Length of timeStampService
        size += 4; // Length of timestamp
        // 0: length of time is known (Long.BYTES)
        size += evidenceItem.getVectorCommitmentScheme().getBytes(StandardCharsets.UTF_8).length; // vectorCommitmentScheme
        size += evidenceItem.getCommitment().length; // commitment
        size += evidenceItem.getDecommitment().length; // decommitment
        size += evidenceItem.getTimeStampService().getBytes(StandardCharsets.UTF_8).length; // timeStampService
        size += evidenceItem.getTimestamp().length; // timestamp
        size += Long.BYTES; // time
        return size;
    }

    private int writeEvidenceItem(LongTermStorage.EvidenceItem evidenceItem, byte[] buffer, int offset) {
        int index = offset;
        byte[] vectorCommitmentSchemeBytes = evidenceItem.getVectorCommitmentScheme().getBytes(StandardCharsets.UTF_8);
        byte[] timestampServiceBytes = evidenceItem.getTimeStampService().getBytes(StandardCharsets.UTF_8);

        buffer[index++] = VERSION;
        index += ByteUtils.intToBytes(vectorCommitmentSchemeBytes.length, buffer, index);
        index += ByteUtils.intToBytes(evidenceItem.getCommitment().length, buffer, index);
        index += ByteUtils.intToBytes(evidenceItem.getDecommitment().length, buffer, index);
        index += ByteUtils.intToBytes(timestampServiceBytes.length, buffer, index);
        index += ByteUtils.intToBytes(evidenceItem.getTimestamp().length, buffer, index);

        System.arraycopy(vectorCommitmentSchemeBytes, 0, buffer, index, vectorCommitmentSchemeBytes.length);
        index += vectorCommitmentSchemeBytes.length;
        System.arraycopy(evidenceItem.getCommitment(), 0, buffer, index, evidenceItem.getCommitment().length);
        index += evidenceItem.getCommitment().length;
        System.arraycopy(evidenceItem.getDecommitment(), 0, buffer, index, evidenceItem.getDecommitment().length);
        index += evidenceItem.getDecommitment().length;
        System.arraycopy(timestampServiceBytes, 0, buffer, index, timestampServiceBytes.length);
        index += timestampServiceBytes.length;
        System.arraycopy(evidenceItem.getTimestamp(), 0, buffer, index, evidenceItem.getTimestamp().length);
        index += evidenceItem.getTimestamp().length;
        index += ByteUtils.longToBytes(evidenceItem.getTime().getEpochSecond(), buffer, index);
        return index - offset;
    }
}
