package de.tu_darmstadt.crossing.composable_crypto.components.jca.signature;

import de.tu_darmstadt.crossing.composable_crypto.components.jca.PrivateKeyProvider;
import de.tu_darmstadt.crossing.composable_crypto.components.jca.PublicKeyProvider;
import de.tu_darmstadt.crossing.composable_crypto.core.ComponentConfiguration;
import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponent;
import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponentBuilder;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.HashFunction;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.SignatureScheme;

import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Deque;

public class RSAWithHash implements SignatureScheme {
    private final HashFunction hashFunction;
    private final PublicKeyProvider publicKeyProvider;
    private final PrivateKeyProvider privateKeyProvider;

    private RSAWithHash(HashFunction hashFunction, PublicKeyProvider publicKeyProvider, PrivateKeyProvider privateKeyProvider) {
        this.hashFunction = hashFunction;
        this.publicKeyProvider = publicKeyProvider;
        this.privateKeyProvider = privateKeyProvider;
    }

    public static class Builder extends CryptographicComponentBuilder<RSAWithHash> {
        private final CryptographicComponentBuilder<? extends HashFunction> hashFunctionBuilder;
        private PublicKeyProvider publicKeyProvider;
        private PrivateKeyProvider privateKeyProvider;

        public Builder(CryptographicComponentBuilder<? extends HashFunction> hashFunctionBuilder) {
            this.hashFunctionBuilder = hashFunctionBuilder;
        }

        public Builder publicKeyProvider(PublicKeyProvider publicKeyProvider) {
            this.publicKeyProvider = publicKeyProvider;
            return this;
        }

        public Builder privateKeyProvider(PrivateKeyProvider privateKeyProvider) {
            this.privateKeyProvider = privateKeyProvider;
            return this;
        }

        @Override
        public RSAWithHash build(Deque<NamedEdge> parents) {
            ComponentConfiguration configuration = getConfiguration();
            if (configuration != null) {
                if (publicKeyProvider == null) {
                    publicKeyProvider = (PublicKeyProvider) configuration.query("publicKeyProvider", parents);
                }

                if (privateKeyProvider == null) {
                    privateKeyProvider = (PrivateKeyProvider) configuration.query("publicKeyProvider", parents);
                }
            }

            if (publicKeyProvider == null && privateKeyProvider == null) {
                throw new IllegalStateException("At least one of the private/public key providers must be configured.");
            }
            return new RSAWithHash(hashFunctionBuilder.build(parents, "Hash"), publicKeyProvider, privateKeyProvider);
        }
    }

    public static Builder builder(CryptographicComponentBuilder<? extends HashFunction> hashFunctionBuilder) {
        return new Builder(hashFunctionBuilder);
    }

    @Override
    public String getName() {
        return "RSAWithHash";
    }

    @Override
    public Signer createSignature() {
        if (privateKeyProvider == null) {
            throw new IllegalStateException("To create a signature, the private key provider must be configured.");
        }
        return new Signer() {
            private final HashFunction.Hasher hash = hashFunction.createHasher();
            private final Signature signature;

            {
                try {
                    signature = Signature.getInstance("NONEwithRSA");
                    signature.initSign(privateKeyProvider.key(this));
                } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public CryptographicComponent getComponent() {
                return RSAWithHash.this;
            }

            @Override
            public OutputStream getStream() {
                return new OutputStream() {
                    @Override
                    public void write(int b) {
                        try {
                            hash.getOutputStream().write(b);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void write(byte[] b) {
                        try {
                            hash.getOutputStream().write(b);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void write(byte[] b, int off, int len) {
                        try {
                            hash.getOutputStream().write(b, off, len);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
            }

            @Override
            public byte[] sign() {
                try {
                    signature.update(hash.hash());
                    return signature.sign();
                } catch (SignatureException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Override
    public Verifier verifySignature() {
        if (publicKeyProvider == null) {
            throw new IllegalStateException("To verify a signature, the public key provider must be configured.");
        }
        return new Verifier() {
            private final HashFunction.Hasher hash = hashFunction.createHasher();
            private final Signature signature;

            {
                try {
                    signature = Signature.getInstance("NONEwithRSA");
                    signature.initVerify(publicKeyProvider.key(this));
                } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public CryptographicComponent getComponent() {
                return RSAWithHash.this;
            }

            @Override
            public OutputStream getStream() {
                return new OutputStream() {
                    @Override
                    public void write(int b) {
                        try {
                            hash.getOutputStream().write(b);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void write(byte[] b) {
                        try {
                            hash.getOutputStream().write(b);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void write(byte[] b, int off, int len) {
                        try {
                            hash.getOutputStream().write(b, off, len);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
            }

            @Override
            public boolean verify(byte[] signatureToVerify) {
                try {
                    signature.update(hash.hash());
                    return signature.verify(signatureToVerify);
                } catch (SignatureException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
