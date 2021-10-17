package de.tu_darmstadt.crossing.composable_crypto.interfaces;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponent;
import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicOperation;

import java.io.OutputStream;

public interface SignatureScheme extends CryptographicComponent {
    Signer createSignature();
    Verifier verifySignature();

    interface Signer extends CryptographicOperation {
        OutputStream getStream();
        byte[] sign();
    }

    interface Verifier extends CryptographicOperation {
        OutputStream getStream();
        boolean verify(byte[] signatureToVerify);
    }
}
