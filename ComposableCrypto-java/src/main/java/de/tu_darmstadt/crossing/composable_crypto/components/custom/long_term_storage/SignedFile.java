package de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage;

import de.tu_darmstadt.crossing.composable_crypto.utils.ByteUtils;

public class SignedFile {
    private static final byte VERSION = 1;
    private final byte[] file;
    private final byte[] signature;

    public SignedFile(byte[] file, byte[] signature) {
        this.file = file;
        this.signature = signature;
    }

    public byte[] getFile() {
        return file;
    }

    public byte[] getSignature() {
        return signature;
    }

    public int getByteSize() {
        // Version (1 byte)
        // File length (4 bytes)
        // File
        // Signature
        return 1 + Integer.BYTES + file.length + signature.length;
    }

    public byte[] toByteArray() {
        // Version (1 byte)
        // File length (4 bytes)
        // File
        // Signature
        byte[] result = new byte[getByteSize()];
        writeToByteArray(result, 0);
        return result;
    }

    public int writeToByteArray(byte[] buffer, int offset) {
        // Version (1 byte)
        // File length (4 bytes)
        // File
        // Signature
        int index = offset;
        buffer[index] = VERSION;
        index++;
        index += ByteUtils.intToBytes(file.length, buffer, index);
        System.arraycopy(file, 0, buffer, index, file.length);
        index += file.length;
        System.arraycopy(signature, 0, buffer, index, signature.length);
        index += signature.length;
        return index - offset;
    }

    public static SignedFile fromByteArray(byte[] array) {
        if (array[0] != VERSION) {
            throw new IllegalArgumentException("Cannot handle this version of serialized data.");
        }
        int index = 1;
        int fileLength = ByteUtils.bytesToInt(array, 1);
        index += Integer.BYTES;
        byte[] file = new byte[fileLength];
        System.arraycopy(array, index, file, 0, file.length);
        index += fileLength;
        byte[] signature = new byte[array.length - index];
        System.arraycopy(array, index, signature, 0, signature.length);
        return new SignedFile(file, signature);
    }
}
