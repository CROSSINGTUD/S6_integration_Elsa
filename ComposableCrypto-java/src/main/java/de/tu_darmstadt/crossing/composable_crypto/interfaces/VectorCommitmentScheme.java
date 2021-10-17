package de.tu_darmstadt.crossing.composable_crypto.interfaces;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponent;
import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicOperation;

import java.io.OutputStream;

public interface VectorCommitmentScheme extends CryptographicComponent {
    Committer createCommitment();
    Opener open();
    Verifier verifyCommitment();

    interface Committer extends CryptographicOperation {
        OutputStream addStream();
        CommitResult commit();
    }

    interface Opener extends CryptographicComponent {
        byte[] open(byte[][] decom, int index);
    }

    interface Verifier extends CryptographicOperation {
        OutputStream getStream();
        boolean verify(byte[] commitment, byte[] decommitment, int index);
    }

    class CommitResult {
        private final byte[] c;
        private final byte[][] D;

        public CommitResult(byte[] c, byte[][] d) {
            this.c = c;
            D = d;
        }

        public byte[] getCommitment() {
            return c;
        }

        public byte[][] getDecommitment() {
            return D;
        }
    }

}
