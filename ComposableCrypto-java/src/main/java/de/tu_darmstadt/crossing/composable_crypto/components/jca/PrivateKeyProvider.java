package de.tu_darmstadt.crossing.composable_crypto.components.jca;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicOperation;

import java.security.PrivateKey;

public interface PrivateKeyProvider {
    PrivateKey key(CryptographicOperation operation);
}
