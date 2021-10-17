package de.tu_darmstadt.crossing.composable_crypto.components.custom.vector_commitment;

import de.tu_darmstadt.crossing.composable_crypto.core.CryptographicComponentBuilder;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.HashFunction;
import de.tu_darmstadt.crossing.composable_crypto.interfaces.VectorCommitmentScheme;

import java.util.Deque;

public class MerkleTreeVectorCommitment implements VectorCommitmentScheme {
    private final HashFunction hashFunction;

    private MerkleTreeVectorCommitment(HashFunction hashFunction) {
        this.hashFunction = hashFunction;
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

    @Override
    public String getName() {
        return null;
    }

    @Override
    public Committer createCommitment() {
        return null;
    }

    @Override
    public Opener open() {
        return null;
    }

    @Override
    public Verifier verifyCommitment() {
        return null;
    }
}
