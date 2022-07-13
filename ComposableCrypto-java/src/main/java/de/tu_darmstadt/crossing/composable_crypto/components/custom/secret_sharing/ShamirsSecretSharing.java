package de.tu_darmstadt.crossing.composable_crypto.components.custom.secret_sharing;

import de.julius_hardt.crypto.shamirs_secret_sharing.InvalidSharesException;
import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponentBuilder;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.SecretSharingScheme;

import java.util.Deque;

public class ShamirsSecretSharing implements SecretSharingScheme {
    private final de.julius_hardt.crypto.shamirs_secret_sharing.ShamirsSecretSharing sss = de.julius_hardt.crypto.shamirs_secret_sharing.ShamirsSecretSharing.create();

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
        return "ShamirsSecretSharing";
    }

    @Override
    public byte[][] share(int shareCount, int threshold, byte[] data) {
        return sss.share(shareCount, threshold, data);
    }

    @Override
    public byte[] reconstruct(byte[][] shares) throws InvalidSharesException {
        try {
            return sss.reconstruct(shares);
        } catch (de.julius_hardt.crypto.shamirs_secret_sharing.InvalidSharesException e) {
            throw new InvalidSharesException();
        }
    }
}
