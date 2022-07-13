package de.tu_darmstadt.composable_crypto.components.custom.timestamp;

import de.tu_darmstadt.crossing.composable_crypto.components.custom.timestamp.RemoteNonInteractiveSignatureBasedTimestamp;
import de.tu_darmstadt.crossing.composable_crypto.components.custom.timestamp.RemoteNonInteractiveTimestampServiceAdapter;
import de.tu_darmstadt.crossing.composable_crypto.components.jca.PrivateKeyProvider;
import de.tu_darmstadt.crossing.composable_crypto.components.jca.PublicKeyProvider;
import de.tu_darmstadt.crossing.composable_crypto.components.jca.hash_function.SHA256;
import de.tu_darmstadt.crossing.composable_crypto.components.jca.signature.RSAWithHash;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.TimestampScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RemoteNonInteractiveSignatureBasedTimestampTest {
    private RemoteNonInteractiveSignatureBasedTimestamp scheme;

    @BeforeEach
    public void setup() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PublicKeyProvider publicKeyProvider = operation -> keyPair.getPublic();
        PrivateKeyProvider privateKeyProvider = operation -> keyPair.getPrivate();

        RemoteNonInteractiveSignatureBasedTimestamp server = RemoteNonInteractiveSignatureBasedTimestamp.builder(
                SHA256.builder(),
                RSAWithHash.builder(SHA256.builder())
                        .privateKeyProvider(privateKeyProvider)
                        .publicKeyProvider(publicKeyProvider)
        ).operationMode(RemoteNonInteractiveSignatureBasedTimestamp.OperationMode.SERVER).build();

        RemoteNonInteractiveTimestampServiceAdapter adapter = data -> {
            TimestampScheme.Stamper stamper = server.createTimestamp();
            try {
                stamper.getStream().write(data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return stamper.stamp();
        };

        scheme = RemoteNonInteractiveSignatureBasedTimestamp.builder(
                SHA256.builder(),
                RSAWithHash.builder(SHA256.builder())
                        .publicKeyProvider(publicKeyProvider)
        )
                .operationMode(RemoteNonInteractiveSignatureBasedTimestamp.OperationMode.CLIENT)
                .timestampServiceAdapter(adapter)
                .build();
    }

    @Test
    public void test() throws IOException {
        byte[] data = "Hello world!".getBytes(StandardCharsets.UTF_8);
        TimestampScheme.Stamper stamper = scheme.createTimestamp();
        stamper.getStream().write(data);
        TimestampScheme.Timestamp timestamp = stamper.stamp();

        TimestampScheme.Verifier verifier = scheme.verifyTimestamp();
        verifier.getStream().write(data);
        assertTrue(verifier.verify(new TimestampScheme.Timestamp(timestamp.getTime(), timestamp.getStamp())));


        byte[] corruptedTimestamp = new byte[timestamp.getStamp().length];
        System.arraycopy(timestamp.getStamp(), 0, corruptedTimestamp, 0, corruptedTimestamp.length);
        corruptedTimestamp[corruptedTimestamp.length / 3]++;
        verifier = scheme.verifyTimestamp();
        verifier.getStream().write(data);
        assertFalse(verifier.verify(new TimestampScheme.Timestamp(timestamp.getTime(), corruptedTimestamp)));
    }
}
