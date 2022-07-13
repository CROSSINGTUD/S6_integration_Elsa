package de.tu_darmstadt.crossing.composable_crypto.interfaces;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponent;
import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicOperation;

import java.io.OutputStream;

public interface CommitmentScheme extends CryptographicComponent {
    Committer createCommitment();
    Verifier verifyCommitment();

    interface Committer extends CryptographicOperation {
        OutputStream getStream();
        CommitResult commit();
    }

    interface Verifier extends CryptographicOperation {
        OutputStream getStream();
        boolean verify(byte[] commitment, byte[] decommitment);
    }

    class CommitResult {
        private final byte[] c;
        private final byte[] d;

        public CommitResult(byte[] c, byte[] d) {
            this.c = c;
            this.d = d;
        }

        public byte[] getCommitment() {
            return c;
        }

        public byte[] getDecommitment() {
            return d;
        }
    }

}
