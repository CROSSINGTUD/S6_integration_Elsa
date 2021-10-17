package de.tu_darmstadt.crossing.composable_crypto.components.jca;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicOperation;

import java.security.PublicKey;

public interface PublicKeyProvider {
    PublicKey key(CryptographicOperation operation);
}
