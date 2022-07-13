package de.tu_darmstadt.crossing.composable_crypto.components.custom.vector_commitment;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponent;
import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponentBuilder;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.HashFunction;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.VectorCommitmentScheme;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public class MerkleTreeVectorCommitment implements VectorCommitmentScheme {
    private final HashFunction hashFunction;
    private final int HASH_LENGTH;

    private MerkleTreeVectorCommitment(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
        HASH_LENGTH = hashFunction.getHashLengthInBytes();
    }

    public static class Builder extends CryptographicComponentBuilder<MerkleTreeVectorCommitment> {
        private final CryptographicComponentBuilder<? extends HashFunction> hashFunctionBuilder;

        public Builder(CryptographicComponentBuilder<? extends HashFunction> hashFunctionBuilder) {
            this.hashFunctionBuilder = hashFunctionBuilder;
        }

        @Override
        public MerkleTreeVectorCommitment build(Deque<NamedEdge> parents) {
            return new MerkleTreeVectorCommitment(hashFunctionBuilder.build(parents, "Hash"));
        }
    }

    public static Builder builder(CryptographicComponentBuilder<? extends HashFunction> hashFunctionBuilder) {
        return new Builder(hashFunctionBuilder);
    }

    static class MerkleTreeDecommitment implements Decommitment {
        private final byte[][] bytes;

        MerkleTreeDecommitment(byte[][] bytes) {
            this.bytes = bytes;
        }

        public byte[][] getBytes() {
            return bytes;
        }
    }

    @Override
    public String getName() {
        return "MerkleTreeVC";
    }

    @Override
    public Committer createCommitment() {
        return new Committer() {
            private final List<HashFunction.Hasher> hashes = new ArrayList<>();

            @Override
            public OutputStream addStream() {
                HashFunction.Hasher hasher = hashFunction.createHasher();
                hashes.add(hasher);
                return hasher.getOutputStream();
            }

            @Override
            public CommitResult commit() {
                int n = hashes.size();
                int l = log2ceil(n); // amount of levels of the tree
                // byte[][] merkleTree = new byte[(int)Math.pow(2, l + 1) - 1][];
                byte[][] merkleTree = new byte[(2 << (l+1)) - 1][]; // equivalent to commented line above, avoids rounding errors.
                // Unterste Ebene mit Hashes des Messages befüllen
                for (int i = 0; i < n; i++) {
                    merkleTree[merkleTree.length / 2 + i] = hashes.get(i).hash();
                }
                // Restliche Blätter mit leeren Hashes auffüllen
                byte[] emptyHash = hash(new byte[0]);
                for (int i = merkleTree.length / 2 + n; i < merkleTree.length; i++) {
                    merkleTree[i] = emptyHash;
                }

                // von unten nach oben Hashes der inneren Knoten berechnen
                for (int i = merkleTree.length / 2 - 1; i >= 0; i--) {
                    merkleTree[i] = hash(merkleTree[2*i + 1], merkleTree[2*i + 2]);
                }
                return new CommitResult(merkleTree[0], new MerkleTreeDecommitment(merkleTree));

            }

            @Override
            public CryptographicComponent getComponent() {
                return MerkleTreeVectorCommitment.this;
            }
        };
    }

    @Override
    public Opener open() {
        return new Opener() {
            @Override
            public byte[] open(Decommitment decommitment, int index) {
                byte[][] decom = ((MerkleTreeDecommitment) decommitment).getBytes();
                int l = log2nlz(decom.length);
                byte[] result = new byte[l * HASH_LENGTH + 4]; // + 4 bytes for the decom index
                // Passende Knoten im Merkle Tree suchen und im Decommitment speichern.
                int currentIndex = siblingNode(decom.length / 2 + index);
                // result[result.length - 1] = D[currentIndex];
                System.arraycopy(decom[currentIndex], 0, result, (l - 1) * HASH_LENGTH, decom[currentIndex].length);
                for (int i = l - 2; i >= 0; i--) {
                    currentIndex = siblingNode(parentIndex(currentIndex));
                    // result[i] = D[currentIndex];
                    System.arraycopy(decom[currentIndex], 0, result, i * HASH_LENGTH, decom[currentIndex].length);
                }

                // Save index in the last four bytes.
                result[result.length - 4] = (byte) (index >> 24);
                result[result.length - 3] = (byte) (index >> 16);
                result[result.length - 2] = (byte) (index >> 8);
                result[result.length - 1] = (byte) index;
                return result;

            }

            @Override
            public String getName() {
                return MerkleTreeVectorCommitment.this.getName();
            }
        };
    }

    @Override
    public Verifier verifyCommitment() {
        return new Verifier() {
            private final HashFunction.Hasher hasher = hashFunction.createHasher();

            @Override
            public OutputStream getStream() {
                return hasher.getOutputStream();
            }

            @Override
            public boolean verify(byte[] commitment, byte[] decommitment, int index) {
                byte[] temp = new byte[HASH_LENGTH];
                byte[] cPrime = hasher.hash();
                int currentIndex = (Byte.toUnsignedInt(decommitment[decommitment.length - 4]) << 24)
                        | (Byte.toUnsignedInt(decommitment[decommitment.length - 3]) << 16)
                        | (Byte.toUnsignedInt(decommitment[decommitment.length - 2]) << 8)
                        | Byte.toUnsignedInt(decommitment[decommitment.length - 1]);
                for (int i = (decommitment.length - 4) / HASH_LENGTH - 1; i >= 0; i--) {
                    System.arraycopy(decommitment, i * HASH_LENGTH, temp, 0, HASH_LENGTH);
                    if (currentIndex % 2 == 0) {
                        // Hash aus decommitment rechts, berechneter Hash links
                        cPrime = hash(cPrime, temp);
                    } else { // currentIndex % 2 == 1
                        // Hash aus decommitment links, berechneter Hash rechts
                        cPrime = hash(temp, cPrime);
                    }
                    currentIndex = currentIndex / 2;
                }
                return Arrays.equals(commitment, cPrime);
            }

            @Override
            public CryptographicComponent getComponent() {
                return MerkleTreeVectorCommitment.this;
            }
        };
    }

    private static int log2ceil(int a) {
        // Achtung: Rundungsfehler
        // return (int) Math.ceil(Math.log(a) / Math.log(2));

        if (a == 1) {
            return 0;
        }

        // log2nlz gibt floor(log2(a)) zurück.
        // Wir wollen aber ceil(log2(a)).
        // ceil(log2(a)) == floor(log2(a)) + 1, falls log2(a) nicht ganzzahling ist.
        // ceil(log2(a)) == floor(log2(a)), falls log2(a) ganzzahlig ist.
        // log2(a) ist ganzzahlig, falls log2(a - 1) != log2(a).
        int log2 = log2nlz(a);
        int log2minus1 = log2nlz(a - 1);
        return log2 == log2minus1 ? log2 + 1 : log2;
    }

    // https://stackoverflow.com/a/3305710
    private static int log2nlz( int bits )
    {
        if( bits == 0 )
            return 0; // or throw exception
        return 31 - Integer.numberOfLeadingZeros( bits );
    }

    private int parentIndex(int index) {
        return (index - 1) / 2;
    }

    private int siblingNode(int index) {
        return index - 1 + 2 * (index % 2);
    }

    private byte[] hash(byte[] input) {
        HashFunction.Hasher hasher = hashFunction.createHasher();
        try {
            hasher.getOutputStream().write(input);
            return hasher.hash();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] hash(byte[] input1, byte[] input2) {
        HashFunction.Hasher hasher = hashFunction.createHasher();
        try {
            OutputStream os = hasher.getOutputStream();
            os.write(input1);
            os.write(input2);
            return hasher.hash();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
