package de.tu_darmstadt.composable_crypto;

import de.tu_darmstadt.crossing.composable_crypto.components.custom.hash_function.BCC17CandidateECRH;
import de.tu_darmstadt.crossing.composable_crypto.components.jca.hash_function.SHA256;
import de.tu_darmstadt.crossing.composable_crypto.components.jca.signature.RSAWithHash;
import de.tu_darmstadt.crossing.composable_crypto.core.ComponentConfiguration;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.HashFunction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Test {
    public void testSignature() throws IOException {
        RSAWithHash.builder(
                SHA256.builder()
        ).configure(
                new ComponentConfiguration()
                        .prop("publicKeyProvider", null)
                        .prop("privateKeyProvider", null)
                        .prop("*.key", null)
        ).build();

        RSAWithHash signatureScheme = RSAWithHash.builder(
                SHA256.builder()
        )
                .privateKeyProvider(null)
                .publicKeyProvider(null).build();

        RSAWithHash.Signer signer = signatureScheme.createSignature();
        signer.getStream().write("Hallo welt".getBytes(StandardCharsets.UTF_8));
        byte[] signature = signer.sign();
    }

    public void testELSA() {
        de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage.ELSA.Builder construction =
                de.tu_darmstadt.crossing.composable_crypto.components.custom.long_term_storage.ELSA.builder(
                        de.tu_darmstadt.crossing.composable_crypto.components.custom.transport_security.TLS13.builder(),
                        de.tu_darmstadt.crossing.composable_crypto.components.custom.secret_sharing.ShamirsSecretSharing.builder(),
                        de.tu_darmstadt.crossing.composable_crypto.components.jca.signature.RSAWithHash.builder(
                                de.tu_darmstadt.crossing.composable_crypto.components.jca.hash_function.SHA256.builder()
                        ),
                        de.tu_darmstadt.crossing.composable_crypto.components.custom.timestamp.RemoteInteractiveSignatureBasedTimestamp.builder(
                                de.tu_darmstadt.crossing.composable_crypto.components.jca.signature.RSAWithHash.builder(
                                        de.tu_darmstadt.crossing.composable_crypto.components.jca.hash_function.SHA256.builder()
                                )
                        ),
                        de.tu_darmstadt.crossing.composable_crypto.components.custom.vector_commitment.ELSA_C2.builder(
                                de.tu_darmstadt.crossing.composable_crypto.components.custom.commitment.HM96.builder(
                                        de.tu_darmstadt.crossing.composable_crypto.components.jca.hash_function.SHA256.builder()
                                ),
                                de.tu_darmstadt.crossing.composable_crypto.components.custom.vector_commitment.MerkleTreeVectorCommitment.builder(
                                        de.tu_darmstadt.crossing.composable_crypto.components.jca.hash_function.SHA256.builder()
                                )
                        )
                );
    }

    public static void main(String[] args) {
        BCC17CandidateECRH hash = BCC17CandidateECRH.builder().build();
        HashFunction.Hasher hasher = hash.createHasher();
        for (int i = 0; i < 99999-90; i++) {
            System.out.println(i);
            try {
                hasher.getOutputStream().write(-127);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(Arrays.toString(hasher.hash()));
    }
}
