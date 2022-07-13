package de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage;

import de.tu_darmstadt.crossing.composable_crypto.utils.ByteUtils;

public class DecommitmentWithIndex {
    private static final byte VERSION = 1;
    private final byte[] decommitment;
    private final int index;

    public DecommitmentWithIndex(byte[] decommitment, int index) {
        this.decommitment = decommitment;
        this.index = index;
    }

    public byte[] getDecommitment() {
        return decommitment;
    }

    public int getIndex() {
        return index;
    }

    public byte[] toByteArray() {
        // Version: 1 byte
        // Decommitment
        // Index: 4 bytes
        byte[] result = new byte[1 + decommitment.length + Integer.BYTES];
        result[0] = VERSION;
        System.arraycopy(decommitment, 0, result, 1, decommitment.length);
        ByteUtils.intToBytes(index, result, result.length - Integer.BYTES);
        return result;
    }

    public static DecommitmentWithIndex fromByteArray(byte[] bytes) {
        if (bytes[0] != VERSION) {
            throw new IllegalArgumentException("Cannot handle this version of serialized data.");
        }

        int index = ByteUtils.bytesToInt(bytes, bytes.length - Integer.BYTES);
        byte[] decommitment = new byte[bytes.length - 1 - Integer.BYTES];
        System.arraycopy(bytes, 1, decommitment, 0, decommitment.length);
        return new DecommitmentWithIndex(decommitment, index);
    }
}
