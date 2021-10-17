package de.tu_darmstadt.crossing.composable_crypto.components.custom.secret_sharing;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponentBuilder;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.SecretSharingScheme;

import java.util.Deque;

public class ShamirsSecretSharing implements SecretSharingScheme {
    private ShamirsSecretSharing() {}

    public static class Builder extends CryptographicComponentBuilder<ShamirsSecretSharing> {

        @Override
        public ShamirsSecretSharing build(Deque<NamedEdge> parents) {
            return new ShamirsSecretSharing();
        }
    }

    public static CryptographicComponentBuilder<ShamirsSecretSharing> builder() {
        return new Builder();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public byte[][] share(int shareCount, int threshold, byte[] data) {
        return new byte[0][];
    }

    @Override
    public byte[] reconstruct(byte[][] shares) throws InvalidSharesException {
        return new byte[0];
    }
}
