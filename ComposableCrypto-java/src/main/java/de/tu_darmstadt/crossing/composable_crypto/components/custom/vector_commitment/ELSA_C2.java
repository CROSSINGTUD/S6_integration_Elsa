package de.tu_darmstadt.crossing.composable_crypto.components.custom.vector_commitment;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponent;
import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponentBuilder;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.CommitmentScheme;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.VectorCommitmentScheme;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * ELSA Construction 2 (Extractable binding and statistically hiding vector commitment scheme)
 */
public class ELSA_C2 implements VectorCommitmentScheme {
    private final CommitmentScheme commitmentScheme;
    private final VectorCommitmentScheme vectorCommitmentScheme;

    private ELSA_C2(CommitmentScheme commitmentScheme, VectorCommitmentScheme vectorCommitmentScheme) {
        this.commitmentScheme = commitmentScheme;
        this.vectorCommitmentScheme = vectorCommitmentScheme;
    }

    public static class Builder extends CryptographicComponentBuilder<ELSA_C2> {
        private final CryptographicComponentBuilder<? extends CommitmentScheme> commitmentSchemeBuilder;
        private final CryptographicComponentBuilder<? extends  VectorCommitmentScheme> vectorCommitmentSchemeBuilder;

        public Builder(CryptographicComponentBuilder<? extends CommitmentScheme> commitmentSchemeBuilder, CryptographicComponentBuilder<? extends VectorCommitmentScheme> vectorCommitmentSchemeBuilder) {
            this.commitmentSchemeBuilder = commitmentSchemeBuilder;
            this.vectorCommitmentSchemeBuilder = vectorCommitmentSchemeBuilder;
        }

        @Override
        public ELSA_C2 build(Deque<NamedEdge> parents) {
            return new ELSA_C2(commitmentSchemeBuilder.build(parents, "COM"), vectorCommitmentSchemeBuilder.build(parents, "VC"));
        }
    }

    public static Builder builder(CryptographicComponentBuilder<? extends CommitmentScheme> commitmentSchemeBuilder, CryptographicComponentBuilder<? extends VectorCommitmentScheme> vectorCommitmentSchemeBuilder) {
        return new Builder(commitmentSchemeBuilder, vectorCommitmentSchemeBuilder);
    }

    static class ELSA_C2Decommitment implements Decommitment {
        private final List<ComDecomPair> comDecomPairs;
        private final Decommitment vcDecommitment;

        public ELSA_C2Decommitment(List<ComDecomPair> comDecomPairs, Decommitment vcDecommitment) {
            this.comDecomPairs = comDecomPairs;
            this.vcDecommitment = vcDecommitment;
        }

        public List<ComDecomPair> getComDecomPairs() {
            return comDecomPairs;
        }

        public Decommitment getVcDecommitment() {
            return vcDecommitment;
        }
    }

    static class ComDecomPair {
        private final byte[] commitment;
        private final byte[] decommitment;

        ComDecomPair(byte[] commitment, byte[] decommitment) {
            this.commitment = commitment;
            this.decommitment = decommitment;
        }

        public byte[] getCommitment() {
            return commitment;
        }

        public byte[] getDecommitment() {
            return decommitment;
        }
    }

    static class OpenedDecommitment {
        private final byte[] scalarCommitment;
        private final byte[] scalarDecommitment;
        private final byte[] vectorDecommitment;

        OpenedDecommitment(byte[] scalarCommitment, byte[] scalarDecommitment, byte[] vectorDecommitment) {
            this.scalarCommitment = scalarCommitment;
            this.scalarDecommitment = scalarDecommitment;
            this.vectorDecommitment = vectorDecommitment;
        }

        public byte[] getScalarCommitment() {
            return scalarCommitment;
        }

        public byte[] getScalarDecommitment() {
            return scalarDecommitment;
        }

        public byte[] getVectorDecommitment() {
            return vectorDecommitment;
        }

        public byte[] toByteArray() {
            byte[] result = new byte[3 * Integer.BYTES + scalarCommitment.length + scalarDecommitment.length + vectorDecommitment.length];
            int index = 0;
            for (int i = Integer.BYTES - 1; i >= 0; i--) {
                result[index++] = (byte) (scalarCommitment.length >> (Byte.SIZE * i));
            }
            for (int i = Integer.BYTES - 1; i >= 0; i--) {
                result[index++] = (byte) (scalarDecommitment.length >> (Byte.SIZE * i));
            }
            for (int i = Integer.BYTES - 1; i >= 0; i--) {
                result[index++] = (byte) (vectorDecommitment.length >> (Byte.SIZE * i));
            }
            System.arraycopy(scalarCommitment, 0, result, index, scalarCommitment.length);
            index += scalarCommitment.length;
            System.arraycopy(scalarDecommitment, 0, result, index, scalarDecommitment.length);
            index += scalarDecommitment.length;
            System.arraycopy(vectorDecommitment, 0, result, index, vectorDecommitment.length);
            return result;
        }

        public static OpenedDecommitment fromByteArray(byte[] bytes) {
            int scalarCommitmentSize = 0, scalarDecommitmentSize = 0, vectorDecommitmentSize = 0;
            int index = 0;

            for (int i = Integer.BYTES - 1; i >= 0; i--) {
                scalarCommitmentSize |= Byte.toUnsignedInt(bytes[index++]) << (Byte.SIZE * i);
            }
            for (int i = Integer.BYTES - 1; i >= 0; i--) {
                scalarDecommitmentSize |= Byte.toUnsignedInt(bytes[index++]) << (Byte.SIZE * i);
            }
            for (int i = Integer.BYTES - 1; i >= 0; i--) {
                vectorDecommitmentSize |= Byte.toUnsignedInt(bytes[index++]) << (Byte.SIZE * i);
            }

            byte[] scalarCommitment = new byte[scalarCommitmentSize];
            byte[] scalarDecommitment = new byte[scalarDecommitmentSize];
            byte[] vectorDecommitment = new byte[vectorDecommitmentSize];

            System.arraycopy(bytes, index, scalarCommitment, 0, scalarCommitmentSize);
            index += scalarCommitmentSize;
            System.arraycopy(bytes, index, scalarDecommitment, 0, scalarDecommitmentSize);
            index += scalarDecommitmentSize;
            System.arraycopy(bytes, index, vectorDecommitment, 0, vectorDecommitmentSize);
            return new OpenedDecommitment(scalarCommitment, scalarDecommitment, vectorDecommitment);
        }
    }

    @Override
    public String getName() {
        return "ELSAConstruction2VC";
    }

    @Override
    public Committer createCommitment() {
        return new Committer() {
            private final Committer vectorCommitter = vectorCommitmentScheme.createCommitment();
            private final List<CommitmentScheme.Committer> scalarCommitters = new ArrayList<>();

            @Override
            public OutputStream addStream() {
                CommitmentScheme.Committer scalarCommitment = commitmentScheme.createCommitment();
                scalarCommitters.add(scalarCommitment);
                return scalarCommitment.getStream();
            }

            @Override
            public CommitResult commit() {
                final List<ComDecomPair> scalarComDecomPairs = new ArrayList<>();
                for (CommitmentScheme.Committer scalarCommitter : scalarCommitters) {
                    // Commit using COM
                    CommitmentScheme.CommitResult scalarCommit = scalarCommitter.commit();
                    byte[] c = scalarCommit.getCommitment();
                    byte[] d = scalarCommit.getDecommitment();

                    try (OutputStream vcos = vectorCommitter.addStream()) {
                        vcos.write(c);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    scalarComDecomPairs.add(new ComDecomPair(c, d));
                }

                CommitResult vcResult = vectorCommitter.commit();
                return new CommitResult(vcResult.getCommitment(), new ELSA_C2Decommitment(scalarComDecomPairs, vcResult.getDecommitment()));
            }

            @Override
            public CryptographicComponent getComponent() {
                return ELSA_C2.this;
            }
        };
    }

    @Override
    public Opener open() {
        return new Opener() {
            @Override
            public byte[] open(Decommitment decommitment, int index) {
                ELSA_C2Decommitment decom = (ELSA_C2Decommitment) decommitment;
                byte[] d = vectorCommitmentScheme.open().open(decom.getVcDecommitment(), index);
                ComDecomPair comDecomPair = decom.getComDecomPairs().get(index);
                OpenedDecommitment openedDecommitment = new OpenedDecommitment(comDecomPair.getCommitment(), comDecomPair.getDecommitment(), d);
                return openedDecommitment.toByteArray();
            }

            @Override
            public String getName() {
                return ELSA_C2.this.getName();
            }
        };
    }

    @Override
    public Verifier verifyCommitment() {
        return new Verifier() {
            private final CommitmentScheme.Verifier scalarVerifier = commitmentScheme.verifyCommitment();

            @Override
            public OutputStream getStream() {
                return scalarVerifier.getStream();
            }

            @Override
            public boolean verify(byte[] commitment, byte[] decommitment, int index) {
                OpenedDecommitment decom = OpenedDecommitment.fromByteArray(decommitment);
                boolean b1 = scalarVerifier.verify(decom.getScalarCommitment(), decom.getScalarDecommitment());
                Verifier vectorVerifier = vectorCommitmentScheme.verifyCommitment();
                try {
                    vectorVerifier.getStream().write(decom.getScalarCommitment());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                boolean b2 = vectorVerifier.verify(commitment, decom.getVectorDecommitment(), index);

                return b1 && b2;
            }

            @Override
            public CryptographicComponent getComponent() {
                return ELSA_C2.this;
            }
        };
    }
}
