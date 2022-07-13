package de.tu_darmstadt.crossing.composable_crypto.components.jca.hash_function;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponent;
import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponentBuilder;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.HashFunction;

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Deque;

public class SHA256 implements HashFunction {
    private SHA256() {}

    public static class Builder extends CryptographicComponentBuilder<SHA256> {
        @Override
        public SHA256 build(Deque<NamedEdge> parents) {
            return new SHA256();
        }
    }

    public static CryptographicComponentBuilder<SHA256> builder() {
        return new Builder();
    }

    @Override
    public String getName() {
        return "SHA-256";
    }

    @Override
    public Hasher createHasher() {
        return new Hasher() {
            private final MessageDigest messageDigest;
            private OutputStream outputStream;

            {
                try {
                    messageDigest = MessageDigest.getInstance("SHA-256");
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public CryptographicComponent getComponent() {
                return SHA256.this;
            }

            @Override
            public OutputStream getOutputStream() {
                if (outputStream == null) {
                    outputStream = new OutputStream() {
                        @Override
                        public void write(int b) throws IOException {
                            if ((byte)b != b) {
                                throw new IOException("The supplied value is not a single byte.");
                            }
                            messageDigest.update((byte) b);
                        }

                        @Override
                        public void write(byte[] b) {
                            messageDigest.update(b);
                        }

                        @Override
                        public void write(byte[] b, int off, int len) {
                            messageDigest.update(b, off, len);
                        }
                    };
                }
                return outputStream;
            }

            @Override
            public byte[] hash() {
                return messageDigest.digest();
            }
        };
    }

    @Override
    public int getHashLengthInBytes() {
        return 256 / 8;
    }
}
