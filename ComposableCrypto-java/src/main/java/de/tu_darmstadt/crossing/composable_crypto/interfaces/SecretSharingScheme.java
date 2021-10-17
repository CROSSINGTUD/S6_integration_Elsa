package de.tu_darmstadt.crossing.composable_crypto.interfaces;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponent;

public interface SecretSharingScheme extends CryptographicComponent {
    byte[][] share(int shareCount, int threshold, byte[] data);
    byte[] reconstruct(byte[][] shares) throws InvalidSharesException;

    class InvalidSharesException extends Exception {}
}
