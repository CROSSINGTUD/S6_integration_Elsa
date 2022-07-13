package de.tu_darmstadt.composable_crypto.components.custom.vector_commitment;

import de.tu_darmstadt.crossing.composable_crypto.components.custom.vector_commitment.MerkleTreeVectorCommitment;
import de.tu_darmstadt.crossing.composable_crypto.components.jca.hash_function.SHA256;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.VectorCommitmentScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MerkleTreeCommitmentSchemeTest {
    MerkleTreeVectorCommitment scheme;

    @BeforeEach
    public void setup() {
        scheme = MerkleTreeVectorCommitment.builder(SHA256.builder()).build();
    }

    @Test
    public void test1() {
        byte[][] data = new byte[][]{
                new byte[]{1, 2, 3, 4, 5, 6},
                new byte[]{7, 8, 9},
                new byte[]{10, 11, 12, 13, 14, 15},
                new byte[]{16, 17, 18, 19}
        };

        // Commit
        VectorCommitmentScheme.Committer builder = scheme.createCommitment();
        for (byte[] message : data) {
            try (OutputStream os = builder.addStream()) {
                os.write(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        VectorCommitmentScheme.CommitResult comResult = builder.commit();
        byte[] commitment = comResult.getCommitment();

        // Open
        VectorCommitmentScheme.Decommitment comResultDecommitment = comResult.getDecommitment();
        byte[][] decommitments = new byte[data.length][];
        for (int i = 0; i < data.length; i++) {
            decommitments[i] = scheme.open().open(comResultDecommitment, i);
        }

        // Verify
        for (int i = 0; i < data.length; i++) {
            VectorCommitmentScheme.Verifier verifier = scheme.verifyCommitment();
            try {
                verifier.getStream().write(data[i]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            assertTrue(verifier.verify(commitment, decommitments[i], i));
        }
    }
}
