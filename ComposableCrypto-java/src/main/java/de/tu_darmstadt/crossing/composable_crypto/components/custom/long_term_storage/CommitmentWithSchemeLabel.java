package de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage;

import de.tu_darmstadt.crossing.composable_crypto.utils.ByteUtils;

import java.nio.charset.StandardCharsets;

public class CommitmentWithSchemeLabel {
    private static final byte VERSION = 1;
    private final String vectorCommitmentScheme;
    private final byte[] commitment;

    public CommitmentWithSchemeLabel(String vectorCommitmentScheme, byte[] commitment) {
        this.vectorCommitmentScheme = vectorCommitmentScheme;
        this.commitment = commitment;
    }

    public byte[] toByteArray() {
        // Version: 1 byte
        // Byte length of vectorCommitmentScheme: 4 bytes
        // vectorCommitmentScheme (UTF-8 encoding)
        // commitment
        byte[] vectorCommitmentSchemeBytes = vectorCommitmentScheme.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[1 + vectorCommitmentSchemeBytes.length + commitment.length];
        int index = 0;
        result[index++] = VERSION;
        index += ByteUtils.intToBytes(vectorCommitmentSchemeBytes.length, result, index);
        System.arraycopy(commitment, 0, result, index, commitment.length);
        // index += commitment.length;
        return result;
    }

    public static CommitmentWithSchemeLabel fromByteArray(byte[] bytes) {
        if (bytes[0] != VERSION) {
            throw new IllegalArgumentException("Cannot handle this version of serialized data.");
        }

        int index = 1;
        int vectorCommitmentSchemeByteSize = ByteUtils.bytesToInt(bytes, 1);
        index += Integer.BYTES;
        String vectorCommitmentScheme = new String(bytes, index, vectorCommitmentSchemeByteSize);
        index += vectorCommitmentSchemeByteSize;
        byte[] commitment = new byte[bytes.length - index];
        System.arraycopy(bytes, index, commitment, 0, commitment.length);
        return new CommitmentWithSchemeLabel(vectorCommitmentScheme, commitment);
    }
}
