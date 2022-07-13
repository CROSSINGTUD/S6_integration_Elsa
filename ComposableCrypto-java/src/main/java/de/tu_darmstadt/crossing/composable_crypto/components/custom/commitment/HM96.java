package de.tu_darmstadt.crossing.composable_crypto.components.custom.commitment;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponent;
import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponentBuilder;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.CommitmentScheme;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.HashFunction;

import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Deque;
import java.util.Random;

/**
 * A commitment scheme based on chapter 4 (modified version) of <a href="http://people.csail.mit.edu/shaih/pubs/hm96.pdf">HM96</a> .
 * Universal hashing is used exactly as described in the paper.
 * We use a Toeplitz Matrix to save memory.
 */
public class HM96 implements CommitmentScheme {
    private final Random random = new SecureRandom();
    private final HashFunction hashFunction;

    private HM96(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
    }

    public static class Builder extends CryptographicComponentBuilder<HM96> {
        private final CryptographicComponentBuilder<? extends HashFunction> hashFunctionBuilder;

        public Builder(CryptographicComponentBuilder<? extends HashFunction> hashFunctionBuilder) {
            this.hashFunctionBuilder = hashFunctionBuilder;
        }

        @Override
        public HM96 build(Deque<NamedEdge> parents) {
            return new HM96(hashFunctionBuilder.build(parents, "Hash"));
        }
    }

    public static Builder builder(CryptographicComponentBuilder<? extends HashFunction> hashFunctionBuilder) {
        return new Builder(hashFunctionBuilder);
    }

    @Override
    public String getName() {
        return "HM96";
    }

    @Override
    public Committer createCommitment() {
        return new Committer() {
            private final HashFunction.Hasher messageDigest = hashFunction.createHasher();

            @Override
            public OutputStream getStream() {
                return messageDigest.getOutputStream();
            }

            @Override
            public CommitResult commit() {
                int hashLength = hashFunction.getHashLengthInBytes();
                byte[] s = messageDigest.hash();
                byte[] r = new byte[hashLength];
                random.nextBytes(r);
                HashFunction.Hasher hasher = hashFunction.createHasher();
                try {
                    hasher.getOutputStream().write(r);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                byte[] y = hasher.hash();
                byte[] A = new byte[2 * hashLength - 1];
                random.nextBytes(A);
                byte[] b = quadMatrixVectorMultAndAddGF2(A, r, s);
                // In the paper, the triple (A, b, y) is the result of this function, we concatenate these (fixed-size) values to obtain a byte array.
                byte[] commitment = new byte[A.length + b.length + y.length];
                System.arraycopy(A, 0, commitment, 0, A.length);
                System.arraycopy(b, 0, commitment, A.length, b.length);
                System.arraycopy(y, 0, commitment, A.length + b.length, y.length);
                return new CommitResult(commitment, r);
            }

            @Override
            public CryptographicComponent getComponent() {
                return HM96.this;
            }
        };
    }

    @Override
    public Verifier verifyCommitment() {
        return new Verifier() {
            private final HashFunction.Hasher messageDigest = hashFunction.createHasher();

            @Override
            public OutputStream getStream() {
                return messageDigest.getOutputStream();
            }

            @Override
            public boolean verify(byte[] commitment, byte[] decommitment) {
                int hashLength = hashFunction.getHashLengthInBytes();
                byte[] y = new byte[hashLength];
                byte[] s = messageDigest.hash();
                byte[] A = new byte[2 * hashLength - 1];
                byte[] b = new byte[hashLength];

                if (commitment == null
                        || decommitment == null
                        || commitment.length != A.length + b.length + y.length
                        || decommitment.length != hashLength) {
                    return false;
                }

                System.arraycopy(commitment, commitment.length - y.length, y, 0, y.length);
                HashFunction.Hasher decomHasher = hashFunction.createHasher();
                try {
                    decomHasher.getOutputStream().write(decommitment);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (!Arrays.equals(decomHasher.hash(), y)) {
                    return false;
                }

                System.arraycopy(commitment, 0, A, 0, A.length);
                System.arraycopy(commitment, A.length, b, 0, b.length);
                byte[] hr = quadMatrixVectorMultAndAddGF2(A, decommitment, b);
                return Arrays.equals(hr, s);
            }

            @Override
            public CryptographicComponent getComponent() {
                return HM96.this;
            }
        };
    }

    /**
     * Calculates Ax + b in GF2 where A is a quadratic matrix.
     * @param A quadratic matrix
     * @param x vector to multiply
     * @param b vector to add
     * @return Ax + b in GF2
     */
    private byte[] quadMatrixVectorMultAndAddGF2(byte[] A, byte[] x, byte[] b) {
        byte[] result = new byte[x.length];
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x.length; j++) {
                result[i] ^= valueAtToeplitzMatrix(A, i, j) & x[j];
            }
            result[i] ^= b[i];
        }
        return result;
    }

    /**
     * Gets the value of the Toeplitz Matrix represented by matrix at the specified indices.
     * @param matrix A representation of a Toeplitz Matrix consisting of each diagonal value from bottom left to top right.
     * @param i row
     * @param j column
     * @return the value of the Toeplitz Matrix represented by matrix at the specified indices
     */
    private byte valueAtToeplitzMatrix(byte[] matrix, int i, int j) {
        int n = (matrix.length + 1) / 2;
        return matrix[j - i + n - 1];
    }
}
