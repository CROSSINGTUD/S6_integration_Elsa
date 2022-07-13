package de.tu_darmstadt.crossing.composable_crypto.utils;

public final class ByteUtils {
    public static int bytesToInt(final byte[] b, int offset) {
        int result = 0;
        for (int i = offset; i < offset + Integer.BYTES; i++) {
            result <<= Byte.SIZE;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    public static byte[] intToBytes(int d) {
        byte[] result = new byte[Integer.BYTES];
        intToBytes(d, result, 0);
        return result;
    }

    public static int intToBytes(int d, byte[] outBuf, int offset) {
        for (int i = offset + Integer.BYTES - 1; i >= offset; i--) {
            outBuf[i] = (byte)(d & 0xFF);
            d >>= Byte.SIZE;
        }
        return Integer.BYTES;
    }

    public static byte[] longToBytes(long l) {
        byte[] result = new byte[Long.BYTES];
        longToBytes(l, result, 0);
        return result;
    }

    public static int longToBytes(long l, byte[] outBuf, int offset) {
        for (int i = offset + Long.BYTES - 1; i >= offset; i--) {
            outBuf[i] = (byte)(l & 0xFF);
            l >>= Byte.SIZE;
        }
        return Long.BYTES;
    }

    public static long bytesToLong(final byte[] b, int offset) {
        long result = 0;
        for (int i = offset; i < offset + Long.BYTES; i++) {
            result <<= Byte.SIZE;
            result |= (b[i] & 0xFF);
        }
        return result;
    }
}
