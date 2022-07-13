package de.tu_darmstadt.composable_crypto.components.custom.commitment;

import de.tu_darmstadt.crossing.composable_crypto.components.custom.commitment.HM96;
import de.tu_darmstadt.crossing.composable_crypto.components.jca.hash_function.SHA256;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.CommitmentScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HM96Test {
    private HM96 scheme;

    @BeforeEach
    public void setup() {
        scheme = HM96.builder(SHA256.builder()).build();
    }

    @Test
    public void testValid() {
        // Create test data
        byte[] data = new byte[129];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) ((3 * i + 17) % 128);
        }

        // Commit
        CommitmentScheme.Committer committer = scheme.createCommitment();
        for (byte value : data) {
            try {
                committer.getStream().write(value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        CommitmentScheme.CommitResult commitResult = committer.commit();

        // Verify
        CommitmentScheme.Verifier verifier = scheme.verifyCommitment();
        for (byte value : data) {
            try {
                verifier.getStream().write(value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Check correctness
        assertTrue(verifier.verify(commitResult.getCommitment(), commitResult.getDecommitment()));
    }

    @Test
    public void testInvalid() {
        // Create test data
        byte[] data = new byte[129];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) ((3 * i + 17) % 128);
        }

        // Commit
        CommitmentScheme.Committer committer = scheme.createCommitment();
        for (byte value : data) {
            try {
                committer.getStream().write(value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        CommitmentScheme.CommitResult commitResult = committer.commit();

        // Corrupt
        data[data.length / 2]--;

        // Verify
        CommitmentScheme.Verifier verifier = scheme.verifyCommitment();
        for (byte value : data) {
            try {
                verifier.getStream().write(value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // Check that verification fails
        assertFalse(verifier.verify(commitResult.getCommitment(), commitResult.getDecommitment()));
    }
}
