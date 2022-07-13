package de.tu_darmstadt.composable_crypto.components.jca.signature;

import de.tu_darmstadt.crossing.composable_crypto.components.jca.PrivateKeyProvider;
import de.tu_darmstadt.crossing.composable_crypto.components.jca.PublicKeyProvider;
import de.tu_darmstadt.crossing.composable_crypto.components.jca.hash_function.SHA256;
import de.tu_darmstadt.crossing.composable_crypto.components.jca.signature.RSAWithHash;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.SignatureScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RSAWithHashTest {
    private RSAWithHash scheme;

    @BeforeEach
    public void setup() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PublicKeyProvider publicKeyProvider = operation -> keyPair.getPublic();
        PrivateKeyProvider privateKeyProvider = operation -> keyPair.getPrivate();

        scheme = RSAWithHash.builder(
                        SHA256.builder()
                )
                .publicKeyProvider(publicKeyProvider)
                .privateKeyProvider(privateKeyProvider).build();
    }

    @Test
    public void test() {
        byte[] data = "Hello world!".getBytes(StandardCharsets.UTF_8);
        SignatureScheme.Signer signer = scheme.createSignature();
        try {
            signer.getStream().write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        byte[] signature = signer.sign();

        SignatureScheme.Verifier verifier = scheme.verifySignature();
        try {
            verifier.getStream().write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertTrue(verifier.verify(signature));

        // Corrupt signature
        signature[signature.length / 2]++;

        verifier = scheme.verifySignature();
        try {
            verifier.getStream().write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertFalse(verifier.verify(signature));
    }
}
