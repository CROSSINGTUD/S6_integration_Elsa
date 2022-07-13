package de.tu_darmstadt.crossing.composable_crypto.interfaces;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponent;
import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicOperation;

import java.io.OutputStream;

public interface HashFunction extends CryptographicComponent {
    Hasher createHasher();
    int getHashLengthInBytes();

    interface Hasher extends CryptographicOperation {
        OutputStream getOutputStream();
        byte[] hash();
    }
}
