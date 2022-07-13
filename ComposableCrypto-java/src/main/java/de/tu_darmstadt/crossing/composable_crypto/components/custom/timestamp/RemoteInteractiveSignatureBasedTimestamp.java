package de.tu_darmstadt.crossing.composable_crypto.components.custom.timestamp;

import de.tu_darmstadt.crossing.composable_crypto.core.ComponentConfiguration;
import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponent;
import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponentBuilder;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.HashFunction;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.SignatureScheme;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.TimestampScheme;
import de.tu_darmstadt.crossing.composable_crypto.utils.ByteUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Deque;

public class RemoteInteractiveSignatureBasedTimestamp implements TimestampScheme {
    private final static byte VERSION = 1;
    private final SignatureScheme signatureScheme;
    private final HashFunction hashFunction;

    private TimestampScheme implementation;

    private RemoteInteractiveSignatureBasedTimestamp(SignatureScheme signatureScheme, HashFunction hashFunction) {
        this.signatureScheme = signatureScheme;
        this.hashFunction = hashFunction;
    }


    public static class Builder extends CryptographicComponentBuilder<RemoteInteractiveSignatureBasedTimestamp> {
        private final CryptographicComponentBuilder<? extends SignatureScheme> signatureSchemeBuilder;
        private final CryptographicComponentBuilder<? extends HashFunction> hashFunctionBuilder;
        private RemoteInteractiveTimestampServiceAdapter adapter;
        private OperationMode operationMode;

        public Builder(CryptographicComponentBuilder<? extends SignatureScheme> signatureSchemeBuilder, CryptographicComponentBuilder<? extends HashFunction> hashFunctionBuilder) {
            this.signatureSchemeBuilder = signatureSchemeBuilder;
            this.hashFunctionBuilder = hashFunctionBuilder;
        }

        public Builder operationMode(OperationMode operationMode) {
            this.operationMode = operationMode;
            return this;
        }

        public Builder timestampServiceAdapter(RemoteInteractiveTimestampServiceAdapter adapter) {
            this.adapter = adapter;
            return this;
        }

        @Override
        public RemoteInteractiveSignatureBasedTimestamp build(Deque<NamedEdge> parents) {
            ComponentConfiguration configuration = getConfiguration();

            if (configuration != null) {
                if (operationMode == null) {
                    operationMode = (OperationMode) configuration.query("operationMode", parents);
                }

                if (adapter == null) {
                    adapter = (RemoteInteractiveTimestampServiceAdapter) configuration.query("timestampServiceAdapter", parents);
                }
            }

            if (operationMode == null) {
                throw new IllegalStateException("The operation mode of the timestamp scheme (client/server) has to be specified.");
            }

            if ((operationMode == OperationMode.CLIENT) == (adapter == null)) {
                throw new IllegalStateException("The adapter must be null if and only if the operation mode is set to SERVER.");
            }

            RemoteInteractiveSignatureBasedTimestamp scheme = new RemoteInteractiveSignatureBasedTimestamp(signatureSchemeBuilder.build(parents, "Signature"), hashFunctionBuilder.build(parents, "Hash"));
            scheme.implementation = operationMode == OperationMode.CLIENT ? scheme.new ClientImpl(adapter) : scheme.new ServerImpl();
            return scheme;
        }
    }

    public static Builder builder(CryptographicComponentBuilder<? extends HashFunction> hashFunctionBuilder, CryptographicComponentBuilder<? extends SignatureScheme> signatureSchemeBuilder) {
        return new Builder(signatureSchemeBuilder, hashFunctionBuilder);
    }

    @Override
    public String getName() {
        return "RemoteInteractiveSignatureBasedTimestamp";
    }

    @Override
    public Stamper createTimestamp() {
        return implementation.createTimestamp();
    }

    @Override
    public Verifier verifyTimestamp() {
        return implementation.verifyTimestamp();
    }

    public enum OperationMode {
        CLIENT, SERVER;
    }

    private final class ClientImpl implements TimestampScheme {
        private final RemoteInteractiveTimestampServiceAdapter adapter;

        private ClientImpl(RemoteInteractiveTimestampServiceAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public String getName() {
            return "RemoteInteractiveSignatureBasedTimestamp_Client";
        }

        @Override
        public Stamper createTimestamp() {
            return new Stamper() {
                private final HashFunction.Hasher hasher = hashFunction.createHasher();

                @Override
                public OutputStream getStream() {
                    return hasher.getOutputStream();
                }

                @Override
                public Timestamp stamp() {
                    byte[] hash = hasher.hash();
                    return adapter.createTimestamp(hash);
                }

                @Override
                public CryptographicComponent getComponent() {
                    return RemoteInteractiveSignatureBasedTimestamp.this;
                }
            };
        }

        @Override
        public Verifier verifyTimestamp() {
            return new Verifier() {
                private final HashFunction.Hasher hasher = hashFunction.createHasher();

                @Override
                public OutputStream getStream() {
                    return hasher.getOutputStream();
                }

                @Override
                public boolean verify(Timestamp timestamp) {
                    byte[] hash = hasher.hash();
                    return adapter.verifyTimestamp(hash, timestamp);
                }

                @Override
                public CryptographicComponent getComponent() {
                    return RemoteInteractiveSignatureBasedTimestamp.this;
                }
            };
        }
    }
    private final class ServerImpl implements TimestampScheme {

        @Override
        public String getName() {
            return "RemoteInteractiveSignatureBasedTimestamp_Server";
        }

        @Override
        public Stamper createTimestamp() {
            return new Stamper() {
                private final SignatureScheme.Signer signer = signatureScheme.createSignature();

                {
                    try {
                        signer.getStream().write(VERSION);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public OutputStream getStream() {
                    return signer.getStream();
                }

                @Override
                public Timestamp stamp() {
                    // Version: 1 byte
                    // Hash: sizeof(H) bytes
                    // Timestamp: long (Long.BYTES bytes)
                    Instant now = Instant.now();
                    try {
                        signer.getStream().write(ByteUtils.longToBytes(now.getEpochSecond()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    byte[] stamp = signer.sign();
                    return new Timestamp(now, stamp);
                }

                @Override
                public CryptographicComponent getComponent() {
                    return RemoteInteractiveSignatureBasedTimestamp.this;
                }
            };
        }

        @Override
        public Verifier verifyTimestamp() {
            return new Verifier() {
                private final SignatureScheme.Verifier verifier = signatureScheme.verifySignature();

                {
                    try {
                        verifier.getStream().write(VERSION);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public OutputStream getStream() {
                    return verifier.getStream();
                }

                @Override
                public boolean verify(Timestamp timestamp) {
                    try {
                        verifier.getStream().write(ByteUtils.longToBytes(timestamp.getTime().getEpochSecond()));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return verifier.verify(timestamp.getStamp());
                }

                @Override
                public CryptographicComponent getComponent() {
                    return RemoteInteractiveSignatureBasedTimestamp.this;
                }
            };
        }
    }
}
