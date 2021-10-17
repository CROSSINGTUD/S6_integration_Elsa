package de.tu_darmstadt.crossing.composable_crypto.interfaces;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponent;
import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicOperation;

import java.io.OutputStream;
import java.time.Instant;

public interface TimestampScheme extends CryptographicComponent {
    Stamper createTimestamp();
    Verifier verifyTimestamp();

    interface Stamper extends CryptographicOperation {
        OutputStream getStream();
        Timestamp stamp();
    }

    interface Verifier extends CryptographicOperation {
        OutputStream getStream();
        boolean verify(Timestamp timestamp);
    }

    class Timestamp {
        private final Instant time;
        private final byte[] stamp;

        public Timestamp(Instant time, byte[] stamp) {
            this.time = time;
            this.stamp = stamp;
        }

        public Instant getTime() {
            return time;
        }

        public byte[] getStamp() {
            return stamp;
        }
    }
}
